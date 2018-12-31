package database.creation;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Path;

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
   */
  void parseSet(Path path) throws IllegalArgumentException, IllegalStateException;


  /**
   * Given a file {@link Path} to a JSON file of a MTG set (from MTGJSON), updates the CDDB with
   * cards from the set.
   * @param path file path to the JSON file
   * @throws IllegalArgumentException if given Path is null, given Path fails to be opened, or is
   *         otherwise invalid
   * @throws IllegalStateException if database hasn't been connected yet
   */
  void parseAllSets(Path path) throws IllegalArgumentException, IllegalStateException;
}
