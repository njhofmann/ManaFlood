package database;

import database.access.DatabasePort;
import database.parsing.DatabaseParser;
import database.parsing.DefaultDatabaseParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Creates a new instance of the Card and Deck Database (CDDB).
 */
public class DatabaseCreation extends DatabasePort {

  private final String initalizationPath = "resources\\database_init.txt";

  /**
   * Takes in a {@link Path} referencing the new Card and Deck Database (CDDB).
   *
   * @param pathToDatabase path to CDDB
   */
  public DatabaseCreation(Path pathToDatabase) {
    super(pathToDatabase, true);
  }

  public void create() throws SQLException, FileNotFoundException {

    StringBuilder initAsString;
    Path initFilePath = Paths.get(initalizationPath).toAbsolutePath();
    try {
      File file = initFilePath.toFile();
      Scanner sc = new Scanner(file);
      initAsString = new StringBuilder();
      while(sc.hasNextLine()) {
        String nextLine = sc.nextLine();
        if (!nextLine.startsWith("--")) {
          initAsString.append(nextLine);
        }
      }
    }
    catch (FileNotFoundException e) {
      throw new FileNotFoundException(e.getMessage() +
          String.format("Failed to find file at path %s", initFilePath.toString()));
    }

    System.out.println(initAsString.toString());
    PreparedStatement preparedStatement = null;
    Connection connection = null;
    try {
      connection = connect();
      Scanner initScanner = new Scanner(initAsString.toString());
      initScanner.useDelimiter(";");
      while (initScanner.hasNext()) {
        String toAdd = initScanner.next();
        preparedStatement = connection.prepareStatement(toAdd);
        preparedStatement.execute();
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          "\nFailed to create new database!");
    }
    finally {
      closePreparedStatement(preparedStatement);
      disconnect(connection);
    }
  }

  public static void main(String[] args) throws SQLException, FileNotFoundException {
    String name = "test";
    String savePath = "resources\\" + name + ".db";
    Path pathToDatabase = Paths.get(savePath).toAbsolutePath();
    DatabaseCreation creation = new DatabaseCreation(pathToDatabase);
    creation.create();
  }
}
