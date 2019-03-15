package g3.coveventry.socialmedia;

import java.util.List;

/**
 * Callback to return retrieved friends list
 */
public interface CallbackFriends {

    /**
     * Retrieve friends
     *
     * @param friends List of user friends
     */
    void retrievedFriends(List<Friend> friends);


    /**
     * When no friends where retrieved
     */
    void noFriendsRetrieved();
}
