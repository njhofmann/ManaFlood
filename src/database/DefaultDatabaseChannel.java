package database;

import database.access.DefaultDatabasePort;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import value_objects.Card;
import value_objects.CardQuery;
import value_objects.card_printing.CardPrinting;
import value_objects.card_printing.DefaultCardPrinting;
import value_objects.deck.Deck;
import value_objects.deck.DefaultDeck;
import value_objects.deck_instance.DeckInstance;
import value_objects.deck_instance.DefaultDeckInstance;

/**
 * Default class to use to access the Card and Deck Database (CDDB) for querying cards and reading,
 * updating, and deleting decks.
 */
public class DefaultDatabaseChannel extends DefaultDatabasePort implements DatabaseChannel {

  /**
   * Takes in a {@link Path} referencing the Card and Deck Database (CDDB).
   * @param pathToDatabase path to CDDB
   */
  public DefaultDatabaseChannel(Path pathToDatabase) {
    super(pathToDatabase);
  }

  @Override
  public HashMap<Integer, String> getDecks() throws RuntimeException {
    try {
      String deckQuery = "SELECT id, name FROM Deck";
      PreparedStatement prep = connection.prepareStatement(deckQuery);
      ResultSet result = prep.executeQuery();

      HashMap<Integer, String> decksInfo = new HashMap<>();
      while (result.next()) {
        int currentDeckID = result.getInt("id");
        String currentDeckName = result.getString("name");
        decksInfo.put(currentDeckID, currentDeckName);
      }
      return decksInfo;
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to query CDDB for deck IDs and names!");
    }
  }

