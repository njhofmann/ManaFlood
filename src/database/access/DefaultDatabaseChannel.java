package database.access;

import database.DatabasePort;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import value_objects.card.Card;
import value_objects.card.printing.InformativeCardPrinting;
import value_objects.card.query.SearchOption;
import value_objects.card.relationship.CardRelationship;
import value_objects.card.relationship.DefaultCardRelationship;
import value_objects.card.query.CardQuery;
import value_objects.card.printing.CardPrinting;
import value_objects.card.printing.DefaultCardPrinting;
import value_objects.deck.Deck;
import value_objects.deck.DefaultDeck;
import value_objects.deck.instance.DeckInstance;
import value_objects.deck.instance.DefaultDeckInstance;
import value_objects.card.query.Comparison;
import value_objects.card.query.Stat;
import value_objects.deck.instance.InformativeDeckInstance;
import value_objects.utility.Triple;

/**
 * Default class to use to access the Card and Deck Database (CDDB) for querying cards and reading,
 * updating, and deleting decks. In addition to accessing enumerated info about card types.
 */
public class DefaultDatabaseChannel extends DatabasePort implements DatabaseChannel {

  /**
   * Sorted set of all the card supertypes stored in the CDDB.
   */
  private final SortedSet<String> supertypes;

  /**
   * Sorted set of all the card types stored in the CDDB.
   */
  private final SortedSet<String> types;

  /**
   * Sorted set of all the card subtypes stored in the CDDB.
   */
  private final SortedSet<String> subtypes;

  /**
   * Sorted set of all the card rarites stored in the CDDB.
   */
  private final SortedSet<String> rarities;

  /**
   * Sorted set of all the card colors stored in the CDDB.
   */
  private final SortedSet<String> colors;

  /**
   * Sorted set of all types of mana symbols stored in the CDDB.
   */
  private final SortedSet<String> manaTypes;

  /**
   * Sorted set of all the card artists stored in the CDDB.
   */
  private final SortedSet<String> artists;

  /**
   * Sorted set of all the expansions stored in the CDDB.
   */
  private final SortedSet<String> sets;

  /**
   * Sorted set of all the blocks stored in the CDDB.
   */
  private final SortedSet<String> blocks;

  /**
   * Takes in a {@link Path} referencing the Card and Deck Database (CDDB) to establish a
   * connection with the database.
   * @param pathToDatabase path to CDDB
   * @throws SQLException failure to retrieve constant data from database
   */
  public DefaultDatabaseChannel(Path pathToDatabase) throws SQLException {
    super(pathToDatabase);
    Connection connection = connect();
    supertypes = retrieveColumnInfo("Supertype", "type", connection);
    types = retrieveColumnInfo("Type", "type", connection);
    subtypes = retrieveColumnInfo("Subtype", "type", connection);
    rarities = retrieveColumnInfo("CardExpansion", "rarity", connection);
    colors = retrieveColumnInfo("Color", "color", connection);
    manaTypes = retrieveColumnInfo("Mana", "mana_type", connection);
    blocks = retrieveColumnInfo("Block", "block", connection);
    artists = retrieveColumnInfo("Artist", "artist", connection);
    sets = retrieveColumnInfo("Expansion", "expansion", connection);
    disconnect(connection);
  }

