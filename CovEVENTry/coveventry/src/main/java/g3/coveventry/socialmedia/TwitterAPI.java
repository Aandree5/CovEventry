package g3.coveventry.socialmedia;

import com.google.gson.GsonBuilder;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.internal.network.OkHttpClientHelper;
import com.twitter.sdk.android.core.models.BindingValues;
import com.twitter.sdk.android.core.models.BindingValuesAdapter;
import com.twitter.sdk.android.core.models.SafeListAdapter;
import com.twitter.sdk.android.core.models.SafeMapAdapter;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.params.Geocode;

import java.util.List;

import retrofit2.Call;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface to connect to twitter API and query about the needed data
 */
public interface TwitterAPI {

    /**
     * Search for tweets with the given properties
     *
     * @param query           (Required) Base query to first filter tweets
     * @param geocode         Define the location from where to get tweets, falls back to user location if tweet has no geolocation
     * @param lang            Define the language of the tweet
     * @param locale          Define the locale of the tweet
     * @param resultType      Type of result, form recent, popular or mixed
     * @param count           Maximum number of tweets to retrieve, max. 100
     * @param until           Date until when to get the tweets
     * @param sinceId         Get tweet from this tweet ID forward
     * @param maxId           Maximum tweet ID to get the tweets to
     * @param includeEntities Include tweet entities, like hashtags and places
     * @return A callback as a Search method that has the retrieved tweets
     */
    @GET("search/tweets.json?tweet_mode=extended")
    Call<Search> searchTweets(@Query("q") String query,
                              //EncodedQuery protects commas from encode
                              @Query(value = "geocode", encoded = true) Geocode geocode,
                              @Query("lang") String lang,
                              @Query("locale") String locale,
                              @Query("result_type") String resultType,
                              @Query("count") Integer count,
                              @Query("until") String until,
                              @Query("since_id") Long sinceId,
                              @Query("max_id") Long maxId,
                              @Query("include_entities") Boolean includeEntities);


    /**
     * Get users with the given properties
     * Users are retrieved in pages of 20
     *
     * @param query           (Required) Base query to first filter tweets
     * @param page            Page for this query
     * @param count           Maximum number of users to retrieve
     * @param includeEntities Include users entities
     * @return A callback as a list of users method that has the retrieved users
     */
    @GET("users/search.json")
    Call<List<User>> searchUsers(@Query("q") String query,
                                 @Query("page") Integer page,
                                 @Query("count") Integer count,
                                 @Query("include_entities") Boolean includeEntities);


    /**
     * Retrieve a list of following users, considered friends by Twitter
     * Users that the given user is following
     *
     * @param userID          Id of the user to get the friends from
     * @param screenName      Screen name of the user to get the friends from
     * @param cursor          Page to check the given users, max 200 users per page
     * @param count           Number of users to retrieve per page, max 200
     * @param skipStatus      When set to true, statuses will not be included in the returned user objects
     * @param includeEntities The user object entities node will not be included when set to false
     * @return A model with a list of users and pointer for the previous and next user pages
     */
    @GET("friends/list.json")
    Call<TwitterFriendsModel> getFriends(@Query("user_id") Long userID,
                                         @Query("screen_name") String screenName,
                                         @Query("cursor") Long cursor,
                                         @Query("count") Integer count,
                                         @Query("skip_status") Boolean skipStatus,
                                         @Query("include_user_entities") Boolean includeEntities);

    /**
     * Create object of TwitterAPI with retrofit
     *
     * @return Returns an object of the TwitterAPI class
     */
    static TwitterAPI build() {
        return new retrofit2.Retrofit.Builder()
                .client(OkHttpClientHelper.getOkHttpClient(TwitterCore.getInstance().getGuestSessionProvider()))
                .baseUrl("https://api.twitter.com/1.1/")
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .registerTypeAdapterFactory(new SafeListAdapter())
                        .registerTypeAdapterFactory(new SafeMapAdapter())
                        .registerTypeAdapter(BindingValues.class, new BindingValuesAdapter())
                        .create()))
                .build()
                .create(TwitterAPI.class);
    }
}