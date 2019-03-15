package g3.coveventry.socialmedia;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * Class to hold each friend information
 */
@SuppressWarnings("WeakerAccess")
public class Friend {
    String id;
    String name;
    String email;
    String username;
    Bitmap profilePicture;

    /**
     * Constructor
     *
     * @param id             ID of the friend
     * @param name           Name of the friend
     * @param email          Email of the friend
     * @param username       Username of the friend
     * @param profilePicture Profile picture of the friend
     */
    public Friend(String id, String name, String email, String username, Bitmap profilePicture) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.profilePicture = profilePicture;
    }

    @NonNull
    @Override
    public String toString() {
        return "ID: " + id + " | Name: " + name + " | Email: " + email;
    }
}
