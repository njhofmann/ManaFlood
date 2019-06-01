package value_objects.deck;

import java.util.SortedSet;
import value_objects.card.Card;
import value_objects.deck.instance.DeckInstance;

/**
 * Represents the "history" of a specific deck of {@link Card}s as stored in the Card & Deck
 * Database (CDDB).
 */
public interface Deck extends Comparable<Deck> {

  /**
   * Returns the unique deck ID of this {@link Deck}.
   * @return deck ID of this {@link Deck}
   */
  int getDeckID();

  /**
   * Returns the deck name of this {@link Deck}.
   * @return name of this {@link Deck}
   */
  String getDeckName();

  /**
   * Returns the description given for this {@link Deck}.
   * @return {@link Deck}'s description
   */
  String getDescription();

  /**
   * Returns a unmodifiable list of all {@link DeckInstance} that make up this {@link Deck}'s
   * history, sorted by oldest to newest.
   * @return sorted set of {@link DeckInstance} that make up this deck
   */
  SortedSet<DeckInstance> getHistory();

  /**
   * Compares this {@link Deck} with another Deck for order based their ids. Returns a negative int if
   * this Deck's name comes after inputted Deck's name alphabetically, and a positive int if this
   * Card's name comes before the inputted Card's name alphabetically. If names are the sames,
   * checks for equality off of deck instances included within each deck. Returns 0 if and only
   * if this Deck and given Deck have the same deck instances.
   * @param other another Deck to compare this Deck to
   * @return int representing ordering of this Deck and given Deck
   * @throws IllegalArgumentException if given Deck is null
   */
  @Override
  int compareTo(Deck other) throws IllegalArgumentException;

  /**
   * Returns if given object is equal to this {@link Deck} if and only if it is a Deck itself, has
   * the same id as this card, and the same deck instances as this card
   * @param other object to compare to
   * @return if this given object equals this Deck
   */
  @Override
  boolean equals(Object other);
}
