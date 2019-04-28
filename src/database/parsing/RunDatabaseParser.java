package database.parsing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Actually adds JSON files of MTG sets from MTGJSON to the Card & Deck Database.
 */
public class RunDatabaseParser {

  public static void main(String[] arg) throws SQLException {
    Path pathToDatabase = Paths.get("resources\\cddb.db").toAbsolutePath();
    DatabaseParser parser = new DefaultDatabaseParser(pathToDatabase);

    parser.connect();


    Path pathToJSON = Paths.get("resources\\GRN.json").toAbsolutePath();
    parser.parseSet(pathToJSON);

    /*
    //src/database/creation/cddb.db"
    Path pathToJSON = Paths.get("resources\\AllSets.json").toAbsolutePath();
    parser.parseAllSets(pathToJSON);

     */
    parser.disconnect();
  }
}
