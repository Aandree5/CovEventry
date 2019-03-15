package g3.coveventry.socialmedia;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.models.Identifiable;
import com.twitter.sdk.android.core.models.User;

import java.io.Serializable;
import java.util.List;

/**
 * Modal to retrieve friends from Twitter
 */
@SuppressWarnings("WeakerAccess")
public class TwitterFriendsModel implements Identifiable, Serializable {

    // List of users
    @SerializedName("users")
    public final List<User> users;

    // Pointers to navigate user pages
    @SerializedName("previous_cursor")
    public final long previousCursor;
    @SerializedName("previous_cursor_str")
    public final String previousCursorStr;
    @SerializedName("next_cursor")
    public final long nextCursor;
    @SerializedName("next_cursor_str")
    public final String nextCursorStr;

    /**
     * Constructor
     *
     * @param users             List of users
     * @param previousCursor    Pointer to the previous page
     * @param previousCursorStr Pointer to the previous page, as a string
     * @param nextCursor        Pointer to the next page
     * @param nextCursorStr     Pointer to the next page, as a string
     */
    public TwitterFriendsModel(List<User> users, long previousCursor, String previousCursorStr, long nextCursor, String nextCursorStr) {
        this.users = users;
        this.previousCursor = previousCursor;
        this.previousCursorStr = previousCursorStr;
        this.nextCursor = nextCursor;
        this.nextCursorStr = nextCursorStr;
    }

    @Override
    public long getId() {
        return Long.valueOf(previousCursorStr + nextCursorStr);
    }
}