  @Override
  public Map<Integer, String> getDecks() throws SQLException {
    String deckQuery = "SELECT id, name FROM Deck";
    try (Connection connection = connect();
        PreparedStatement preparedStatement = connection.prepareStatement(deckQuery);
        ResultSet result = preparedStatement.executeQuery()){
      Map<Integer, String> decksInfo = new HashMap<>();
      while (result.next()) {
        int currentDeckID = result.getInt("id");
        String currentDeckName = result.getString("name");
        decksInfo.put(currentDeckID, currentDeckName);
      }
      return decksInfo;
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          "\nFailed to query CDDB for deck IDs and names!");
    }
  }

  @Override
  public Deck getDeck(int deckID) throws IllegalArgumentException, SQLException {
    hasDeckBeenAdded(deckID);

    Connection connection = connect();

    String deckName;
    String deckDesp;
    // Query for deck info
    String deckInfoQuery = "SELECT * FROM Deck WHERE id=?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(deckInfoQuery);) {
      preparedStatement.setInt(1, deckID);
      ResultSet deckInstancesInfo = preparedStatement.executeQuery();
      deckName = deckInstancesInfo.getString("name");
      deckDesp = deckInstancesInfo.getString("desp");
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for info related to deck %d!", deckID));
    }

    // Query for info of all deck instances related to current deck
    List<LocalDateTime> deckInstanceKeys = new ArrayList<>();
    String deckInstanceInfoQuery = "SELECT * FROM DeckInstance WHERE deck_id=?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(deckInstanceInfoQuery);) {
      preparedStatement.setInt(1, deckID);
      ResultSet deckInstancesInfo = preparedStatement.executeQuery();

      // Get datetimes from deck instances, retrieve as TimeStamp --> LocalDateTime
      while (deckInstancesInfo.next()) {
        Timestamp timestamp = deckInstancesInfo.getTimestamp("creation");
        LocalDateTime toAdd = timestamp.toLocalDateTime();
        deckInstanceKeys.add(toAdd);
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for info of deck instances"
          + " related to deck %d!", deckID));
    }


    // Build deck instances,
    SortedSet<DeckInstance> deckInstances = new TreeSet<>();
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();
    Map<CardPrinting, Integer> cardPrintingQuantities = new HashMap<>();
    for (LocalDateTime creation : deckInstanceKeys) {
      Set<String> categories = new HashSet<>();
      // Get categories
      String categoriesQuery = "SELECT category FROM DeckInstCategory WHERE deck_id=? "
          + "AND deck_inst_creation=?";
      try (PreparedStatement preparedStatement = connection.prepareStatement(categoriesQuery);) {
        preparedStatement.setInt(1, deckID);
        preparedStatement.setTimestamp(2, Timestamp.valueOf(creation));
        ResultSet categoriesResult = preparedStatement.executeQuery();
        while (categoriesResult.next()) {
          categories.add(categoriesResult.getString("category"));
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to query for categories for deck with"
            + " ID %d!", deckID));
      }

      // Get category cards
      try {
        for (String currentCategory : categories) {
          String categoryQuery = "SELECT * FROM DeckInstCardCategory WHERE deck_id=? "
              + "AND deck_inst_creation=? AND category=?";

          ResultSet cardsInCategory = null;
          try (PreparedStatement preparedStatement = connection.prepareStatement(categoryQuery);) {
            preparedStatement.setInt(1, deckID);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(creation));
            preparedStatement.setString(3, currentCategory);
            cardsInCategory = preparedStatement.executeQuery();

            SortedSet<String> cardsToAdd = new TreeSet<>();
            while (cardsInCategory.next()) {
              String cardToAdd = cardsInCategory.getString("card_name");
              if (cardsToAdd.contains(cardToAdd)) {
                throw new SQLException(String.format("Connected database is malformed for"
                    + "allowing duplicate entries of card %d in category %d of deck instance"
                    + "%d %s!", cardToAdd, currentCategory, deckID, creation.toString()));
              }
              cardsToAdd.add(cardToAdd);
            }
            categoryContents.put(currentCategory, cardsToAdd);
          }
          catch (SQLException e) {
            throw new SQLException(e.getMessage() +
                String.format("\nFailed to query for cards in category %s for"
                + " deck instance %d %s!", currentCategory, deckID, creation.toString()));
          }
          finally {
            closeResultSet(cardsInCategory);
          }
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to query for card categories for deck "
            + "instance %d %s!", deckID, creation.toString()));
      }

      // Get card printings
      String cardPrintingsQuery = "SELECT * FROM DeckInstCardExpansion WHERE deck_id=? "
          + "AND deck_inst_creation=?";
      try (PreparedStatement preparedStatement = connection.prepareStatement(cardPrintingsQuery);) {
        preparedStatement.setInt(1, deckID);
        preparedStatement.setTimestamp(2, Timestamp.valueOf(creation));
        ResultSet cardPrintings = preparedStatement.executeQuery();

        try {
          while (cardPrintings.next()) {
            String cardName = cardPrintings.getString("card_name");
            String expansion = cardPrintings.getString("expansion");
            String cardNumber = cardPrintings.getString("card_number");
            CardPrinting toAdd = new DefaultCardPrinting(cardName, expansion, cardNumber);

            int cardQuantity = cardPrintings.getInt("quantity");
            cardPrintingQuantities.put(toAdd, cardQuantity);
          }
        }
        catch (SQLException e) {
          throw new SQLException(e.getMessage() +
              String.format("\nFailed to get card printing info for deck "
              + "instance %d %s!", deckID, creation.toString()));
        }
        finally {
          closeResultSet(cardPrintings);
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to query for card printings in deck"
            + "instance %d %s!", deckID, creation.toString()));
      }

      DeckInstance toAdd = new DefaultDeckInstance(deckID, creation, categoryContents, cardPrintingQuantities);
      deckInstances.add(toAdd);
    }

    disconnect(connection);

    return new DefaultDeck(deckID, deckName, deckDesp, deckInstances);
  }

  @Override
  public void addDeck(Deck deck) throws IllegalArgumentException, SQLException {
    if (deck == null) {
      throw new IllegalArgumentException("Given deck can't be null!");
    }
    hasDeckNotBeenAdded(deck.getDeckID());

    int deckID = deck.getDeckID();
    String deckName = deck.getDeckName();
    String deckInsert = "INSERT INTO DECK(id,name,desp) VALUES (?,?,?)";
    try (Connection connection = connect();
        PreparedStatement preparedStatement = connection.prepareStatement(deckInsert)) {
      preparedStatement.setInt(1, deckID);
      preparedStatement.setString(2, deckName);
      preparedStatement.setString(3, deck.getDescription());
      preparedStatement.executeUpdate();
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to add new deck with ID %d and name %s!", deckID, deckName));
    }

    SortedSet<DeckInstance> history = deck.getHistory();
    for (DeckInstance deckInstance : history) {
      addDeckInstance(deckInstance);
    }
  }

  @Override
  public void addDeckInstance(DeckInstance deck) throws IllegalArgumentException, SQLException {
    if (deck == null) {
      throw new IllegalArgumentException("Given deck instance can't be null!");
    }
    hasDeckBeenAdded(deck.getParentDeckID());

    Connection connection = connect();
    String insertStatement;

    // Add deck instance info
    int deckID = deck.getParentDeckID();
    LocalDateTime creationInfo = deck.getCreationInfo();
    Timestamp creationTimestamp = Timestamp.valueOf(creationInfo);
    insertStatement = "INSERT INTO DeckInstance(deck_id, creation) VALUES (?,?)";
    try (PreparedStatement preparedStatement = connection.prepareStatement(insertStatement)) {
      preparedStatement.setInt(1, deckID);
      preparedStatement.setTimestamp(2, creationTimestamp);
      preparedStatement.executeUpdate();
    }
    catch (SQLException e){
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to add deck instance %s, %d!", creationInfo.toString(),
          deckID));
    }

    // Add categories
    Set<String> categories = deck.getCategories();
    for (String category : categories) {
      insertStatement = "INSERT INTO DeckInstCategory(deck_id, deck_inst_creation, category) "
          + "VALUES (?,?,?)";
      try (PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);) {
        preparedStatement.setInt(1, deckID);
        preparedStatement.setTimestamp(2, creationTimestamp);
        preparedStatement.setString(3, category);
        preparedStatement.executeUpdate();
      }
      catch (SQLException e){
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to add category %s for deck instance %d, "
                + "%s!", category, deck, creationInfo.toString()));
      }
    }

    // Add cards
    Set<String> cards = deck.getCardNames();
    for (String card : cards) {
      insertStatement = "INSERT INTO DeckInstCard(deck_id, deck_inst_creation, card_name) VALUES (?,?,?)";
      try (PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);) {
        preparedStatement.setInt(1, deckID);
        preparedStatement.setTimestamp(2, creationTimestamp);
        preparedStatement.setString(3, card);
        preparedStatement.executeUpdate();
      }
      catch (SQLException e){
        throw new SQLException(e.getMessage() + String.format("\nFailed to add card %s for deck instance %s, "
            + "%s!", card, deck, creationInfo.toString()));
      }
    }

    // Add card expansions
    Map<CardPrinting, Integer> cardExpansion = deck.getCardPrintingQuantities();
    for (CardPrinting cardPrinting : cardExpansion.keySet()) {
      String cardName = cardPrinting.getCardName();
      String expansion = cardPrinting.getCardExpansion();
      String identifier = cardPrinting.getIdentifyingNumber();
      int quantity = cardExpansion.get(cardPrinting);
      insertStatement = "INSERT INTO DeckInstCardExpansion(deck_id, deck_inst_creation, "
          + "card_name, expansion, card_number, quantity) VALUES (?,?,?,?,?,?)";
      try (PreparedStatement preparedStatement = connection.prepareStatement(insertStatement)) {
        preparedStatement.setInt(1, deckID);
        preparedStatement.setTimestamp(2, creationTimestamp);
        preparedStatement.setString(3, cardName);
        preparedStatement.setString(4, expansion);
        preparedStatement.setString(5, identifier);
        preparedStatement.setInt(6, quantity);
        preparedStatement.executeUpdate();
      }
      catch (SQLException e){
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to add card printing %s, %s, %s for deck "
            + "instance %s, %s!", cardName, expansion, identifier, deck,
            creationInfo.toString()));
      }
    }

    // Add cards in categories
    Map<String, SortedSet<String>> cardCategories = deck.getCardNamesByCategory();
    for (String category : cardCategories.keySet()) {
      Set<String> categoryCards = cardCategories.get(category);
      for (String card : categoryCards) {
        insertStatement = "INSERT INTO DeckInstCardCategory(deck_id, deck_inst_creation, "
            + "card_name, category) VALUES (?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertStatement)){
          preparedStatement.setInt(1, deckID);
          preparedStatement.setTimestamp(2, creationTimestamp);
          preparedStatement.setString(3, card);
          preparedStatement.setString(4, category);
          preparedStatement.executeUpdate();
        }
        catch (SQLException e){
          throw new SQLException(e.getMessage() +
              String.format("\nFailed to add card %s, to category %s for deck "
                  + "instance %s, %s!", card, category, deck, creationInfo.toString()));
        }
      }
    }
    disconnect(connection);
  }

  @Override
  public void deleteDeck(int deckID) throws IllegalArgumentException, SQLException {
    // Check if deck exists
    hasDeckBeenAdded(deckID);

    String deletionRequest = "DELETE FROM Deck WHERE id=?";
    try (Connection connection = connect();
    PreparedStatement preparedStatement = connection.prepareStatement(deletionRequest);) {
      preparedStatement.setInt(1, deckID);
      preparedStatement.executeUpdate();
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to delete deck with ID %d!", deckID));
    }
  }

  @Override
  public void updateDeckName(int deckID, String newName) throws IllegalArgumentException, SQLException {
    if (newName == null) {
      throw new IllegalArgumentException("Given new name can't be null!");
    }
    // Check if deck exists
    hasDeckBeenAdded(deckID);

    String updateRequest = "UPDATE Deck SET name=? WHERE id=?";
    try (Connection connection = connect();
    PreparedStatement preparedStatement = connection.prepareStatement(updateRequest);) {
      preparedStatement.setString(1, newName);
      preparedStatement.setInt(2, deckID);
      preparedStatement.executeUpdate();
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to update deck with ID %d with new name \"%s\"!", deckID, newName));
    }
  }

  @Override
  public void updateDeckDesp(int deckID, String newDesp) throws IllegalArgumentException, SQLException {
    if (newDesp == null) {
      throw new IllegalArgumentException("Given new description can't be null!");
    }
    // Check if deck exists
    hasDeckBeenAdded(deckID);

    String updateRequest = "UPDATE Deck SET desp=? WHERE id=?";
    try (Connection connection = connect();
        PreparedStatement preparedStatement = connection.prepareStatement(updateRequest);){
      preparedStatement.setString(1, newDesp);
      preparedStatement.setInt(2, deckID);
      preparedStatement.executeUpdate();
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("Failed to update deck with ID %d with new"
              + " description \"%s\"!", deckID, newDesp));
    }
  }

  @Override
  public InformativeDeckInstance getDeckInstanceInfo(DeckInstance deckInstance) throws SQLException {
    return new DefaultInformativeDeckInstance(deckInstance);
  }

  @Override
  public CardQuery getQuery() {
    return new DefaultCardQuery();
  }

  @Override
  public SortedSet<Card> queryCards(CardQuery cardQuery) throws IllegalArgumentException, SQLException {
    if (cardQuery == null) {
      throw new IllegalArgumentException("Given cardQuery can't be null!");
    }

    Map<String, Map<String, Set<String>>> cardNameToExpansionsToNumbers = new HashMap<>();
    String query = cardQuery.asQuery();
    try (Connection connection = connect();
    PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet cardQueryResults = preparedStatement.executeQuery();) {

      while (cardQueryResults.next()) {
        String cardName = cardQueryResults.getString("card_name");
        // Replace single quotes with doubled up single quotes for SQL
        String expansion = cardQueryResults.getString("expansion");
        String number = cardQueryResults.getString("number");

        // Card name and expansion info defaults
        Map<String, Set<String>> expansionInfo = new HashMap<>();
        expansionInfo.put(expansion, new TreeSet<>());

        // Check that card name and expansion info are added
        if (cardNameToExpansionsToNumbers.containsKey(cardName) ) {
          expansionInfo = cardNameToExpansionsToNumbers.get(cardName);
          if (!expansionInfo.containsKey(expansion)) {
            expansionInfo.put(expansion, new TreeSet<>());
          }
        }
        else {
          cardNameToExpansionsToNumbers.put(cardName, expansionInfo);
        }

        // Add number info
        cardNameToExpansionsToNumbers.get(cardName).get(expansion).add(number);
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() + "\nFailed to query for given card query!");
    }

    SortedSet<Card> cards = new TreeSet<>();
    for (String cardName : cardNameToExpansionsToNumbers.keySet()) {
      Map<String, Set<String>> expansions = cardNameToExpansionsToNumbers.get(cardName);
      Card toAdd = new DefaultCard(cardName, expansions);
      cards.add(toAdd);
    }

    return cards;
  }

  /**
   * Checks if the CDDB contains a deck with given ID, checks table Deck for row with entry under
   * "id" column, else throws an error.
   * @param deckID deck id to check for
   * @throws IllegalArgumentException if there is no deck in CDDB with matching ID
   */
  private void hasDeckBeenAdded(int deckID) throws IllegalArgumentException, SQLException {
    // Check if CDDB has deck with given ID
    Set<Integer> deckIDs = getDecks().keySet();
    if (!deckIDs.contains(deckID)) {
      throw new IllegalArgumentException("CDDB doesn't contain deck with given ID!");
    }
  }

  /**
   * Checks if the CDDB contains a deck with given ID, checks table Deck for row with entry under
   * "id" column, if so throws an error.
   * @param deckID deck id to check for
   * @throws IllegalArgumentException if there is a deck in CDDB with a matching ID
   */
  private void hasDeckNotBeenAdded(int deckID) throws IllegalArgumentException, SQLException {
    // Check if CDDB has deck with given ID
    Set<Integer> deckIDs = getDecks().keySet();
    if (deckIDs.contains(deckID)) {
      throw new IllegalArgumentException("CDDB already contains deck with given ID!");
    }
  }

  @Override
  public SortedSet<String> getSupertypes() {
    return Collections.unmodifiableSortedSet(supertypes);
  }

  @Override
  public SortedSet<String> getTypes() {
    return Collections.unmodifiableSortedSet(types);
  }

  @Override
  public SortedSet<String> getSubtypes() {
    return Collections.unmodifiableSortedSet(subtypes);
  }

  @Override
  public SortedSet<String> getManaTypes() {
    return Collections.unmodifiableSortedSet(manaTypes);
  }

  @Override
  public SortedSet<String> getRarityTypes() {
    return Collections.unmodifiableSortedSet(rarities);
  }

  @Override
  public SortedSet<String> getColors() {
    return Collections.unmodifiableSortedSet(colors);
  }

  @Override
  public SortedSet<String> getMultifacedTypes() throws SQLException {
    SortedSet<String> twoTypes = retrieveColumnInfo("TwoCards", "type");
    SortedSet<String> threeTypes = retrieveColumnInfo("ThreeCards", "type");

    SortedSet<String> types = new TreeSet<>();
    types.addAll(twoTypes);
    types.addAll(threeTypes);

    return Collections.unmodifiableSortedSet(types);
  }

  @Override
  public SortedSet<String> getBlocks() {
    return Collections.unmodifiableSortedSet(blocks);
  }

  @Override
  public SortedSet<String> getArtists(){
    return Collections.unmodifiableSortedSet(artists);
  }

  @Override
  public SortedSet<String> getSets() {
    return Collections.unmodifiableSortedSet(sets);
  }

  /**
   * Retrieves all information stored in the given column apart of the given table from the CDDB.
   * @param tableName name of the table to query
   * @param columnName name of the column that is apart of the given table to query from
   * @return sorted set of the row info from the given column apart of the
   * @throws SQLException failure to query information from the CDDB
   * @throws IllegalStateException if connection to the database is closed or not working
   */
  private SortedSet<String> retrieveColumnInfo(String tableName, String columnName) throws SQLException {
    Connection connection = connect();
    SortedSet<String> toReturn = retrieveColumnInfo(tableName, columnName, connection);
    disconnect(connection);
    return toReturn;
  }

  /**
   * Given the name of a table in the CDDB, and a column in that table that contains String values,
   * retrieves a SortedSet of all the values stored in that column in the CDDB.
   * @param tableName name of the table to retrieve info from
   * @param columnName name of column to retrieve info from
   * @param connection connection to the CCCB to use for withdrawing information
   * @return sorted set of all the String values stored in the given column of the given table
   * @throws IllegalArgumentException if given table name and / or column name is null, empty, or
   * contains only spaces - or if given connection is null or closed
   * @throws SQLException if there is an error retrieving info from the CDDB
   */
  private SortedSet<String> retrieveColumnInfo(String tableName, String columnName,
      Connection connection) throws IllegalArgumentException, SQLException {
    if (connection == null || connection.isClosed()) {
      throw new IllegalArgumentException("Given connection can't be null or closed!");
    }
    else if (tableName == null || tableName.isBlank() || columnName == null || columnName.isBlank()) {
      throw new IllegalArgumentException("Given table and column name can't be "
          + "null, empty, or full of spaces!");
    }

    SortedSet<String> toReturn = new TreeSet<>();
    String query = String.format("SELECT DISTINCT(%s) FROM %s", columnName, tableName);
    try (PreparedStatement preparedStatement = connection.prepareStatement(query);
    ResultSet queryResult = preparedStatement.executeQuery()) {

      while (queryResult.next()) {
        String result = queryResult.getString(columnName);
        toReturn.add(result);
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for column %s from table %s!", columnName, tableName));
    }
    return Collections.unmodifiableSortedSet(toReturn);
  }

  @Override
  public Card getCard(String name) throws SQLException, IllegalArgumentException {
    if (name == null) {
      throw new IllegalArgumentException("Given card can't be null!");
    }

    // Check if card is in database

    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    try {
      connection = connect();
      String checkQuery = "SELECT name FROM Card WHERE name = ?";
      preparedStatement = connection.prepareStatement(checkQuery);
      preparedStatement.setString(1, name);
      resultSet = preparedStatement.executeQuery();

      if (!resultSet.next()) {
        throw new IllegalArgumentException(String.format("Database doesn't contain card %s!", name));
      }

      close(resultSet, preparedStatement);

      String cardQuery = "";


      Card card = null;

      return card;
    }
    catch (SQLException e) {
      throw new SQLException("Failed to query for card");
    }
    finally {
      close(resultSet, preparedStatement);
      disconnect(connection);
    }
  }

  /**
   * Formats and returns a given String that is malformatted by most SQL implementation Replaces
   * any single quote with double up single quotes to prevent invalid SQL queries
   * "foo's" --> "foo''s"
   * @param word word to format
   * @return properly formatted word
   */
  private String formatWordToSQL(String word) {
    if (word == null) {
      throw new IllegalArgumentException("Given word can't be null!");
    }
    return word.replace("'", "''");
  }

  /**
   * Default implementation of the {@link Card} interface, a simple container to hold all the
   * information pertaining to a given card (and its relevant expansions). Embedded with
   * {@link DefaultDatabaseChannel} to have access to the CDDB for retriving needed card info.
   */
  public class DefaultCard implements Card {

    /**
     * Name of the Card as it appears in the CDDB.
     */
    private final String name;

    /**
     * Converted mana cost of the associated card as it appears in the CDDB.
     */
    private final int cmc;

    /**
     * Manacosts and their associated manacosts as they appear in the CDDB for this Card.
     */
    private final Map<String, Integer> manaCosts;

    /**
     * Text making up the Card as it appears in the CDDB.
     */
    private final String text;

    /**
     * Supertypes of the Card as it appears in the CDDB.
     */
    private final SortedSet<String> supertypes;

    /**
     * Types of the Card as it appears in the CDDB.
     */
    private final SortedSet<String> types;

    /**
     * Subtypes of the Card as it appears in the CDDB.
     */
    private final SortedSet<String> subtypes;

    /**
     * Set of colors making up this Card's colors.
     */
    private final SortedSet<String> colors;

    /**
     * Set of colors making up this Card's color identity.
     */
    private final SortedSet<String> colorIdentity;

    /**
     * Relationship this Card has with other Cards, if any.
     */
    private final CardRelationship relationship;

    /**
     * Sorted set of all the card printings associated with this Card
     */
    private final SortedSet<InformativeCardPrinting> cardPrintings;

    /**
     * Any additional info of the Card as it appears in the CDDB.
     */
    private final Map<String, String> additionalInfo;

    /**
     * Builds a {@link DefaultCard} from a given card name and list of associated expansions from
     * which the card was printed - make up its associated {@link InformativeCardPrinting}s.
     * @param name name of card to associate with
     * @param expansions expansions card was printed in to associate with, with associated numbers
     * @throws SQLException if there is a failure in retrieving any of the card's associated info
     * @throws IllegalArgumentException if any given parameters are null, given set of expansions
     * is empty, if given name is not a card name in the CDDB, or if expansions contain an expansion
     * not within the CDDB.
     */
    protected DefaultCard(String name, Map<String, Set<String>> expansions) throws SQLException,
        IllegalArgumentException {
      if (name == null) {
        throw new IllegalArgumentException("Given name can't be null!");
      }
      else if (expansions == null || expansions.isEmpty()) {
        throw new IllegalArgumentException("Given expansions can't be null nor empty!");
      }

      for (String expansion : expansions.keySet()) {
        if (!sets.contains(expansion)) {
          throw new IllegalArgumentException(String.format("Database doesn't contain printing "
              + "for card %s from expansion %s!", name, expansion));
        }
      }

      Connection connection = connect();
      this.name = setName(connection, name);
      this.text = setText(connection, name);
      this.cmc = setCMC(connection, name);
      this.manaCosts = setManaCosts(connection, name);
      this.supertypes = setSupertypes(connection, name);
      this.types = setTypes(connection, name);
      this.subtypes = setSubtypes(connection, name);
      this.colors = setColors(connection, name);
      this.colorIdentity = setColorIdentity(connection, name);
      this.relationship = setCardRelationship(connection, name);
      this.additionalInfo = setAdditionalInfo(connection, name);
      this.cardPrintings = setCardPrintings(connection, expansions, name);
      disconnect(connection);
    }

    /**
     * Retrieves the name of this {@link Card} using the given connection to the CDDB.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return name of this card from the database
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private String setName(Connection connection, String cardName) throws SQLException {
      return getCardInfo(connection,"name", "name", cardName);
    }

    /**
     * Retrieves the text of this {@link Card} using the given connection to the CDDB.
     * @param connection connection to the CDDB to use
     * @param cardName name of card to retrieve data for
     * @return text of this card from the CDDB
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private String setText(Connection connection, String cardName) throws SQLException {
      return getCardInfo(connection,"text", "card text", cardName);
    }

    /**
     * Retrieves some base info of this {@link Card} using the given connection to the CDDB, from
     * the Card table in the CDDB.
     * @param connection connection to the CDDB to use for retrieving data
     * @param column name of column to retrieve data from
     * @param infoType type of info retrieved from the column
     * @param cardName name of card to retrieve data for
     * @return some piece of base info of this card from the Card table in the CDDB
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if given connection is closed, or if any of the given
     * parameters are null
     */
    private String getCardInfo(Connection connection, String column, String infoType,
        String cardName) throws SQLException {
      Map<String, String> conditions = new HashMap<>();
      conditions.put("name", cardName);
      Set<String> toReturn = retrieveSingleColumn(connection, "Card", column,
          conditions, true, infoType, cardName);
      return singleItemSetToString(toReturn);
    }

    /**
     * Retrieves all the info from a single column, for a given table, associated this {@link Card}.
     * @param connection connection to the CDDB to use for retrieving data
     * @param table name of table to retrieve info from
     * @param column name of column on given table to retrieve data from
     * @param conditions search conditions to apply to query, in form of column name to a matching
     * string
     * @param singleResult boolean flag to single if returned set should contain a single item or
     * multiple items
     * @param infoType type of info being retrieved
     * @param cardName name of card to retrieve data for
     * @return set of all data from given column of given table associated with given card name, as
     * per given conditions
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private SortedSet<String> retrieveSingleColumn(Connection connection, String table, String column,
        Map<String, String> conditions, boolean singleResult, String infoType, String cardName)
        throws SQLException {

      if (connection == null || table == null || column == null ||
          conditions == null || infoType == null) {
        throw new IllegalArgumentException("None of the given parameters can be null!");
      }
      else if (connection.isClosed()) {
        throw new IllegalArgumentException("Given connection can't be closed!");
      }

      StringBuilder query = new StringBuilder(String.format("SELECT %s FROM %s", column, table));

      boolean singleCondition = true;
      for (String key : conditions.keySet()) {
        String value = formatWordToSQL(conditions.get(key));
        String mergeCond;
        if (singleCondition) {
          singleCondition = false;
          mergeCond = "WHERE";
        }
        else {
          mergeCond = "AND";
        }

        query.append(String.format(" %s %s = '%s'", mergeCond, key, value));
      }

      try (PreparedStatement preparedStatement = connection.prepareStatement(query.toString());
          ResultSet resultSet = preparedStatement.executeQuery()) {
        SortedSet<String> toReturn = new TreeSet<>();
        while (resultSet.next()) {
          toReturn.add(resultSet.getString(column));
          if (singleResult) {
            assert toReturn.size() == 1;
            return toReturn;
          }
        }
        return toReturn;
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("Failed to retrieve %s for card %s from database!", infoType, cardName));
      }
    }

    /**
     * Retrieves the mana symbols and respective quantities associated with this {@link Card}.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return mapping of mana symbols to their quantities for this card
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private Map<String, Integer> setManaCosts(Connection connection, String cardName) throws SQLException {
      cardName = formatWordToSQL(cardName);
      String query = String.format("SELECT mana_type, quantity FROM Mana WHERE card_name = '%s'", cardName);
      try (PreparedStatement preparedStatement = connection.prepareStatement(query);
          ResultSet resultSet = preparedStatement.executeQuery()) {
        Map<String, Integer> manaTypeToQuantity = new HashMap<>();
        while (resultSet.next()) {
          String manaType = resultSet.getString("mana_type");
          int quantity = resultSet.getInt("quantity");
          manaTypeToQuantity.put(manaType, quantity);
        }
        return manaTypeToQuantity;
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("Failed to retrieve mana type info for card %s from database!", cardName));
      }
    }

    /**
     * Retrieves the supertypes associated with this {@link Card}.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return set of supertypes associated with this card
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private SortedSet<String> setSupertypes(Connection connection, String cardName) throws SQLException {
      return retrieveTypeInfo(connection, "Supertype", "supertype", cardName);
    }

    /**
     * Retrieves the types associated with this {@link Card}.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return set of types associated with this card
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private SortedSet<String> setTypes(Connection connection, String cardName) throws SQLException {
      return retrieveTypeInfo(connection,"Type", "type", cardName);
    }

    /**
     * Retrieves the subtypes associated with this {@link Card}.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return set of subtypes associatd with this card
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if given connection is closed, or if any of the given
     * parameters are null
     */
    private SortedSet<String> setSubtypes(Connection connection, String cardName) throws SQLException {
      return retrieveTypeInfo(connection,"Subtype", "subtype", cardName);
    }

    /**
     * Retrieves types associated with this {@link Card}, for a given category of type (supertype,
     * type, subtype).
     * @param connection connection to the CDDB to use for retrieving data
     * @param table to retrieve data from
     * @param type category of type to retrieve
     * @param cardName name of card to retrieve data for
     * @return set of given category of type associated with this card
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if given connection is closed, or if any of the given
     * parameters are null
     */
    private SortedSet<String> retrieveTypeInfo(Connection connection, String table, String type, String cardName) throws SQLException {
      Map<String, String> conditions = new HashMap<>();
      conditions.put("card_name", cardName);
      return retrieveSingleColumn(connection, table, "type", conditions,
          false, type, cardName);
    }

    /**
     * Retrieves the colors associated with this {@link Card}.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return set of colors associated with this card
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private SortedSet<String> setColors(Connection connection, String cardName) throws SQLException {
      return getColorInfo(connection,"Color", "color", cardName);
    }

    /**
     * Retrieves the colors associated with this {@link Card}'s color identity.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return colors making up this card's color identity
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private SortedSet<String> setColorIdentity(Connection connection, String cardName) throws SQLException {
      return getColorInfo(connection,"ColorIdentity", "color identity", cardName);
    }

    /**
     * Retrieves color info from a given table associated with this {@link Card}.
     * @param connection connection to the CDDB to use for retrieving data
     * @param table table to retrieve info from
     * @param infoType type of info being retrieved
     * @param cardName name of card to retrieve data for
     * @return set of colors from given table associated with this card
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private SortedSet<String> getColorInfo(Connection connection, String table, String infoType,
        String cardName) throws SQLException {
      Map<String, String> conditions = new HashMap<>();
      conditions.put("card_name", cardName);
      return retrieveSingleColumn(connection, table, "color", conditions,
          false, infoType, cardName);
    }

    /**
     * Builds a {@link CardRelationship} for any relationships this {@link Card} may have with
     * other cards.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return a CardRelationship to represent the relationship this card may have with other cards
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private CardRelationship setCardRelationship(Connection connection,
        String cardName) throws SQLException {
      SortedSet<String> cardNames = new TreeSet<>();

      cardName = formatWordToSQL(cardName);

      String twoCardQuery = String.format("SELECT * FROM TwoCards WHERE card_a = '%s' "
          + "OR card_b = '%s'", cardName, cardName);
      try (PreparedStatement preparedStatement = connection.prepareStatement(twoCardQuery);
          ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          String relationship = resultSet.getString("type");
          cardNames.add(resultSet.getString("card_a"));
          cardNames.add(resultSet.getString("card_b"));
          return new DefaultCardRelationship(cardNames, relationship);
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("Failed to check for two card relationship info for card %s from "
                + "database!", cardName));
      }

      String threeCardQuery = String.format("SELECT * FROM ThreeCards WHERE card_a = '%s' "
          + "OR card_b = '%s' OR card_c = '%s'", cardName, cardName, cardName);
      try (PreparedStatement preparedStatement = connection.prepareStatement(threeCardQuery);
          ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          String relationship = resultSet.getString("type");
          cardNames.add(resultSet.getString("card_a"));
          cardNames.add(resultSet.getString("card_b"));
          cardNames.add(resultSet.getString("card_c"));
          return new DefaultCardRelationship(cardNames, relationship);
        }
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("Failed to check for three card relationship info for card %s from database!", cardName));
      }

      return new DefaultCardRelationship();
    }

    /**
     * Retrieves any additional information associated with this {@link Card} such as power,
     * toughness, or loyalty.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return any additional info and their respective quantities associated with this {@link Card}
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private Map<String, String> setAdditionalInfo(Connection connection,
        String cardName) throws SQLException {
      // When constructing variables, call after initiating types
      Map<String, String> additionalInfo = new HashMap<>();
      Map<String, String> conditions = new HashMap<>();
      if (types.contains("Planeswalker")) {
        String loyalty = "loyalty";
        conditions.put("card_name", cardName);
        Set<String> result = retrieveSingleColumn(connection, "Loyalty", loyalty,
            conditions, true, loyalty, cardName);
        additionalInfo.put(loyalty, singleItemSetToString(result));
      }
      else if (types.contains("Creature") || subtypes.contains("Vehicle")) {
        String[] powerToughness = new String[]{"power", "toughness"};
        for (String category : powerToughness) {
          conditions.put("card_name", cardName);
          Set<String> result = retrieveSingleColumn(connection,"PowerToughness", category,
              conditions, true, category, cardName);
          additionalInfo.put(category, singleItemSetToString(result));
        }
      }
      return additionalInfo;
    }

    /**
     * For a set of expansions this {@link Card } was printed in, retrieves the unique information
     * associated with each card printing.
     * @param connection connection to the CDDB to use for retrieving data
     * @param expansions set of expansions associated card was printed in to retrieve data from,
     * with associated numbers
     * @param cardName name of card to retrieve data for
     * @return set of InformativeCardPrinting for the expansions given t
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, if given connection is
     * closed, or if given set of expansions is null
     */
    private SortedSet<InformativeCardPrinting> setCardPrintings(Connection connection,
        Map<String, Set<String>> expansions, String cardName) throws SQLException {

      if (connection == null || expansions == null || cardName == null) {
        throw new IllegalArgumentException("Given parameters can't be null!");
      }
      else if (connection.isClosed()) {
        throw new IllegalArgumentException("Given connection can't be closed!");
      }
      else if (expansions.isEmpty()) {
        throw new IllegalArgumentException("Given set of expansions can't be empty!");
      }

      SortedSet<InformativeCardPrinting> informativeCardPrinting = new TreeSet<>();
      for (String expansion : expansions.keySet()) {
        for (String number : expansions.get(expansion)) {
          informativeCardPrinting
              .add(new DefaultInformativeCardPrinting(connection, cardName, expansion, number));

        }
      }
      return informativeCardPrinting;
    }

    /**
     * Retrieves the converted mana cost with this {@link Card}.
     * @param connection connection to the CDDB to use for retrieving data
     * @param cardName name of card to retrieve data for
     * @return associated card's converted mana cost
     * @throws SQLException if there is an error in retrieving data from the CDDB
     * @throws IllegalArgumentException if any given parameter is null, or if given connection is
     * closed
     */
    private int setCMC(Connection connection, String cardName) throws SQLException {
      Map<String, String> conditions = new HashMap<>();
      conditions.put("name", cardName);
      Set<String> singleResult = retrieveSingleColumn(connection, "Card", "cmc",
          conditions, true, "converted mana cost", cardName);
      return Integer.parseInt(singleItemSetToString(singleResult));
    }

    /**
     * Given a set of Strings with a single item, returns the sole String in that set.
     * @param set set of Strings to pull from
     * @return single String in the given set
     * @throws IllegalArgumentException if given set in null or contains a number of items other
     * than one
     */
    private String singleItemSetToString(Set<String> set) {
      if (set == null) {
        throw new IllegalArgumentException("Given set can't be null!");
      }
      else if (set.size() != 1) {
        throw new IllegalArgumentException("Given set should only contain a single item!");
      }
      String[] dummy = new String[1];
      dummy = set.toArray(dummy);
      return dummy[0];
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public int getConvertedManaCost() {
      return cmc;
    }

    @Override
    public Map<String, Integer> getManaCost() {
      return Collections.unmodifiableMap(manaCosts);
    }

    @Override
    public SortedSet<String> getColors() {
      return Collections.unmodifiableSortedSet(colors);
    }

    @Override
    public SortedSet<String> getColorIdentity() {
      return Collections.unmodifiableSortedSet(colorIdentity);
    }

    @Override
    public String getText() {
      return text;
    }

    @Override
    public SortedSet<String> getSupertypes() {
      return Collections.unmodifiableSortedSet(supertypes);
    }

    @Override
    public SortedSet<String> getTypes() {
      return Collections.unmodifiableSortedSet(types);
    }

    @Override
    public SortedSet<String> getSubtypes() {
      return Collections.unmodifiableSortedSet(subtypes);
    }

    @Override
    public SortedSet<InformativeCardPrinting> getCardPrintings() {
      return Collections.unmodifiableSortedSet(cardPrintings);
    }

    @Override
    public Map<String, String> getExtraStats() {
      return Collections.unmodifiableMap(additionalInfo);
    }

    @Override
    public CardRelationship getRelationships() {
      return relationship;
    }

    @Override
    public int compareTo(Card other) throws IllegalArgumentException {
      if (other == null) {
        throw new IllegalArgumentException("Given card can't be null!");
      }

      int nameCompare = getName().compareTo(other.getName());
      if (nameCompare != 0) {
        return nameCompare;
      }

      // Neither should be empty
      SortedSet<InformativeCardPrinting> otherExpansions = other.getCardPrintings();
      assert !getCardPrintings().isEmpty() && !otherExpansions.isEmpty();

      boolean thisIsShorter;
      SortedSet<InformativeCardPrinting> shorterExpansions;
      SortedSet<InformativeCardPrinting> longerExpansions;
      if (getCardPrintings().size() < otherExpansions.size()) {
        thisIsShorter = true;
        shorterExpansions = getCardPrintings();
        longerExpansions = otherExpansions;
      }
      else {
        thisIsShorter = false;
        shorterExpansions = otherExpansions;
        longerExpansions = getCardPrintings();
      }

      Iterator<InformativeCardPrinting> shortExpansionsIterator = shorterExpansions.iterator();
      Iterator<InformativeCardPrinting> longerExpansionsIterator = longerExpansions.iterator();
      while (shortExpansionsIterator.hasNext()) {
        InformativeCardPrinting curShorterExpansionsItem = shortExpansionsIterator.next();
        InformativeCardPrinting curLongerExpansionItem = longerExpansionsIterator.next();
        int curComparison = thisIsShorter ?
            curShorterExpansionsItem.compareTo(curLongerExpansionItem) :
            curLongerExpansionItem.compareTo(curShorterExpansionsItem);

        if (curComparison != 0) {
          return curComparison;
        }
      }

      int remainingDifference = 0;
      while (longerExpansionsIterator.hasNext()) {
        longerExpansionsIterator.next();

        if (thisIsShorter) {
          remainingDifference--;
        }
        else {
          remainingDifference++;
        }
      }

      // If remaining difference is 0, should be the exact same card with exact same printings
      return remainingDifference;
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof Card) {
        Card otherCard = (Card) other;
        return name.equals(otherCard.getName()) && cardPrintings.equals(otherCard.getCardPrintings());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, cardPrintings);
    }
  }

  /**
   * Default implementation of {@link InformativeCardPrinting}, represents additional info for a given
   * {@link CardPrinting} such as artists, rarity, etc.
   */
  public class DefaultInformativeCardPrinting implements InformativeCardPrinting {

    /**
     * Card Printing this {@link InformativeCardPrinting} represents.
     */
    private final CardPrinting cardPrinting;

    private final SortedSet<String> artists;

    private final String flavorText;

    private final String rarity;

    private final String scryfallId;

    public DefaultInformativeCardPrinting(Connection connection, String cardName,
        String cardExpansion, String identifyingNumber) throws SQLException {
      if (connection == null || connection.isClosed()) {
        throw new IllegalArgumentException("Given connection can't be null or closed!");
      }
      this.cardPrinting = new DefaultCardPrinting(cardName, cardExpansion, identifyingNumber);

      cardName = formatWordToSQL(cardName);
      String formattedExpansion = formatWordToSQL(cardExpansion);
      String cardExpansionQuery = String.format("SELECT * FROM CardExpansion "
              + "WHERE card_name = '%s' AND expansion = '%s' AND number = '%s'",
          cardName, formattedExpansion, identifyingNumber);
      try (PreparedStatement preparedStatement = connection.prepareStatement(cardExpansionQuery);
          ResultSet resultSet = preparedStatement.executeQuery()) {
        assert resultSet.next(); // Should only have one row
        this.rarity = resultSet.getString("rarity");
        this.flavorText = resultSet.getString("flavor_text");
        this.scryfallId = resultSet.getString("scryfall_id");
        assert !resultSet.next(); // Should return false

      } catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("Failed to retrieve card expansion info for card %s from database!",
                cardName));
      }

      this.artists = setArtists(connection);
    }

    private SortedSet<String> setArtists(Connection connection) {
      String cardName = formatWordToSQL(cardPrinting.getCardName());
      String expansion = cardPrinting.getCardExpansion();
      String number = cardPrinting.getIdentifyingNumber();

      String artistsQuery = String.format("SELECT artist FROM Artist WHERE card_name = '%s' "
          + "AND expansion = '%s' AND number = '%s'",
          cardName, formatWordToSQL(expansion), number);
      SortedSet<String> artists = new TreeSet<>();
      try (PreparedStatement preparedStatement1 = connection.prepareStatement(artistsQuery);
          ResultSet artistResultSet = preparedStatement1.executeQuery()) {
        while (artistResultSet.next()) {
          artists.add(artistResultSet.getString("artist"));
        }
      }
      catch (SQLException e) {
        throw new IllegalArgumentException(String.format("Failed to find artists for "
            + "card printing %s, %s, %s!", cardName, expansion, number));
      }
      return artists;
    }

    @Override
    public String getFlavorText() {
      return flavorText;
    }

    @Override
    public SortedSet<String> getArtists() {
      return artists;
    }

    @Override
    public String getRarity() {
      return rarity;
    }

    @Override
    public String getScryfallID() {
      return scryfallId;
    }

    @Override
    public String getCardName() {
      return cardPrinting.getCardName();
    }

    @Override
    public String getCardExpansion() {
      return cardPrinting.getCardExpansion();
    }

    @Override
    public String getIdentifyingNumber() {
      return cardPrinting.getIdentifyingNumber();
    }

    @Override
    public int compareTo(CardPrinting o) {
      return cardPrinting.compareTo(o);
    }

    @Override
    public boolean equals(Object other) {
      return cardPrinting.equals(other);
    }

    @Override
    public int hashCode() {
      return cardPrinting.hashCode();
    }
  }

  /**
   * Default implementation of the {@link CardQuery} interface, listed inside
   * {@link DefaultDatabaseChannel} due its tight coupling with it for purposes of determining
   * what is and is not a valid parameter. Stores lists of added parameters of each type of
   * parameter supported, then builds the desired query on demand to retrieved desired info from
   * the CDDB.
   */
  private class DefaultCardQuery implements CardQuery {

    /**
     * Specific table names to use for each type of {@link Stat}.
     */
    private final Map<Stat, String> statTableNames;

    /**
     * Specific column names to use for each type of {@link Stat} to retrieve actual data from.
     */
    private final Map<Stat, String> statValueColumnNames;

    /**
     * Specific column names to use for each type of {@link Stat} to retrieve the name of the
     * associated card.
     */
    private final Map<Stat, String> statCardNameColumnNames;

    /**
     * List of parameters added to search by a card's name.
     */
    private final Map<SearchOption, SortedSet<String>> nameParams;

    /**
     * List of parameters added to search by a card's text.
     */
    private final Map<SearchOption, SortedSet<String>> textParams;

    /**
     * List of parameters added to search by a card's colors.
     */
    private final Map<SearchOption, SortedSet<String>> colorParams;

    /**
     * List of parameters added to search by a card's color identity.
     */
    private final Map<SearchOption, SortedSet<String>> colorIdentityParams;

    /**
     * List of parameters added to search by a card's supertypes.
     */
    private final Map<SearchOption, SortedSet<String>> supertypeParams;

    /**
     * List of parameters added to search by a card's types.
     */
    private final Map<SearchOption, SortedSet<String>> typeParams;

    /**
     * List of parameters added to search by a card's subtypes.
     */
    private final Map<SearchOption, SortedSet<String>> subtypeParams;

    /**
     * List of parameters added to search by the expansions a card was printed in.
     */
    private final Map<SearchOption, SortedSet<String>> setParams;

    /**
     * List of parameters added to search by the artists who painted a card.
     */
    private final Map<SearchOption, SortedSet<String>> artistParams;

    /**
     * List of parameters added to search by the rarities a card has been printed with.
     */
    private final Map<SearchOption, SortedSet<String>> rarityParams;

    /**
     * List of parameters added to search by the flavor text a card has been printed with.
     */
    private final Map<SearchOption, SortedSet<String>> flavorTextParams;

    /**
     * List of parameters added to search by the stats of a card compared to a given quantity.
     */
    private final List<Triple<Stat, Comparison, Integer>> statParams;

    /**
     * List of parameters added to search by the stats of a card compared to another stat of the
     * same card.
     */
    private final List<Triple<Stat, Comparison, Stat>> statVersusStatParams;

    /**
     * List of parameters added to search by the quantities of mana symbols of a card.
     */
    private final List<Triple<String, Comparison, Integer>> manaTypeParams;

    /**
     * Default constructor for this {@link DefaultCardQuery}, sets empty array lists for each type
     * of parameter list
     */
    private DefaultCardQuery() {
      nameParams = new HashMap<>();
      textParams = new HashMap<>();
      colorParams = new HashMap<>();
      colorIdentityParams = new HashMap<>();
      supertypeParams = new HashMap<>();
      typeParams = new HashMap<>();
      subtypeParams = new HashMap<>();
      setParams = new HashMap<>();
      artistParams = new HashMap<>();
      rarityParams = new HashMap<>();
      flavorTextParams = new HashMap<>();
      statParams = new ArrayList<>();
      statVersusStatParams = new ArrayList<>();
      manaTypeParams = new ArrayList<>();

      // Initalize Stat info maps
      statTableNames = new EnumMap<>(Stat.class);
      statValueColumnNames = new EnumMap<>(Stat.class);
      statCardNameColumnNames = new EnumMap<>(Stat.class);

      statTableNames.put(Stat.CMC, "Card");
      statValueColumnNames.put(Stat.CMC, "cmc");
      statCardNameColumnNames.put(Stat.CMC, "name");

      statTableNames.put(Stat.LOYALTY, "Loyalty");
      statValueColumnNames.put(Stat.LOYALTY, "loyalty_value");
      statCardNameColumnNames.put(Stat.LOYALTY, "card_name");

      statTableNames.put(Stat.POWER, "PowerToughness");
      statValueColumnNames.put(Stat.POWER, "power_value");
      statCardNameColumnNames.put(Stat.POWER, "card_name");

      statTableNames.put(Stat.TOUGHNESS, "PowerToughness");
      statValueColumnNames.put(Stat.TOUGHNESS, "toughness_value");
      statCardNameColumnNames.put(Stat.TOUGHNESS, "card_name");
    }

    @Override
    public void byName(String word, SearchOption searchFor) throws IllegalArgumentException {
      if (searchFor == null) {
        throw new IllegalArgumentException("Given search for param can't be null!");
      }
      validWord(word);
      addSearchOptionParam(nameParams, searchFor, word);
    }

    @Override
    public void byText(String word, SearchOption searchFor) throws IllegalArgumentException {
      if (searchFor == null) {
        throw new IllegalArgumentException("Given search for param can't be null!");
      }
      validWord(word);
      addSearchOptionParam(textParams, searchFor, word);
    }

    @Override
    public void byColor(String color, SearchOption searchFor) throws IllegalArgumentException {
      if (color == null || searchFor == null) {
        throw new IllegalArgumentException("Given params can't be null!");
      }
      else if (!colors.contains(color)) {
        throw new IllegalArgumentException(String.format("Given color %s not contained in the CDDB!", color));
      }
      addSearchOptionParam(colorParams, searchFor, color);
    }

    @Override
    public void byColorIdentity(String color, SearchOption searchFor) throws IllegalArgumentException {
      if (color == null || searchFor == null) {
        throw new IllegalArgumentException("Given params can't be null!");
      }
      else if (!colors.contains(color)) {
        throw new IllegalArgumentException(String.format("Given color %s not contained in the CDDB!", color));
      }
      addSearchOptionParam(colorIdentityParams, searchFor, color);
    }

    @Override
    public void bySupertype(String type, SearchOption searchFor) throws IllegalArgumentException {
      if (type == null || searchFor == null) {
        throw new IllegalArgumentException("Given params can't be null!");
      }
      else if (!supertypes.contains(type)) {
        throw new IllegalArgumentException(String.format("Given supertype %s is not contained in the CDDB!", type));
      }
      addSearchOptionParam(supertypeParams, searchFor, type);
    }

    @Override
    public void byType(String type, SearchOption searchFor) throws IllegalArgumentException {
      if (type == null || searchFor == null) {
        throw new IllegalArgumentException("Given type can't be null!");
      }
      else if (!types.contains(type)) {
        throw new IllegalArgumentException(String.format("Given type %s is not contained in the CDDB!", type));
      }
      addSearchOptionParam(typeParams, searchFor, type);
    }

    @Override
    public void bySubtype(String type, SearchOption searchFor) throws IllegalArgumentException {
      if (type == null || searchFor == null) {
        throw new IllegalArgumentException("Given params can't be null!");
      }
      else if (!subtypes.contains(type)) {
        throw new IllegalArgumentException(String.format("Given subtype %s is not contained in the CDDB!", type));
      }
      addSearchOptionParam(subtypeParams, searchFor, type);
    }

    @Override
    public void bySet(String set, SearchOption searchFor) throws IllegalArgumentException {
      if (set == null) {
        throw new IllegalArgumentException("Given parameters can't be null!");
      }
      else if (!sets.contains(set)) {
        throw new IllegalArgumentException(String.format("Given set %s is not contained in the CDDB!", set));
      }
      addSearchOptionParam(setParams, searchFor, set);
    }

    @Override
    public void byArtist(String artist, SearchOption searchFor) throws IllegalArgumentException {
      if (artist == null) {
        throw new IllegalArgumentException("Given type can't be null!");
      }
      else if (!artists.contains(artist)) {
        throw new IllegalArgumentException(String.format("Given artist %s is not contained in the CDDB!", artist));
      }
      addSearchOptionParam(artistParams, searchFor, artist);
    }

    @Override
    public void byFlavorText(String word, SearchOption searchFor) throws IllegalArgumentException {
      if (searchFor == null) {
        throw new IllegalArgumentException("Given params can't be null!");
      }
      validWord(word);
      addSearchOptionParam(flavorTextParams, searchFor, word);
    }

    @Override
    public void byRarity(String rarity, SearchOption searchFor) throws IllegalArgumentException {
      if (rarity == null || searchFor == null) {
        throw new IllegalArgumentException("Given params can't be null!");
      }
      else if (!rarities.contains(rarity)) {
        throw new IllegalArgumentException(String.format("Given rarity %s is not contained in the CDDB!", rarity));
      }
      addSearchOptionParam(rarityParams, searchFor, rarity);
    }

    @Override
    public void byBlock(String block, SearchOption searchFor) throws IllegalArgumentException {
      if (block == null) {
        throw new IllegalArgumentException("Given block can't be null!");
      }
      else if (!blocks.contains(block)) {
        throw new IllegalArgumentException(String.format("Given block %s is not contained in the CDDB!", block));
      }
      block = formatWordToSQL(block);

      String expansionsFromBlockQuery = String.format("SELECT expansion FROM Block WHERE block = '%s'", block);
      try (Connection connection = connect();
      PreparedStatement preparedStatement = connection.prepareStatement(expansionsFromBlockQuery)) {
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
          addSearchOptionParam(setParams, searchFor, resultSet.getString("expansion"));
        }
      }
      catch (SQLException e) {
       throw new IllegalArgumentException("Failed to add expansions from associated block!");
      }
    }

    /**
     * Given a set of parameters in the form of Map<SearchOption, SortedSet<String>>, adds given
     * {@param addUnder} to the under the SortedSet associated with the given {@link SearchOption}.
     * @param params set of parameters to add info to
     * @param addUnder SearchOption to add given parameter under
     * @param paramToAdd parameter to add
     * @throws IllegalArgumentException if any given parameter is null
     */
    private void addSearchOptionParam(Map<SearchOption, SortedSet<String>> params,
        SearchOption addUnder, String paramToAdd) {
      if (params == null || addUnder == null || paramToAdd == null) {
        throw new IllegalArgumentException("Given params can't be null!");
      }

      paramToAdd = formatWordToSQL(paramToAdd);

      if (params.containsKey(addUnder)) {
        params.get(addUnder).add(paramToAdd);
      }
      else {
        SortedSet<String> newParamList = new TreeSet<>();
        newParamList.add(paramToAdd);
        params.put(addUnder, newParamList);
      }
    }

    @Override
    public void byStat(Stat stat, Comparison comparison, int quantity)
        throws IllegalArgumentException {
      if (stat == null) {
        throw new IllegalArgumentException("Given stat can't be null!");
      }
      else if (comparison == null) {
        throw new IllegalArgumentException("Given comparison can't be null!");
      }
      statParams.add(new Triple<>(stat, comparison, quantity));
    }

    @Override
    public void byStatVersusStat(Stat thisStat, Comparison comparison, Stat otherStat)
        throws IllegalArgumentException {
      if (thisStat == null || otherStat == null) {
        throw new IllegalArgumentException("Given stats can't be null!");
      }
      else if (comparison == null) {
        throw new IllegalArgumentException("Given comparison can't be null!");
      } else if (thisStat.equals(otherStat)) {
        throw new IllegalArgumentException("Given stats must be different!");
      }
      statVersusStatParams.add(new Triple<>(thisStat, comparison, otherStat));
    }

    @Override
    public void byManaType(String type, Comparison comparison, int quantity) throws IllegalArgumentException {
      if (type == null) {
        throw new IllegalArgumentException("Given mana type can't be null!");
      }
      else if (comparison == null) {
        throw new IllegalArgumentException("Given comparison can't be null!");
      }
      else if (!manaTypes.contains(type)) {
        throw new IllegalArgumentException("Given mana type is not contained in the CDDB!");
      }
      manaTypeParams.add(new Triple<>(type, comparison, quantity));
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * name parameters entered so far.
     * @return part of this CardQuery dealing with name and text parameters as a StringBuilder
     */
    private StringBuilder buildNameQuery() {
      return buildGenericCardQuery(nameParams, "name");
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * text parameters entered so far.
     * @return part of this CardQuery dealing with name and text parameters as a StringBuilder
     */
    private StringBuilder buildTextQuery() {
      return buildGenericCardQuery(textParams, "text");
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet given set
     * of parameters, where the given set of parameters relate to a "core" part of the card - name
     * or text - tat are independent of printing or card type.
     * @param params set of parameters to draw from
     * @param conditionalColumn column in Card table to compare info against
     * @return part of this CardQuery dealing with color identity parameters as a StringBuilder
     */
    private StringBuilder buildGenericCardQuery(Map<SearchOption, SortedSet<String>> params, String conditionalColumn) {
      String table = "Card";
      String[] returnColumns = new String[]{"name"};
      String innerQueryColumn = "name";
      return buildGenericQuery(params, table, returnColumns, conditionalColumn, innerQueryColumn, false);
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * color identities parameters entered so far.
     * @return part of this CardQuery dealing with color identity parameters as a StringBuilder
     */
    private StringBuilder buildColorIdentityQuery() {
      return buildGenericColorQuery(colorParams, "Color");
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * color parameters entered so far.
     * @return part of this CardQuery dealing with color parameters as a StringBuilder
     */
    private StringBuilder buildColorQuery() {
      return buildGenericColorQuery(colorIdentityParams, "ColorIdentity");
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet given set
     * of parameters, where the given set of parameters relate to some part of a card's color or
     * color identity.
     * @param table table to compare info to
     * @return part of this CardQuery dealing with color identity parameters as a StringBuilder
     */
    private StringBuilder buildGenericColorQuery(Map<SearchOption, SortedSet<String>> params, String table) {
      String[] returnColumns = new String[]{"card_name"};
      String conditionalColumn = "color";
      String innerQueryColumn = "card_name";
      return buildGenericQuery(params, table, returnColumns, conditionalColumn, innerQueryColumn, true);
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * supertype parameters entered so far.
     * @return part of this CardQuery dealing with supertype parameters as a StringBuilder
     */
    private StringBuilder buildSupertypeQuery() {
      return buildGenericTypeQuery(supertypeParams, "Supertype");
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * text parameters entered so far.
     * @return part of this CardQuery dealing with type parameters as a StringBuilder
     */
    private StringBuilder buildTypeQuery() {
      return buildGenericTypeQuery(typeParams, "Type");
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * subtype parameters entered so far.
     * @return part of this CardQuery dealing with subtype parameters as a StringBuilder
     */
    private StringBuilder buildSubtypeQuery() {
      return buildGenericTypeQuery(subtypeParams, "Subtype");
    }

    /**
     * Builds a query for a given set of type parameters, as per the given table to use in query.
     * Type of types are supertypes, types, and subtypes.
     * @param params type params to use in query
     * @param table table to draw type info from
     * @return part of this CardQuery dealing with given type parameters as a StringBuilder
     */
    private StringBuilder buildGenericTypeQuery(Map<SearchOption, SortedSet<String>> params, String table) {
      String[] returnColumns = new String[]{"card_name"};
      String conditionalColumn = "type";
      String innerQueryColumn = "card_name";
      return buildGenericQuery(params, table, returnColumns, conditionalColumn, innerQueryColumn, true);
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * expansion parameters entered so far.
     * @return part of this CardQuery dealing with expansion parameters as a StringBuilder
     */
    private StringBuilder buildExpansionQuery() {
      String table = "CardExpansion";
      String conditionalColumn = "expansion";
      return buildGenericCardPrintingQuery(setParams, table, conditionalColumn, true);
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * artist parameters entered so far.
     * @return part of this CardQuery dealing with artist parameters as a StringBuilder
     */
    private StringBuilder buildArtistQuery() {
      String table = "Artist";
      String conditionalColumn = "artist";
      return buildGenericCardPrintingQuery(artistParams, table, conditionalColumn, true);
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * rarity parameters entered so far.
     * @return part of this CardQuery dealing with rarity parameters as a StringBuilder
     */
    private StringBuilder buildRarityQuery() {
      String table = "CardExpansion";
      String conditionalColumn = "rarity";
      return buildGenericCardPrintingQuery(rarityParams, table, conditionalColumn, true);
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * flavor text parameters entered so far.
     * @return part of this CardQuery dealing with flavor text parameters as a StringBuilder
     */
    private StringBuilder buildFlavorTextQuery() {
      String table = "CardExpansion";
      String conditionalColumn = "flavor_text";
      return buildGenericCardPrintingQuery(flavorTextParams, table, conditionalColumn, false);
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * a given set of parameters related to a card's printing - such as artist, rarity, etc.
     * @return part of this CardQuery dealing with a set of given card printing parameters
     * as a StringBuilder
     */
    private StringBuilder buildGenericCardPrintingQuery(Map<SearchOption, SortedSet<String>> params,
        String table, String conditionalColumn, boolean inOrLike) {
      String[] returnColumns = new String[]{"card_name", "expansion", "number"};
      String innerQueryColumn = "card_name";
      return buildGenericQuery(params, table, returnColumns, conditionalColumn, innerQueryColumn,
          inOrLike);
    }

    /**
     * Builds a part of this {@link CardQuery} concered with querying cards that meet the given
     * set of parameters, as per given query specific info
     * @param params set of parameters to use in this query
     * @param table table to draw info from
     * @param returnColumns columns to return from the query
     * @param conditionalColumn column in given table to check parameters against
     * @param innerQueryColumn name of column to use for inner "MustInclude" query, for returning
     * and grouping
     * @param inOrLike whether to compare given parameters in form of "IN (x, y, ..., z)" or
     * "(x LIKE "%a%" AND x LIKE "%b%" AND ... AND x LIKE "%y%")
     * @return part of this CardQuery built for the given parameters and query specific info as
     * a StringBuilder
     */
    private StringBuilder buildGenericQuery(Map<SearchOption, SortedSet<String>> params,
        String table, String[] returnColumns, String conditionalColumn, String innerQueryColumn,
        boolean inOrLike) {

      if (params.isEmpty()) {
        return new StringBuilder();
      }
      else if (table == null || table.isEmpty()) {
        throw new IllegalArgumentException("Given table can't be null or empty!");
      }

      StringBuilder completeQuery = new StringBuilder();
      boolean mergeCondAsWhere = true;

      completeQuery.append("SELECT ");
      completeQuery.append(stringArrayToStringList(returnColumns, false));
      completeQuery.append(String.format(" FROM %s", table));

      if (params.containsKey(SearchOption.Disallow)) {
        Set<String> disallowParams = params.get(SearchOption.Disallow);

        StringBuilder conditionals = inOrLike ?
            stringCollectionToStringInList(conditionalColumn, disallowParams, true,
                mergeCondAsWhere, false, innerQueryColumn) :
            stringCollectionToStringLikeList(conditionalColumn, disallowParams, true,
                mergeCondAsWhere, false);

        completeQuery.append(conditionals);
        mergeCondAsWhere = false;
      }

      boolean hasMustInclude = params.containsKey(SearchOption.MustInclude);
      boolean hasOneOf = params.containsKey(SearchOption.OneOf);
      if ((inOrLike && hasMustInclude) || hasOneOf) {
        Set<String> oneOfParams = new TreeSet<>();
        if (inOrLike && hasMustInclude) {
          oneOfParams.addAll(params.get(SearchOption.MustInclude));
        }

        if (hasOneOf) {
          oneOfParams.addAll(params.get(SearchOption.OneOf));
        }

        StringBuilder conditionals = inOrLike ?
            stringCollectionToStringInList(conditionalColumn, oneOfParams, false,
                mergeCondAsWhere, true, "") :
            stringCollectionToStringLikeList(conditionalColumn, oneOfParams, false,
                mergeCondAsWhere, true);

        completeQuery.append(conditionals);
        mergeCondAsWhere = false;
      }

      if (hasMustInclude) {
        Set<String> mustIncludeParams = params.get(SearchOption.MustInclude);
        if (inOrLike) {
          String mergeCond = mergeCondAsWhere ? "WHERE" : "AND";
          completeQuery.append(String.format(" %s %s IN (", mergeCond, innerQueryColumn));

          StringBuilder conditionals = stringCollectionToStringInList(conditionalColumn, mustIncludeParams,
                  false, true, false, innerQueryColumn);

          String mustIncludeQuery =
              String.format("SELECT %s FROM %s%s",
                  innerQueryColumn, table,
                  conditionals.toString());

          completeQuery.append(mustIncludeQuery);
          completeQuery.append(")");
        }
        else {
          completeQuery.append(stringCollectionToStringLikeList(conditionalColumn, mustIncludeParams,
              false, mergeCondAsWhere, false));
        }
      }

      return completeQuery;
    }

    /**
     * Turns the given collection of Strings into a "in" list formatted for SQL in the form of
     * "WHERE/AND columnName IN ('x', 'y', ...,'z').
     * @param columnName column name to compare against
     * @param strings collection of Strings to use as parameters to compare column name against
     * @param includeNot whether or not to include "NOT" before "IN", "IN" --> "NOT IN"
     * @param whereAsMergeCond whether to use "WHERE" or "AND"
     * @param optionalOrRequired whether column being compared meet one or all the parameters, if
     * required (false) will add condition "GROUP BY columnName HAVING
     * COUNT(DISTINCT(groupingColumn)) = x" where x is the number of given parameters
     * @param requiredGroupingColumn column to group results by if {@param requiredGroupingColumn}
     * is false
     * @return StringBuilder in form describe above, as per given parameters
     */
    private StringBuilder stringCollectionToStringInList(String columnName, Collection<String> strings,
        boolean includeNot, boolean whereAsMergeCond, boolean optionalOrRequired,
        String requiredGroupingColumn) {
      String mergeCond = whereAsMergeCond ? "WHERE" : "AND";
      String includeNotFormat = includeNot ? " NOT" : "";
      StringBuilder toReturn = new StringBuilder(String.format(" %s %s%s IN (%s)",
          mergeCond, columnName, includeNotFormat,
          stringCollectionToStringList(strings, true)));

      if (!optionalOrRequired && !includeNot) {
        int numOfParams = strings.size();
        toReturn.append(String.format(" GROUP BY %s HAVING COUNT(DISTINCT(%s)) = %d",
            requiredGroupingColumn, columnName, numOfParams));
      }
      return toReturn;
    }

    /**
     * Turns the given collection of Strings into a "like" list formatted for SQL in the form of
     * "WHERE/AND (columnName LIKE '%x%' AND/OR ... AND/OR columnName LKE '%z%').
     * @param columnName column name to compare against
     * @param strings collection of Strings to use as parameters to compare column name against
     * @param includeNot whether or not to include "NOT" before every "LIKE", "LIKE" --> "NOT LIKE"
     * @param whereAsMergeCond whether to use "WHERE" or "AND"
     * @param optionalOrRequired whether to use "OR" (optional) or "AND" (required), should column
     * being compared meet one or all the parameters
     * @return StringBuilder in form describe above, as per given parameters
     */
    private StringBuilder stringCollectionToStringLikeList(String columnName, Collection<String> strings,
        boolean includeNot, boolean whereAsMergeCond, boolean optionalOrRequired) {
      String mergeCond = whereAsMergeCond ? "WHERE" : "AND";
      StringBuilder toReturn = new StringBuilder(String.format(" %s (", mergeCond));

      String includeNotFormat = includeNot ? " NOT" : "";
      String notLikeFormat = "%s" + includeNotFormat + " LIKE '%%%s%%'";

      String optionalOrRequiredFormat = optionalOrRequired ? "OR" : "AND";

      boolean firstParam = true;
      for (String disallowParam : strings) {
        if (firstParam) {
          firstParam = false;
        }
        else {

          toReturn.append(String.format(" %s ", optionalOrRequiredFormat));
        }
        toReturn.append(String.format(notLikeFormat, columnName, disallowParam));
      }
      toReturn.append(")");
      return toReturn;
    }

    /**
     * Turns given array of Strings into a single StringBuilder "list" in the form of "x, y, ..., z"
     * or "'x', 'y', ...,'z'" if {@param includeQuote} is true.
     * @param strings array of strings to work with
     * @param includeQuote whether or not to include quotes around each String
     * @return "list" of given Strings as a StringBuilder
     */
    private StringBuilder stringArrayToStringList(String[] strings, boolean includeQuote) {
      return stringCollectionToStringList(Arrays.asList(strings), includeQuote);
    }

    /**
     * Turns given collection of Strings into a single StringBuilder "list" in the form of "x, y, ..., z"
     * or "'x', 'y', ...,'z'" if {@param includeQuote} is true.
     * @param strings collection of strings to work with
     * @param includeQuotes whether or not to include quotes around each String
     * @return "list" of given Strings as a StringBuilder
     */
    private StringBuilder stringCollectionToStringList(Collection<String> strings, boolean includeQuotes) {
      if (strings == null) {
        throw new IllegalArgumentException("Given string array can't be null!");
      }
      StringBuilder toReturn = new StringBuilder();

      boolean first = true;
      for (String string : strings) {
        if (first) {
          first = false;
        }
        else {
          toReturn.append(", ");
        }
        String toAppend = includeQuotes ? String.format("'%s'", string) : string;
        toReturn.append(toAppend);
      }

      return toReturn;
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * stat parameters entered so far.
     * @return part of this CardQuery dealing with color parameters as a StringBuilder
     */
    private StringBuilder buildStatQuery() {
      StringBuilder completeQuery = new StringBuilder();
      StringBuilder conditions = new StringBuilder();
      int i = 0;
      String startingTable = "";
      String startingTableCardNameColumn = "";
      for (Triple<Stat, Comparison, Integer> conditional : statParams) {
        Stat curStat = conditional.getA();
        String comparison = conditional.getB().getValue();
        String comparisonQuantity = conditional.getC().toString();
        String curTable = statTableNames.get(curStat);
        String curCardNameColumn = statCardNameColumnNames.get(curStat);
        String curComparisonColumn = statValueColumnNames.get(curStat);
        String cond = "WHERE";
        String curTableShorthand = "t" + i;
        if (i == 0) {
          completeQuery.append(String.format("SELECT %s.%s FROM %s %s",
              curTableShorthand, curCardNameColumn, curTable, curTableShorthand));
          startingTable = curTableShorthand;
          startingTableCardNameColumn = curCardNameColumn;
        }
        else {
          completeQuery.append(String.format(" JOIN %s %s ON %s.%s = %s.%s",
              curTable, curTableShorthand, startingTable, startingTableCardNameColumn,
              curTableShorthand, curCardNameColumn));
          cond = "AND";
        }

        conditions.append(String.format(" %s %s.%s %s %s",
            cond, curTableShorthand, curComparisonColumn, comparison, comparisonQuantity));
        i++;
      }
      completeQuery.append(conditions);
      return completeQuery;
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * mana symbol parameters entered so far.
     * @return part of this CardQuery dealing with mana symbol parameters as a StringBuilder
     */
    private StringBuilder buildManaTypeQuery() {
      StringBuilder completeQuery = new StringBuilder();
      StringBuilder conditions = new StringBuilder();
      String table = "Mana";
      String categoryColumn = "mana_type";
      String cardNameColumn = "card_name";
      String comparisonColumn = "quantity";
      int i = 0;
      String startingTable = "";
      for (Triple<String, Comparison, Integer> conditional : manaTypeParams) {
        String curTable = "t" + i;
        String cond;
        if (i == 0) {
          completeQuery.append(String.format("SELECT %s.%s FROM %s %s",
              curTable, cardNameColumn, table, curTable));
          cond = "WHERE";
          startingTable = curTable;
        }
        else {
          completeQuery.append(String.format(" JOIN %s %s ON %s.%s = %s.%s",
              table, curTable, startingTable, cardNameColumn, curTable, cardNameColumn));
          cond = "AND";
        }

        String categoryType = conditional.getA();
        String comparison = conditional.getB().getValue();
        String comparisonQuantity = conditional.getC().toString();
        conditions.append(String.format(" %s %s.%s = '%s'",
            cond, curTable, categoryColumn, categoryType));
        conditions.append(String.format(" AND %s.%s %s %s",
            curTable, comparisonColumn, comparison, comparisonQuantity));
        i++;
      }
      completeQuery.append(conditions);
      return completeQuery;
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet the
     * stat vs stat parameters entered so far.
     * @return part of this CardQuery dealing with color parameters as a StringBuilder
     */
    private StringBuilder buildStatVersusStatQuery() {
      StringBuilder completeQuery = new StringBuilder();
      StringBuilder conditions = new StringBuilder();
      int i = 0;
      String startingTable = "";
      String startingTableCardNameColumn = "";
      String cond = "WHERE";
      for (Triple<Stat, Comparison, Stat> conditional : statVersusStatParams) {
        Stat firstStat = conditional.getA();
        String comparison = conditional.getB().getValue();
        Stat secondStat = conditional.getC();

        String firstTableShorthand = "t" + i;
        String firstTable = statTableNames.get(firstStat);
        String firstTableValueColumn = statValueColumnNames.get(firstStat);
        String firstTableCardNameColumn = statCardNameColumnNames.get(firstStat);

        String secondTableShorthand = "t" + (i + 1);
        String secondTable = statTableNames.get(secondStat);
        String secondTableValueColumn = statValueColumnNames.get(secondStat);
        String secondTableCardNameColumn = statCardNameColumnNames.get(secondStat);;

        for (int j = 0; j < 2; j++) {
          String curTableShorthand;
          String curTable;
          String curCardNameColumn;
          if (j == 0) {
            curTableShorthand = firstTableShorthand;
            curTable = firstTable;
            curCardNameColumn = firstTableCardNameColumn;
          }
          else {
            curTableShorthand = secondTableShorthand;
            curTable = secondTable;
            curCardNameColumn = secondTableCardNameColumn;
          }

          if (i == 0) {
            startingTable = firstTableShorthand;
            completeQuery.append(String.format("SELECT %s.%s FROM %s %s",
                startingTable, curCardNameColumn, curTable, startingTable));
            startingTableCardNameColumn = curCardNameColumn;
          }
          else {
            completeQuery.append(String.format(" JOIN %s %s ON %s.%s = %s.%s",
                curTable, curTableShorthand,
                startingTable, startingTableCardNameColumn, curTableShorthand, curCardNameColumn));

            if (i > 1) {
              cond = "AND";
            }
          }
          i++;
        }

        // Compare
        conditions.append(String.format(" %s %s.%s %s %s.%s",
            cond, firstTableShorthand, firstTableValueColumn, comparison,
            secondTableShorthand, secondTableValueColumn));
      }
      return completeQuery.append(conditions);
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet parameters
     * that deal with parts specific to a card name, text, cmc, etc.
     * @return part of this CardQuery dealing with card parameters as a StringBuilder
     */
    private StringBuilder buildMergedCardQuery() {
      StringBuilder[] queries = new StringBuilder[]{buildNameQuery(), buildTextQuery(),
      buildTypeQuery(), buildSubtypeQuery(), buildColorIdentityQuery(), buildColorQuery(),
      buildSupertypeQuery(), buildManaTypeQuery(), buildStatQuery(), buildStatVersusStatQuery()};
      return buildGenericMergedQuery(queries);
    }

    /**
     * Builds the part of this {@link CardQuery} concerned with querying cards that meet parameters
     * that deal with parts specific to a card artists, expansion, etc.
     * @return part of this CardQuery dealing with card printing parameters as a StringBuilder
     */
    private StringBuilder buildMergedCardPrintingQuery() {
      StringBuilder[] queries = new StringBuilder[]{buildArtistQuery(), buildRarityQuery(),
          buildFlavorTextQuery(), buildExpansionQuery()};
      return buildGenericMergedQuery(queries);
    }

    /**
     * Given an array of StringBuilders, or subqueries), merges them into a single StringBuilder
     * in the order they are given, given a StringBuilder included is not empty. Adds " INTERSECT "
     * between each StringBuilder
     * @param queries queries to merge
     * @return StringBuilder of merged queries
     */
    private StringBuilder buildGenericMergedQuery(StringBuilder[] queries) {
      if (queries == null) {
        throw new IllegalArgumentException("Given array of queries can't be null!");
      }
      StringBuilder completeQuery = new StringBuilder();
      for (StringBuilder query : queries) {
        if (!isStringBuilderEmpty(completeQuery) && !isStringBuilderEmpty(query)) {
          completeQuery.append(" INTERSECT ");
        }
        completeQuery.append(query);
      }
      return completeQuery;
    }

    @Override
    public String asQuery() {
      StringBuilder completeQuery = new StringBuilder("SELECT card_name, expansion, number "
          + "FROM CardExpansion");

      String mergeCond = "WHERE";

      // Get card specific queries
      StringBuilder mergedCardQueries = buildMergedCardQuery();
      if (!isStringBuilderEmpty(mergedCardQueries)) {
        completeQuery.append(String.format(" %s card_name IN (", mergeCond));
        completeQuery.append(mergedCardQueries);
        completeQuery.append(")");
        mergeCond = "AND";
      }

      //Get card printing specific queries
      StringBuilder mergedCardPrintingQueries = buildMergedCardPrintingQuery();
      if (!isStringBuilderEmpty(mergedCardPrintingQueries)) {
        completeQuery.append(String.format(" %s (card_name, expansion, number) IN (", mergeCond));
        completeQuery.append(mergedCardPrintingQueries);
        completeQuery.append(")");
      }

      return completeQuery.toString();
    }

    @Override
    public void clear() {
      nameParams.clear();
      textParams.clear();
      colorParams.clear();
      colorIdentityParams.clear();
      supertypeParams.clear();
      typeParams.clear();
      subtypeParams.clear();
      setParams.clear();
      artistParams.clear();
      flavorTextParams .clear();
      statParams.clear();
      statVersusStatParams.clear();
      rarityParams.clear();
      manaTypeParams.clear();
    }

    /**
     * Returns if the given StringBuilder is empty.
     * @param toCheck StringBuilder to check
     * @return if given StringBuilder is empty
     */
    private boolean isStringBuilderEmpty(StringBuilder toCheck) {
      return toCheck.length() == 0;
    }

    /**
     * Checks if a given word - defined as a parameter argument that is being used to check text
     * that is "like" it, not strictly equal to it. Should be non-null, non-empty, and not contain
     * any spaces.
     * @param word word to check
     * @throws IllegalArgumentException if given word is either non-null, non-empty, or contains a
     * space
     */
    private void validWord(String word) {
      if (word == null) {
        throw new IllegalArgumentException("Given word can't be null!");
      }
      else if (word.contains(" ")) {
        throw new IllegalArgumentException("Given word can't contain spaces!");
      }
      else if (word.isEmpty()) {
        throw new IllegalArgumentException("Given word can't be empty!");
      }
    }
  }

  /**
   * Default implementation of {@link InformativeDeckInstance}, a container that identifies a
   * DeckInstance in addition to as the categories, cards, card quantities, and card printings that
   * comprise it - and information specific to included Cards and CardPrintings.
   */
  private class DefaultInformativeDeckInstance implements InformativeDeckInstance {

    private final DeckInstance deckInstance;

    /**
     * The sorted set of {@link Card}s that make up this {@link InformativeDeckInstance},
     * without quantities.
     */
    private final SortedSet<Card> cards;

    /**
     * Mapping of categories in this {@link InformativeDeckInstance} to the {@link Card}s
     * that are apart of them.
     */
    private final Map<String, SortedSet<Card>> categoryCardContents;

    /**
     * Mapping of specific {@link InformativeCardPrinting}s to their quantities in this
     * {@link InformativeDeckInstance}.
     */
    private final Map<InformativeCardPrinting, Integer> informativeCardPrintingQuantities;

    /**
     * Mapping of card names to associated {@link Card}s.
     */
    private final Map<String, Card> cardNameToCard;

    /**
     * Builds a {@link InformativeDeckInstance} from the given {@link DeckInstance} by retrieving
     * information specific to the Cards and CardPrintings stored in the DeckInstance from the
     * CDDB, while preserving the relationships given in the DeckInstance.
     * @param deckInstance DeckInstance to retrieve info for
     * @throws IllegalArgumentException if the given DeckInstance is null
     * @throws SQLException if there is an error retrieving info from the CDDB
     */
    private DefaultInformativeDeckInstance(DeckInstance deckInstance) throws SQLException {
      if (deckInstance == null) {
        throw new IllegalArgumentException("Given deck instance can't be null!");
      }

      this.deckInstance = deckInstance;

      // Get card names
      Map<String, Map<String, Set<String>>> cardToExpansionToNumbers = new HashMap<>();
      for (String cardName : deckInstance.getCardNames()) {
        cardToExpansionToNumbers.put(cardName, new HashMap<>());
      }

      // Add each card printing info to associated card
      for (CardPrinting cardPrinting : deckInstance.getCardPrintings()) {
        String cardPrintingName = cardPrinting.getCardName();
        if (!cardToExpansionToNumbers.containsKey(cardPrintingName)) {
          throw new IllegalArgumentException("");
        }

        String cardPrintingExpansion = cardPrinting.getCardExpansion();
        Map<String, Set<String>> expansionToNumbers = cardToExpansionToNumbers.get(cardPrintingName);
        if (!expansionToNumbers.containsKey(cardPrintingExpansion)) {
          expansionToNumbers.put(cardPrintingExpansion, new HashSet<>());
        }
        expansionToNumbers.get(cardPrintingExpansion).add(cardPrinting.getIdentifyingNumber());
      }

      // Get actual card info
      cardNameToCard = new HashMap<>();
      for (String cardName : cardToExpansionToNumbers.keySet()) {
        cardNameToCard.put(cardName, new DefaultCard(cardName, cardToExpansionToNumbers.get(cardName)));
      }

      // Reassociate card info with categories
      Map<String, SortedSet<String>> cardCategories = deckInstance.getCardNamesByCategory();
      Map<String, SortedSet<Card>> categoryCardContents = new HashMap<>();

      for (String category : cardCategories.keySet()) {
        SortedSet<Card> informativeCardsInCategory = new TreeSet<>();
        for (String cardInCategory : cardCategories.get(category)) {
          informativeCardsInCategory.add(cardNameToCard.get(cardInCategory));
        }
        categoryCardContents.put(category, informativeCardsInCategory);
      }

      // Reassociate card printings with quantities
      Map<CardPrinting, Integer> cardPrintingQuantities = deckInstance.getCardPrintingQuantities();
      Map<InformativeCardPrinting, Integer> informativeCardPrintingQuantities = new HashMap<>();
      for (CardPrinting cardPrinting : cardPrintingQuantities.keySet()) {
        String cardPrintingName = cardPrinting.getCardName();
        Card associatedCard = cardNameToCard.get(cardPrintingName);
        Set<InformativeCardPrinting> associatedCardPrintings = associatedCard.getCardPrintings();
        if (associatedCardPrintings.contains(cardPrinting)) {
          for (InformativeCardPrinting informativeCardPrinting : associatedCardPrintings) {
            if (cardPrinting.equals(informativeCardPrinting)) {
              informativeCardPrintingQuantities.put(informativeCardPrinting,
                  informativeCardPrintingQuantities.get(cardPrinting));
            }
          }
        }
        else {
          throw new IllegalArgumentException("");
        }
      }

      SortedSet<InformativeCardPrinting> cardPrintingsFromCard = new TreeSet<>();
      SortedSet<Card> cards = new TreeSet<>();
      for (Set<Card> categoryCardContent : categoryCardContents.values()) {
        cards.addAll(categoryCardContent);
        for (Card card : categoryCardContent) {
          cardPrintingsFromCard.addAll(card.getCardPrintings());
          String cardName = card.getName();
          if (cardNameToCard.containsKey(cardName)) {
            throw new IllegalArgumentException("");
          }
          cardNameToCard.put(cardName, card);
        }
      }

      // Cumulative set of card printings included from each given card should equal the card
      // printings given in map of card printings to quantities
      SortedSet<InformativeCardPrinting> cardPrintingsFromQuantity = new TreeSet<>(
          informativeCardPrintingQuantities.keySet());
      if (!cardPrintingsFromCard.equals(cardPrintingsFromQuantity)) {
        throw new IllegalArgumentException("Given");
      }

      // Check for nonpositive card printing quantities
      for (int quantity : informativeCardPrintingQuantities.values()) {
        if (quantity < 1) {
          throw new IllegalArgumentException("Given mapping of card printings to quantities contains"
              + "a non-positive quantity of a card!");
        }
      }

      this.cards = Collections.unmodifiableSortedSet(cards);
      this.categoryCardContents = Collections.unmodifiableMap(categoryCardContents);
      this.informativeCardPrintingQuantities = Collections.unmodifiableMap(
          informativeCardPrintingQuantities);
    }

    @Override
    public int getParentDeckID() {
      return deckInstance.getParentDeckID();
    }

    @Override
    public LocalDateTime getCreationInfo() {
      return deckInstance.getCreationInfo();
    }

    @Override
    public Map<String, Integer> getCardNameQuantities() {
      return deckInstance.getCardNameQuantities();
    }

    @Override
    public Map<String, SortedSet<String>> getCardNamesByCategory() {
      return deckInstance.getCardNamesByCategory();
    }

    @Override
    public SortedSet<CardPrinting> getCardPrintings() {
      return deckInstance.getCardPrintings();
    }

    @Override
    public Map<Card, Integer> getCardQuantities() {
      Map<Card, Integer> toReturn = new HashMap<>();

      for (Card card : cards) {
        toReturn.put(card, 0);
      }

      for (InformativeCardPrinting cardPrinting : informativeCardPrintingQuantities.keySet()) {
        Card associatedCard = cardNameToCard.get(cardPrinting.getCardName());
        if (associatedCard.getCardPrintings().contains(cardPrinting)) {
          int newQuantity = informativeCardPrintingQuantities.get(cardPrinting) + toReturn.get(associatedCard);
          toReturn.put(associatedCard, newQuantity);
        }
        else {
          // Should never be reached
          throw new IllegalStateException(String.format("Card printing %s, %s, %s was not included "
                  + "in the set of card printings making up the card %s!",
              cardPrinting.getCardName(), cardPrinting.getCardExpansion(),
              cardPrinting.getIdentifyingNumber(), associatedCard.getName()));
        }
      }
      return toReturn;
    }

    @Override
    public Map<String, SortedSet<Card>> getCardsByCategory() {
      return categoryCardContents;
    }

    @Override
    public SortedSet<InformativeCardPrinting> getInformativeCardPrintings() {
      return Collections.unmodifiableSortedSet(new TreeSet<>(informativeCardPrintingQuantities.keySet()));
    }

    @Override
    public SortedSet<String> getCategories() {
      return deckInstance.getCategories();
    }

    @Override
    public SortedSet<String> getCardNames() {
      return deckInstance.getCardNames();
    }

    @Override
    public Map<CardPrinting, Integer> getCardPrintingQuantities() {
      return deckInstance.getCardPrintingQuantities();
    }

    @Override
    public SortedSet<Card> getCards() {
      return cards;
    }

    @Override
    public Map<InformativeCardPrinting, Integer> getInformativeCardPrintingQuantities() {
      return informativeCardPrintingQuantities;
    }

    @Override
    public boolean equals(Object other) {
      return deckInstance.equals(other);
    }

    @Override
    public int hashCode() {
      return Objects.hash(getParentDeckID(), getCreationInfo());
    }

    @Override
    public int compareTo(DeckInstance other) throws IllegalArgumentException {
      return deckInstance.compareTo(other);
    }
  }
}
