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
 * {@link DatabaseView} implementation that provides a terminal interface for a user to interact
 * with.
 */
public class TerminalView extends BaseView implements DatabaseView {


  @Override
  public void acceptRelayRunnables(EnumMap<DatabaseViewConnection, Runnable> relayRunnables) {
    checkRelayRunnables(relayRunnables);

    // TODO assign runnables
  }

  @Override
  public void acceptAvailableDecksInfo(Map<Integer, String> deckInfo)
      throws IllegalArgumentException {

  }

  @Override
  public void acceptCardQuery(CardQuery cardQuery) throws IllegalArgumentException {

  }


  @Override
  public void acceptCards(SortedSet<Card> cards) throws IllegalArgumentException {

  }

  @Override
  public void acceptDeckInfo(Deck deck) throws IllegalArgumentException {

  }


  @Override
  public int deckToDelete() throws IllegalStateException {
    return 0;
  }

  @Override
  public Deck newDeckToAdd() throws IllegalStateException {
    return null;
  }

  @Override
  public DeckInstance deckInstanceToAdd() throws IllegalStateException {
    return null;
  }

  @Override
  public Pair<Integer, String> newDeckName() throws IllegalStateException {
    return null;
  }

  @Override
  public Pair<Integer, String> newDeckDesp() throws IllegalStateException {
    return null;
  }

  @Override
  public Parent asParent() {
    return null;
  }

}
