package database.creation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.sqlite.SQLiteConfig;

/**
 * Default base class for opening and closing the CDDB.
 */
public abstract class DefaultDatabasePort implements DatabasePort{

  /**
   * Path to the Card and Deck database.
   */
  private final Path pathToDatabase;

  /**
   * Connection to the database.
   */
  protected Connection connection;

  /**
   * Takes in a {@link Path} referencing the Card and Deck Database (CDDB).
   * @param pathToDatabase path to CDDB
   */
  protected DefaultDatabasePort(Path pathToDatabase) {
    if (pathToDatabase == null) {
      throw new IllegalArgumentException("Give path can't be null!");
    }
    else if (Files.notExists(pathToDatabase)) {
      throw new IllegalArgumentException("Give path doesn't reference an existing file!");
    }
    this.pathToDatabase = pathToDatabase;
  }

  @Override
  public void connect() throws RuntimeException {
    try {
      // Path to CDDB
      String url = "jdbc:sqlite:" + pathToDatabase.toString();

      // Enable foreign keys
      SQLiteConfig config = new SQLiteConfig();
      config.enforceForeignKeys(true);

      // Connect to CDDB
      connection = DriverManager.getConnection(url, config.toProperties());
      System.out.println("Connected to CDDB successfully!");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      throw new RuntimeException("Failed to connect to CDDB!");
    }
  }

  @Override
  public void disconnect() throws RuntimeException {
    try {
      if (connection != null) {
        System.out.println("Closed connection to CDDB successfully!");
        connection.close();
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      throw new RuntimeException("Failed to close CDDB!");
    }
  }
}
