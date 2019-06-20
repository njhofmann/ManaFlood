package relay;

import view.DatabaseView;
import value_objects.card.Card;
import value_objects.card.query.CardQuery;
import value_objects.deck.Deck;

/**
 * A enumeration of all the types of behavior (interactions between the CDDB and a user display) a
 * {@link DatabaseView} must support. Such behavior as querying for and returning set of
 * {@link Card}s as per a {@link CardQuery}, asking for info of {@link Deck}s in the CDDB, etc.
 */
public enum DatabaseViewConnection {

  RetrieveDeckInfo(),

  QueryCards(),

  DeleteDeck(),

  NewDeck(),

  NewDeckInstance(),

  EditDeckName(),

  EditDeckDesp(),
}
