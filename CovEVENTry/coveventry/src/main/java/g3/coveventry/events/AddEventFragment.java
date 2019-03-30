package g3.coveventry.events;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import g3.coveventry.R;
import g3.coveventry.database.CallbackDBSimple;
import g3.coveventry.database.Database;
import g3.coveventry.user.User;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddEventFragment extends Fragment {

    ImageView imageView;
    Button ImageBtn, CancelBtn, SaveBtn;
    EditText EventName, EventVenue, EventPostCode, EventDate, EventTime, EventDescription;
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
        SaveBtn = view.findViewById(R.id.SaveBtn);
        EventName = view.findViewById(R.id.EventName);
        EventVenue = view.findViewById(R.id.EventVenue);
        EventPostCode = view.findViewById(R.id.EventPostCode);
        EventDate = view.findViewById(R.id.EventDate);
        EventTime = view.findViewById(R.id.EventTime);
        EventDescription = view.findViewById(R.id.EventDescription);


        //Pull image from local gallery
        ImageBtn.setOnClickListener(v -> openGallery());


        CancelBtn.setOnClickListener(v -> {
            EventName.setText("");
            EventVenue.setText("");
            EventPostCode.setText("");
            EventDate.setText("");
            EventTime.setText("");
            EventDescription.setText("");
            imageView.setImageResource(0);
        });


        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(User.getCurrentUser().isFacebookConnected() || User.getCurrentUser().isTwitterConnected()) {
                    long id = 1;
                    String name = EventName.getText().toString();
                    String description = EventDescription.getText().toString();
                    Bitmap image = null;
                    try {
                        image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String venue = EventVenue.getText().toString();
                    String postcode = EventPostCode.getText().toString();
                    SimpleDateFormat simpleDate = new SimpleDateFormat("ddMMyyHHmm", Locale.getDefault());
                    Date date = null;
                    try {
                        date = simpleDate.parse(EventDate.getText().toString() + EventTime.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Database.getInstance().addEvent(id, name, description, image, venue, postcode, date, new CallbackDBSimple() {
                        @Override
                        public void connectionSuccessful() {
                            Log.i("yes", "yes");
                            Toast.makeText(getContext(), "Your Event have been saved",
                                    Toast.LENGTH_LONG).show();
                            EventName.setText("");
                            EventVenue.setText("");
                            EventPostCode.setText("");
                            EventDate.setText("");
                            EventTime.setText("");
                            EventDescription.setText("");
                            imageView.setImageResource(0);
                        }

                        @Override
                        public void connectionFailed(String message) {
                            Log.i("no", "no");
                        }
                    });
                }
                else{
                    Toast.makeText(getContext(), "You must be logged in to make an event",
                            Toast.LENGTH_LONG).show();
                }
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
