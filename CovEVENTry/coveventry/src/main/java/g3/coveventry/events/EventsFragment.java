package g3.coveventry.events;


import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.params.Geocode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import g3.coveventry.R;
import g3.coveventry.customviews.CovImageView;
import g3.coveventry.database.CallbackDBResults;
import g3.coveventry.database.Database;
import g3.coveventry.socialmedia.TwitterAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static g3.coveventry.events.FetchAddressIntentService.FAILURE_RESULT;
import static g3.coveventry.events.FetchAddressIntentService.LOCATION_DATA_EXTRA;
import static g3.coveventry.events.FetchAddressIntentService.RECEIVER;
import static g3.coveventry.events.FetchAddressIntentService.RESULT_CITY_KEY;

//TODO: Save events on bundle for screen rotation and coming back from img_splashscreen_background
public class EventsFragment extends Fragment {
    RecyclerView recyclerView;
    SupportMapFragment mapFragment;
    EditText edtTxtSearch;
    Button buttonSwap;

    TwitterAPI twitterApi;

    boolean showMap;
    LatLngBounds.Builder markerBounds;

    LatLng userLocation;
    String city;
    ArrayList<Event> events = new ArrayList<>();

    String filterText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        buttonSwap = view.findViewById(R.id.eventlist_list_swap);
        edtTxtSearch = view.findViewById(R.id.eventlist_searchtext);

