package database;

import database.access.DefaultDatabasePort;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import value_objects.Card;
import value_objects.CardQuery;
import value_objects.deck.Deck;
import value_objects.deck_instance.DeckInstance;

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
    // Check if CDDB has deck with given ID
    Set<Integer> deckIDs = getDecks().keySet();
    if (!deckIDs.contains(deckID)) {
      throw new IllegalArgumentException("CDDB doesn't contain deck with given ID!");
    }

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

    // Build deck instances,
    return null;
  }

  @Override
  public void updateDeck(int deckID, DeckInstance deck) throws IllegalArgumentException {

  }

  @Override
  public void deleteDeck(int deckID) throws IllegalArgumentException {

  }

  @Override
  public void updateDeckName(int deckID, String newName) throws IllegalArgumentException {

  }

  @Override
  public void updateDeckDesp(int deckID, String newDesp) throws IllegalArgumentException {

  }

  @Override
  public List<Card> queryCards(CardQuery cardQuery) throws IllegalArgumentException {
    return null;
  }
}
