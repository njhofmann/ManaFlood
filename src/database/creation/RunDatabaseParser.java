package database.creation;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Actually adds JSON files of MTG sets from MTGJSON to the Card & Deck Database.
 */
public class RunDatabaseParser {

  public static void main(String[] arg) {
    Path pathToDatabase = Paths.get("src\\database\\creation\\test.db").toAbsolutePath();
    DatabaseParser parser = new DefaultDatabaseParser(pathToDatabase);

    parser.connect();

    /*
    Path pathToJSON = Paths.get("resources\\GRN.json").toAbsolutePath();
    parser.parseSet(pathToJSON);
    */

    //src/database/creation/test.db"
    Path pathToJSON = Paths.get("resources\\AllSets.json").toAbsolutePath();
    parser.parseAllSets(pathToJSON);

    parser.disconnect();
  }
}
