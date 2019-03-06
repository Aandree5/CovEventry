package g3.coveventry.database;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import g3.coveventry.events.Event;

public class Database {
    // One and only Database instance
    private static final Database database = new Database();
    // Weak reference to application context, doesn't prevent garbage collection
    private WeakReference<Context> contextRef = null;

    // Process dialog to show when connecting to database
    private ProgressDialog progressDialog;

    // Constants
    private final String dbURL = "https://coveventry.andrefmsilva.coventry.domains/";
    private final String FILE_CREATE_EVENT = dbURL + "create_event.php";
    private final String FILE_GET_EVENTS = dbURL + "get_events.php";

    /**
     * Private constructor so there is no instantiation outside class
     */
    private Database() {
    }


    /**
     * Prepare singleton to be ready for tasks
     *
     * @param context Constext of activity to show the dialogs in
     */
    public static void initialize(@NonNull Context context) {
        // Save weak reference of context
        database.contextRef = new WeakReference<>(context);

        // Prepare process dialog of object
        database.progressDialog = new ProgressDialog(context);
        database.progressDialog.setCancelable(false); // Disable click to cancel dialog
    }

    /**
     * Retrieve the Database object to run methods, checks if object was initialized
     *
     * @return The database object
     */
    public static Database getInstance() {
        // Check if context was set, and object initialized
        if (database.contextRef.get() == null)
            throw new RuntimeException("Context reference not set, make sure to initialize database first.");

        return database;
    }


    /**
     * Start progress dialog to prevent any user intervention and show progress
     *
     * @param title   Title fot he dialog
     * @param message Message to show on dialog
     */
    private void startDialog(String title, String message) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }


    /**
     * Close progress dialog
     */
    public void stopDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }


    /**
     * Add an event to the database
     *
     * @param hostID      UserID of the user creating the event
     * @param title       Title for the event
     * @param description Description of the event
     * @param image       Image of the event
     * @param venue       Name of where the event will take place
     * @param post_code   Post code where the event will take place
     * @param dateTime    Date and time for the event start
     * @param callback    Interface to be called according to connection progress
     */
    public void addEvent(long hostID, String title, String description, Bitmap image, String venue, String post_code,
                         Date dateTime, @NonNull CallbackDBSimple callback) {

        // Start dialog
        startDialog("Creating event", "Creating event. Please wait...");

        // Define the date and time format for MySQL to handle
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Prepare HashMap with values to send to the database
        HashMap<String, String> requestInfo = new HashMap<>();
        requestInfo.put("host_id", String.valueOf(hostID));
        requestInfo.put("title", title);
        requestInfo.put("description", description);
        requestInfo.put("venue", venue);
        requestInfo.put("post_code", post_code);
        requestInfo.put("date", simpleDateFormat.format(dateTime));


        // Compress image on a background task, to no prevent the UI thread from building the dialog
        AsyncTask.execute(() -> {

            // If an image was set, prepare it to be sent to the database
            if (image != null) {
                // Convert image to array of bytes
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Encode array of byte into a string to be stored in the database
                requestInfo.put("image", Base64.encodeToString(imageBytes, Base64.DEFAULT));
            }

            // Create connection to send the information to the database and handle callback calls
            new connectMySQL(FILE_CREATE_EVENT, requestInfo, new connectMySQL.CallbackMySQLConnection() {
                @Override
                public void connectionSuccessful(JSONArray results) {
                    callback.connectionSuccessful();
                }

                @Override
                public void connectionStarted() {
                    callback.connectionStarted();
                }

                @Override
                public void connectionFinished() {
                    callback.connectionFinished();
                }

                @Override
                public void connectionFailed(String message) {
                    callback.connectionFailed(message);
                }
            }).execute();

        });
    }


    /**
     * Retrieve events from the database on a given date
     *
     * @param date     Date to request the database for events
     * @param callback Callback to return the retrieved events
     */
    public void getEvents(Date date, @NonNull CallbackDBResults<Event> callback) {
        startDialog("Retrieving event", "Searching for events around you");

        // Define the date format for MySQL to handle, only date to search for the entire day
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Prepare HashMap with values to send to the database
        HashMap<String, String> requestInfo = new HashMap<>();
        requestInfo.put("date", simpleDateFormat.format(date));

        // Create connection to send the information to the database and handle callback calls
        new connectMySQL(FILE_GET_EVENTS, requestInfo, new connectMySQL.CallbackMySQLConnection() {
            @Override
            public void connectionStarted() {
                callback.connectionStarted();
            }

            @Override
            public void connectionFinished() {
                callback.connectionFinished();
            }

            @Override
            public void connectionSuccessful(JSONArray jsonArray) {
                ArrayList<Event> events = new ArrayList<>();

                // Change pattern to read date from MySQL database format
                simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");

                // Format JSON into list of events
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jsonEvent = jsonArray.getJSONObject(i);

                        // Read image into bitmap if set
                        Bitmap image = null;
                        if (jsonEvent.getString("image") != null) {
                            byte[] imageArray = Base64.decode(jsonEvent.getString("image"), Base64.DEFAULT);

                            image = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
                        }

                        // Create and add event into list
                        events.add(new Event(jsonEvent.getLong("id"), jsonEvent.getLong("host_id"), jsonEvent.getString("host_name"),
                                jsonEvent.getString("title"), jsonEvent.getString("description"), image,
                                jsonEvent.getString("venue"), jsonEvent.getString("post_code"),
                                simpleDateFormat.parse(jsonEvent.getString("date")),
                                simpleDateFormat.parse(jsonEvent.getString("created"))));

                    } catch (ParseException | JSONException e) {
                        e.printStackTrace();

                    }
                }

                callback.connectionSuccessful(events);
            }

            @Override
            public void connectionFailed(String message) {
                callback.connectionFailed(message);
            }
        }).execute();

    }

}

