package database;

import database.access.DatabasePort;
import java.sql.SQLException;
import java.util.SortedSet;

/**
 * Provides methods for accessing enumerated info about cards from the Card and Deck Database 
 * (CDDB), such as card supertypes, types, subtypes, colors, etc. 
 */
public interface DatabaseInfoAccess extends DatabasePort {
  /**
   * Returns a unmodifiable sorted set of all the card supertypes held in the CDDB.
   * @return unmodifiable sorted set of all the card supertypes
   * @throws SQLException if connection to CDDB has not yet been established
   */
  SortedSet<String> getSupertypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the card types held in the CDDB.
   * @return unmodifiable sorted set of all the card types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  SortedSet<String> getTypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the card subtypes held in the CDDB.
   * @return unmodifiable sorted set of all the card subtypes
   * @throws SQLException if connection to CDDB has not yet been established
   */
  SortedSet<String> getSubtypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the card rarity types held in the CDDB.
   * @return unmodifiable sorted set of all the card rarity types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  SortedSet<String> getRarityTypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all colors held in the CDDB.
   * @return unmodifiable sorted set of all the three faced card types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  SortedSet<String> getColors() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the card mana types held in the CDDB.
   * @return unmodifiable sorted set of all the card mana types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  SortedSet<String> getManaTypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the two faced card types held in the CDDB.
   * @return unmodifiable sorted set of all the two faced card types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  SortedSet<String> getTwoFacedTypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the three faced card types held in the CDDB.
   * @return unmodifiable sorted set of all the three faced card types
   * @throws SQLException if connection to CDDB has not yet been established
   */
  SortedSet<String> getThreeFacedTypes() throws SQLException;
}
