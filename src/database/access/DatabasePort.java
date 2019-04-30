package database.access;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.sqlite.SQLiteConfig;

/**
 * Provides methods for opening and closing a connection to the Card & Deck Database (CDDB).
 */
public abstract class DatabasePort {

  /**
   * Path to the Card and Deck database.
   */
  private final Path pathToDatabase;

  /**
   * Takes in a {@link Path} referencing the Card and Deck Database (CDDB).
   * @param pathToDatabase path to CDDB
   * @param createNew to create a new database from the given path
   */
  protected DatabasePort(Path pathToDatabase, boolean createNew) {
    if (pathToDatabase == null) {
      throw new IllegalArgumentException("Give path can't be null!");
    }
    else if (!createNew && Files.notExists(pathToDatabase)) {
      throw new IllegalArgumentException("Give path doesn't reference an existing file!");
    }
    this.pathToDatabase = pathToDatabase;
  }

  protected DatabasePort(Path pathToDatabase) {
    this(pathToDatabase, false);
  }

  /**
   * Opens a connection to the CDDB.
   * @throws SQLException if there is a failure to connect to the CDDB
   */
  public Connection connect() throws SQLException {
    try {
      // Path to CDDB
      String url = "jdbc:sqlite:" + pathToDatabase.toString();

      // Enable foreign keys
      SQLiteConfig config = new SQLiteConfig();
      config.enforceForeignKeys(true);

      // Return connection to CDDB
      return DriverManager.getConnection(url, config.toProperties());
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() + "\nFailed to connect to CDDB!");
    }
  }

  /**
   * Closes an a given connection to the CDDB.
   * @throws SQLException if there is a failure to close the CDDB
   */
  public void disconnect(Connection connection) throws SQLException {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      throw new SQLException(e.getMessage() + "\nFailed to close CDDB!");
    }
  }

  /**
   *
   * @param preparedStatement
   * @throws SQLException
   */
  protected void closePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    if (preparedStatement != null) {
      preparedStatement.close();
    }
  }

  /**
   *
   * @param resultSet
   * @throws SQLException
   */
  protected void closeResultSet(ResultSet resultSet) throws SQLException {
    if (resultSet != null) {
      resultSet.close();
    }
  }


  /**
   *
   * @param resultSet
   * @param preparedStatement
   * @throws SQLException
   */
  protected void close(ResultSet resultSet, PreparedStatement preparedStatement)
      throws SQLException {
    closeResultSet(resultSet);

    closePreparedStatement(preparedStatement);
  }
}
