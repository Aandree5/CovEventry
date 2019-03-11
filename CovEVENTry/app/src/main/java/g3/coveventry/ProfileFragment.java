package g3.coveventry;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import static java.nio.file.Files.exists;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView n = view.findViewById(R.id.user_name);

        n.setText(User.getCurrentUser().getName());

        TextView t = view.findViewById(R.id.twt_username);

        t.setText(User.getCurrentUser().getTwitterUsername());
        //not sure about this one
        TextView w = view.findViewById(R.id.fac_username);
        w.setText(User.getCurrentUser().getName());

        TextView e = view.findViewById(R.id.user_email);

        e.setText(User.getCurrentUser().getName());

 //       ImageView i = view.findViewById(R.id.profile_photo);

//        i.setImageResource(User.getCurrentUser().getPhoto());
        //Andre how do I get user profile picture?

        return view;
    }
}
