package g3.coveventry.events;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import g3.coveventry.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddEventFragment extends Fragment {

    ImageView imageView;
    Button ImageBtn, CancelBtn;
    EditText EventName, EventVenue, EventPostCode, EventDate, EventDescription;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;

    public AddEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_event, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Declare items
        imageView = view.findViewById(R.id.imageView);
        ImageBtn = view.findViewById(R.id.ImageBtn);
        CancelBtn = view.findViewById(R.id.CancelBtn);
        EventName = view.findViewById(R.id.EventName);
        EventVenue = view.findViewById(R.id.EventVenue);
        EventPostCode = view.findViewById(R.id.EventPostCode);
        EventDate = view.findViewById(R.id.EventDate);
        EventDescription = view.findViewById(R.id.EventDescription);

        //Pull image from local gallery
        ImageBtn.setOnClickListener(v -> openGallery());


        CancelBtn.setOnClickListener(v -> {
            EventName.setText("");
            EventVenue.setText("");
            EventPostCode.setText("");
            EventDate.setText("");
            EventDescription.setText("");
            imageView.setImageResource(0);
        });

    }

    // Open local gallery
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    // Set ImageView to the selected image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }


}
