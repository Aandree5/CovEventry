package g3.coveventry;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.util.Base64;
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
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.security.MessageDigest;
import java.util.Arrays;


public class HomeFragment extends Fragment {
    CallbackManager callbackManager;
    NavigationView navigationView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        final LoginButton loginButton = view.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile"));
        loginButton.setFragment(this);

        navigationView = getActivity().findViewById(R.id.nav_view);

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        Log.d("facebooklog", loginResult.toString());

                        GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                Log.d("FIELDS", object.toString());

                                View navHeader = navigationView.getHeaderView(0);

                                try {
                                    ((TextView)navHeader.findViewById(R.id.nav_header_name)).setText(object.getString("name"));
                                    ((ProfilePictureView)navHeader.findViewById(R.id.nav_header_photo)).setProfileId(loginResult.getAccessToken().getUserId());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        Bundle params = new Bundle();
                        params.putString("fields", "name");
                        graphRequest.setParameters(params);
                        graphRequest.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });


        // Development hash key
        try {
            final PackageInfo info = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                final String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("AppLog", "HashKey: " + hashKey + "=");
                ((TextView)view.findViewById(R.id.home_dev_hashkey)).setText(hashKey);
            }
        } catch (Exception e) {
            Log.e("AppLog", "error:", e);
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
