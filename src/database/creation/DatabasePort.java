package database.creation;

/**
 * Provides methods for opening and closing the Card & Deck Database (CDDB).
 */
public interface DatabasePort {

  /**
   * Opens a connection to the CDDB. Shoulde be called after database is done being used.
   */
  void connect();

  /**
   * Closes any open connection to the CDDB. Should be called after database is done being used.
   */
  void disconnect();
}
