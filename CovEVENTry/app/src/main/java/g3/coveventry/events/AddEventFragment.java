package g3.coveventry;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //Declair items
        imageView = (ImageView) getView().findViewById(R.id.imageView);
        ImageBtn = (Button) getView().findViewById(R.id.ImageBtn);
        CancelBtn = (Button) getView().findViewById(R.id.CancelBtn);
        EventName = (EditText) getView().findViewById(R.id.EventName);
        EventVenue = (EditText) getView().findViewById(R.id.EventVenue);
        EventPostCode = (EditText) getView().findViewById(R.id.EventPostCode);
        EventDate = (EditText) getView().findViewById(R.id.EventDate);
        EventDescription = (EditText) getView().findViewById(R.id.EventDescription);

        //Pull image from local gallery
        ImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventName.setText("");
                EventVenue.setText("");
                EventPostCode.setText("");
                EventDate.setText("");
                EventDescription.setText("");
                imageView.setImageResource(0);
            }
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
