package database;

import database.access.DatabasePort;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import value_objects.Card;
import value_objects.CardQuery;
import value_objects.deck.Deck;
import value_objects.deck_instance.DeckInstance;

/**
 * Represents the set of methods available for interacting with the Card & Deck Database (CDDB).
 */
public interface DatabaseChannel extends DatabasePort {

  /**
   * Returns a hashmap of decks in the CDDB, as represented by their unique integer IDs and names.
   * @return collection of deck IDs to deck names of all decks in the CDDB
   * @throws SQLException if there is a failure to query the CDDB for the decks info
   */
  HashMap<Integer, String> getDecks() throws SQLException;

  /**
   * Returns {@link Deck} in the CDDB with the corresponding unique ID.
   * @param deckID unique integer ID of deck
   * @return deck with corresponding unique ID
   * @throws IllegalArgumentException if CDDB doesn't contain a deck with given ID
   * @throws SQLException if there is a failure to query the CDDB for the desired deck info
   */
  Deck getDeck(int deckID) throws IllegalArgumentException, SQLException;

  /**
   * Adds this {@link Deck} and its {@link DeckInstance}s to the CDDB.
   * @param deck deck to add
   * @throws IllegalArgumentException if given deck is null, or if there is already a deck in the
   *         CDDB that has the unique ID of the given deck
   * @throws SQLException if there is a failure to add deck info to the CDDB
   */
  void addDeck(Deck deck) throws IllegalArgumentException, SQLException;

  /**
   * Updates {@link Deck} in the CDDB with matching {@param deckID} by adding the given
   * {@link DeckInstance} to the matching {@link Deck}.
   * @param deck deck instance to add to corresponding {@link Deck}
   * @throws IllegalArgumentException if CDDB doesn't contain a deck with given ID, or if given
   *        {@link DeckInstance} is null
   * @throws SQLException if there is a failure to update the CDDB
   */
  void updateDeck(DeckInstance deck) throws IllegalArgumentException, SQLException;

  /**
   * Deletes the {@link Deck} in the CDDB with the given {@param deckID}.
   * @param deckID unique deck ID of {@link Deck} to delete
   * @throws IllegalArgumentException if CDDB doesn't contain a deck with given ID
   * @throws SQLException if there is a failure to query card info or delete desired deck from CDDB
   */
  void deleteDeck(int deckID) throws IllegalArgumentException, SQLException;

  /**
   * Updates {@link Deck} with given {@param deckID} with new name as given by {@param newName}.
   * @param deckID unique deck ID of {@link Deck} to update
   * @param newName new name to give to corresponding {@link Deck}
   * @throws IllegalArgumentException if CDDB doesn't contain a deck with given ID, or if
   *         {@param newName} is null
   * @throws SQLException if there is a failure to query deck info from the CDDB or to update deck
   *         name
   */
  void updateDeckName(int deckID, String newName) throws IllegalArgumentException, SQLException;

  /**
   * Updates {@link Deck} with given {@param deckID} with new description as given by
   * {@param newDesp}.
   * @param deckID unique deck ID of {@link Deck} to update
   * @param newDesp new description to give to corresponding {@link Deck}
   * @throws IllegalArgumentException if CDDB doesn't contain a deck with given ID, or if
   *         {@param newDesp} is null
   * @throws SQLException if there is a failure to query deck info or update deck desp with the CDDB
   */
  void updateDeckDesp(int deckID, String newDesp) throws IllegalArgumentException, SQLException;

  /**
   * Returns a list of {@link Card} s from the CDDB that match the parameters given by the inputted
   * {@link CardQuery}.
   * @param cardQuery desired query parameters to match cards against
   * @return list of {@link Card}s that match given card parameters
   * @throws IllegalArgumentException if given {@link CardQuery} is null.
   * @throws SQLException if there is a failure to query card info from the CDDB
   */
  List<Card> queryCards(CardQuery cardQuery) throws IllegalArgumentException, SQLException;
}
