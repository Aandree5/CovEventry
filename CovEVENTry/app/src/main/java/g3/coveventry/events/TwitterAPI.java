package g3.coveventry.events;

import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.params.Geocode;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface to connect to twitter API and query about the needed data
 *
 */
public interface TwitterAPI {

    /**
     * Search for tweets with the given properties
     *
     * @param query (Required) Base query to first filter tweets
     * @param geocode Define the location from where to get tweets, falls back to user location if tweet has no geolocation
     * @param lang Define the language of the tweet
     * @param locale Define the locale of the tweet
     * @param resultType Type of result, form recent, popular or mixed
     * @param count Maximum number of tweets to retrieve, max. 100
     * @param until Date until when to get the tweets
     * @param sinceId Get tweet from this tweet ID forward
     * @param maxId Maximum tweet ID to get the tweets to
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
     * @param query (Required) Base query to first filter tweets
     * @param page Page for this query
     * @param count Maximum number of users to retrieve
     * @param includeEntities Include users entities
     * @return A callback as a list of users method that has the retrieved users
     */
    @GET("users/search.json")
    Call<List<User>> searchUsers(@Query("q") String query,
                                 @Query("page") Integer page,
                                 @Query("count") Integer count,
                                 @Query("include_entities") Boolean includeEntities);
}