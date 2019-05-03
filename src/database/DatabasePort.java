package database;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.sqlite.SQLiteConfig;

/**
 * Provides methods for opening and closing a connection to the Card & Deck Database (CDDB),
 * creating a new CDDB, and utility methods for querying and inserting info into the CDDB.
 */
public class DatabasePort {

  /**
   * Path to the CDDB.
   */
  private final Path pathToDatabase;

  /**
   * Takes in a {@link Path} referencing the Card and Deck Database (CDDB), that may or may not
   * exist. If given initalizationPath is null, signals to simply connect to an existing database,
   * else will attempt to create a new CDDB at the path given by pathToDatabase using the
   * information drawn from the initalizationPath.
   * @param pathToDatabase path to CDDB, either existing or one to create
   * @param initalizationPath to create a new database from the given path
   * @throws IllegalArgumentException if given Path is null, or given Path exists and createNew is
   * false, or given Path
   * @throws SQLException if there is a failure to insert info from the given initalization path,
   * should only occur if trying to create a new database
   */
  public DatabasePort(Path pathToDatabase, Path initalizationPath)
      throws IllegalArgumentException, SQLException {
    boolean createNew = initalizationPath != null;
    if (pathToDatabase == null) {
      throw new IllegalArgumentException("Give path to database can't be null!");
    }
    else if (createNew && Files.exists(pathToDatabase)) {
      throw new IllegalArgumentException("Told to create a new database, but give path references "
          + "an existing file!");
    }
    else if (createNew && Files.notExists(initalizationPath)) {
      throw new IllegalArgumentException("Told to connect to existing database, but given path "
          + "to initalization file doesn't reference an existing file!");
    }
    else if (!createNew && Files.notExists(pathToDatabase)) {
      throw new IllegalArgumentException("Told to connect to existing database, but given path "
          + "doesn't reference an existing file!");
    }
    this.pathToDatabase = pathToDatabase;

    if (createNew) {
      try {
        createDatabase(initalizationPath);
      }
      catch (FileNotFoundException e) {
        throw new IllegalArgumentException(e.getMessage() +
            "\nCould not open file from given initalization path!");
      }
    }
  }

  /**
   * Takes in a {@link Path} referencing an existing Card and Deck Database (CDDB).
   * @param pathToDatabase path to existing CDDB
   * @throws SQLException should never occur as no information is being inserted
   */
  public DatabasePort(Path pathToDatabase) throws SQLException {
    this(pathToDatabase, null);
  }

  /**
   * If creating a new database, creates a new database at pathToDatabase and inserts into it data
   * stored in the file at the given initalizationPath
   * @param initalizationPath path to draw database info from
   * @throws SQLException if failure to insert info into newly created database
   * @throws FileNotFoundException if file listed at initalizationPath fails to be open, read, etc.
   */
  private void createDatabase(Path initalizationPath) throws SQLException, FileNotFoundException {
    StringBuilder initAsString;
    try {
      File file = initalizationPath.toFile();
      Scanner sc = new Scanner(file);
      initAsString = new StringBuilder();
      while(sc.hasNextLine()) {
        String nextLine = sc.nextLine();
        if (!nextLine.startsWith("--")) { // Ignore comments
          initAsString.append(nextLine);
        }
      }
    }
    catch (FileNotFoundException e) {
      throw new FileNotFoundException(e.getMessage() +
          String.format("Failed to find file at path %s", initalizationPath.toString()));
    }

    Connection connection = null;
    PreparedStatement preparedStatement = null;
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
      throw new SQLException(e.getMessage() + "\nFailed to create new database!");
    }
    finally {
      closePreparedStatement(preparedStatement);
      disconnect(connection);
    }
  }

  /**
   * Opens a connection to the CDDB.
   * @throws SQLException if there is a failure to connect to the CDDB
   */
  protected Connection connect() throws SQLException {
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
  protected void disconnect(Connection connection) throws SQLException {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      throw new SQLException(e.getMessage() + "\nFailed to close CDDB!");
    }
  }

  /**
   * Closes a given {@link PreparedStatement}.
   * @param preparedStatement PreparedStatement to close
   * @throws SQLException if given PreparedStatement fails to close
   */
  protected void closePreparedStatement(PreparedStatement preparedStatement) throws SQLException {
    if (preparedStatement != null) {
      preparedStatement.close();
    }
  }

  /**
   * Closes a given {@link ResultSet}.
   * @param resultSet PreparedStatement to close
   * @throws SQLException if given ResultSet fails to close
   */
  protected void closeResultSet(ResultSet resultSet) throws SQLException {
    if (resultSet != null) {
      resultSet.close();
    }
  }

  /**
   * Closes a given {@link ResultSet} and {@link PreparedStatement}.
   * @param resultSet ResultSet to close
   * @param preparedStatement PreparedStatement to close
   * @throws SQLException if given ResultSet fails to close
   */
  protected void close(ResultSet resultSet, PreparedStatement preparedStatement) throws SQLException {
    closeResultSet(resultSet);
    closePreparedStatement(preparedStatement);
  }
}