        recyclerView = view.findViewById(R.id.eventlist_list_view);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.eventlist_map_view);

        // Create object of TwitterAPI with retrofit
        twitterApi = TwitterAPI.build();

        // Start service to retrieve information about user location
        FusedLocationProviderClient locClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        // If location was successfully retrieved, start service to find the city name
                        if (location != null) {
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());

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


        filterText = "";
        edtTxtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterText = s.toString();
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Listener to swap between map and list view
        buttonSwap.setOnClickListener(view1 -> {
            showMap = !showMap;

            // Check whether to show the map or the list view
            if (showMap) {
                // Show map and hide recycler view
                recyclerView.setVisibility(View.GONE);
                Objects.requireNonNull(mapFragment.getView()).setVisibility(View.VISIBLE);

                // Set first camera position and zoom
                // If no marker has been set, defaults to city location, because was added first
                mapFragment.getMapAsync(googleMap ->
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(markerBounds.build(), 100))
                );

                edtTxtSearch.setVisibility(View.GONE);

            } else {
                // Show recycler view and hide map
                recyclerView.setVisibility(View.VISIBLE);
                Objects.requireNonNull(mapFragment.getView()).setVisibility(View.GONE);

                // Set recycler view properties
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(new EventsAdapter());

                edtTxtSearch.setVisibility(View.VISIBLE);
            }
        });

        mapFragment.getMapAsync(googleMap ->
                googleMap.setOnMarkerClickListener(marker ->
                {
                    Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.dialog_event);

                    ((TextView) dialog.findViewById(R.id.dialogevent_hostName))
                            .setText(events.get(Integer.valueOf(marker.getTitle())).hostName);

                    ((TextView) dialog.findViewById(R.id.dialogevent_description))
                            .setText(events.get(Integer.valueOf(marker.getTitle())).description);

                    ((CovImageView) dialog.findViewById(R.id.dialogevent_image))
                            .setImageBitmap(events.get(Integer.valueOf(marker.getTitle())).image);

                    dialog.show();

                    return true;
                })
        );

        // Check if was just open or is rebuilding from background
        if (savedInstanceState == null) {
            showMap = true;
        }

        return view;
    }


    /**
     * Notify either the recycler view or the map when a new event has been added
     *
     * @param newEvents List of new added events
     */
    void notifyEventRangeAdded(List<Event> newEvents) {
        // Whether to notify the map or the recycler view
        if (showMap) {
            // Add events, events have to be added here because of the recycler view updating only after it's ready,
            // so the array might be changed in the meantime, so have to be added right before updating the views
            events.addAll(newEvents);

            mapFragment.getMapAsync(googleMap -> {
                for (int i = 0; i < newEvents.size(); i++) {
                    LatLng eventLoc = newEvents.get(i).location != null ? newEvents.get(i).location : getRandomDefaultLocation();

                    // Add a marker for the event
                    googleMap.addMarker(new MarkerOptions()
                            .position(eventLoc)
                            .title(String.valueOf(events.size() - newEvents.size() + i)) // i is indexed at 0, so no need to decrease 1 from size()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

                    // Extend the markers bounds
                    markerBounds.include(eventLoc);

                    // Move camera to show all markers
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(markerBounds.build(), 100));
                }

            });
        } else {
            // Notify recycler view of data changed
            // post() will allow the recycler view to complete what it's doing before trying to update again
            recyclerView.post(() ->
                    {
                        // Add events, events have to be added here because of the recycler view updating only after it's ready,
                        // so the array might be changed in the meantime, so have to be added right before updating the views
                        events.addAll(newEvents);

                        // Notify the recycler view
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemRangeInserted(events.size() - newEvents.size(), newEvents.size());
                    }
            );
        }
    }


    /**
     * Notify either the recycler view or the map when a new event has been added
     *
     * @param newEvent Event to update the views
     */
    void notifyEventAdded(Event newEvent) {
        // Whether to notify the map or the recycler view
        if (showMap) {
            // Add events, events have to be added here because of the recycler view updating only after it's ready,
            // so the array might be changed in the meantime, so have to be added right before updating the views
            events.add(newEvent);

            mapFragment.getMapAsync(googleMap -> {
                LatLng eventLoc = newEvent.location != null ? newEvent.location : getRandomDefaultLocation();

                // Add a marker for the event
                googleMap.addMarker(new MarkerOptions()
                        .position(eventLoc)
                        .title(String.valueOf(events.size() - 1)) // Otherwise would be out of bounds
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                // Extend the markers bounds
                markerBounds.include(eventLoc);

                // Move camera to show all markers
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(markerBounds.build(), 100));

            });
        } else {
            // Notify recycler view of data changed
            // post() will allow the recycler view to complete what it's doing before trying to update again
            recyclerView.post(() ->
                    {
                        // Add events, events have to be added here because of the recycler view updating only after it's ready,
                        // so the array might be changed in the meantime, so have to be added right before updating the views
                        events.add(newEvent);

                        // Notify the recycler view
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(events.size() - 1);
                    }
            );
        }
    }

    /**
     * Load event for the day, from database and twitter
     */
    private void loadEvents() {
        // Reset events list and marker bounds
        events.clear();

        // Initiate marker bound builder
        markerBounds = new LatLngBounds.Builder();

        // Set the user location inside the marker bounds
        markerBounds.include(userLocation);

        // Load events from database
        Database.getInstance().getEvents(Calendar.getInstance().getTime(), new CallbackDBResults<ArrayList<Event>>() {
            @Override
            public void connectionSuccessful(ArrayList<Event> results) {
                // Notify views
                if (!results.isEmpty())
                    notifyEventRangeAdded(results);
            }

            @Override
            public void connectionFailed(String message) {
                Log.e("AppLog", message);
                Toast.makeText(getContext(), "Error retrieving from database", Toast.LENGTH_SHORT).show();
            }
        });


        // Load events from twitter
        // Set date formatting for twitter API
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Create twitter request for tweets data
       /* Call<Search> call = twitterApi.searchTweets("(drinks OR shots) OR (nightclub OR club) OR prize OR prize OR admission " +
                        "OR \"live set\" tonight -meeting -i (-courses -tea -table) (-proceedings -presentation) " +
                        "filter:links -filter:retweets since:" + simpleDateFormat.format(Calendar.getInstance().getTime()),
                new Geocode(userLocation.latitude, userLocation.longitude, 50, Geocode.Distance.KILOMETERS), null, null,
                null, 100, null, null, null, true);*/

        // TODO: TESTING
        Call<Search> call = twitterApi.searchTweets("(drinks OR shots) OR (nightclub OR club) OR prize OR prize OR admission " +
                        "OR \"live set\" tonight -meeting -i (-courses -tea -table) (-proceedings -presentation) " +
                        "filter:links -filter:retweets",
                new Geocode(userLocation.latitude, userLocation.longitude, 50, Geocode.Distance.KILOMETERS), null, null,
                null, 100, null, null, null, true);

        // Callback to read retrieved data
        call.enqueue(new Callback<Search>() {
            @Override
            public void onResponse(@NonNull Call<Search> call, @NonNull Response<Search> response) {
                if (response.body() != null && response.body().tweets.size() > 0)
                    for (Tweet tweet : response.body().tweets) {
                        // Filter out tweets that user location is not coventry
                        // Run an AsyncTask to check events location
                        if (tweet.user.location.toLowerCase().contains(city.toLowerCase()))
                            new FormatEvent(EventsFragment.this, userLocation).execute(tweet);

                    }


            }

            @Override
            public void onFailure(@NonNull Call<Search> call, @NonNull Throwable t) {
                // On failure log error
                Toast.makeText(getContext(), "Error retrieving from Twitter", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }


    private LatLng getRandomDefaultLocation() {
        return new LatLng(userLocation.latitude + (Math.random() % 0.01d - 0.005d),
                userLocation.longitude + (Math.random() % 0.01d - 0.005d));
    }


    /**
     * Adapter for the recycler view to show the tweets appropriately
     */
    class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {

        /**
         * Class to hold the view for each item
         */
        final class EventsViewHolder extends RecyclerView.ViewHolder {
            private TextView hostName;
            private TextView description;
            private CovImageView image;
            private RecyclerView.LayoutParams params;

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

                params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            }

            void setVisible(Boolean visible) {
                if (visible) {
                    this.itemView.setVisibility(View.VISIBLE);

                    this.itemView.setLayoutParams(params);
                } else {
                    this.itemView.setVisibility(View.GONE);

                    this.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
        }

        /**
         * Constructor
         */
        EventsAdapter() {
            setHasStableIds(true);
        }


        @NonNull
        @Override
        public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Create view for event item
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_eventlist_item, parent, false);

            return new EventsViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull EventsViewHolder viewHolder, int position) {
            // Bind data to the current view holder
            Event event = events.get(position);

            if (filterText.isEmpty() || event.hostName.contains(filterText.toLowerCase()) ||
                    event.description.contains(filterText.toLowerCase()) ||
                    event.title.contains(filterText.toLowerCase()) ||
                    event.postCode.contains(filterText.toLowerCase()) ||
                    event.venue.contains(filterText.toLowerCase())) {
                viewHolder.setVisible(true);

                viewHolder.hostName.setText(event.hostName);
                viewHolder.description.setText(event.description);


                if (event.image != null)
                    viewHolder.image.setImageBitmap(event.image);

                else
                    viewHolder.image.setImageDrawable(R.drawable.ic_event_placeholder, Objects.requireNonNull(getActivity()).getTheme());

                //TODO: Allow to click on event to open more details
                /*viewHolder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweet.entities.media.get(0).expandedUrl));
                    startActivity(intent);
                });*/
            } else
                viewHolder.setVisible(false);
        }


        @Override
        public int getItemCount() {
            return events.size();
        }

        @Override
        public long getItemId(int position) {
            return events.get(position).id;
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
            String cityName = resultData.getString(RESULT_CITY_KEY);

            // Check information validity, and call function to get tweets
            if (cityName != null) {
                city = cityName;

                // Initiate map view, if is being used
                if (showMap)
                    // Set first camera position and zoom
                    mapFragment.getMapAsync(googleMap ->
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                    .target(userLocation)
                                    .zoom(15)
                                    .build()))
                    );

                loadEvents();
            } else {
                Toast.makeText(getContext(), "Error retrieving location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Class to format events, download images and get their location
     */
    private static class FormatEvent extends AsyncTask<Tweet, Void, Event> {
        // Reference to the events fragment to retrieve needed data
        private WeakReference<EventsFragment> eventsFragmentRef;
        private LatLng userLocation;

        /**
         * Constructor
         *
         * @param eventsFragment Fragment to retrieve data
         */
        FormatEvent(@NonNull EventsFragment eventsFragment, LatLng userLocation) {
            this.eventsFragmentRef = new WeakReference<>(eventsFragment);
            this.userLocation = userLocation;
        }

        @Override
        protected Event doInBackground(Tweet... tweets) {
            Tweet tweet = tweets[0];

            // Get location from fragment
            LatLng loc = eventsFragmentRef.get().userLocation;
            // Get city name from fragment
            String city = eventsFragmentRef.get().city;

            try {
                // Get image bitmap from URL
                Bitmap image = null;
                if (!tweet.entities.media.isEmpty())
                    image = BitmapFactory.decodeStream(new URL(tweet.entities.media.get(0).mediaUrlHttps).openConnection().getInputStream());

                // Regex to filter out emoji form user's names
                String regex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]";
                String userNameNoEmoji = tweet.user.name.replaceAll(regex, "");

                // Data to query Google Places API for the location of an event
                HashMap<String, String> requestInfo = new HashMap<>();
                requestInfo.put("key", eventsFragmentRef.get().getString(R.string.GOOGLE_API_KEY));
                requestInfo.put("input", userNameNoEmoji);
                requestInfo.put("inputtype", "textquery");
                requestInfo.put("locationbias", "circle:" + 5000 + "@" + loc.latitude + "," + loc.longitude);
                requestInfo.put("fields", "geometry/location");

                // Keep event location
                LatLng eventLocation = null;

                StringBuilder data = new StringBuilder();

                // Append all the information for the request into a string
                for (String rI : requestInfo.keySet())
                    data.append(URLEncoder.encode(rI, "UTF-8")).append("=")
                            .append(URLEncoder.encode(requestInfo.get(rI), "UTF-8")).append("&");

                // Open connection to Places API, append string to link because it's not a .php page
                // Wasn't sending the data in time
                URLConnection conn = new URL("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?" + data.toString()).openConnection();
                conn.setDoOutput(true);

                // Receive and read data into a HashMaps
                BufferedReader buffReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                // Read retrieved data
                StringBuilder resp = new StringBuilder();
                String line;
                while ((line = buffReader.readLine()) != null)
                    resp.append(line);

                // Check if there was a response
                if (resp.length() > 0) {
                    JSONObject results = new JSONObject(resp.toString());

                    // Parse result into a LatLng
                    if (results.has("candidates")) {
                        JSONArray candidates = results.getJSONArray("candidates");
                        if (candidates.length() > 0 && candidates.getJSONObject(0).has("geometry")) {
                            JSONObject geometry = candidates.getJSONObject(0).getJSONObject("geometry");
                            if (geometry.has("location")) {
                                JSONObject location = geometry.getJSONObject("location");
                                if (location.has("lat") && location.has("lng")) {
                                    LatLng l = new LatLng(location.getDouble("lat"), location.getDouble("lng"));

                                    // Calculate the distance from the found location and the user location,
                                    // only use it if is near, because if not it found the wrong place
                                    double earthRadius = 3958.75;
                                    double latDistance = Math.toRadians(l.latitude - userLocation.latitude);
                                    double lonDistance = Math.toRadians(l.longitude - userLocation.longitude);
                                    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                                            Math.cos(Math.toRadians(userLocation.latitude)) * Math.cos(Math.toRadians(l.latitude)) *
                                                    Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                                    double distance = earthRadius * c;

                                    // Check if within a 5km radius
                                    if (distance < 5)
                                        eventLocation = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                                }
                            }
                        }
                    }
                }


                // Set simpleDateFormat for twitter createdAt property format
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.getDefault());

                // Save date into variable to only be computed once
                Date createdDate = simpleDateFormat.parse(tweet.createdAt);

                // Return formatted event
                return new Event(tweet.id, tweet.user.id, tweet.user.name, tweet.user.screenName, tweet.text, image,
                        tweet.user.screenName, city, createdDate, createdDate, true, eventLocation);

            } catch (ParseException | IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Event event) {
            // Notify views
            if (event != null)
                eventsFragmentRef.get().notifyEventAdded(event);

            else
                Log.e("AppLog", "Error formatting event!");
        }
    }
}
