package view;

import java.util.EnumMap;
import relay.DatabaseViewConnection;
import value_objects.card.Card;
import value_objects.deck.Deck;
import value_objects.card.query.CardQuery;

/**
 * Abstract class providing methods common to all {@link DatabaseView} implementations.
 */
public abstract class BaseView implements DatabaseView {

  /**
   * {@link CardQuery} this {@link DatabaseView} is currently using for querying for {@link Card}.
   * If it is null, means it has not yet been assigned a working CardQuery.
   */
  protected CardQuery cardQuery;

  /**
   * ID of the {@link Deck} the user of this {@link DatabaseView} has selected for viewing. If it
   * is null, means that no Deck has been selected.
   */
  protected Integer selectedDeckId;

  /**
   * Returns if this {@link DatabaseView}'s {@link CardQuery} has been assigned a working
   * implementation and thus is usable.
   * @return if the CardQuery is usable
   */
  protected boolean isCardQueryAssigned() {
    return cardQuery == null;
  }

  /**
   * Returns if the user of this {@link DatabaseView} has selected a {@link Deck} (in the form of
   * its ID) to view.
   * @return if user has selected a Deck for viewing.
   */
  protected boolean isDeckSelected() {
    return selectedDeckId == null;
  }

  /**
   * Checks that a given mapping of {@link DatabaseViewConnection}s to their respectable
   * {@link Runnable}s is valid. Throws an IllegalArgumentException otherwise.
   * @param relayRunnables mapping to check
   * @throws IllegalArgumentException if given mapping is null, a value is null, or not every value
   * of a DatabaseViewConnection is included in the mapping
   */
  protected void checkRelayRunnables(EnumMap<DatabaseViewConnection, Runnable> relayRunnables) {
    if (relayRunnables == null) {
      throw new IllegalArgumentException("Given mapping can't be null!");
    }

    for (DatabaseViewConnection connection : DatabaseViewConnection.values()) {
      if (!relayRunnables.keySet().contains(connection)) {
        throw new IllegalArgumentException("Given relay mapping must contain a runnable for each "
            + "type of required connection!");
      } else if (relayRunnables.get(connection) == null) {
        throw new IllegalArgumentException("Given relay mapping can't contain a null value for a "
            + "connection!");
      }
    }
  }

  @Override
  public final void acceptCardQuery(CardQuery cardQuery) throws IllegalArgumentException {
    if (cardQuery == null) {
      throw new IllegalArgumentException("Given car dquery can't be null!");
    }
    this.cardQuery = cardQuery;
  }

  @Override
  public final CardQuery getCardQuery() throws IllegalStateException {
    if (isCardQueryAssigned()) {
      throw new IllegalStateException("This view hasn't been assigned a card query to work "
          + "with yet!");
    }
    return cardQuery;
  }

  @Override
  public final int deckToRetrieveInfoOn() throws IllegalStateException {
    if (!isDeckSelected()) {
      throw new IllegalStateException("No deck has yet been selected!");
    }
    return selectedDeckId;
  }
}
