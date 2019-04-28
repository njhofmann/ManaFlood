package database.access;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.sqlite.SQLiteConfig;

/**
 * Default base class for opening and closing the CDDB.
 */
public abstract class DefaultDatabasePort implements DatabasePort {

  /**
   * Path to the Card and Deck database.
   */
  private final Path pathToDatabase;

  /**
   * Connection to the database.
   */
  protected Connection connection;

  /**
   * Prepared statement to use for accessing the database.
   */
  protected PreparedStatement preparedStatement;

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
  public void connect() throws SQLException {
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
      throw new IllegalStateException("Failed to connect to CDDB!");
    }
  }

  @Override
  public void disconnect() throws SQLException {
    try {
      if (connection != null) {
        System.out.println("Closed connection to CDDB successfully!");
        connection.close();
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      throw new SQLException("Failed to close CDDB!");
    }
  }

  /**
   * Checks if a connection to the CDDB has been established yet, throws an error if not. Used to
   * prevent calling methods that rely on a connection to the database.
   * @throws IllegalStateException if connection to CDDB hasn't been established yet
   */
  protected void isConnected() throws IllegalStateException {
    if (connection == null) {
      throw new IllegalStateException("Connection to the CDDB has not yet been established!");
    }
  }

}
