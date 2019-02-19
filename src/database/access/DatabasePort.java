package database.access;

import java.sql.SQLException;
import java.util.List;

/**
 * Provides methods for opening and closing the Card & Deck Database (CDDB), as well as methods for
 * accessing enumerated types stored in the DB.
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

  /**
   * Returns a unmodifiable list of all the card supertypes held in the CDDB.
   * @return unmodifiable list of all the card supertypes
   * @throws SQLException if connection to CDDB has not yet been established
   */
  List<String> getSupertypes() throws IllegalStateException;

  /**
   * Returns a unmodifiable list of all the card types held in the CDDB.
   * @return unmodifiable list of all the card types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  List<String> getTypes() throws IllegalStateException;

  /**
   * Returns a unmodifiable list of all the card rarity types held in the CDDB.
   * @return unmodifiable list of all the card rarity types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  List<String> getRarityTypes() throws IllegalStateException;

  /**
   * Returns a unmodifiable list of all the card mana types held in the CDDB.
   * @return unmodifiable list of all the card mana types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  List<String> getManaTypes() throws IllegalStateException;

  /**
   * Returns a unmodifiable list of all the two faced card types held in the CDDB.
   * @return unmodifiable list of all the two faced card types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  List<String> getTwoFacedTypes() throws IllegalStateException;

  /**
   * Returns a unmodifiable list of all the three faced card types held in the CDDB.
   * @return unmodifiable list of all the three faced card types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  List<String> getThreeFacedTypes() throws IllegalStateException;
}
