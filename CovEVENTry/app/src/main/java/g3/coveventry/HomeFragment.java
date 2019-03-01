package g3.coveventry;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
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

import g3.coveventry.customviews.FacebookLoginButton;
import g3.coveventry.customviews.TwitterLoginButton;
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
                Bundle params = new Bundle();
                params.putString("fields", "name,email,picture.type(normal)");

                new GraphRequest(loginResult.getAccessToken(), "me", params, HttpMethod.GET, response -> {
                    if (response != null) {
                        try {
                            JSONObject data = response.getJSONObject();
                            String photoUrl = null;

                            // Get photo URL
                            if (data.has("picture"))
                                photoUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");

                            User.getCurrentUser().saveFacebook(loginResult.getAccessToken().getUserId(),
                                    data.getString("name"), photoUrl, data.getString("email"));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).executeAsync();
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
                        User.getCurrentUser().saveTwitter(String.valueOf(result.data.getUserId()), userResult.data.name,
                                userResult.data.screenName, userResult.data.profileImageUrl, userResult.data.email);
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
}
