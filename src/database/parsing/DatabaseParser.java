package database.parsing;

import database.access.DatabasePort;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Given the JSON file of a MTG set from MTGJSON, updates the Card & Deck Database (CDDB) with the
 * cards from the set, as appropriate.
 */
public interface DatabaseParser extends DatabasePort {

  /**
   * Given a file {@link Path} to a JSON file of a MTG set (from MTGJSON), updates the CDDB with
   * cards from the set.
   * @param path file path to the JSON file
   * @throws IllegalArgumentException if given Path is null, given Path fails to be opened, or is
   *         otherwise invalid
   * @throws IllegalStateException if database hasn't been connected yet
   * @throws SQLException if there is a failure in giving data to the CDDB
   */
  void parseSet(Path path) throws IllegalArgumentException, IllegalStateException, SQLException;

  /**
   * Given a  {@link Path} to a JSON file of all MTG sets (from MTGJSON), updates the CDDB with
   * cards from all sets.
   * @param path file path to the JSON file
   * @throws IllegalArgumentException if given Path is null, given Path fails to be opened, or is
   *         otherwise invalid
   * @throws IllegalStateException if database hasn't been connected yet
   * @throws SQLException if there is a failure in giving data to the CDDB
   */
  void parseAllSets(Path path) throws IllegalArgumentException, IllegalStateException, SQLException;
}
