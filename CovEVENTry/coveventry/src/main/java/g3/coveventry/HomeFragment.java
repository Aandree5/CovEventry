package g3.coveventry;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.services.AccountService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import g3.coveventry.customviews.FacebookLoginButton;
import g3.coveventry.customviews.TwitterLoginButton;
import g3.coveventry.user.CallbackDownloadPicture;
import g3.coveventry.user.User;
import retrofit2.Call;


public class HomeFragment extends Fragment {

    // Login buttons
    TwitterLoginButton twitterLoginButton;
    FacebookLoginButton facebookLoginButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        facebookLoginButton = view.findViewById(R.id.login_facebook);
        twitterLoginButton = view.findViewById(R.id.login_twitter);

        // Facebook login
        facebookLoginButton.setCallback(new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                // If login is successful, create the request for the needed user data
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), (object, response) -> {
                    if (response != null) {
                        JSONObject data = response.getJSONObject();

                        try {
                            // Download user picture if exists, run code to save user only after image was downloaded
                            // picture is null if there was no image
                            new DownloadProfilePicture(picture -> {
                                try {
                                    // Save retrieved user information
                                    User.getCurrentUser().saveFacebook(data.getString("id"), data.getString("name"),
                                            picture, data.getString("email"));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }).execute(data.getJSONObject("picture").getJSONObject("data").getString("url"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Set request parameters and execute request
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,picture");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        });

        // Twitter login
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                AccountService accountService = twitterApiClient.getAccountService();
                Call<com.twitter.sdk.android.core.models.User> call = accountService.verifyCredentials(true, true, true);
                call.enqueue(new Callback<com.twitter.sdk.android.core.models.User>() {
                    @Override
                    public void success(Result<com.twitter.sdk.android.core.models.User> userResult) {
                        // Download user picture if exists, run code to save user only after image was downloaded
                        // picture is null if there was no image
                        new DownloadProfilePicture(picture -> {

                            // Save retrieved user information
                            User.getCurrentUser().saveTwitter(String.valueOf(result.data.getUserId()), userResult.data.name,
                                    userResult.data.screenName, picture, userResult.data.email, userResult.data.verified);

                        }).execute(userResult.data.profileImageUrl);

                    }

                    @Override
                    public void failure(TwitterException exception) {
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
            }
        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass the activity result to the login buttons
        facebookLoginButton.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Class to download user social media pictures in the img_splashscreen_background
     */
    private static class DownloadProfilePicture extends AsyncTask<String, Void, Bitmap> {
        private CallbackDownloadPicture callback;

        // Set callback with code to run after getting the image
        DownloadProfilePicture(CallbackDownloadPicture callback) {
            this.callback = callback;
        }


        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap profilePicture = null;
            try {
                // Download image from url
                profilePicture = BitmapFactory.decodeStream(new URL(urls[0]).openConnection().getInputStream());


            } catch (IOException e) {
                e.printStackTrace();
            }

            return profilePicture;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            // After image downloaded call method to run code
            // bitmap will be null if there was no image to download
            callback.pictureDownloaded(bitmap);
        }
    }

}