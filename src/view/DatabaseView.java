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

  void acceptInfo(String infoType, SortedSet<String> info);

  CardQuery getCardQuery();

  void acceptCards(SortedSet<Card> cards);

  int deckToRetrieveInfo();

  int deckToDelete();

  Deck getDeck();

  DeckInstance getDeckInstance();

  Pair<Integer, String> getNewDeckName();

  Pair<Integer, String> getNewDeckDesp();

}
