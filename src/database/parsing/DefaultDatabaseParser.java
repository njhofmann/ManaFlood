package database.parsing;

import database.DatabasePort;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Default implementation of the {@link DatabaseParser} interface. Parses one or more JSON files
 * from MTGJSON to add them to the Card and Deck Database.
 */
public class DefaultDatabaseParser extends DatabasePort implements DatabaseParser {

  /**
   * Shorthand names of sets not supported for processing.
   */
  private static final String[] unsupportedSets = {"UNH", "UGL", "UST", "PCEL"};

  /**
   * Representation to use for a card lacks any color or color identity, i.e. colorless cards.
   */
  private static final String colorlessRepresentation = "C";

  /**
   * Takes in a {@link Path} referencing the Card and Deck Database (CDDB).
   * @param pathToDatabase path to CDDB
   * @throws SQLException should never be thrown
   */
  public DefaultDatabaseParser(Path pathToDatabase) throws SQLException {
    super(pathToDatabase);
  }

  /**
   * Given a {@link Path} to a JSON file of one or more MTG sets from MTGJSON, attempts to create
   * a {@link JSONObject} from that file.
   * @param path path to JSON file to parse
   * @return JSONObject created by path
   * @throws SQLException if connection to CDDB hasn't yet been established
   * @throws IllegalArgumentException if given path is null or doesn't reference an existing file.
   *         or if failed to create JSON object from given path
   */
  private JSONObject canParsePath(Path path) {
    if (path == null) {
      throw new IllegalArgumentException("Given path can't be null!");
    }
    else if (Files.notExists(path)) {
      throw new IllegalArgumentException("Given path doesn't exist!");
    }

    JSONObject JSONatPath;
    try {
      String fileAsString = pathToString(path);
      JSONatPath = new JSONObject(fileAsString);
      return JSONatPath;
    }
    catch (IOException e) { ;
      throw new IllegalArgumentException(e.getMessage() +
          "\nFailed to create JSON object from given path!");
    }
  }

  @Override
  public void parseSet(Path path)
      throws IllegalArgumentException, IllegalStateException, SQLException {
    JSONObject setBeingRead = canParsePath(path);
    Connection cddbConnection = connect();
    addSet(setBeingRead, cddbConnection);
    disconnect(cddbConnection);
  }

  @Override
  public void parseAllSets(Path path)
      throws IllegalArgumentException, IllegalStateException, SQLException {
    JSONObject allSets = canParsePath(path);
    Iterator<String> iterator = allSets.keys();

    Connection cddbConnection = connect();
    while (iterator.hasNext()) {
      String currentSetName = iterator.next();
      if (!Arrays.asList(unsupportedSets).contains(currentSetName)) {
        addSet(allSets.getJSONObject(currentSetName), cddbConnection);
      }
    }
    disconnect(cddbConnection);
  }

