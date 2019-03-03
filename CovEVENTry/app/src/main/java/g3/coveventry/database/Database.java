package g3.coveventry.database;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import g3.coveventry.MainActivity;
import g3.coveventry.callbacks.CallbackDatabase;

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
     * @param imageUri    Image file link of the image for the event
     * @param venue       Name of where the event will take place
     * @param city        City where the event will take place
     * @param dateTime    Date and time for the event start
     * @param callback    Interface to be called according to connection progress
     */
    public void addEvent(String hostID, String title, String description, Uri imageUri,
                         String venue, String city, Date dateTime, @NonNull CallbackDatabase callback) {

        // Start dialog
        startDialog("Creating event", "Creating event. Please wait...");

        // Define the date and time format for MySQL to handle
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Prepare hashmap with values to send to the database
        HashMap<String, String> requestInfo = new HashMap<>();
        requestInfo.put("host_id", hostID);
        requestInfo.put("title", title);
        requestInfo.put("description", description);
        requestInfo.put("venue", venue);
        requestInfo.put("city", city);
        requestInfo.put("dateTime", simpleDateFormat.format(dateTime));

        // If an image was set, prepare it to be sent to the database
        if (imageUri != null) {

            // Compress image on a background task, to no prevent the UI thread from building the dialog
            AsyncTask.execute(() -> {
                try {
                    // Load image from file
                    Bitmap image = MediaStore.Images.Media.getBitmap(contextRef.get().getContentResolver(), imageUri);

                    // Convert image to array of bytes
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    // Encode array of byte into a string to be stored in the database
                    requestInfo.put("image", Base64.encodeToString(imageBytes, Base64.DEFAULT));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        // Create connection to send the information to the database, send callback
        new connectMySQL(FILE_CREATE_EVENT, requestInfo, callback).execute();

    }
}

/**
 * Class to actually perform database operations
 */
class connectMySQL extends AsyncTask<Void, Void, HashMap<String, String>> {
    private CallbackDatabase callback;
    private String fileURL;
    private HashMap<String, String> requestInfo;

    /**
     * Constructor with retry automatically set to true
     *
     * @param fileURL     URL of file to connect to
     * @param requestInfo Hashmap of information to send to database
     * @param callback    Interface to call methods according to progression
     */
    connectMySQL(@NonNull String fileURL, @NonNull HashMap<String, String> requestInfo, @NonNull CallbackDatabase callback) {
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
    protected HashMap<String, String> doInBackground(Void... voids) {
        HashMap<String, String> results = new HashMap<>();

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

            if (response.length() < 1)
                return new HashMap<>();

            JSONObject jsonObject = new JSONObject(response.toString());

            Iterator<String> it = jsonObject.keys();
            while (it.hasNext()) {
                String key = it.next();
                results.put(key, jsonObject.getString(key));
            }

        } catch (JSONException | IOException e) {
            Log.e("connectMySQL", e.getMessage());
            return new HashMap<>();
        }

        return results;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> results) {
        super.onPostExecute(results);

        Database.getInstance().stopDialog();

        if (results.size() > 0)
            callback.connectionSuccessful(results);

        else
            callback.connectionFailed();

        callback.connectionFinished();

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        Database.getInstance().stopDialog();

        callback.connectionFailed();
        callback.connectionFinished();
    }
}