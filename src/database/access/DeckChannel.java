package database.access;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import value_objects.deck.Deck;
import value_objects.deck.instance.DeckInstance;
import value_objects.deck.instance.InformativeDeckInstance;
import value_objects.card.Card;
import value_objects.card.printing.InformativeCardPrinting;
import value_objects.card.printing.CardPrinting;

/**
 * Represents the set of methods available for interacting with the decks stored in the Card &
 * Deck Database (CDDB).
 */
public interface DeckChannel {

  /**
   * Returns a hashmap of decks in the CDDB, as represented by their unique integer IDs and names.
   * @return collection of deck IDs to deck names of all decks in the CDDB
   * @throws SQLException if there is a failure to query the CDDB for the decks info
   */
  Map<Integer, String> getDecks() throws SQLException;

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
   * Retrieves specific information about Cards and {@link CardPrinting}s in the given
   * {@link DeckInstance} in the form of a {@link InformativeDeckInstance}, which contains
   * {@link Card}s for each Card and {@link InformativeCardPrinting} for each CardPrinting.
   * @param deckInstance DeckInstance to retrieve info on
   * @return a InformativeDeckInstance of the given DeckInstance
   */
  InformativeDeckInstance getDeckInstanceInfo(DeckInstance deckInstance) throws SQLException;
}
