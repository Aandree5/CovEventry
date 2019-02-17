package g3.coveventry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import g3.coveventry.customViews.FacebookLoginButton;
import g3.coveventry.customViews.TwitterLoginButton;

import static g3.coveventry.User.KEY_EMAILS;
import static g3.coveventry.User.KEY_FACEBOOKID;
import static g3.coveventry.User.KEY_NAME;
import static g3.coveventry.User.KEY_PHOTOURL;
import static g3.coveventry.User.KEY_TWITTERID;
import static g3.coveventry.User.KEY_TWITTERUSERNAME;


public class HomeFragment extends Fragment {

    // Callback for Facebook login
    TwitterLoginButton twitterLoginButton;
    FacebookLoginButton facebookLoginButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        facebookLoginButton = view.findViewById(R.id.login_facebook);

        // Facebook login
        facebookLoginButton.setCallback(new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();

                // If login is successful, create the request for the needed user data
                Bundle params = new Bundle();
                params.putString("fields", "name,email,picture.type(normal)");

                // TODO: Test GraphRequestAsyncTask to get user photo
                new GraphRequest(loginResult.getAccessToken(), "me", params, HttpMethod.GET, response -> {
                    if (response != null) {
                        try {
                            JSONObject data = response.getJSONObject();
                            String photoUrl = null;

                            // Get photo URL
                            if (data.has("picture"))
                                photoUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");

                            // Save user info
                            SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext())).edit();
                            sharedPreferences.putString(KEY_FACEBOOKID, loginResult.getAccessToken().getUserId());
                            sharedPreferences.putString(KEY_NAME, data.getString("name"));

                            Set<String> emails = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext())).getStringSet(KEY_EMAILS, null);

                            if (emails != null) {
                                emails.add(data.getString("email"));
                                sharedPreferences.putStringSet(KEY_EMAILS, emails).apply();
                            } else {
                                sharedPreferences.putStringSet(KEY_EMAILS, new HashSet<>(Collections.singletonList(data.getString("email")))).apply();
                            }

                            // Only save photo url if there was one
                            if (photoUrl != null)
                                sharedPreferences.putString(KEY_PHOTOURL, photoUrl);

                            sharedPreferences.apply();

                            ((MainActivity) Objects.requireNonNull(getActivity())).createUser();

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
        twitterLoginButton = view.findViewById(R.id.login_twitter);


        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterAuthClient twitterAuthClient = new TwitterAuthClient();
                twitterAuthClient.requestEmail(result.data, new Callback<String>() {
                    @Override
                    public void success(Result<String> result) {
                        // Save email
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
                        Set<String> emails = sharedPreferences.getStringSet(KEY_EMAILS, null);

                        if (emails != null) {
                            emails.add(result.data);
                            sharedPreferences.edit().putStringSet(KEY_EMAILS, emails).apply();
                        } else {
                            sharedPreferences.edit().putStringSet(KEY_EMAILS, new HashSet<>(Collections.singletonList(result.data))).apply();
                        }
                    }

                    @Override
                    public void failure(TwitterException exception) {

                    }
                });

                // Save user info
                SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext())).edit();
                sharedPreferences.putString(KEY_TWITTERID, String.valueOf(result.data.getUserId()));
                sharedPreferences.putString(KEY_TWITTERUSERNAME, result.data.getUserName());

                sharedPreferences.apply();
            }

            @Override
            public void failure(TwitterException exception) {
            }
        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Send data to facebook sdk to be checked
        facebookLoginButton.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result to the login button
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }
}
