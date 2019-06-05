package view;

import java.util.SortedSet;
import value_objects.card.Card;
import value_objects.card.query.CardQuery;
import value_objects.deck.Deck;
import value_objects.deck.instance.DeckInstance;
import value_objects.utility.Pair;

/**
 * Takes in card, deck, and other info from the Card and Deck Database(CDDB) to display to a user,
 * and returns any information queries and deck updates the user wishes to make to the CDDB.
 * (CDDB)
 */
public interface DatabaseView {

  /**
   * Takes in some sorted set of information to display to the user. Such as supported types,
   * rarities, expansions, etc. in the CDDB.
   * @param infoType type of info being displayed
   * @param info info to display
   * @throws IllegalArgumentException if any param is null or if info is empty
   */
  void acceptInfo(String infoType, SortedSet<String> info) throws IllegalArgumentException;

  /**
   * Returns a {@link CardQuery} the user has interacted with in order to find {@link Card}s that
   * meet the parameters entered into the CardQuery from the CDDB
   * @return CardQuery the user has interacted with
   */
  CardQuery getCardQuery();

  /**
   * Takes in a sorted set of {@link Card}s to display to the user.
   * @param cards Cards to display
   * @throws IllegalArgumentException if given set of Cards is null or empty
   */
  void acceptCards(SortedSet<Card> cards);

  /**
   * Returns the integer ID of a {@link Deck} the user wishes to retrieve info about from the CDDB
   * in the form of a Deck.
   * @return ID of Deck to retrieve info about
   */
  int deckToRetrieveInfo();

  /**
   * Returns the integer ID of a {@link Deck} the user wishes to delete from the CDDB.
   * @return ID of Deck to delete from the CDDB
   */
  int deckToDelete();

  /**
   * Retrieves a new {@link Deck} the user has created to add to the CDDB.
   * @return Deck to add to the CDDB
   */
  Deck getDeck();

  /**
   * Retrieves a new {@link DeckInstance} the user has created to add to the CDDB.
   * @return DeckInstance to add to the CDDB
   */
  DeckInstance getDeckInstance();

  /**
   * Returns a pairing of a {@link Deck} ID and a new name for the Deck associated with the ID.
   * @return pairing of a Deck ID and new Deck name
   */
  Pair<Integer, String> getNewDeckName();

  /**
   * Returns a pairing of a {@link Deck} ID and a new description for the Deck associated with the ID.
   * @return pairing of a Deck ID and new Deck name
   */
  Pair<Integer, String> getNewDeckDesp();
}
