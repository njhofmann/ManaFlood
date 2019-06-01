package database.mains;

import database.DatabasePort;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Builds a brand new, empty Card and Deck database.
 */
public class BuildDatabase {

  /**
   * Builds a brand new Card and Deck Database
   * @throws SQLException if there is a failure in initializing the database
   */
  public static void main(String[] args) throws SQLException {
    Path pathToDatabase = Paths.get("resources\\cddb.db").toAbsolutePath();
    Path pathToInitFile = Paths.get("resources\\database_init.txt");
    DatabasePort port = new DatabasePort(pathToDatabase, pathToInitFile);
  }
}
