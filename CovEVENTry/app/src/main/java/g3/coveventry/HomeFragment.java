package g3.coveventry;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import g3.coveventry.customViews.RoundImage;

import static g3.coveventry.MainActivity.loginForm;
import static g3.coveventry.MainActivity.userEmail;
import static g3.coveventry.MainActivity.userID;
import static g3.coveventry.MainActivity.userName;


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

                        // If login is successful, create the request for the needed user data
                        Bundle params = new Bundle();
                        params.putString("fields", "name,email,picture.type(normal)");
                        new GraphRequest(loginResult.getAccessToken(), "me", params, HttpMethod.GET, new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                if (response != null) {
                                    try {
                                        JSONObject data = response.getJSONObject();

                                        // Find drawer header
                                        NavigationView navigationView = Objects.requireNonNull(getActivity()).findViewById(R.id.nav_view);
                                        View navHeader = navigationView.getHeaderView(0);

                                        // Save user info
                                        userID = loginResult.getAccessToken().getUserId();
                                        userName = data.getString("name");
                                        loginForm = MainActivity.LoginForm.LF_FACEBOOK;
                                        userEmail = data.getString("email");

                                        // Set user info
                                        ((TextView)navHeader.findViewById(R.id.nav_header_name)).setText(userName);

                                        if (data.has("picture")) {
                                            // Get photo URL
                                            URL photoURL = new URL(data.getJSONObject("picture").getJSONObject("data").getString("url"));

                                            // Start background task to download image
                                            new DownloadUserPhoto(new WeakReference<RoundImage>((RoundImage) navHeader.findViewById(R.id.nav_header_photo)))
                                                    .execute(photoURL);
                                        }
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                    }
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


    /**
     * Downloads an image from the given URL to the given RoundImage view
     */
    public static class DownloadUserPhoto extends AsyncTask<URL, Void, Bitmap>
    {
        WeakReference<RoundImage> rndImgView;

        /**
         * With a reference to a RoundImage view where to put the downloaded image
         * @param rndImgView Weak reference to the RoundImage view
         */
        DownloadUserPhoto(WeakReference<RoundImage> rndImgView) {
            this.rndImgView = rndImgView;
        }

        @Override
        protected Bitmap doInBackground(URL... urls) {

            try {
                //TODO:Save image to file
                // Download photo from url
                return BitmapFactory.decodeStream(urls[0].openConnection().getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            RoundImage riView = rndImgView.get();
            if (bitmap != null &&  riView!= null)
            {
                // Set user photo
                riView.setImageBitmap(bitmap);
            }
        }
    }
}
