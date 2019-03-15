package g3.coveventry.database;

import java.util.ArrayList;

public interface CallbackDBResults<T> extends CallbackDatabase {

    /**
     * Called on successful connection, with the results from the connection
     *
     * @param results Data retrieved from the database
     */
    void connectionSuccessful(T results);
}
