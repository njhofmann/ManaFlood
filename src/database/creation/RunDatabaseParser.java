package database.creation;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Actually adds JSON files of MTG sets from MTGJSON to the Card & Deck Database.
 */
public class RunDatabaseParser {

  public static void main(String[] arg) {
    DatabaseParser parser = new DefaultDatabaseParser();

    parser.connect();

    Path pathToJSON = Paths.get("resources\\GRN.json").toAbsolutePath();
    parser.parse(pathToJSON);

    parser.disconnect();
  }
}
