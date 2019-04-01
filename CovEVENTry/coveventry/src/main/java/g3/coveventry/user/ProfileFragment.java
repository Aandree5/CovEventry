package g3.coveventry.user;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import g3.coveventry.R;
import g3.coveventry.customviews.CovImageView;
import g3.coveventry.customviews.FacebookLoginButton;
import g3.coveventry.customviews.TwitterLoginButton;
import retrofit2.Call;

public class ProfileFragment extends Fragment {

    // Login buttons
    TwitterLoginButton twitterLoginButton;
    FacebookLoginButton facebookLoginButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        facebookLoginButton = view.findViewById(R.id.login_facebook);
        twitterLoginButton = view.findViewById(R.id.login_twitter);

        TextView n = view.findViewById(R.id.user_name);

        n.setText(User.getCurrentUser().getName());

        TextView t = view.findViewById(R.id.twt_username);

        t.setText(User.getCurrentUser().getUsername());
        //not sure about this one
        TextView w = view.findViewById(R.id.fac_username);
        w.setText(User.getCurrentUser().getName());

        TextView e = view.findViewById(R.id.user_email);

        e.setText(User.getCurrentUser().getName());

        CovImageView i = view.findViewById(R.id.profile_photo);

        i.setImageBitmap(User.getCurrentUser().getProfilePicture());


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
                            new ProfileFragment.DownloadProfilePicture(picture -> {
                                try {
                                    // Save retrieved user information
                                    User.getCurrentUser().saveFacebook(data.getString("id"), data.getString("name"),
                                            picture, data.getString("email"));

                                    // Not disturbing other code
                                    n.setText(data.getString("name"));
                                    t.setText(User.getCurrentUser().getUsername());
                                    w.setText(User.getCurrentUser().getName());
                                    e.setText(data.getString("email"));
                                    i.setImageBitmap(picture);

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
                        new ProfileFragment.DownloadProfilePicture(picture -> {

                            // Save retrieved user information
                            User.getCurrentUser().saveTwitter(String.valueOf(result.data.getUserId()), userResult.data.name,
                                    userResult.data.screenName, picture, userResult.data.email, userResult.data.verified);


                            // Not disturbing other code
                            n.setText(userResult.data.name);
                            t.setText(userResult.data.screenName);
                            w.setText(User.getCurrentUser().getName());
                            e.setText(userResult.data.email);
                            i.setImageBitmap(picture);

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
