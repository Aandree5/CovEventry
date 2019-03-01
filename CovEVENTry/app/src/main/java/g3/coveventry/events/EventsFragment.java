package g3.coveventry;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.GsonBuilder;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.internal.network.OkHttpClientHelper;
import com.twitter.sdk.android.core.models.BindingValues;
import com.twitter.sdk.android.core.models.BindingValuesAdapter;
import com.twitter.sdk.android.core.models.SafeListAdapter;
import com.twitter.sdk.android.core.models.SafeMapAdapter;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import com.twitter.sdk.android.core.services.params.Geocode;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.SearchTimeline;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import g3.coveventry.customviews.CovImageView;
import g3.coveventry.events.FetchAddressIntentService;
import g3.coveventry.events.TwitterAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static g3.coveventry.events.FetchAddressIntentService.FAILURE_RESULT;
import static g3.coveventry.events.FetchAddressIntentService.LOCATION_DATA_EXTRA;
import static g3.coveventry.events.FetchAddressIntentService.RECEIVER;
import static g3.coveventry.events.FetchAddressIntentService.RESULT_CITY_KEY;
import static g3.coveventry.events.FetchAddressIntentService.RESULT_LAT_KEY;
import static g3.coveventry.events.FetchAddressIntentService.RESULT_LON_KEY;


public class EventsFragment extends Fragment {
    RecyclerView recyclerView;
    TwitterAPI twitterApi;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.fe_list);

        // Set recycler view properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Prepare retrofit to create an object of the TwitterAPI
        Retrofit retrofit = new Retrofit.Builder()
                .client(OkHttpClientHelper.getOkHttpClient(TwitterCore.getInstance().getGuestSessionProvider()))
                .baseUrl("https://api.twitter.com/1.1/")
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .registerTypeAdapterFactory(new SafeListAdapter())
                        .registerTypeAdapterFactory(new SafeMapAdapter())
                        .registerTypeAdapter(BindingValues.class, new BindingValuesAdapter())
                        .create()))
                .build();

        // Create object of TwitterAPI with retrofit
        twitterApi = retrofit.create(TwitterAPI.class);


        // Start service to retrieve information about user location
        FusedLocationProviderClient locClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        // If location was successfully retrieved, start service to find the city name
                        if (location != null) {
                            Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
                            intent.putExtra(RECEIVER, new AddressResultReceiver());
                            intent.putExtra(LOCATION_DATA_EXTRA, location);

                            // Start service
                            Objects.requireNonNull(getActivity()).startService(intent);
                        }

                    })
                    .addOnFailureListener(e -> {
                        // On failure log error
                        Log.e("AppLog", e.getMessage());
                        e.printStackTrace();
                    });

        }


        return view;
    }


    /**
     * Get the tweets from Twitter, matching the given properties and only from the day
     *
     * @param city City to filter the tweets user location
     * @param lat  Latitude to query the TwitterAPI for tweets
     * @param lon  Longitude to query the TwitterAPI for tweets
     */
    private void getTweets(String city, double lat, double lon) {
        // Get today's date, to filter tweets
        Calendar cal = Calendar.getInstance();

        // Set date formatting for twitter API
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Create twitter request for tweets data
        Call<Search> call = twitterApi.searchTweets("(drinks OR shots) OR (nightclub OR club) OR prize OR \"live set\" " +
                        "tonight -meeting -i filter:links -filter:retweets since:" + sdf.format(cal.getTime()),
                new Geocode(lat, lon, 50, Geocode.Distance.KILOMETERS), null, null, null, 100,
                null, null, null, true);

        // Callback to read retrieved data
        call.enqueue(new Callback<Search>() {
            @Override
            public void onResponse(@NonNull Call<Search> call, @NonNull Response<Search> response) {
                if (response.body() != null) {
                    if (response.body().tweets.size() > 0) {
                        List<Tweet> ts = new ArrayList<>();

                        // Filter out tweets that user location is not coventry
                        for (Tweet tweet : response.body().tweets)
                            if (tweet.user.location.toLowerCase().contains(city.toLowerCase()))
                                ts.add(tweet);


                        Log.i("AppLog", String.format("%s tweets found, after filter %s", response.body().tweets.size(), ts.size()));

                        // Set adapter to load tweets into recyclerv view
                        recyclerView.setAdapter(new EventsAdapter(ts));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Search> call, @NonNull Throwable t) {
                // On failure log error
                Toast.makeText(getContext(), "There was an error retrieving data.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    /**
     * Adapter for the recycler view to show the tweets appropriately
     */
    class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {
        private List<Tweet> tweets;

        /**
         * Class to hold the view for each item
         */
        final class EventsViewHolder extends RecyclerView.ViewHolder {
            private TextView hostName;
            private TextView description;
            private CovImageView image;

            /**
             * Constructor, find views inside view for data to be bound later
             *
             * @param itemView View for the item
             */
            EventsViewHolder(@NonNull View itemView) {
                super(itemView);

                hostName = itemView.findViewById(R.id.eventlistitem_host_name);
                description = itemView.findViewById(R.id.eventlistitem_description);
                image = itemView.findViewById(R.id.eventlistitem_image);
            }
        }

        /**
         * Constructor
         *
         * @param tweets List of tweets to show on recycler view
         */
        EventsAdapter(List<Tweet> tweets) {
            this.tweets = tweets;
            setHasStableIds(true);
        }


        @NonNull
        @Override
        public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Create view for event item
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_event_list_item, parent, false);

            return new EventsViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull EventsViewHolder viewHolder, int position) {
            // Bind data to the current view holder
            Tweet tweet = tweets.get(position);

            viewHolder.hostName.setText(tweet.user.name);
            viewHolder.description.setText(tweet.text);

            if (!tweets.get(position).entities.media.isEmpty()) try {
                viewHolder.image.setImageBitmap(new URL(tweet.entities.media.get(0).mediaUrlHttps));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            viewHolder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweet.entities.media.get(0).expandedUrl));
                startActivity(intent);
            });
        }


        @Override
        public int getItemCount() {
            return tweets.size();
        }

        @Override
        public long getItemId(int position) {
            return tweets.get(position).id;
        }
    }

    /**
     * Class to receive the information gathered from the current location, to get the tweets from
     */
    class AddressResultReceiver extends ResultReceiver {

        /**
         * Constructor
         */
        AddressResultReceiver() {
            super(new Handler());
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            // Check if successful data was sent back, warn user and terminate function
            if (resultData == null || resultCode == FAILURE_RESULT) {
                Toast.makeText(getContext(), "Error retrieving data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Read sent back information
            String city = resultData.getString(RESULT_CITY_KEY);
            double lat = resultData.getDouble(RESULT_LAT_KEY);
            double lon = resultData.getDouble(RESULT_LON_KEY);

            // Check information validity, and call function to get tweets
            if (city != null && lat > -9999 && lon > -9999)
                getTweets(city, lat, lon);

            else {
                Toast.makeText(getContext(), "No data retrieved", Toast.LENGTH_SHORT).show();
            }
        }
    }
}