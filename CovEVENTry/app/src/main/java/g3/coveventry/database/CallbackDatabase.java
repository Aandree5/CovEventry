package g3.coveventry.database;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

interface CallbackDatabase {
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
     * Called on failed connection
     *
     * @param message Message returned from database with error for failure
     */
    void connectionFailed(String message);
}
