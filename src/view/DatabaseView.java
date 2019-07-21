package view;

import java.util.EnumMap;
import java.util.Map;
import java.util.SortedSet;
import javafx.scene.Parent;
import relay.DatabaseViewConnection;
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
   * Accepts a mapping of {@link DatabaseViewConnection} to {@link Runnable}s for this
   * {@link DatabaseView} to utilize in the form of supported behaviors (interactions between
   * the CDDB and a user display that must be supported). A mapping of the type of behavior to the
   * behavior that will actually be executed when needed. This method should be called before any
   * other method to ensure that need connections have been established with the CDDB, else other
   * methods will throw a {@link IllegalStateException}.
   * @param relayRunnables mapping to accept
   * @throws IllegalArgumentException if given mapping is null, any key or value is null, or not
   * every DatabaseViewConnection is included in the mapping
   */
  void acceptRelayRunnables(EnumMap<DatabaseViewConnection, Runnable> relayRunnables);

  /**
   * Accepts a mapping of all {@link Deck} IDs and their associated names currently in the CDDB.
   * @param  deckInfo
   * @throws IllegalArgumentException if the given mapping is null
   */
  void acceptAvailableDecksInfo(Map<Integer, String> deckInfo) throws IllegalArgumentException;

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
   * work with yet
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
   * Retrieves a name and a description for a new {@link Deck} the user wishes to add to the CDDB.
   * @return name and desp of a new Deck to add to CDDB
   */
  Pair<String, String> newDeckToAdd() throws IllegalStateException;

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

  /**
   * Returns the parent pane of this {@link DatabaseView} so that it can be displayed to the user.
   * @return parent pane of this DatabaseView
   */
  Parent asParent();
}
