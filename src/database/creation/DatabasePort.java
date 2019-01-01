package database.creation;

/**
 * Provides methods for opening and closing the Card & Deck Database (CDDB).
 */
public interface DatabasePort {

  /**
   * Opens a connection to the CDDB. Should be called after database is done being used.
   * @throws RuntimeException if there is a failure to connect to the CDDB
   */
  void connect() throws RuntimeException;

  /**
   * Closes any open connection to the CDDB. Should be called after database is done being used.
   * @throws RuntimeException if there is a failure to close the CDDB
   */
  void disconnect() throws RuntimeException;
}
