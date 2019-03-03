package g3.coveventry.database;

import java.util.ArrayList;

public interface CallbackDBResults<T> extends CallbackDatabase {

    /**
     * Called on successful connection, with the results from the connection
     *
     * @param results HashMap with the data retrieved from the database
     */
    void connectionSuccessful(ArrayList<T> results);
}
