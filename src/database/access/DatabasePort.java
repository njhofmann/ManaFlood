package database.access;

import java.sql.SQLException;
import java.util.SortedSet;

/**
 * Provides methods for opening and closing the Card & Deck Database (CDDB).
 */
public interface DatabasePort {

  /**
   * Opens a connection to the CDDB. Should be called after database is done being used.
   * @throws SQLException if there is a failure to connect to the CDDB
   */
  void connect() throws SQLException;

  /**
   * Closes any open connection to the CDDB. Should be called after database is done being used.
   * @throws SQLException if there is a failure to close the CDDB
   */
  void disconnect() throws SQLException;
}
