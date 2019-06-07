package database.mains;

import database.DatabasePort;
import database.parsing.DatabaseParser;
import database.parsing.DefaultDatabaseParser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Builds a brand new, empty Card and Deck database.
 */
public class BuildDatabase {

  /**
   * Builds a brand new Card and Deck Database with full set of Card info.
   * @throws SQLException if there is a failure in initializing the database
   */
  public static void main(String[] args) throws SQLException {
    Path pathToDatabase = Paths.get("resources\\cddb.db").toAbsolutePath();
    Path pathToInitFile = Paths.get("resources\\database_init.txt");
    DatabaseParser parser = new DefaultDatabaseParser(pathToDatabase, pathToInitFile);
    Path pathToJSON = Paths.get("resources\\AllSets.json").toAbsolutePath();
    parser.parseAllSets(pathToJSON);
  }
}
