package g3.coveventry.events;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Fetch user current city thought user location services
 */
public class FetchAddressIntentService extends IntentService {
    // Receiver to send back the calculated information
    protected ResultReceiver receiver;

    // Constants
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String RECEIVER = "RECEIVER";
    public static final String RESULT_CITY_KEY = "RESULT_CITY_KEY";
    public static final String RESULT_LAT_KEY = "RESULT_LAT_KEY";
    public static final String RESULT_LON_KEY = "RESULT_LON_KEY";
    public static final String LOCATION_DATA_EXTRA = "LOCATION_DATA_EXTRA";

    /**
     * Constructor
     */
    public FetchAddressIntentService() {
        super("FetchAddress");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        // Create Geocoder object to get location information
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Message for errors
        String errorMessage = "";

        // Get the location and receiver object passed to this service through extras
        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);
        receiver = intent.getParcelableExtra(RECEIVER);

        // Function retrieves a list of addresses
        List<Address> addresses = null;

        try {
            // Get a list of addresses form given location
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        } catch (IOException ioException) {
            errorMessage = "Service not available";
            Log.e("AppLog", errorMessage, ioException);

        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "Invalid location data";
            Log.e("AppLog", errorMessage + ". " + "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        // When no address is found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No addresses found";
                Log.e("AppLog", errorMessage);
            }
            deliverResultToReceiver(FAILURE_RESULT, null, -9999, -9999);

        } else {
            // Get the first address in the list (only one was retrieved)
            Address address = addresses.get(0);

            deliverResultToReceiver(SUCCESS_RESULT, address.getLocality(), location.getLatitude(), location.getLongitude());
        }
    }

    /**
     * Handle returning information to the activity that requested the location information
     *
     * @param resultCode Code to say if was successful or failed (SUCCESS_RESULT | FAILURE_RESULT)
     * @param city       City found for the given location
     * @param lat        Latitude for the given location
     * @param lon        Longitude for the given location
     */
    private void deliverResultToReceiver(int resultCode, String city, double lat, double lon) {
        // Put information into a bundle
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_CITY_KEY, city);
        bundle.putDouble(RESULT_LAT_KEY, lat);
        bundle.putDouble(RESULT_LON_KEY, lon);

        // Send bundled information back to the calling activity
        receiver.send(resultCode, bundle);
    }
}
