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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

import static g3.coveventry.User.KEY_EMAIL;
import static g3.coveventry.User.KEY_FACEBOOKID;
import static g3.coveventry.User.KEY_NAME;
import static g3.coveventry.User.KEY_PHOTOURL;


public class HomeFragment extends Fragment {

    // Callback for Facebook login
    CallbackManager callbackManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        final LoginButton loginButton = view.findViewById(R.id.login_button);

        // Set needed permission
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.setFragment(this);

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {

                        Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();

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

                                    // Save user info
                                    SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext())).edit();
                                    sharedPreferences.putString(KEY_FACEBOOKID, loginResult.getAccessToken().getUserId());
                                    sharedPreferences.putString(KEY_NAME, data.getString("name"));
                                    sharedPreferences.putString(KEY_EMAIL, data.getString("email"));

                                    // Only save photo url if there was one
                                    if (photoUrl != null)
                                        sharedPreferences.putString(KEY_PHOTOURL, photoUrl);

                                    sharedPreferences.apply();

                                    ((MainActivity)Objects.requireNonNull(getActivity())).createUser();

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

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Send data to facebook sdk to be checked
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
