package view;

import java.util.Map;
import java.util.SortedSet;
import value_objects.card.Card;
import value_objects.card.query.CardQuery;
import value_objects.deck.Deck;
import value_objects.deck.instance.DeckInstance;
import value_objects.utility.Pair;

/**
 * Takes in card, deck, and other info from the Card and Deck Database (CDDB) to display to a user,
 * and returns any information queries and deck updates the user wishes to make to the CDDB.
 * (CDDB).
 */
public interface DatabaseView {

  /**
   * Accepts a mapping of all {@link Deck} IDs and their associated names currently in the CDDB.
   * @param deckInfo
   * @throws IllegalArgumentException if the given mapping is null
   */
  void acceptAvailableDecksInfo(Map<Integer, String> deckInfo) throws IllegalArgumentException;

  /**
   * Takes in some sorted set of information to display to the user. Such as supported types,
   * rarities, expansions, etc. in the CDDB.
   * @param infoType type of info being displayed
   * @param info info to display
   * @throws IllegalArgumentException if any param is null or if info is empty
   */
  void acceptInfo(String infoType, SortedSet<String> info) throws IllegalArgumentException;

  /**
   * Takes in a {@link CardQuery} to use for card searching in this {@link DatabaseView}.
   * @param cardQuery CardQuery to assign to this view
   * @throws IllegalArgumentException if given CardQuery is null
   */
  void acceptCardQuery(CardQuery cardQuery) throws IllegalArgumentException;

  /**
   * Returns a {@link CardQuery} the user has interacted with in order to find {@link Card}s that
   * meet the parameters entered into the CardQuery from the CDDB
   * @return CardQuery the user has interacted with
   * @throws IllegalStateException if this {@link DatabaseView} hasn't been given a CardQuery to
   * work with
   */
  CardQuery getCardQuery() throws IllegalStateException;

  /**
   * Takes in a sorted set of {@link Card}s to display to the user.
   * @param cards Cards to display
   * @throws IllegalArgumentException if given set of Cards is null or empty
   */
  void acceptCards(SortedSet<Card> cards) throws IllegalArgumentException;

  /**
   * Takes in a {@link Deck} to display to the user.
   * @param deck Deck to display
   * @throws IllegalArgumentException if given Deck is null
   */
  void acceptDeckInfo(Deck deck) throws IllegalArgumentException;

  /**
   * Returns the integer ID of a {@link Deck} the user wishes to retrieve info about from the CDDB
   * in the form of a Deck.
   * @return ID of Deck to retrieve info about
   * @throws IllegalStateException if no deck has been selected to retrieve info on
   */
  int deckToRetrieveInfoOn() throws IllegalStateException;

  /**
   * Returns the integer ID of a {@link Deck} the user wishes to delete from the CDDB.
   * @return ID of Deck to delete from the CDDB
   * @throws IllegalStateException if no deck has been selected to delete
   */
  int deckToDelete() throws IllegalStateException;

  /**
   * Retrieves a new {@link Deck} the user has created to add to the CDDB.
   * @return Deck to add to the CDDB
   * @throws IllegalStateException if no deck has created to add
   */
  Deck deckToAdd() throws IllegalStateException;

  /**
   * Retrieves a new {@link DeckInstance} the user has created to add to the CDDB.
   * @return DeckInstance to add to the CDDB
   * @throws IllegalStateException if no DeckInstance has been selected to add
   */
  DeckInstance deckInstanceToAdd() throws IllegalStateException;

  /**
   * Returns a pairing of a {@link Deck} ID and a new name for the Deck associated with the ID.
   * @return pairing of a Deck ID and new Deck name
   * @throws IllegalStateException if no Deck has been selected to have its name updated
   */
  Pair<Integer, String> newDeckName() throws IllegalStateException;

  /**
   * Returns a pairing of a {@link Deck} ID and a new description for the Deck associated with the ID.
   * @return pairing of a Deck ID and new Deck name
   * @throws IllegalStateException if no Deck has been selected to have its name updated
   */
  Pair<Integer, String> newDeckDesp() throws IllegalStateException;
}
