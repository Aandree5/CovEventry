package g3.coveventry;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.params.Geocode;
import com.twitter.sdk.android.tweetui.BasicTimelineFilter;
import com.twitter.sdk.android.tweetui.CollectionTimeline;
import com.twitter.sdk.android.tweetui.FilterValues;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineFilter;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;
import com.twitter.sdk.android.tweetui.TwitterListTimeline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class EventsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.fe_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        final TimelineFilter timelineFilter = new TimelineFilter() {
            @Override
            public List<Tweet> filter(List<Tweet> tweets) {
                ArrayList<Tweet> filteredTweets = new ArrayList<>();
                for (Tweet tweet : tweets) {
                    String date = tweet.createdAt;

                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.getDefault());
                    try {
                        Date strDate = sdf.parse(date);

                        //if(DateUtils.isToday(strDate.getTime()))
                        //   filteredTweets.add(tweet);

                       /* Date today = Calendar.getInstance().getTime();
                        sdf.applyPattern("yyyyMMdd");
                        if(sdf.format(strDate).equals(sdf.format(today)))
                           filteredTweets.add(tweet);*/

                        Date today = Calendar.getInstance().getTime();
                        sdf.applyPattern("yyyyMMdd");
                        if (sdf.format(strDate).equals("20190216"))
                            filteredTweets.add(tweet);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                return Collections.unmodifiableList(filteredTweets);
            }

            @Override
            public int totalFilters() {
                return 1;
            }
        };

        SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query("coventry&party")
                .build();

        TweetTimelineRecyclerViewAdapter adapter = new TweetTimelineRecyclerViewAdapter.Builder(getContext())
                .setTimeline(searchTimeline)
                .setTimelineFilter(timelineFilter)
                .build();

        recyclerView.setAdapter(adapter);

        return view;
    }

}