  @Override
  public Deck getDeck(int deckID) throws IllegalArgumentException, RuntimeException {
    doesDeckExist(deckID);

    // Query for info of all deck instances related to current deck
    ResultSet deckInstancesInfo;
    try {
      String deckInstanceInfoQuery = "SELECT * FROM DeckInstance WHERE deck_id=?";
      PreparedStatement prep = connection.prepareStatement(deckInstanceInfoQuery);
      prep.setInt(1, deckID);
      deckInstancesInfo = prep.executeQuery();
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(String.format("Failed to query for info of deck instances"
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
      e.printStackTrace();
      throw new RuntimeException("Failed to query for deck instance creations date & times!");
    }

    // Build deck instances,
    List<DeckInstance> deckInstances = new ArrayList<>();
    Map<String, Set<String>> categoryContents = new HashMap<>();
    Map<CardPrinting, Integer> cardPrintingQuantities = new HashMap<>();
    for (LocalDateTime creation : deckInstanceKeys) {
      // Get categories
      List<String> categories = new ArrayList<>();
      ResultSet categoriesResult;
      try {
        String categoriesQuery = "SELECT * FROM DeckInstCategory WHERE deck_id=? "
            + "AND deck_inst_creation=?";
        PreparedStatement prep = connection.prepareStatement(categoriesQuery);
        prep.setInt(1, deckID);
        prep.setTimestamp(2, Timestamp.valueOf(creation));
        categoriesResult = prep.executeQuery();
      }
      catch (SQLException e) {
        e.printStackTrace();
        throw new RuntimeException(String.format("Failed to query for categories for deck with"
            + " ID %d!", deckID));
      }

      // Get category cards
      try {
        while (categoriesResult.next()) {
          String currentCategory = categoriesResult.getString("category");
          try {
            String categoryQuery = "SELECT * FROM DeckInstCardCategory WHERE deck_id=? "
                + "AND deck_inst_creation=? AND category=?";
            PreparedStatement prep = connection.prepareStatement(categoryQuery);
            prep.setInt(1, deckID);
            prep.setTimestamp(2, Timestamp.valueOf(creation));
            prep.setString(3, currentCategory);
            ResultSet cardsInCategory = prep.executeQuery();

            Set<String> cardsToAdd = new HashSet<>();
            while (cardsInCategory.next()) {
              String cardToAdd = cardsInCategory.getString("card_name");
              if (cardsToAdd.contains(cardToAdd)) {
                throw new RuntimeException(String.format("Connected database is malformed for"
                    + "allowing duplicate entries of card %d in category %d of deck instance"
                    + "%d %s!", cardToAdd, currentCategory, deckID, creation.toString()));
              }
              cardsToAdd.add(cardToAdd);
            }
            categoryContents.put(currentCategory, cardsToAdd);
          }
          catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Failed to query for cards in category %s for"
                + " deck instance %d %s!", currentCategory, deckID, creation.toString()));
          }
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
        throw new RuntimeException(String.format("Failed to query for card categories for deck "
            + "instance %d %s!", deckID, creation.toString()));
      }

      // Get card printings
      try {
        String cardPrintingsQuery = "SELECT * FROM DeckInstCardExpansion WHERE deck_id=? "
            + "AND deck_inst_creation=?";
        PreparedStatement prep = connection.prepareStatement(cardPrintingsQuery);
        prep.setInt(1, deckID);
        prep.setTimestamp(2, Timestamp.valueOf(creation));
        ResultSet cardPrintings = prep.executeQuery();

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
          e.printStackTrace();
          throw new RuntimeException(String.format("Failed to get card printing info for deck "
              + "instance %d %s!", deckID, creation.toString()));
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
        throw new RuntimeException(String.format("Failed to query for card printings in deck"
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
      e.printStackTrace();
      throw new RuntimeException(String.format("Failed to query for name of deck with id %d!", deckID));
    }

    String deckDesp = "";
    try {
      deckDesp = deckInstancesInfo.getString("desp");
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(String.format("Failed to query for description of deck with id"
          + " %d!", deckID));
    }

    Deck toReturn = new DefaultDeck(deckID, deckName, deckDesp, deckInstances);
    return toReturn;
  }

  @Override
  public void updateDeck(DeckInstance deck) throws IllegalArgumentException {
    if (deck == null) {
      throw new IllegalArgumentException("Given deck instance can't be null!");
    }
    doesDeckExist(deck.getParentDeckID());
  }

  @Override
  public void deleteDeck(int deckID) throws IllegalArgumentException {
    // Check if deck exists
    doesDeckExist(deckID);

    try {
      String deletionRequest = "DELETE FROM Deck WHERE id=?";
      PreparedStatement prep = connection.prepareStatement(deletionRequest);
      prep.setInt(1, deckID);
      prep.executeUpdate();
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(String.format("Failed to delete deck with ID %d!", deckID));
    }
  }

  @Override
  public void updateDeckName(int deckID, String newName) throws IllegalArgumentException {
    if (newName == null) {
      throw new IllegalArgumentException("Given new name can't be null!");
    }
    // Check if deck exists
    doesDeckExist(deckID);

    try {
      String updateRequest = "UPDATE Deck SET name=? WHERE id=?";
      PreparedStatement prep = connection.prepareStatement(updateRequest);
      prep.setString(1, newName);
      prep.setInt(2, deckID);
      prep.executeUpdate();
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(String.format("Failed to update deck with ID %d with new"
          + " name \"%s\"!", deckID, newName));
    }
  }

  @Override
  public void updateDeckDesp(int deckID, String newDesp) throws IllegalArgumentException {
    if (newDesp == null) {
      throw new IllegalArgumentException("Given new description can't be null!");
    }
    // Check if deck exists
    doesDeckExist(deckID);

    try {
      String updateRequest = "UPDATE Deck SET desp=? WHERE id=?";
      PreparedStatement prep = connection.prepareStatement(updateRequest);
      prep.setString(1, newDesp);
      prep.setInt(2, deckID);
      prep.executeUpdate();
    }
    catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(String.format("Failed to update deck with ID %d with new"
          + " description \"%s\"!", deckID, newDesp));
    }
  }

  @Override
  public List<Card> queryCards(CardQuery cardQuery) throws IllegalArgumentException {
    return null;
  }

  /**
   * Checks if the CDDB contains a deck with given ID, checks table Deck for row with entry under
   * "id" column, else throws an error.
   * @param deckID
   * @throws IllegalArgumentException if there is no deck in CDDB with matching ID
   */
  private void doesDeckExist(int deckID) {
    // Check if CDDB has deck with given ID
    Set<Integer> deckIDs = getDecks().keySet();
    if (!deckIDs.contains(deckID)) {
      throw new IllegalArgumentException("CDDB doesn't contain deck with given ID!");
    }
  }
}
