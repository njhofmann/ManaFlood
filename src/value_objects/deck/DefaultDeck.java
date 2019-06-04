package value_objects.deck;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;
import value_objects.deck.instance.DeckInstance;

/**
 * Default implementation of {@link Deck}, contains all identifying info of a Deck and the list of
 * {@link DeckInstance} that make up the history of this Deck.
 */
public class DefaultDeck implements Deck {

  /**
   * ID of this Deck as it appears in the CDDB.
   */
  private final int deckID;

  /**
   * Name of this Deck as it appears in the CDDB.
   */
  private final String deckName;

  /**
   * Description explaining this Deck as it appears in the CDDB.
   */
  private final String desp;

  /**
   * {@link DeckInstance}s that make up the history of this deck.
   */
  private final SortedSet<DeckInstance> history;

  /**
   * Creates a representation of a Deck in the CDDB with a ID, name, description, and history
   * of "revisions" or past deck iterations. Sorts history to ensure it is in order.
   * @param deckID ID of a deck to assign to this Deck
   * @param deckName name of deck
   * @param desp description explaining deck
   * @param history past iterations of this deck
   * @throws IllegalArgumentException if any of the given parameters are null, or if history is
   *         empty
   */
  public DefaultDeck(int deckID, String deckName, String desp, SortedSet<DeckInstance> history) {
    if (deckName == null) {
      throw new IllegalArgumentException("Given deck name can't be null!");
    }
    else if (desp == null) {
      throw new IllegalArgumentException("Given deck description can't be null!");
    }
    else if (history == null) {
      throw new IllegalArgumentException("Given deck history can't be null!");
    }
    else if (history.isEmpty()) {
      throw new IllegalArgumentException("A deck must have at least one deck instance in its"
          + "history!");
    }
    this.deckID = deckID;
    this.deckName = deckName;
    this.desp = desp;
    this.history = Collections.unmodifiableSortedSet(history);
  }

  @Override
  public int getDeckID() {
    return deckID;
  }

  @Override
  public String getDeckName() {
    return deckName;
  }

  @Override
  public String getDescription() {
    return desp;
  }

  @Override
  public SortedSet<DeckInstance> getHistory() {
    return history;
  }

  @Override
  public int compareTo(Deck other) throws IllegalArgumentException {
    if (other == null) {
      throw new IllegalArgumentException("Given deck can't be null!");
    }

    int otherDeckId = other.getDeckID();
    if (deckID < otherDeckId) {
      return deckID - otherDeckId;
    }
    else if (deckID > otherDeckId) {
      return otherDeckId - deckID;
    }

    // Neither should be empty
    SortedSet<DeckInstance> otherExpansions = other.getHistory();
    assert !getHistory().isEmpty() && !otherExpansions.isEmpty();

    boolean thisIsShorter;
    SortedSet<DeckInstance> shorterExpansions;
    SortedSet<DeckInstance> longerExpansions;
    if (getHistory().size() < otherExpansions.size()) {
      thisIsShorter = true;
      shorterExpansions = getHistory();
      longerExpansions = otherExpansions;
    }
    else {
      thisIsShorter = false;
      shorterExpansions = otherExpansions;
      longerExpansions = getHistory();
    }

    Iterator<DeckInstance> shortExpansionsIterator = shorterExpansions.iterator();
    Iterator<DeckInstance> longerExpansionsIterator = longerExpansions.iterator();
    while (shortExpansionsIterator.hasNext()) {
      DeckInstance curShorterExpansionsItem = shortExpansionsIterator.next();
      DeckInstance curLongerExpansionItem = longerExpansionsIterator.next();
      int curComparison = thisIsShorter ?
          curShorterExpansionsItem.compareTo(curLongerExpansionItem) :
          curLongerExpansionItem.compareTo(curShorterExpansionsItem);

      if (curComparison != 0) {
        return curComparison;
      }
    }

    int remainingDifference = 0;
    while (longerExpansionsIterator.hasNext()) {
      longerExpansionsIterator.next();

      if (thisIsShorter) {
        remainingDifference--;
      }
      else {
        remainingDifference++;
      }
    }

    // If remaining difference is 0, should be the exact same card with exact same printings
    return remainingDifference;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof Deck) {
      Deck otherDeck = (Deck) other;
      return otherDeck.getDeckID() == deckID && history.equals(otherDeck.getHistory());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(deckID, history);
  }
}