/**
 * Class to actually perform database operations
 */
class connectMySQL extends AsyncTask<Void, Void, JSONArray> {
    // Callback to update on task progress
    private CallbackMySQLConnection callback;
    // File URL to connect to
    private String fileURL;
    // HashMap of information to send to the database
    private HashMap<String, String> requestInfo;
    // Store message if error occurred, predefined as just error to prevent crashes
    private String errorMessage = "Error";

    /**
     * Constructor to prepare the connection object
     *
     * @param fileURL     URL of file to connect to
     * @param requestInfo HashMap of information to send to database
     * @param callback    Interface to call methods according to progression
     */
    connectMySQL(@NonNull String fileURL, @NonNull HashMap<String, String> requestInfo, @NonNull CallbackMySQLConnection callback) {
        this.fileURL = fileURL;
        this.requestInfo = requestInfo;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        callback.connectionStarted();
    }

    @Override
    protected JSONArray doInBackground(Void... voids) {
        JSONArray results = new JSONArray();

        try {
            StringBuilder data = new StringBuilder();

            // Append all the information for the request into a string
            for (String rI : requestInfo.keySet())
                data.append(URLEncoder.encode(rI, "UTF-8")).append("=")
                        .append(URLEncoder.encode(requestInfo.get(rI), "UTF-8")).append("&");

            // Open connection to webservices
            URLConnection conn = new URL(fileURL).openConnection();
            conn.setDoOutput(true);

            // Send data through connection
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(conn.getOutputStream());
            outStreamWriter.write(data.toString());
            outStreamWriter.flush();

            outStreamWriter.close();

            // Receive and read data into a HashMaps
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = buffReader.readLine()) != null)
                response.append(line);

            // Check if there was a response
            if (response.length() > 0) {
                // If starts with '[' means that it's an array
                if (response.charAt(0) == '[') {
                    results = new JSONArray(response.toString());
                }
                // If starts with '{' means that it's an object
                else if (response.charAt(0) == '{') {
                    results.put(new JSONObject(response.toString()));
                }
                // Otherwise it was an error
                else {
                    errorMessage = response.toString();
                }
            }

        } catch (JSONException | IOException e) {
            Log.e("connectMySQL", e.getMessage());
        }

        return results;
    }

    @Override
    protected void onPostExecute(JSONArray results) {
        super.onPostExecute(results);

        // Dismiss progress dialog
        Database.getInstance().stopDialog();

        // If items were returned it was a success, otherwise send error message
        if (results.length() > 0)
            callback.connectionSuccessful(results);

        else
            callback.connectionFailed(errorMessage);

        // Call finished method, independent of successful or not
        callback.connectionFinished();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        // If items were returned it was a success, otherwise send error message
        Database.getInstance().stopDialog();

        // Call failed connection with error as canceled
        callback.connectionFailed("Background task canceled");

        // Call finished method, independent of successful or not
        callback.connectionFinished();
    }


    /**
     * Interface to return JSONArray to be handled after and passed to the needed callback
     * Used only by this two classes to read and process retrieved information
     */
    interface CallbackMySQLConnection extends CallbackDatabase {
        /**
         * Called on successful connection, with a jsonArray from the connection
         *
         * @param jsonArray Array with the data retrieved from the database
         */
        void connectionSuccessful(JSONArray jsonArray);
    }
}