package g3.coveventry.customViews;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdkNotInitializedException;
import com.facebook.Profile;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import g3.coveventry.R;


/**
 * Class to handle Facebook's log in and log out
 */
public class FacebookLoginButton extends BaseLoginButton {
    // Hold the manager that receives the call when the user tries to log in
    CallbackManager callbackManager;
    // Callback to execute after the user attempts to login
    FacebookCallback<LoginResult> callback;

    public FacebookLoginButton(Context context) {
        this(context, null);
    }

    public FacebookLoginButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public FacebookLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Set Facebook specific logo, color and text
        Resources res = getResources();
        setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(R.drawable.ic_logo_facebook, getContext().getTheme()),
                null, null, null);

        setBackground(res.getDrawable(R.drawable.dr_login_facebook, getContext().getTheme()));

        setText(getResources().getString(R.string.login_facebook));

        // Prevent code to run on the editor, allow to visualize the button
        if (!isInEditMode()) {
            // If user was already logged in, change the text to "Log out"
            if (Profile.getCurrentProfile() != null)
                setText(getResources().getString(R.string.logout));

            // Create the callback manager to handle the callback call, and register with the login manager
            // callback just to update the text on success and send the call through the next callback
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // Update text
                    setText(getResources().getString(R.string.logout));

                    callback.onSuccess(loginResult);
                }

                @Override
                public void onCancel() {
                    callback.onCancel();
                }

                @Override
                public void onError(FacebookException error) {
                    callback.onError(error);
                }
            });

            // Check needed initialization
            checkFacebookSDK();
        }

        // Log in or log out, when clicking the button, depending if the user is logged or not
        setOnClickListener(view -> {
            // Check if callback was defined
            if (callback == null)
                throw new RuntimeException("Callback not set!");

            if (Profile.getCurrentProfile() == null) {
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email"));

            } else {
                LoginManager.getInstance().logOut();

                // Update text, there's no callback on log out, so the text has to be updated here
                setText(getResources().getString(R.string.login_facebook));
            }
        });
    }


    /**
     * Set the function to be called when user attempts to log in
     *
     * @param callback Function to be executed
     */
    public void setCallback(@NonNull FacebookCallback<LoginResult> callback) {
        this.callback = callback;
    }


    /**
     * Called to complete authorization flow, sends the information to the callback manager
     *
     * @param requestCode The request code used for the authentication
     * @param resultCode  The result code returned by the authentication activity
     * @param data        the result data returned by the authentication activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Only continue to send data if it's the requested code
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode())
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Check if Facebook SDK has been initialized, disable button if not
     */
    private void checkFacebookSDK() {
        try {
            LoginManager.getInstance();
        } catch (FacebookSdkNotInitializedException ex) {
            // Disable if Facebook sdk hasn't started
            Log.e("AppLog", "Facebook sdk not initialized");
            setEnabled(false);
        }
    }
}
