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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import value_objects.card.Card;
import value_objects.card.relationship.CardRelationship;
import value_objects.card.relationship.DefaultCardRelationship;
import value_objects.card_printing.CardPrintingInfo;
import value_objects.card_printing.DefaultCardPrintingInfo;
import value_objects.query.CardQuery;
import value_objects.card_printing.CardPrinting;
import value_objects.card_printing.DefaultCardPrinting;
import value_objects.deck.Deck;
import value_objects.deck.DefaultDeck;
import value_objects.deck_instance.DeckInstance;
import value_objects.deck_instance.DefaultDeckInstance;
import value_objects.query.Comparison;
import value_objects.query.Stat;
import value_objects.utility.Pair;
import value_objects.utility.Triple;

/**
 * Default class to use to access the Card and Deck Database (CDDB) for querying cards and reading,
 * updating, and deleting decks. In addition to accessing enumerated info about card types.
 */
public class DefaultDatabaseChannel extends DatabasePort implements DeckChannel,
    CardChannel {

  private final SortedSet<String> types;

  private final SortedSet<String> rarities;

  private final SortedSet<String> colors;

  private final SortedSet<String> manaTypes;

  private final SortedSet<String> artists;

  private final SortedSet<String> sets;

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
    types = retrieveColumnInfo("Type", "type", connection);
    rarities = retrieveColumnInfo("CardExpansion", "rarity", connection);
    colors = retrieveColumnInfo("Color", "color", connection);
    manaTypes = retrieveColumnInfo("Mana", "mana_type", connection);
    blocks = retrieveColumnInfo("Block", "block", connection);
    artists = retrieveColumnInfo("CardExpansion", "artist", connection);
    sets = retrieveColumnInfo("Expansion", "expansion", connection);
    disconnect(connection);
  }

  @Override
  public HashMap<Integer, String> getDecks() throws SQLException {
    String deckQuery = "SELECT id, name FROM Deck";
    try (Connection connection = connect();
        PreparedStatement preparedStatement = connection.prepareStatement(deckQuery);
        ResultSet result = preparedStatement.executeQuery()){
      HashMap<Integer, String> decksInfo = new HashMap<>();
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

    // Query for info of all deck instances related to current deck
    ResultSet deckInstancesInfo;
    String deckInstanceInfoQuery = "SELECT * FROM DeckInstance WHERE deck_id=?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(deckInstanceInfoQuery);) {
      preparedStatement.setInt(1, deckID);
      deckInstancesInfo = preparedStatement.executeQuery();
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for info of deck instances"
          + " related to deck %d!", deckID));
    }

    // Get datetimes from deck instances, retrieve as TimeStamp --> LocalDateTime
    List<LocalDateTime> deckInstanceKeys = new ArrayList<>();
    try {
      while (deckInstancesInfo.next()) {
        Timestamp timestamp = deckInstancesInfo.getTimestamp("creation");
        LocalDateTime toAdd = timestamp.toLocalDateTime();
        deckInstanceKeys.add(toAdd);
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          "\nFailed to query for deck instance creations date & times!");
    }
    finally {
      closeResultSet(deckInstancesInfo);
    }

    // Build deck instances,
    SortedSet<DeckInstance> deckInstances = new TreeSet<>();
    Map<String, Set<String>> categoryContents = new HashMap<>();
    Map<CardPrinting, Integer> cardPrintingQuantities = new HashMap<>();
    for (LocalDateTime creation : deckInstanceKeys) {
      // Get categories
      List<String> categories = new ArrayList<>();
      ResultSet categoriesResult= null;
      String categoriesQuery = "SELECT * FROM DeckInstCategory WHERE deck_id=? "
          + "AND deck_inst_creation=?";
      try (PreparedStatement preparedStatement = connection.prepareStatement(categoriesQuery);) {
        preparedStatement.setInt(1, deckID);
        preparedStatement.setTimestamp(2, Timestamp.valueOf(creation));
        categoriesResult = preparedStatement.executeQuery();
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("\nFailed to query for categories for deck with"
            + " ID %d!", deckID));
      }
      finally {
        closeResultSet(categoriesResult);
      }

      // Get category cards
      try {
        while (categoriesResult.next()) {
          String currentCategory = categoriesResult.getString("category");
          String categoryQuery = "SELECT * FROM DeckInstCardCategory WHERE deck_id=? "
              + "AND deck_inst_creation=? AND category=?";
          ResultSet cardsInCategory = null;
          try (PreparedStatement preparedStatement = connection.prepareStatement(categoryQuery);) {
            preparedStatement.setInt(1, deckID);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(creation));
            preparedStatement.setString(3, currentCategory);
            cardsInCategory = preparedStatement.executeQuery();

            Set<String> cardsToAdd = new HashSet<>();
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

    String deckName = "";
    try {
      deckName = deckInstancesInfo.getString("name");
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for name of deck with id %d!", deckID));
    }

    String deckDesp = "";
    try {
      deckDesp = deckInstancesInfo.getString("desp");
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for description of deck with id %d!", deckID));
    }

    disconnect(connection);

    Deck toReturn = new DefaultDeck(deckID, deckName, deckDesp, deckInstances);
    return toReturn;
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
      updateDeck(deckInstance);
    }
  }

  @Override
  public void updateDeck(DeckInstance deck) throws IllegalArgumentException, SQLException {
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
          String.format("\nFailed to add deck instance %d, %s!", deck,
          creationInfo.toString()));
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
    Set<String> cards = deck.getCards();
    for (String card : cards) {
      insertStatement = "INSERT INTO DeckInstCard(deck_id, deck_inst_creation, card_name) VALUES (?,?,?)";
      try (PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);) {
        preparedStatement.setInt(1, deckID);
        preparedStatement.setTimestamp(2, creationTimestamp);
        preparedStatement.setString(3, card);
        preparedStatement.executeUpdate();
      }
      catch (SQLException e){
        throw new SQLException(e.getMessage() + String.format("\nFailed to add card %s for deck instance %d, "
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
            + "instance %d, %s!", cardName, expansion, identifier, cardPrinting, deck,
            creationInfo.toString()));
      }
    }

    // Add cards in categories
    Map<String, Set<String>> cardCategories = deck.getCardsByCategory();
    for (String category : cardCategories.keySet()) {
      Set<String> categoryCards = cardCategories.get(category);
      for (String card : categoryCards) {
        insertStatement = "INSERT INTO DeckInstCardCategory(deck_id, deck_inst_creation, "
            + "card_name, cateogry) VALUES (?,?,?,?)";
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
                  + "instance %d, %s!", card, category, deck, creationInfo.toString()));
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
  public CardQuery getQuery() {
    return new DefaultCardQuery();
  }

  @Override
  public SortedSet<Card> queryCards(CardQuery cardQuery) throws IllegalArgumentException, SQLException {
    if (cardQuery == null) {
      throw new IllegalArgumentException("Given cardQuery can't be null!");
    }

    Map<String, Set<String>> cardNameToExpansions = new HashMap<>();
    String query = cardQuery.asQuery();
    try (Connection connection = connect();
    PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet cardQueryResults = preparedStatement.executeQuery();) {

      while (cardQueryResults.next()) {
        String cardName = cardQueryResults.getString("card_name");
        String expansion = cardQueryResults.getString("expansion");
        if (cardNameToExpansions.containsKey(cardName)) {
          cardNameToExpansions.get(cardName).add(expansion);
        }
        else {
          Set<String> value = new TreeSet<>();
          value.add(expansion);
          cardNameToExpansions.put(cardName, value);
        }
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for given card query!"));
    }

    SortedSet<Card> cards = new TreeSet<>();
    for (String cardName : cardNameToExpansions.keySet()) {
      Set<String> expansions = cardNameToExpansions.get(cardName);
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

  public SortedSet<String> getTypes() {
    return types;
  }

  @Override
  public SortedSet<String> getManaTypes() {
    return manaTypes;
  }

  @Override
  public SortedSet<String> getRarityTypes() {
    return rarities;
  }

  @Override
  public SortedSet<String> getColors() {
    return colors;
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
    return blocks;
  }

  @Override
  public SortedSet<String> getArtists(){
    return artists;
  }

  @Override
  public SortedSet<String> getSets() {
    return sets;
  }

  /**
   * Default implementation of the {@link Card} interface, a simple container to hold all the
   * information pertaining to a given card.
   */
  public class DefaultCard implements Card {

    /**
     * Name of the Card as it appears in the CDDB.
     */
    private final String name;

    /**
     *
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
    private final Set<String> supertypes;

    /**
     * Types of the Card as it appears in the CDDB.
     */
    private final Set<String> types;

    /**
     * Subtypes of the Card as it appears in the CDDB.
     */
    private final Set<String> subtypes;

    /**
     * Set of colors making up this Card's colors.
     */
    private final Set<String> colors;

    /**
     * Set of colors making up this Card's color identity.
     */
    private final Set<String> colorIdentity;

    /**
     * Relationship this Card has with other Cards, if any.
     */
    private final CardRelationship relationship;

    /**
     *
     */
    private final SortedSet<CardPrintingInfo> cardPrintings;

    /**
     * Any additional info of the Card as it appears in the CDDB.
     */
    private final Map<String, String> additionalInfo;
    
    protected DefaultCard(String name, Set<String> expansions) throws SQLException {
      if (name == null) {
        throw new IllegalArgumentException("Given name can't be null!");
      }
      else if (expansions == null || expansions.isEmpty()) {
        throw new IllegalArgumentException("Given expansions can't be null nor empty!");
      }

      Connection connection = connect();
      this.name = setName(connection, name);
      this.text = setText(connection, name);
      this.cmc = setCMC(connection);
      this.manaCosts = setManaCosts(connection);
      this.supertypes = setSupertypes(connection);
      this.types = setTypes(connection);
      this.subtypes = setSubtypes(connection);
      this.colors = setColors(connection);
      this.colorIdentity = setColorIdentity(connection);
      this.relationship = setCardRelationship(connection);
      this.additionalInfo = setAdditionalInfo(connection);
      this.cardPrintings = setCardPrintings(connection, expansions);
      disconnect(connection);
    }

    private String setName(Connection connection, String cardName) throws SQLException {
      return getCardInfo(connection,"name", "name", cardName);
    }
    
    private String setText(Connection connection, String cardName) throws SQLException{
      return getCardInfo(connection,"card_text", "card text", cardName);
    }

    private String getCardInfo(Connection connection, String column, String infoType, String cardName) throws SQLException {
      Map<String, String> conditions = new HashMap<>();
      conditions.put("name", cardName);
      Set<String> toReturn = retrieveSingleColumn(connection, "Card", column,
          conditions, true, infoType);
      return singleItemSetToString(toReturn);
    }

    private Set<String> retrieveSingleColumn(Connection connection, String table, String column,
        Map<String, String> conditions, boolean singleResult, String infoType)
        throws SQLException {
      StringBuilder query = new StringBuilder(String.format("SELECT %s FROM %s", column, table));

      boolean singleCondition = true;
      for (String key : conditions.keySet()) {
        String value = conditions.get(key);
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
        Set<String> toReturn = new TreeSet<>();
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
            String.format("Failed to retrieve %s for card %s from database!", infoType, name));
      }
    }

    private Map<String, Integer> setManaCosts(Connection connection) throws SQLException {
      String query = String.format("SELECT mana_type, quantity FROM Mana WHERE card_name = '%s'", name);
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
            String.format("Failed to retrieve mana type info for card %s from database!", name));
      }
    }

    private Set<String> setSupertypes(Connection connection) throws SQLException {
      return retrieveTypeInfo(connection, "supertype");
    }

    private Set<String> setTypes(Connection connection) throws SQLException {
      return retrieveTypeInfo(connection,"type");
    }


    private Set<String> setSubtypes(Connection connection) throws SQLException {
      return retrieveTypeInfo(connection,"subtype");
    }

    private Set<String> retrieveTypeInfo(Connection connection, String type) throws SQLException {
      Map<String, String> conditions = new HashMap<>();
      conditions.put("card_name", name);
      conditions.put("category", type);
      return retrieveSingleColumn(connection,"Type", "type", conditions, false, type);
    }

    private Set<String> setColors(Connection connection) throws SQLException {
      return getColorInfo(connection,"Color", "color");
    }

    private Set<String> setColorIdentity(Connection connection) throws SQLException {
      return getColorInfo(connection,"ColorIdentity", "color identity");
    }

    private Set<String> getColorInfo(Connection connection, String table, String infoType) throws SQLException {
      Map<String, String> conditions = new HashMap<>();
      conditions.put("card_name", name);
      return retrieveSingleColumn(connection, table, "color", conditions, false, infoType);
    }

    private CardRelationship setCardRelationship(Connection connection) throws SQLException {
      SortedSet<String> cardNames = new TreeSet<>();

      String twoCardQuery = String.format("SELECT * FROM TwoCards WHERE card_a = '%s' OR card_b = '%s'", name, name);
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
            String.format("Failed to check for two card relationship info for card %s from database!", name));
      }

      String threeCardQuery = String.format("SELECT * FROM ThreeCards WHERE card_a = '%s' OR card_b = '%s' OR card_c = '%s'", name, name, name);
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
            String.format("Failed to check for three card relationship info for card %s from database!", name));
      }

      return new DefaultCardRelationship();
    }

    private Map<String, String> setAdditionalInfo(Connection connection) throws SQLException {
      // When constructing variables, call after initiating types
      Map<String, String> additionalInfo = new HashMap<>();
      Map<String, String> conditions = new HashMap<>();
      if (types.contains("planeswalker")) {
        String loyalty = "loyalty";
        conditions.put("card_name", name);
        conditions.put("category", loyalty);
        Set<String> result = retrieveSingleColumn(connection, "Stat", "value", conditions, true, "loyalty");
        additionalInfo.put(loyalty, singleItemSetToString(result));
      }
      else if (types.contains("creature") || subtypes.contains("vehicle")) {
        String[] powerToughness = new String[]{"power", "toughness"};
        for (String category : powerToughness) {
          conditions.put("card_name", name);
          conditions.put("category", category);
          Set<String> result = retrieveSingleColumn(connection,"Stat", "value", conditions, true, category);
          additionalInfo.put(category, singleItemSetToString(result));
        }
      }
      return additionalInfo;
    }

    private SortedSet<CardPrintingInfo> setCardPrintings(Connection connection, Set<String> expansions) throws SQLException {
      StringBuilder expansionsIn = new StringBuilder("(");
      boolean afterFirst = false;
      for (String expansion : expansions) {
        if (afterFirst) {
          expansionsIn.append(", ");
        }
        else {
          afterFirst = true;
        }
        expansionsIn.append(String.format("'%s'", expansion));
      }
      expansionsIn.append(")");

      SortedSet<CardPrintingInfo> cardPrintingInfo = new TreeSet<>();
      String query = String.format("SELECT * FROM CardExpansion "
          + "WHERE card_name = '%s' AND expansion IN %s", name, expansionsIn.toString());
      try (PreparedStatement preparedStatement = connection.prepareStatement(query);
          ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          String expansion = resultSet.getString("expansion");
          String number = resultSet.getString("number");
          String artist = resultSet.getString("artist");
          String rarity = resultSet.getString("rarity");
          String flavor_text = resultSet.getString("flavor_text");
          cardPrintingInfo.add(new DefaultCardPrintingInfo(name, expansion, number, artist, flavor_text, rarity));
        }
        return cardPrintingInfo;
      }
      catch (SQLException e) {
        throw new SQLException(e.getMessage() +
            String.format("Failed to retrieve card expansion info for card %s from database!", name));
      }
    }

    private int setCMC(Connection connection) throws SQLException {
      Map<String, String> conditions = new HashMap<>();
      conditions.put("card_name", name);
      conditions.put("category", "cmc");
      Set<String> singleResult = retrieveSingleColumn(connection, "Stat", "base_value", conditions, true, "converted mana cost");
      return Integer.parseInt(singleItemSetToString(singleResult));
    }

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
    public Set<String> getColors() {
      return Collections.unmodifiableSet(colors);
    }

    @Override
    public Set<String> getColorIdentity() {
      return Collections.unmodifiableSet(colorIdentity);
    }

    @Override
    public String getText() {
      return text;
    }

    @Override
    public Set<String> getSupertypes() {
      return Collections.unmodifiableSet(supertypes);
    }

    @Override
    public Set<String> getTypes() {
      return Collections.unmodifiableSet(types);
    }

    @Override
    public Set<String> getSubtypes() {
      return Collections.unmodifiableSet(subtypes);
    }

    @Override
    public SortedSet<CardPrintingInfo> getCardPrintings() {
      return cardPrintings;
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
      return getName().compareTo(other.getName());
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof Card) {
        return name.equals(((Card) other).getName());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
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

  private SortedSet<String> retrieveColumnInfo(String tableName, String columnName,
      Connection connection) throws SQLException {
    if (connection == null || connection.isClosed()) {
      throw new IllegalArgumentException("Given connection can't be null or closed!");
    }

    SortedSet<String> toReturn = new TreeSet<>();
    String query = String.format("SELECT DISTINCT(%s) FROM %s", columnName, tableName);
    try (PreparedStatement preparedStatement = connection.prepareStatement(query);
    ResultSet queryResult = preparedStatement.executeQuery()) {

      while (queryResult.next()) {
        toReturn.add(queryResult.getString(columnName));
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for column %s from table %s!", columnName, tableName));
    }
    return Collections.unmodifiableSortedSet(toReturn);
  }

  /**
   * Returns {@link ResultSet} of all the information stored under table of the given name from the
   * CDDB.
   * @param tableName name of table to retrieve
   * @return ResultSet of all info is in the database
   * @throws SQLException failure to query info from the database
   * @throws IllegalStateException if connection to the database is closed or not working
   */
  private ResultSet retrieveTableInfo(String tableName) throws SQLException {
    if (tableName == null) {
      throw new IllegalArgumentException("Given table name can't be null!");
    }

    // Check if table exists, then retrieve info
    String checkQuery = "SELECT name FROM sqlite_master WHERE name='?'";
    try (Connection connection = connect();
        PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);) {
      preparedStatement.setString(1, tableName);
      ResultSet checkQueryResult = preparedStatement.executeQuery();

      // Should only have one resulting row if table exists
      if (!checkQueryResult.next()) {
        throw new IllegalArgumentException("Table matching given table name doesn't exist in "
            + "database!");
      }
      else if (!checkQueryResult.next()) { // Now should return false, passed first and only row
        String query = "SELECT * FROM %s";
        query = String.format(query, tableName);
        PreparedStatement preparedStatementRetrieval = connection.prepareStatement(query);
        ResultSet queryResult = preparedStatementRetrieval.executeQuery();
        closeResultSet(queryResult);
        return queryResult;
      }
      else {
        throw new IllegalArgumentException("Database returned multiple tables matching given table"
            + " name, should only return zero or one tables!");
      }
    }
    catch (SQLException e) {
      throw new SQLException(e.getMessage() +
          String.format("\nFailed to query for table %s!", tableName));
    }
  }

  /**
   * Default implementation of the {@link CardQuery} interface, listed inside
   * {@link DefaultDatabaseChannel} due its tight coupling with it for purposes of determining
   * what is and is not a valid parameter.
   */
  private class DefaultCardQuery implements CardQuery {
    
    private final List<Pair<String, Boolean>> nameParams;
    
    private final List<Pair<String, Boolean>> textParams;
    
    private final List<Pair<String, Boolean>> colorParams;
    
    private final List<Pair<String, Boolean>> colorIdentityParams;
    
    private final List<Pair<String, Boolean>> typeParams;

    private final List<Pair<String, Boolean>> blockParams;

    private final List<Pair<String, Boolean>> setParams;

    private final List<Pair<String, Boolean>> artistParams;

    private final List<Pair<String, Boolean>> rarityParams;

    private final List<Pair<String, Boolean>> flavorTextParams;
    
    private final List<Triple<Stat, Comparison, Integer>> statParams;
    
    private final List<Triple<Stat, Comparison, Stat>> statVersusStatParams;

    private final List<Triple<String, Comparison, Integer>> manaTypeParams;

    private DefaultCardQuery() {
      nameParams = new ArrayList<>();
      textParams = new ArrayList<>();
      colorParams = new ArrayList<>();
      colorIdentityParams = new ArrayList<>();
      typeParams = new ArrayList<>();
      blockParams = new ArrayList<>();
      setParams = new ArrayList<>();
      artistParams = new ArrayList<>();
      flavorTextParams = new ArrayList<>();
      statParams = new ArrayList<>();
      statVersusStatParams = new ArrayList<>();
      rarityParams = new ArrayList<>();
      manaTypeParams = new ArrayList<>();
    }

    class BooleanToLike implements BiFunction<Boolean, String, String> {

      @Override
      public String apply(Boolean include, String parameter) {
        String includeString = include ? "LIKE" : "NOT LIKE";
        return String.format("%s '%%%s%%'", includeString, parameter);
      }
    }

    class BooleanToEqual implements BiFunction<Boolean, String, String> {

      @Override
      public String apply(Boolean include, String parameter) {
        String includeString = include ? "=" : "!=";
        return String.format("%s '%s'", includeString, parameter);
      }
    }


    @Override
    public void byName(String word, boolean searchFor) throws IllegalArgumentException {
      validWord(word);
      nameParams.add(new Pair<>(word, searchFor));
    }

    /**
     *
     * @return
     */
    private StringBuilder buildNameAndTextQuery() {
      if (nameParams.isEmpty() && textParams.isEmpty()) {
        return new StringBuilder();
      }

      StringBuilder query = new StringBuilder("SELECT name FROM Card");

      Map<String, List<Pair<String, Boolean>>> nameAndText = new HashMap<>();
      nameAndText.put("name", nameParams);
      nameAndText.put("card_text", textParams);

      boolean first = false;
      for (String category : nameAndText.keySet()) {
        List<Pair<String, Boolean>> params = nameAndText.get(category);
        for (Pair<String, Boolean> param : params) {
          String cond = !first ? "WHERE" : "AND";
          first = true;
          String include = new BooleanToLike().apply(param.getB(), param.getA());
          String toAdd = String.format(" %s %s %s",
              cond, category, include);
          query.append(toAdd);
        }
      }
      return query;
    }

    @Override
    public void byText(String word, boolean searchFor) throws IllegalArgumentException {
      validWord(word);
      textParams.add(new Pair<>(word, searchFor));
    }

    @Override
    public void byColor(String color, boolean searchFor) throws IllegalArgumentException {
      if (color == null) {
        throw new IllegalArgumentException("Given color can't be null!");
      }
      else if (!colors.contains(color)) {
        throw new IllegalArgumentException("Given color is not contained in the CDDB!");
      }
      colorParams.add(new Pair<>(color, searchFor));
    }

    private StringBuilder buildColorQuery() {
      return buildGenericCardQuery(colorParams, "Color", "card_name", "color");
    }

    private StringBuilder buildGenericCardQuery(List<Pair<String, Boolean>> params, String table,
        String returnJoinColumn, String connectColumn) {
      if (params.isEmpty()) {
        return new StringBuilder();
      }

      StringBuilder query = new StringBuilder();
      StringBuilder conditions = new StringBuilder();
      int i = 0;
      String curTable = "t" + i;
      String startingTable = curTable;
      for (Pair<String, Boolean> pair : params) {
        curTable = "t" + i;
        String cond;
        if (i == 0) {
          query.append(String.format("SELECT %s.%s FROM %s %s",
              curTable, returnJoinColumn, table, curTable));
          cond = "WHERE";
        }
        else {
          query.append(String.format(" JOIN %s %s ON %s.%s = %s.%s",
              table, curTable, startingTable, returnJoinColumn, curTable, returnJoinColumn));
          cond = "AND";
        }
        String include = new BooleanToEqual().apply(pair.getB(), pair.getA());
        conditions.append(String.format(" %s %s.%s %s",
            cond, curTable, connectColumn, include));
        i++;
      }
      return query.append(conditions);
    }

    private Triple<StringBuilder, Boolean, String> buildEvenMoreGenericCardQuery(String table, String[] tableMatchColumns,
        List<Triple<String, List<Pair<String, Boolean>>, BiFunction<Boolean, String, String>>> conditionals) {

      StringBuilder query = new StringBuilder("SELECT ");
      StringBuilder conditions = new StringBuilder();

      int i = 0;
      String curTable = "t" + i;
      String startingTable = curTable;

      int j = 0;
      for (String returnParam : tableMatchColumns) {
        if (j != 0) {
          query.append(", ");
        }
        query.append(String.format("%s.%s %s", curTable, returnParam, returnParam));
        j++;
      }
      query.append(String.format(" FROM %s %s", table, curTable));

      String cond = "";
      for (Triple<String, List<Pair<String, Boolean>>, BiFunction<Boolean, String, String>> paramType : conditionals ) {
        String column = paramType.getA();
        List<Pair<String, Boolean>> params = paramType.getB();
        BiFunction<Boolean, String, String> toInclude = paramType.getC();

        for (Pair<String, Boolean> param : params) {
          curTable = "t" + i;
          if (i == 0) {
            cond = "WHERE";
          }
          else {
            query.append(String.format(" JOIN %s %s", table, curTable));
            int k = 0;
            for (String joinParam : tableMatchColumns) {
              if (k == 0) {
                query.append(" ON");
              }
              else {
                query.append(" AND");
              }
              query.append(String.format(" %s.%s = %s.%s",
                  startingTable, joinParam, curTable, joinParam));
              k++;
            }
            cond = "AND";
          }
          String booleanToString = toInclude.apply(param.getB(), param.getA());
          String condToAdd = String.format(" %s %s.%s %s",
              cond, curTable, column, booleanToString);
          conditions.append(condToAdd);
          i++;
        }
      }

      query.append(conditions);
      boolean conditionsAdded = !cond.isEmpty();
      return new Triple<>(query, conditionsAdded, startingTable);
    }

    @Override
    public void byColorIdentity(String color, boolean searchFor) throws IllegalArgumentException {
      if (color == null) {
        throw new IllegalArgumentException("Given color can't be null!");
      }
      else if (!colors.contains(color)) {
        throw new IllegalArgumentException("Given color is not contained in the CDDB!");
      }
      colorIdentityParams.add(new Pair<>(color, searchFor));
    }

    private StringBuilder buildColorIdentityQuery() {
      return buildGenericCardQuery(colorIdentityParams, "ColorIdentity", "card_name", "color");
    }

    @Override
    public void byType(String type, boolean searchFor) throws IllegalArgumentException {
      if (type == null) {
        throw new IllegalArgumentException("Given type can't be null!");
      }
      else if (!types.contains(type)) {
        throw new IllegalArgumentException("Given type is not contained in the CDDB!");
      }
      typeParams.add(new Pair<>(type, searchFor));
    }

    private StringBuilder buildTypeQuery() {
      return buildGenericCardQuery(typeParams, "Type", "card_name", "type");
    }

    @Override
    public void byBlock(String block, boolean searchFor) throws IllegalArgumentException {
      if (block == null) {
        throw new IllegalArgumentException("Given block can't be null!");
      }
      else if (!blocks.contains(block)) {
        throw new IllegalArgumentException("Given block is not contained in the CDDB!");
      }
      blockParams.add(new Pair<>(block, searchFor));
    }

    private StringBuilder buildBlockQuery() {
      if (blockParams.isEmpty()) {
        return new StringBuilder();
      }

      StringBuilder query = new StringBuilder("SELECT expansion FROM Block");

      List<String> blockInclude = new ArrayList<>();
      List<String> blockDisallow = new ArrayList<>();

      for (Pair<String, Boolean> param : blockParams) {
        String value = param.getA();
        boolean include = param.getB();
        if (include) {
          blockInclude.add(value);
        }
        else {
          blockDisallow.add(value);
        }
      }

      Map<Boolean, List<String>> lists = new HashMap<>();
      lists.put(true, blockInclude);
      lists.put(false, blockDisallow);

      boolean first = true;

      for (boolean key : lists.keySet()) {
        List<String> list = lists.get(key);
        if (!list.isEmpty()) {
          String addCond;
          String include = key ? "" : " NOT";
          if (first) {
            first = false;
            addCond = "WHERE";
          }
          else {
            addCond = "AND";
          }

          query.append(String.format(" %s block%s IN (",
              addCond, include));
          for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
              query.append(", ");
            }
            query.append(String.format("'%s'", list.get(i)));
          }
          query.append(")");
        }
      }

      return query;
    }

    @Override
    public void bySet(String set, boolean searchFor) throws IllegalArgumentException {
      if (set == null) {
        throw new IllegalArgumentException("Given set can't be null!");
      }
      else if (!sets.contains(set)) {
        throw new IllegalArgumentException("Given set is not contained in the CDDB!");
      }
      setParams.add(new Pair<>(set, searchFor));
    }

    @Override
    public void byArtist(String artist, boolean searchFor) throws IllegalArgumentException {
      if (artist == null) {
        throw new IllegalArgumentException("Given type can't be null!");
      }
      else if (!artists.contains(artist)) {
        throw new IllegalArgumentException("Given artist is not contained in the CDDB!");
      }
      artistParams.add(new Pair<>(artist, searchFor));
    }

    @Override
    public void byFlavorText(String word, boolean searchFor) throws IllegalArgumentException {
      validWord(word);
      flavorTextParams.add(new Pair<>(word, searchFor));
    }

    @Override
    public void byRarity(String rarity, boolean searchFor) throws IllegalArgumentException {
      if (rarity == null) {
        throw new IllegalArgumentException("Given rarity can't be null!");
      }
      else if (!rarities.contains(rarity)) {
        throw new IllegalArgumentException("Given rarity is not contained in the CDDB!");
      }
      rarityParams.add(new Pair<>(rarity, searchFor));
    }

    private Triple<StringBuilder, Boolean, String> buildCardExpansionQuery() {
      String[] returnAndMatchColumns = new String[]{"card_name", "expansion"};

      List<Triple<String, List<Pair<String, Boolean>>, BiFunction<Boolean, String, String>>> params =
          new ArrayList<>(4);
      params.add(new Triple<>("expansion", setParams, new BooleanToEqual()));
      params.add(new Triple<>("rarity", rarityParams, new BooleanToEqual()));
      params.add(new Triple<>("flavor_text", flavorTextParams, new BooleanToLike()));
      params.add(new Triple<>("artist", artistParams, new BooleanToEqual()));

      return buildEvenMoreGenericCardQuery("CardExpansion", returnAndMatchColumns, params);
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

    private StringBuilder buildStatQuery() {
      List<Triple<String, Comparison, Integer>> convertedParams = new ArrayList<>();
      for (Triple<Stat, Comparison, Integer> param : statParams) {
        convertedParams.add(new Triple<>(param.getA().getValue(), param.getB(), param.getC()));
      }

      return buildGenericComparisonToIntQuery("Stat", "card_name",
          "category", "base_value", convertedParams);
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

    private StringBuilder buildStatVersusStatQuery() {
      return buildGenericComparisonToStatQuery("Stat", "card_name",
          "category", "base_value", statVersusStatParams);
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

    private StringBuilder buildManaTypeQuery() {
      return buildGenericComparisonToIntQuery("Mana", "card_name",
          "mana_type", "quantity", manaTypeParams);
    }

    private StringBuilder buildGenericComparisonToIntQuery(String table, String tableMatchColumn,
        String categoryColumn, String comparisonColumn,
        List<Triple<String, Comparison, Integer>> conditionals) {
      StringBuilder query = new StringBuilder();
      StringBuilder conditions = new StringBuilder();
      int i = 0;
      String startingTable = "";
      for (Triple<String, Comparison, Integer> conditional : conditionals ) {
        String curTable = "t" + i;
        StringBuilder tableSelect = new StringBuilder();
        String cond;
        if (i == 0) {
          tableSelect.append(String.format("SELECT %s.%s FROM %s %s",
              curTable, tableMatchColumn, table, curTable));
          cond = "WHERE";
          startingTable = curTable;
        }
        else {
          tableSelect.append(String.format(" JOIN %s %s ON %s.%s = %s.%s",
              table, curTable, startingTable, tableMatchColumn, curTable, tableMatchColumn));
          cond = "AND";
        }

        String categoryType = conditional.getA();
        String comparison = conditional.getB().getValue();
        String comparisonQuantity = conditional.getC().toString();
        query.append(tableSelect);
        conditions.append(String.format(" %s %s.%s = '%s'",
            cond, curTable, categoryColumn, categoryType));
        conditions.append(String.format(" AND %s.%s %s %s",
            curTable, comparisonColumn, comparison, comparisonQuantity));
        i++;
      }
      return query.append(conditions);
    }

    private StringBuilder buildGenericComparisonToStatQuery(String table, String tableMatchColumn,
        String categoryColumn, String comparisonColumn,
        List<Triple<Stat, Comparison, Stat>> conditionals) {
      StringBuilder query = new StringBuilder();
      StringBuilder conditions = new StringBuilder();
      int i = 0;
      String startingTable = "";
      for (Triple<Stat, Comparison, Stat> conditional : conditionals ) {
        String cond = "";
        String firstTable = "t" + i;
        String secondTable = "t" + (i + 1);
        String firstCategoryType = conditional.getA().getValue();
        String comparison = conditional.getB().getValue();
        String secondCategoryType = conditional.getC().getValue();
        for (int j = 0; j < 2; j++) {
          String curTable = j == 0 ? firstTable : secondTable;
          String categoryType = j == 0 ? firstCategoryType : secondCategoryType;
          if (i == 0) {
            startingTable = firstTable;
            query.append(String.format("SELECT %s.%s FROM %s %s",
                startingTable, tableMatchColumn, table, startingTable));
            cond = "WHERE";
          }
          else {
              query.append(String.format(" JOIN %s %s ON %s.%s = %s.%s",
                  table, curTable, startingTable, tableMatchColumn, curTable, tableMatchColumn));

            if (i > 0) {
              cond = "AND";
            }
          }

          conditions.append(String.format(" %s %s.%s = '%s'",
              cond, curTable, categoryColumn, categoryType));
          i++;
        }

        // Compare
        conditions.append(String.format(" AND %s.%s %s %s.%s",
            firstTable, comparisonColumn, comparison, secondTable, comparisonColumn));
      }
      return query.append(conditions);
    }

    private boolean isStringBuilderEmpty(StringBuilder toCheck) {
      return toCheck.length() == 0;
    }

    @Override
    public String asQuery() {
      StringBuilder completeCardQuery = new StringBuilder();

      StringBuilder[] cardQueries = new StringBuilder[]{buildNameAndTextQuery(), buildColorQuery(),
      buildColorIdentityQuery(), buildTypeQuery(), buildStatQuery(), buildStatVersusStatQuery(),
      buildManaTypeQuery()};

      for (StringBuilder query : cardQueries) {
        if (!isStringBuilderEmpty(query)) {
          if (!isStringBuilderEmpty(completeCardQuery)) {
            completeCardQuery.append(" INTERSECT ");
          }
          completeCardQuery.append(query);
        }
      }

      Triple<StringBuilder, Boolean, String> cardExpansionResults = buildCardExpansionQuery();
      StringBuilder completeQuery = cardExpansionResults.getA();
      boolean cardExpansionConditionsAdded = cardExpansionResults.getB();
      String cardExpansionQueryStartingTable = cardExpansionResults.getC();

      String mergeCond = cardExpansionConditionsAdded ? "AND" : "WHERE";
      if (!isStringBuilderEmpty(completeCardQuery)) {
        completeQuery.append(String.format(" %s %s.card_name IN (",
            mergeCond, cardExpansionQueryStartingTable));
        completeQuery.append(completeCardQuery);
        completeQuery.append(")");
        mergeCond = "AND";
      }

      StringBuilder blockQuery = buildBlockQuery();
      if (!isStringBuilderEmpty(blockQuery)) {
        completeQuery.append(String.format(" %s %s.expansion IN (",
            mergeCond, cardExpansionQueryStartingTable));
        completeQuery.append(blockQuery);
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
      typeParams.clear();
      blockParams.clear();
      setParams.clear();
      artistParams.clear();
      flavorTextParams .clear();
      statParams.clear();
      statVersusStatParams.clear();
      rarityParams.clear();
      manaTypeParams.clear();
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

    /**
     * Given a string, splits all sequences of non-space characters along any spaces (" "), and
     * removes and other present spaces. Returns the sequences of non-space characters as a String
     * array.
     * @param string string to work on
     * @return string array of all non-space characters
     */
    private String[] splitAndTrimSpaces(String string) {
      if (string == null) {
        throw new IllegalArgumentException("Given string can't be null!");
      }
      else if (string.isEmpty() || string.isBlank()) {
        return new String[]{""};
      }

      List<String> words = new ArrayList<>();
      StringBuilder curWord = new StringBuilder();
      char[] chars = string.toCharArray();
      for (int i = 0; i < chars.length; i++) {
        char curChar = chars[i];
        if (curChar != ' ') {
          curWord.append(curChar);
          if (i == chars.length - 1 || chars[i+1] == ' ') {
            words.add(curWord.toString());
            curWord.delete(0, curWord.length());
            i++;
          }
        }
      }
      String[] toReturn = new String[words.size()];
      return words.toArray(toReturn);
    }
  }
}
