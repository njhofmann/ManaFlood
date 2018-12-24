package database.creation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.json.JSONObject;

/**
 * Default implementation of the {@link DatabaseParser} interface.
 */
public class DefaultDatabaseParser implements DatabaseParser {

  /**
   * JSON object of
   */
  private JSONObject beingRead;

  /**
   * Connection to the database.
   */
  private final Connection connection;

  public DefaultDatabaseParser() {
    connection = connect();
  }

  /**
   * Connects this parser to the target database.
   * @return the connection to the database
   */
  private Connection connect() {
    Connection conn = null;
    try {
      // Path to CDDB
      String url = "jdbc:sqlite:src/database/creation/test.db";

      // Connect to CDDB
      conn = DriverManager.getConnection(url);

      System.out.println("Connection to CDDB successful!");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return conn;
  }

  /**
   * Creates a new MTG set in the CDDB based off the given info.
   * @param abbreviation shorthand name of the set
   * @param expansion full name of the set
   * @param block block the set is apart of
   * @throws IllegalArgumentException if any of the given parameters are null, or if CDDB fails
   *         add the given set info
   */
  private void addSet(String abbreviation, String expansion, String block) throws IllegalArgumentException {
    if (abbreviation == null || expansion == null || block == null) {
      throw new IllegalArgumentException("Given parameters can't be null!");
    }
    String insertStatement = "INSERT INTO Expansion(abbrv,expansion,block) VALUES (?,?,?)";

    try {
      PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);
      preparedStatement.setString(1, abbreviation);
      preparedStatement.setString(2, expansion);
      preparedStatement.setString(3, block);
      preparedStatement.executeUpdate();
    }
    catch (SQLException e) {
      System.out.println("Failed to add set!");
      System.out.println(e.getMessage());
    }
  }

  @Override
  public void parse(Path path) throws IllegalArgumentException {
    if (path == null) {
      throw new IllegalArgumentException("Given path can't be null!");
    }
    else if (!Files.exists(path)) {
      throw new IllegalArgumentException("Given path doesn't exist!");
    }

    try {
      String fileAsString = pathToString(path);
      beingRead = new JSONObject(fileAsString);
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
      throw new IllegalArgumentException("Given path failed to be successfully read!");
    }

    System.out.println(beingRead.getString("code") + beingRead.getString("name") + beingRead.getString("block"));
    addSet(beingRead.getString("code"),
           beingRead.getString("name"),
           beingRead.getString("block"));
  }

  /**
   * Given a file {@link Path} to a JSON file of a MTG set from MTGJSON, attempts to convert that object
   * into a String.
   * @param toConvert path to convert
   * @return String of the contents of the given path
   */
  private String pathToString(Path toConvert) throws IOException {
    File converedFile = toConvert.toFile();
    BufferedReader reader = new BufferedReader(new FileReader(converedFile));

    try {
      StringBuilder toReturn = new StringBuilder();
      String currentLine = reader.readLine();

      while (currentLine != null) {
        toReturn.append(currentLine);
        currentLine = reader.readLine();
      }
      return toReturn.toString();
    }
    finally {
      reader.close();
    }
  }
}
