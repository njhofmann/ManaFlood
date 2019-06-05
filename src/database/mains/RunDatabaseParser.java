package database.mains;

import database.parsing.DatabaseParser;
import database.parsing.DefaultDatabaseParser;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Used to add JSON files of MTG sets from MTGJSON to the Card & Deck Database.
 */
public class RunDatabaseParser {

  public static void main(String[] arg) throws SQLException {
    Path pathToDatabase = Paths.get("resources\\cddb.db").toAbsolutePath();
    DatabaseParser parser = new DefaultDatabaseParser(pathToDatabase);

    Path pathToJSON = Paths.get("resources\\AllSets.json").toAbsolutePath();
    parser.parseAllSets(pathToJSON);
  }
}
