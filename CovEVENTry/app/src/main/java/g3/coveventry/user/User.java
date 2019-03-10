package g3.coveventry.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Profile;
import com.twitter.sdk.android.core.TwitterCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import g3.coveventry.database.CallbackDBSimple;
import g3.coveventry.database.Database;
import g3.coveventry.utils.CallbackUser;

/**
 * Singleton class to hold user information at runtime
 */
public class User {
    // File to save the user profilePicture to
    static final String FILE_USER_PHOTO = "userPhoto.png";

    // One and only User instance
    private static User user = new User();
    // Weak reference to application context, doesn't prevent garbage collection
    private WeakReference<Context> contextRef = null;
    // Callback to execute when data is updated
    private CallbackUser callback = null;

    // Constant values for shared preferences data keys
    static final String KEY_ID = "userID";
    static final String KEY_NAME = "userName";
    static final String KEY_USERNAME = "userUsername";
    static final String KEY_EMAIL = "userEmail";
    static final String KEY_FACEBOOKID = "userFacebookID";
    static final String KEY_TWITTERID = "userTwitterID";
    static final String KEY_VERIFIED = "userVerified";

    // User data
    private long id = 0;
    private String name = null;
    private String username = null;
    private String email = null;
    private Bitmap profilePicture = null;
    private String facebookID = null;
    private String twitterID = null;
    private boolean verified = false;


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

            sharedPreferences.putLong(KEY_ID, id);
            sharedPreferences.putString(KEY_NAME, name);
            sharedPreferences.putString(KEY_EMAIL, email);
            sharedPreferences.putString(KEY_FACEBOOKID, facebookID);
            sharedPreferences.putString(KEY_TWITTERID, twitterID);
            sharedPreferences.putString(KEY_USERNAME, username);
            sharedPreferences.putBoolean(KEY_VERIFIED, verified);

            sharedPreferences.apply();

            // If profile picture is set, save it into a file
            if (profilePicture != null) {
                // Save profilePicture to file
                FileOutputStream fOutStream = null;
                try {
                    fOutStream = context.openFileOutput(FILE_USER_PHOTO, Context.MODE_PRIVATE);

                    profilePicture.compress(Bitmap.CompressFormat.PNG, 100, fOutStream);

                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                    if (fOutStream != null) {
                        try {
                            fOutStream.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


            // If set, run callback function
            if (callback != null)
                callback.userDataUpdated();

            // Save user on database
            Database.getInstance().saveUser(this, new CallbackDBSimple() {
                @Override
                public void connectionSuccessful() {

                }

                @Override
                public void connectionFailed(String message) {
                    Log.e("AppLog", message);
                    Toast.makeText(context, "Error connecting to database", Toast.LENGTH_SHORT).show();
                }
            });

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
     * @param context  Application context to hold a weak reference to
     * @param callback Function to execute when data is updated
     */
    public static void initialize(@NonNull Context context, @Nullable CallbackUser callback) {
        user.contextRef = new WeakReference<>(context);
        user.callback = callback;

        // Update user data, from shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        user.id = sharedPreferences.getLong(KEY_ID, 0);
        user.name = sharedPreferences.getString(KEY_NAME, null);
        user.email = sharedPreferences.getString(KEY_EMAIL, null);
        user.facebookID = sharedPreferences.getString(KEY_FACEBOOKID, null);
        user.twitterID = sharedPreferences.getString(KEY_TWITTERID, null);
        user.username = sharedPreferences.getString(KEY_USERNAME, null);
        user.verified = sharedPreferences.getBoolean(KEY_VERIFIED, false);

        if (new File(FILE_USER_PHOTO).exists()) {
            user.profilePicture = BitmapFactory.decodeFile(FILE_USER_PHOTO);
        }

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
     * Retrieves the user id
     *
     * @return The user ID
     */
    public long getID() {
        return id;
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
     * Retrieve emails
     *
     * @return The user email
     */
    public String getEmail() {
        return email;
    }


    /**
     * Retrieve the user profilePicture
     *
     * @return The user profile picture
     */
    public Bitmap getProfilePicture() {
        return profilePicture;
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
     * Retrieve username
     *
     * @return The user's username
     */
    public String getUsername() {
        return username;
    }


    /**
     * Check if current user has been verified on Twitter
     *
     * @return True if user has been verified, false otherwise
     */
    public boolean isVerified() {
        return verified;
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
     * @param id             Facebook ID of the user
     * @param name           Facebook name of the user
     * @param profilePicture Facebook profile picture of the user
     * @param email          Facebook email of the user
     */
    public void saveFacebook(@NonNull String id, @NonNull String name, @Nullable Bitmap profilePicture, @NonNull String email) {
        this.facebookID = id;
        this.name = name;

        // If there is no email set add it
        if (this.email == null)
            this.email = email;

        // If there is no profilePicture set add it
        if (this.profilePicture == null)
            this.profilePicture = profilePicture;

        // Save data to shared preferences, database and execute callback
        persistData();
    }


    /**
     * Save given information as data from Twitter - set on object, update shared preferences and
     * execute callback if is set
     *
     * @param id             Twitter ID of the user
     * @param name           Twitter name of the user
     * @param username       Twitter username of the user
     * @param profilePicture Twitter profile picture of the user
     * @param email          Twitter email of the user
     * @param verified       If twitter account is verified
     */
    public void saveTwitter(@NonNull String id, @NonNull String name, @NonNull String username, @Nullable Bitmap profilePicture, @NonNull String email, boolean verified) {
        this.twitterID = id;
        this.name = name;
        this.verified = verified;


        // If there is no username set add it
        if (this.username == null)
            this.username = username;

        // If there is no email set add it
        if (this.email == null)
            this.email = email;

        // If there is no profilePicture set add it
        if (this.profilePicture == null)
            this.profilePicture = profilePicture;

        // Save data to shared preferences, database and execute callback
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
            email = null;
            facebookID = null;
            profilePicture = null;

            // Delete user profilePicture file
            //noinspection ResultOfMethodCallIgnored
            new File(FILE_USER_PHOTO).delete();

            // Remove data from shared preferences
            Context context = contextRef.get();
            if (context != null) {
                SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();

                sharedPreferences.remove(KEY_NAME);
                sharedPreferences.remove(KEY_EMAIL);
                sharedPreferences.remove(KEY_FACEBOOKID);

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
            username = null;
            email = null;
            twitterID = null;
            profilePicture = null;

            // Delete user profilePicture file
            //noinspection ResultOfMethodCallIgnored
            new File(FILE_USER_PHOTO).delete();

            // Remove data from shared preferences
            Context context = contextRef.get();
            if (context != null) {
                SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();

                sharedPreferences.remove(KEY_NAME);
                sharedPreferences.remove(KEY_EMAIL);
                sharedPreferences.remove(KEY_TWITTERID);
                sharedPreferences.remove(KEY_USERNAME);

                sharedPreferences.apply();
            }

            // Execute callback if set
            if (callback != null)
                callback.userDataUpdated();
        } else {
            twitterID = null;
            username = null;

            // Remove id and username from shared preferences
            Context context = contextRef.get();
            if (context != null) {
                SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit();

                sharedPreferences.remove(KEY_TWITTERID);
                sharedPreferences.remove(KEY_USERNAME);

                sharedPreferences.apply();
            }
        }
    }

}