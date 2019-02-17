package g3.coveventry.customViews;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import g3.coveventry.R;

/**
 * Class to handle Twitter's log in and log out
 */
public class TwitterLoginButton extends BaseLoginButton {
    // Callback to execute after the user attempts to login
    Callback<TwitterSession> callback;
    // Twitter authentication client, manages authentications
    volatile TwitterAuthClient authClient;

    public TwitterLoginButton(Context context) {
        this(context, null);
    }

    public TwitterLoginButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public TwitterLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Set Twitter specific logo, color and text
        Resources res = getResources();
        setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(R.drawable.ic_logo_twitter, getContext().getTheme()),
                null, null, null);

        setBackground(res.getDrawable(R.drawable.dr_login_twitter, getContext().getTheme()));

        setText(res.getString(R.string.login_twitter));

        // Prevent code to run on the editor, allow to visualize the button
        if (!isInEditMode()) {
            // If user was already logged in, change the text to "Log out"
            TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();

            if (twitterSession != null)
                setText(res.getString(R.string.logout));

            // Check needed initialization
            checkTwitterCore();
        }

        // Log in or log out, when clicking the button, depending if the user is logged or not
        setOnClickListener(view -> {
            // Check if callback was defined
            if (callback == null)
                throw new RuntimeException("Callback not set!");

            TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();

            if (twitterSession == null) {
                // Callback just to update the text on success and send the call through the next callback
                getTwitterAuthClient().authorize(getActivity(), new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {
                        // Update text
                        setText(res.getString(R.string.logout));

                        Toast.makeText(getContext(), res.getString(R.string.login_successful), Toast.LENGTH_SHORT).show();

                        callback.success(result);
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        callback.failure(exception);
                    }
                });
            } else {
                TwitterCore.getInstance().getSessionManager().clearActiveSession();

                // Update text
                setText(res.getString(R.string.login_twitter));
            }
        });
    }


    /**
     * SSet the function to be called when user attempts to log in
     *
     * @param callback Function to be executed.
     */
    public void setCallback(@NonNull Callback<TwitterSession> callback) {
        this.callback = callback;
    }


    /**
     * Called to complete authorization flow, sends the information to the authentication client
     *
     * @param requestCode The request code used for the authentication
     * @param resultCode  The result code returned by the authentication activity
     * @param data        the result data returned by the authentication activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Only continue to send data if it's the requested code
        if (requestCode == getTwitterAuthClient().getRequestCode())
            getTwitterAuthClient().onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Creates only one authentication client and returns it
     *
     * @return The button's authentication client
     */
    TwitterAuthClient getTwitterAuthClient() {
        // Creates an intance if there's none yet
        if (authClient == null) {
            synchronized (TwitterLoginButton.class) {
                if (authClient == null) {
                    authClient = new TwitterAuthClient();
                }
            }
        }

        return authClient;
    }

    /**
     * Check if TwitterCore has been initialized, disable button if not
     */
    private void checkTwitterCore() {
        try {
            TwitterCore.getInstance();
        } catch (IllegalStateException ex) {
            // Disable if TwitterCore hasn't started
            Log.e("AppLog", "TwitterCore not initialized");
            setEnabled(false);
        }
    }
}
