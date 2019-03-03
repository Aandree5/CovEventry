package g3.coveventry.callbacks;

import java.util.HashMap;

public interface CallbackDatabase {
    /**
     * Gets called before the async task begins
     */
    default void connectionStarted() {

    }

    /**
     * Gets called after the async task finished
     */
    default void connectionFinished() {

    }

    /**
     * Called on successful connection, with the results from the connection
     *
     * @param results Hashmap with the data retrieved from the database, will be empty if succeeded but no data was to be retrieved
     */
    void connectionSuccessful(HashMap<String, String> results);

    /**
     * Called on unsuccessful connection, with the information if was a "retry" type connection or not
     */
    void connectionFailed();
}
