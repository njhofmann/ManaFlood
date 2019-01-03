package database.access;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  private final List<String> rarities;

  private final List<String> supertypes;

  private final List<String> types;

  private final List<String> twoFacedTypes;

  private final List<String> threeFacedTypes;

  private final List<String> manaTypes;

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

    // Initalize types
    rarities = new ArrayList<>();
    supertypes = new ArrayList<>();
    types = new ArrayList<>();
    twoFacedTypes = new ArrayList<>();
    threeFacedTypes = new ArrayList<>();
    manaTypes = new ArrayList<>();
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

    retrieveDatabaseEnumerations();
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

  @Override
  public List<String> getSupertypes() {
    isConnected();
    return Collections.unmodifiableList(supertypes);
  }

  @Override
  public List<String> getTypes() {
    isConnected();
    return Collections.unmodifiableList(types);
  }

  @Override
  public List<String> getManaTypes() {
    isConnected();
    return Collections.unmodifiableList(manaTypes);
  }

  @Override
  public List<String> getRarityTypes() {
    isConnected();
    return Collections.unmodifiableList(rarities);
  }

  @Override
  public List<String> getTwoFacedTypes() {
    isConnected();
    return Collections.unmodifiableList(twoFacedTypes);
  }

  @Override
  public List<String> getThreeFacedTypes() {
    isConnected();
    return Collections.unmodifiableList(threeFacedTypes);
  }

  /**
   * Checks if a connection to the CDDB has been established yet, throws an error if not. Used to
   * prevent calling methods that rely on a connection to the database.
   * @throws IllegalStateException if connection to CDDB hasn't been established yet
   */
  private void isConnected() throws IllegalStateException {
    if (connection == null) {
      throw new IllegalStateException("Connection to the CDDB has not yet been established!");
    }
  }

  /**
   * Once a connection to the CDDB has been established, retrieves enumeration info from the CDDB.
   * @throws RuntimeException if there is a failure to retrieve any enumeration info
   */
  private void retrieveDatabaseEnumerations() throws RuntimeException {
    isConnected();

    PreparedStatement prep;
    ResultSet queryResult;

    // Fill supertypes
    try {
      String supertypeQuery = "SELECT type FROM Supertype";
      prep = connection.prepareStatement(supertypeQuery);
      queryResult = prep.executeQuery();

      while (queryResult.next()) {
        supertypes.add(queryResult.getString("type"));
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to query for supertypes!");
    }

    // Fill types
    try {
      String typeQuery = "SELECT type FROM Type";
      prep = connection.prepareStatement(typeQuery);
      queryResult = prep.executeQuery();

      while (queryResult.next()) {
        types.add(queryResult.getString("type"));
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to query for types!");
    }

    // Fill rarity types
    try {
      String rarityTypeQuery = "SELECT type FROM Rarity";
      prep = connection.prepareStatement(rarityTypeQuery);
      queryResult = prep.executeQuery();

      while (queryResult.next()) {
        rarities.add(queryResult.getString("type"));
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to query for rarity types!");
    }
    // Fill mana types
    try {
      String manaTypesQuery = "SELECT type FROM ManaType";
      prep = connection.prepareStatement(manaTypesQuery);
      queryResult = prep.executeQuery();

      while (queryResult.next()) {
        manaTypes.add(queryResult.getString("type"));
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to query for mana types!");
    }

    // Fill two faced types
    try {
      String twoFaceTypeQuery = "SELECT type FROM TwoCardsType";
      prep = connection.prepareStatement(twoFaceTypeQuery);
      queryResult = prep.executeQuery();

      while (queryResult.next()) {
        twoFacedTypes.add(queryResult.getString("type"));
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to query for two card types!");
    }

    // Fill three faced types
    try {
      String threeFaceTypeQuery = "SELECT type FROM ThreeCardsType";
      prep = connection.prepareStatement(threeFaceTypeQuery);
      queryResult = prep.executeQuery();

      while (queryResult.next()) {
        threeFacedTypes.add(queryResult.getString("type"));
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to query for three card types!");
    }
  }
}
