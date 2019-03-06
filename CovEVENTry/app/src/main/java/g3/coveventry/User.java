package g3.coveventry;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.Profile;
import com.twitter.sdk.android.core.TwitterCore;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import g3.coveventry.utils.CallbackUser;

/**
 * Singleton class to hold user information at runtime
 */
public class User {
    // File to save the user photo to
    static final String FILE_USER_PHOTO = "userPhoto.png";

    // One and only User instance
    private static User user = new User();
    // Weak reference to application context, doesn't prevent garbage collection
    private WeakReference<Context> contextRef = null;
    // Callback to execute when data is updated
    private CallbackUser callback = null;

    // Constant values for shared preferences data keys
    static final String KEY_NAME = "name";
    static final String KEY_EMAILS = "emails";
    static final String KEY_PHOTOURL = "photoUrl";
    static final String KEY_FACEBOOKID = "facebookID";
    static final String KEY_TWITTERID = "twitterID";
    static final String KEY_TWITTERUSERNAME = "twiterUsername";

    // User data
    private String name = null;
    private Set<String> emails = null;
    private String facebookID = null;
    private String twitterID = null;
    private String twitterUsername = null;


    /**
     * Private constructor, so class can't be instantiated outside itself
     */
    private User() {
    }


    /**
     * Save user data to shared preferences and execute callback function if set
     */
    private void persistData() {
        // Check if context still exists
        Context context = contextRef.get();
        if (context != null) {
            // Update shared preferences
            SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();

            sharedPreferences.putString(KEY_NAME, name);
            sharedPreferences.putStringSet(KEY_EMAILS, emails);
            sharedPreferences.putString(KEY_FACEBOOKID, facebookID);
            sharedPreferences.putString(KEY_TWITTERID, twitterID);
            sharedPreferences.putString(KEY_TWITTERUSERNAME, twitterUsername);

            sharedPreferences.apply();

            // If set, run callback function
            if (callback != null)
                callback.userDataUpdated();
        } else
            throw new RuntimeException("Context reference not set, make sure to initialize user first.");
    }

    /**
     * Confirms if Facebook's and Twitter's SDK still have an active connection, if don't
     * remove the information from the user
     */
    private void checkSocialMediaAPIStatus() {
        // If has Facebook ID confirm if Facebook SDK still has a profile
        if (user.facebookID != null && Profile.getCurrentProfile() == null)
            user.removeFacebook();

        // If has Twitter ID confirm if Twitter SDK still has a session
        if (user.twitterID != null && TwitterCore.getInstance().getSessionManager().getActiveSession() == null)
            user.removeTwitter();
    }


    /**
     * Initialize user object, setting context reference and callback function, updates user
     * data from shared preferences and executes callback function
     *
     * @param context Application context to hold a weak reference to
     * @param callback Function to execute when data is updated
     */
    static void initialize(@NonNull Context context, @Nullable CallbackUser callback) {
        user.contextRef = new WeakReference<>(context);
        user.callback = callback;

        // Update user data, from shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        user.name = sharedPreferences.getString(KEY_NAME, null);
        user.emails = sharedPreferences.getStringSet(KEY_EMAILS, null);
        user.facebookID = sharedPreferences.getString(KEY_FACEBOOKID, null);
        user.twitterID = sharedPreferences.getString(KEY_TWITTERID, null);
        user.twitterUsername = sharedPreferences.getString(KEY_TWITTERUSERNAME, null);

        // Check SDKs status
        user.checkSocialMediaAPIStatus();

        // If set, run callback function
        if (callback != null)
            callback.userDataUpdated();
    }


    /**
     * Checks if context is still valid and return the user object
     *
     * @return The user object
     */
    public static User getCurrentUser() {
        if (user.contextRef.get() == null)
            throw new RuntimeException("Context reference not set, make sure to initialize user first.");

        // Check SDKs status
        user.checkSocialMediaAPIStatus();

        return user;
    }


    /**
     * Retrieve name
     *
     * @return The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Add and email to the set
     *
     * @param email Email to be added to the set
     */
    public void addEmail(String email) {
        emails.add(email);

        persistData();
    }

    /**
     * Retrieve emails
     *
     * @return Unmodifiable set of the user's emails
     */
    public Set<String> getEmails() {
        return Collections.unmodifiableSet(emails);
    }

    /**
     * Retrieve Facebook ID
     *
     * @return The user's Facebook ID
     */
    public String getFacebookID() {
        return facebookID;
    }

    /**
     * Retrieve Twitter ID
     *
     * @return The user's Twitter ID
     */
    public String getTwitterID() {
        return twitterID;
    }

    /**
     * Retrieve Twitter username
     *
     * @return The user's Twitter username
     */
    public String getTwitterUsername() {
        return twitterUsername;
    }


