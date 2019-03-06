package g3.coveventry.database;

public interface CallbackDBSimple extends CallbackDatabase {
    /**
     * Called on successful connection, without results
     */
    void connectionSuccessful();
}
