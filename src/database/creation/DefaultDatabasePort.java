package database.creation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.sqlite.SQLiteConfig;

/**
 * Default base class for opening and closing the CDDB.
 */
public abstract class DefaultDatabasePort implements DatabasePort{

  /**
   * Connection to the database.
   */
  protected Connection connection;

  @Override
  public void connect() {
    try {
      // Path to CDDB
      String url = "jdbc:sqlite:src/database/creation/test.db";

      // Enable foreign keys
      SQLiteConfig config = new SQLiteConfig();
      config.enforceForeignKeys(true);

      // Connect to CDDB
      connection = DriverManager.getConnection(url, config.toProperties());

      System.out.println("Connection to CDDB successful!");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  @Override
  public void disconnect() {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      throw new IllegalStateException("Failed to close database!");
    }
  }
}