  /**
   * Given a JSONObject of a MTG set from MTGJSON, adds its info to the CDB. If set has already been
   * added, does nothing.
   * @param set JSON object of a MTG set to add to
   * @param connection connection to the CDDB to use
   * @throws IllegalArgumentException if given set is null, doesn't have one of the required /
   *         documented fields
   * @throws SQLException if JSONObject fails to be queried from or added to the CDDB
   */
  private void addSet(JSONObject set, Connection connection) throws IllegalArgumentException, SQLException {
    if (set == null) {
      throw new IllegalArgumentException("Given set can't be null!");
    }

    // Check for required keys
    String[] requiredKeys = new String[]{"code", "totalSetSize", "name", "cards", "releaseDate"};
    for (String key : requiredKeys) {
      if (!set.has(key)) {
        throw new IllegalArgumentException(String.format("Given JSONObject isn't a set, "
            + "doesn't have required field \"%s\"!", key));
      }
    }

    String shorthandSetName = set.getString("code").toUpperCase();
    PreparedStatement preparedStatement;

    // Check if set has been added
    boolean setAdded = true;
    try {
      String checkForSet = "SELECT abbrv FROM Expansion WHERE abbrv = ?";
      preparedStatement = connection.prepareStatement(checkForSet);
      preparedStatement.setString(1, shorthandSetName);
      ResultSet resultSet = preparedStatement.executeQuery();

      if (!resultSet.next()) {
        setAdded = false;
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for set %s!", shorthandSetName));
    }

    if (!setAdded) {
      String name = set.getString("name");
      int size = set.getInt("totalSetSize");

      String releaseDateString = set.getString("releaseDate");
      LocalDate releaseDate = LocalDate.parse(releaseDateString);
      Timestamp releaseDateTimestamp = Timestamp.valueOf(releaseDate.atStartOfDay());

      try {
        String insertStatement = "INSERT INTO Expansion(abbrv,expansion,size,release_date) VALUES (?,?,?,?)";
        preparedStatement = connection.prepareStatement(insertStatement);
        preparedStatement.setString(1, shorthandSetName);
        preparedStatement.setString(2, name);
        preparedStatement.setInt(3, size);
        preparedStatement.setTimestamp(4, releaseDateTimestamp);
        preparedStatement.executeUpdate();
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to add info for given set %s!", name));
      }
      finally {
        closePreparedStatement(preparedStatement);
      }
    }

    // Add expansion's associated block if associated with one
    if (set.has("block")) {
      String block = set.getString("block");
      try {
        String insertStatement = "INSERT INTO Block(expansion,block) VALUES (?,?)";
        preparedStatement = connection.prepareStatement(insertStatement);
        preparedStatement.setString(1, shorthandSetName);
        preparedStatement.setString(2, block);
        preparedStatement.executeUpdate();
      }
      catch (SQLException e) {
        throw new SQLException(String.format(e.getMessage() +
            "\nFailed to insert block %s for associated expansion"
            + " %s!", block, shorthandSetName));
      }
    }

    JSONArray cards = set.getJSONArray("cards");
    int length = cards.length();
    for (int i = 0; i < length; i += 1) {
      JSONObject card = cards.getJSONObject(i);
      addCard(card, shorthandSetName, connection);
    }
  }

  /**
   * Given the JSON object of a MTG card, from JSON file from MTGJSON, adds it to the CDDB as
   * appropriate.
   * @param card card to add
   * @param shorthandSetName shorthand name of the expansion the given card is associated with
   * @param connection connection to the CDDB to use
   * @throws IllegalArgumentException if given JSON object is null or doesn't have a required key
   *         as per MTGJSON's documentation
   * @throws SQLException if some part of card fails to be queried from or added to CDDB
   */
  private void addCard(JSONObject card, String shorthandSetName, Connection connection) throws IllegalArgumentException,
      SQLException {
    if (card == null) {
      throw new IllegalArgumentException("Give card can't be null!");
    }

    // Check for required keys
    String[] requiredKeys = new String[]{"name", "convertedManaCost", "number", "rarity"};
    for (String key : requiredKeys) {
      if (!card.has(key)) {
        throw new IllegalArgumentException(String.format("Given JSONObject isn't a card, "
            + "doesn't have required field \"%s\"!", key));
      }
    }

    String cardName = card.getString("name");

    // Card hasn't been added, add card, super types, types, and subtypes, mana types, color, and
    // color identity info
    if (!cardAdded(cardName, connection)) {
      // Add basic info about card
      addBaseCardInfo(card, connection);

      // Add colors of the card
      addCardColors(card, connection);

      // Add colors making up the card's color identity
      addCardColorIdentity(card, connection);

      // Every card should have supertypes, add them
      addCardSupertypes(card, connection);

      // If card has types, add them
      addCardTypes(card, connection);

      // If card has subtypes, add them
      addCardSubtypes(card, connection);

      // If card has mana cost, add them
      addCardManaCosts(card, connection);

      // If card has power & toughness, or planeswalker loyalty, add those extra stats
      addExtraStats(card, connection);
    }

    // If card is multifaced, and all its components have been added, add multifaced relation to set
    addMultifacedStats(card, connection);

    // Card is for sure in database, add relevant set info
    addSetCardInfo(card, connection, shorthandSetName);
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds it base stats like its
   * name, card text, and converted mana costs to the CDDB given info for that card hasn't been added
   * before.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addBaseCardInfo(JSONObject card, Connection connection) throws SQLException {
    String cardName = card.getString("name");
    PreparedStatement preparedStatement = null;
    try {
      String insertCard = "INSERT INTO Card(name,card_text,cmc) VALUES (?,?,?)";
      preparedStatement = connection.prepareStatement(insertCard);

      String cardText = "";
      if (card.has("text")) {
        cardText = card.getString("text");
      }

      String cardCMCQuery;
      if (card.has("faceConvertedManaCost")) {
        cardCMCQuery = "faceConvertedManaCost";
      }
      else {
        cardCMCQuery = "convertedManaCost";
      }

      int cardCMC = (int) card.getFloat(cardCMCQuery);

      preparedStatement.setString(1, cardName);
      preparedStatement.setString(2, cardText);
      preparedStatement.setInt(3, cardCMC);
      preparedStatement.executeUpdate();
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to add base info for card %s!", cardName));
    }
    finally {
      closePreparedStatement(preparedStatement);
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds it colors to the
   * CDDB, if it has any. Else adds an association to being colorless.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to add data to the CDDB about given card
   */
  private void addCardColors(JSONObject card, Connection connection) throws SQLException {
    addCardColorInfo(card, connection, "colors", "CardColor");
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds the colors making up
   * its color identity to the CDDB, if it has any. Else adds an association to being colorless.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to add data to the CDDB about given card
   */
  private void addCardColorIdentity(JSONObject card, Connection connection) throws SQLException {
    addCardColorInfo(card, connection, "colorIdentity", "CardColorIdentity");
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds the colors making up
   * the card's color to the CDDB, if it has any. Else adds an association to being colorless.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to add data to the CDDB about given card
   */
  private void addCardColorInfo(JSONObject card, Connection connection, String attributeName, String tableName) throws SQLException {
    String cardName = card.getString("name");
    String[] colors;
    if (card.has(attributeName)) {
      colors = JSONArrayToStringArray(card.getJSONArray(attributeName));
      if (colors.length == 0) {
        colors = new String[]{colorlessRepresentation};
      }
    }
    else {
      colors = new String[]{colorlessRepresentation};
    }

    PreparedStatement preparedStatement = null;
    try {
      for (String color : colors) {
        String insertColor = "INSERT INTO %s(card_name,color) VALUES (?,?)";
        insertColor = String.format(insertColor, tableName);
        preparedStatement = connection.prepareStatement(insertColor);
        preparedStatement.setString(1, cardName);
        preparedStatement.setString(2, color);
        preparedStatement.executeUpdate();
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to add colors for card %s into table %s from"
          + " JSON attribute %s!", cardName, tableName, attributeName));
    }
    finally {
      closePreparedStatement(preparedStatement);
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds it supertypes to the
   * CDDB, if it has any
   * before.
   * @param card JSONObject of card to add
   * @throws IllegalArgumentException if card contains an unsupported supertype
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addCardSupertypes(JSONObject card, Connection connection) throws SQLException {
    if (card.has("supertypes")) {
      String cardName = card.getString("name");
      String[] supertypes = JSONArrayToStringArray(card.getJSONArray("supertypes"));
      PreparedStatement preparedStatement = null;
      try {
        for (String supertype : supertypes) {
          String insertSupertype = "INSERT INTO CardSupertype(card_name,supertype) VALUES (?,?)";
          preparedStatement = connection.prepareStatement(insertSupertype);
          preparedStatement.setString(1, cardName);
          preparedStatement.setString(2, supertype.toLowerCase());
          preparedStatement.executeUpdate();
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to add supertypes for card %s!", cardName));
      }
      finally {
        closePreparedStatement(preparedStatement);
      }
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds it types to the
   * CDDB, if it has any.
   * before.
   * @param card JSONObject of card to add
   * @throws IllegalArgumentException if given card contains an unsupported type
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addCardTypes(JSONObject card, Connection connection) throws SQLException {
    if (card.has("types")) {
      String cardName = card.getString("name");
      String[] types = JSONArrayToStringArray(card.getJSONArray("types"));
      PreparedStatement preparedStatement = null;
      try {
        //Add types
        for (String type : types) {
          String insertType = "INSERT INTO CardType(card_name,type) VALUES (?,?)";
          preparedStatement = connection.prepareStatement(insertType);
          preparedStatement.setString(1, cardName);
          preparedStatement.setString(2, type.toLowerCase());
          preparedStatement.executeUpdate();
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to add types for card %s!", cardName));
      }
      finally {
        closePreparedStatement(preparedStatement);
      }
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds it subtypes to the
   * CDDB, if it has any.
   * before.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addCardSubtypes(JSONObject card, Connection connection) throws SQLException {
    if (card.has("subtypes")) {
      String cardName = card.getString("name");
      String[] subtypes = JSONArrayToStringArray(card.getJSONArray("subtypes"));
      PreparedStatement preparedStatement = null;
      try {
        // Add subtypes
        for (String subtype : subtypes) {
          String insertSubtype = "INSERT INTO CardSubtype(card_name,subtype) VALUES (?,?)";
          preparedStatement = connection.prepareStatement(insertSubtype);
          preparedStatement.setString(1, cardName);
          preparedStatement.setString(2, subtype.toLowerCase());
          preparedStatement.executeUpdate();
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to add subtypes for %s!", cardName));
      }
      finally {
        closePreparedStatement(preparedStatement);
      }
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds it mana costs to the
   * CDDB, if it has any.
   * before.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addCardManaCosts(JSONObject card, Connection connection) throws SQLException {
    if (card.has("manaCost")) {
      String cardName = card.getString("name");
      String[] manaCosts = card.getString("manaCost").split("(?=\\{)");
      PreparedStatement preparedStatement = null;
      try {
        HashMap<String, Integer> uniqueManaCosts = new HashMap<>();
        for (String manaCost : manaCosts) {
          if (uniqueManaCosts.containsKey(manaCost)) {
            uniqueManaCosts.replace(manaCost, uniqueManaCosts.get(manaCost) + 1);
          } else {
            String withOutBrackets = manaCost.substring(1, manaCost.length() - 1);
            if (withOutBrackets.matches("\\d+")) {
              uniqueManaCosts.put("{1}", Integer.parseInt(withOutBrackets));
            }
            else {
              uniqueManaCosts.put(manaCost, 1);
            }
          }
        }
        for (String manaCost : uniqueManaCosts.keySet()) {
          String manaCostInsert = "INSERT INTO CardMana(card_name,mana_type,quantity) VALUES (?,?,?)";

          int quantity =  uniqueManaCosts.get(manaCost);
          preparedStatement = connection.prepareStatement(manaCostInsert);
          preparedStatement.setString(1, cardName);
          preparedStatement.setString(2, manaCost);
          preparedStatement.setInt(3, quantity);
          preparedStatement.executeUpdate();
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to add subtypes for %s!", cardName));
      }
      finally {
        closePreparedStatement(preparedStatement);
      }
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds any extra info it may
   * have, if any, either power & toughness or loyalty.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addExtraStats(JSONObject card, Connection connection) throws SQLException {
    String cardName = card.getString("name");
    PreparedStatement preparedStatement = null;
    try {
      if (card.has("power") && card.has("toughness")) {
        String ptInsert = "INSERT INTO PTStats(card_name,power,toughness) VALUES (?,?,?)";
        preparedStatement = connection.prepareStatement(ptInsert);
        preparedStatement.setString(1, cardName);
        preparedStatement.setString(2, card.getString("power"));
        preparedStatement.setString(3, card.getString("toughness"));
        preparedStatement.executeUpdate();
      } else if (card.has("loyalty")) {
        String planeswalkerInsert = "INSERT INTO PlaneswalkerStats(card_name,loyalty) VALUES (?,?)";
        preparedStatement = connection.prepareStatement(planeswalkerInsert);
        preparedStatement.setString(1, cardName);
        preparedStatement.setString(2, card.getString("loyalty"));
        preparedStatement.executeUpdate();
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("Failed to add extra stats for card %s!", cardName));
    }
    finally {
      closePreparedStatement(preparedStatement);
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, if it has relationship with
   * any other other cards, adds that relationship to the CDDB.
   * before.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addTwoFacedStats(JSONObject card, Connection connection) throws SQLException {
    String layout = card.getString("layout");
    String[] names = JSONArrayToStringArray(card.getJSONArray("names"));
    String cardA = names[0];
    String cardB = names[1];

    // Check that each card has been added
    boolean cardsAdded = true;
    for (String name : names) {
      if (!cardAdded(name, connection)) {
        cardsAdded = false;
      }
    }

    // Cards have been added, add relationship
    if (cardsAdded) {
      // Check that the query hasn't been added
      boolean relationshipAdded;
      PreparedStatement preparedStatement = null;
      ResultSet resultSet = null;
      try {
        String multifaceQuery = "SELECT * FROM TwoCards WHERE card_a=? AND card_b=?";
        preparedStatement = connection.prepareStatement(multifaceQuery);
        preparedStatement.setString(1, cardA);
        preparedStatement.setString(2, cardB);
        resultSet = preparedStatement.executeQuery();
        relationshipAdded = resultSet.next();
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to query relationship between"
            + " cards %s and %s!", cardA, cardB));
      }
      finally {
        close(resultSet, preparedStatement);
      }

      if (!relationshipAdded) {
        try {
          String multifaceUpdate = "INSERT INTO TwoCards(card_a,card_b,type,total_cmc) "
              + "VALUES (?,?,?,?)";
          preparedStatement = connection.prepareStatement(multifaceUpdate);
          preparedStatement.setString(1, cardA);
          preparedStatement.setString(2, cardB);
          preparedStatement.setString(3, layout);
          preparedStatement.setInt(4, (int) card.getFloat("convertedManaCost"));
          preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
          throw new SQLException(e.getMessage() + String.format("\nFailed to add relationship between"
              + " cards %s and %s!", cardA, cardB));
        }
        finally {
          closePreparedStatement(preparedStatement);
        }
      }
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, if it has relationship with
   * any two other cards, adds that relationship to the CDDB.
   * before.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addThreeFacedStats(JSONObject card, Connection connection) throws SQLException {
    String layout = card.getString("layout");
    String[] names = JSONArrayToStringArray(card.getJSONArray("names"));
    String cardA = names[0];
    String cardB = names[1];
    String cardC = names[2];

    // Check that each card has been added
    boolean cardsAdded = true;
    for (String name : names) {
      if (!cardAdded(name, connection)) {
        cardsAdded = false;
      }
    }

    // All cards added, try to add relationship
    if (cardsAdded) {
      // Check that the query hasn't been added
      boolean relationshipAdded = true;
      PreparedStatement preparedStatement = null;
      ResultSet resultSet = null;
      try {
        String multifaceQuery = "SELECT * FROM ThreeCards WHERE card_a=? AND card_b=? AND card_c=?";
        preparedStatement = connection.prepareStatement(multifaceQuery);
        preparedStatement.setString(1, cardA);
        preparedStatement.setString(2, cardB);
        preparedStatement.setString(3, cardC);
        resultSet = preparedStatement.executeQuery();

        if (!resultSet.next()) {
          relationshipAdded = false;
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() + String.format("\nFailed to add relationship between cards "
            + "%s, %s, and %s!", cardA, cardB, cardC));
      }
      finally {
        close(resultSet, preparedStatement);
      }


      if (!relationshipAdded) {
        try {
          String multifaceUpdate = "INSERT INTO ThreeCards(card_a,card_b,card_c,type,total_cmc) "
              + "VALUES (?,?,?,?,?)";
          preparedStatement = connection.prepareStatement(multifaceUpdate);
          preparedStatement.setString(1, cardA);
          preparedStatement.setString(2, cardB);
          preparedStatement.setString(3, cardC);
          preparedStatement.setString(4, layout);
          preparedStatement.setInt(5, (int) card.getFloat("convertedManaCost"));
          preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
          throw new SQLException(e.getMessage() + String.format("\nFailed to query relationship between cards "
              + "%s, %s, and %s!", cardA, cardB, cardC));
        }
        finally {
          closePreparedStatement(preparedStatement);
        }
      }
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, if it has relationship with
   * any other cards (two or three sided cards), adds that relationship to the CDDB.
   * before.
   * @param card JSONObject of card to add
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addMultifacedStats(JSONObject card, Connection connection) throws SQLException {
    if (card.has("names")) {
      String cardName = card.getString("name");
      String[] cardNames = JSONArrayToStringArray(card.getJSONArray("names"));
      if (cardNames.length > 1) {
        if (!card.has("layout")) {
          throw new IllegalStateException(String.format("Given card %s is malformed, has multiple"
              + " card names associated with it, but no stated layout!", cardName));
        }

        if (cardNames.length == 2) {
          addTwoFacedStats(card, connection);
        }
        else if (cardNames.length == 3) {
          addThreeFacedStats(card, connection);
        }
        else {
          // Should never reach
          throw new IllegalStateException(String.format("Given card %s has an unsupported number "
              + "of faces, %d!", cardName, cardNames.length));
        }
      }
    }
  }

  /**
   * Given {@link JSONObject} of a MTG card from a MTGJSON JSON file, adds info about the
   * relationship between card and set currently being parsed (which card is apart of).
   * @param card JSONObject of card to add
   * @param shorthandSetName name of the set the given card is associated with
   * @throws SQLException there is a failure to query data from or add data to the CDDB about
   *         given card
   */
  private void addSetCardInfo(JSONObject card, Connection connection, String shorthandSetName) throws SQLException {
    String cardName = card.getString("name");

    boolean cardSetAdded;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    try {
      // See if card has already been added to the CDDB
      String selectStatement = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name = ?"
          + "AND expansion = ?"
          + "AND number = ?";
      preparedStatement = connection.prepareStatement(selectStatement);
      preparedStatement.setString(1, cardName);
      preparedStatement.setString(2, shorthandSetName);
      preparedStatement.setString(3, card.getString("number"));
      resultSet = preparedStatement.executeQuery();
      cardSetAdded = resultSet.next();
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() + String.format("\nFailed to query for card %s and set %s!",
          cardName, shorthandSetName));
    }
    finally {
      close(resultSet, preparedStatement);
    }

    try {
      if (!cardSetAdded) {
        String insertStatement
            = "INSERT INTO CardExpansion(card_name,expansion,number,rarity,flavor_text,artist) "
            + "VALUES (?,?,?,?,?,?)";
        preparedStatement = connection.prepareStatement(insertStatement);
        preparedStatement.setString(1, cardName);
        preparedStatement.setString(2, shorthandSetName);
        preparedStatement.setString(3, card.getString("number"));

        String rarity = card.getString("rarity");

        preparedStatement.setString(4, rarity);

        String flavorText = "";
        if (card.has("flavorText")) {
          flavorText = card.getString("flavorText");
        }

        preparedStatement.setString(5, flavorText);
        preparedStatement.setString(6, card.getString("artist"));
        preparedStatement.executeUpdate();
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() + String.format("\nFailed to add set info for card %s for "
          + "set %s!", cardName, shorthandSetName));
    }
    finally {
      closePreparedStatement(preparedStatement);
    }
  }

  /**
   * Given a JSONArray of Strings, converts it to an array of Strings.
   * @param toConvert JSONArray of Strings to convert
   * @return resulting String array
   * @throws SQLException if given JSONArray is null, or isn't entirely made of Strings
   */
  private String[] JSONArrayToStringArray(JSONArray toConvert) throws SQLException {
    if (toConvert == null) {
      throw new IllegalArgumentException("Give JSONArray can't be null!");
    }

    try {
      int length = toConvert.length();
      String[] toReturn = new String[length];
      for (int i = 0; i < length; i += 1) {
        toReturn[i] = toConvert.getString(i);
      }
      return toReturn;
    }
    catch (JSONException e) {
      throw new SQLException(e.getMessage() + "\nGiven JSON array isn't entirely made of Strings!");
    }
  }

  /**
   * Given the name of a card, checks if it has been added to the CDDB under the 'Card' table.
   * @param toCheck name of card to check
   * @return if card is in CDDB
   * @throws IllegalArgumentException if given string is null
   * @throws SQLException failure to query CDDB
   */
  private boolean cardAdded(String toCheck, Connection connection) throws SQLException {
    if (toCheck == null) {
      throw new IllegalArgumentException("Given string can't be null!");
    }

    boolean returned;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    try {
      String cardAddedQuery = "SELECT name FROM Card WHERE name=?";
      preparedStatement = connection.prepareStatement(cardAddedQuery);
      preparedStatement.setString(1, toCheck);
      resultSet = preparedStatement.executeQuery();
      returned = resultSet.next();
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to is see if card %s is in CDDB!", toCheck));
    }
    finally {
      close(resultSet, preparedStatement);
    }
    return returned;
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