    /**
     * Check if has user's Facebook data
     *
     * @return True if has user's Facebook data, false otherwise
     */
    public boolean isFacebookConnected() {
        return facebookID != null;
    }


    /**
     * Check if has user's Twitter data
     *
     * @return True if has user's Twitter data, false otherwise
     */
    public boolean isTwitterConnected() {
        return twitterID != null;
    }


    /**
     * Save given information as data from Facebook - set on object, update shared preferences and
     * execute callback if is set
     *
     * @param id       Facebook ID of the user
     * @param name     Facebook name of the user
     * @param photoUrl Facebook profile picture of the user
     * @param email    Facebook email of the user
     */
    void saveFacebook(@NonNull String id, @NonNull String name, @Nullable String photoUrl, @NonNull String email) {
        this.facebookID = id;
        this.name = name;

        // If there where already emails add a new, otherwise create a new set
        if (this.emails != null)
            this.emails.add(email);

        else
            this.emails = new HashSet<>(Collections.singletonList(email));

        // If photo is given save it to shared preferences, as is not kept on the object
        if (photoUrl != null) {
            Context context = contextRef.get();
            if (context != null)
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .putString(KEY_PHOTOURL, photoUrl)
                        .apply();
        }

        // Save data to shared preferences and execute callback
        persistData();
    }


    /**
     * Save given information as data from Twitter - set on object, update shared preferences and
     * execute callback if is set
     *
     * @param id       Twitter ID of the user
     * @param name     Twitter name of the user
     * @param username Twitter username of the user
     * @param photoUrl Twitter profile picture of the user
     * @param email    Twitter email of the user
     */
    void saveTwitter(@NonNull String id, @NonNull String name, @NonNull String username,
                     @Nullable String photoUrl, @NonNull String email) {
        this.twitterID = id;
        this.name = name;
        this.twitterUsername = username;

        // If there where already emails add a new, otherwise create a new set
        if (this.emails != null)
            this.emails.add(email);

        else
            this.emails = new HashSet<>(Collections.singletonList(email));

        // If photo is given save it to shared preferences, as is not kept on the object
        if (photoUrl != null) {
            Context context = contextRef.get();
            if (context != null)
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .putString(KEY_PHOTOURL, photoUrl)
                        .apply();
        }

        // Save data to shared preferences and execute callback
        persistData();
    }


    /**
     * Remove information kept from Facebook, both from object and shared preferences,
     * also execute callback if set
     */
    public void removeFacebook() {
        // If only logged in with Facebook, remove all data, otherwise remove just the Facebook id
        if (!isTwitterConnected()) {
            name = null;
            emails = null;
            facebookID = null;

            // Delete user photo file
            //noinspection ResultOfMethodCallIgnored
            new File(FILE_USER_PHOTO).delete();

            // Remove data from shared preferences
            Context context = contextRef.get();
            if (context != null) {
                SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();

                sharedPreferences.remove(KEY_NAME);
                sharedPreferences.remove(KEY_EMAILS);
                sharedPreferences.remove(KEY_FACEBOOKID);
                sharedPreferences.remove(KEY_PHOTOURL);

                sharedPreferences.apply();
            }

            // Execute callback if set
            if (callback != null)
                callback.userDataUpdated();
        } else {
            facebookID = null;

            // Remove id from shared preferences
            Context context = contextRef.get();
            if (context != null)
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .remove(KEY_FACEBOOKID)
                        .apply();
        }
    }


    /**
     * Remove information kept from Twitter, both from object and shared preferences,
     * also execute callback if set
     */
    public void removeTwitter() {
        // If only logged in with Twitter, remove all data, otherwise remove just the Twitter id and username
        if (!isFacebookConnected()) {
            name = null;
            emails = null;
            twitterID = null;
            twitterUsername = null;

            // Delete user photo file
            //noinspection ResultOfMethodCallIgnored
            new File(FILE_USER_PHOTO).delete();

            // Remove data from shared preferences
            Context context = contextRef.get();
            if (context != null) {
                SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();

                sharedPreferences.remove(KEY_NAME);
                sharedPreferences.remove(KEY_EMAILS);
                sharedPreferences.remove(KEY_TWITTERID);
                sharedPreferences.remove(KEY_TWITTERUSERNAME);
                sharedPreferences.remove(KEY_PHOTOURL);

                sharedPreferences.apply();
            }

            // Execute callback if set
            if (callback != null)
                callback.userDataUpdated();
        } else {
            twitterID = null;
            twitterUsername = null;

            // Remove id and username from shared preferences
            Context context = contextRef.get();
            if (context != null) {
                SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();

                sharedPreferences.remove(KEY_TWITTERID);
                sharedPreferences.remove(KEY_TWITTERUSERNAME);

                sharedPreferences.apply();
            }
        }
    }
}
