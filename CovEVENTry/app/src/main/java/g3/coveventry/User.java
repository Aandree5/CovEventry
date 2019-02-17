package g3.coveventry;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;
import java.util.Set;

import g3.coveventry.customViews.CovImageView;

class User {
    static final String FILE_USER_PHOTO = "userPhoto.png";

    // TODO: Better handle user info, with secial media log out, save info to database, will help with the task
    static final String KEY_FACEBOOKID = "facebookID";
    static final String KEY_NAME = "name";
    static final String KEY_EMAILS = "emails";
    static final String KEY_PHOTOURL = "photoUrl";
    static final String KEY_TWITTERID = "twitterID";
    static final String KEY_TWITTERUSERNAME = "twiterUsername";

    String facebookID = null;
    String name = null;
    Set<String> email = null;

    public User(String facebookID, String name, Set<String> email) {
        this.facebookID = facebookID;
        this.name = name;
        this.email = email;
    }
}
