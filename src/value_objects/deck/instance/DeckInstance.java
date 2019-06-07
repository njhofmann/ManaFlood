package value_objects.deck.instance;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import value_objects.card.Card;
import value_objects.card.printing.CardPrinting;
import value_objects.deck.Deck;

/**
 * Represents a single instance in the history of a {@link Deck}. Implements Comparable so a
 * collection of {@link DeckInstance} can be sorted by their creation date & time.
 */
public interface DeckInstance extends Comparable<DeckInstance> {

  /**
   * Returns the unique deck ID of the {@link Deck} this {@link DeckInstance} is apart of.
   * @return deck ID of parent deck
   */
  int getParentDeckID();

  /**
   * Returns the date and time this {@link DeckInstance} was created.
   * @return date & time of this DeckInstance's creation
   */
  LocalDateTime getCreationInfo();

  /**
   * Returns a unmodifiable, uncategorized list of the cards (by name) that are apart of this
   * {@link DeckInstance}, and the quantity of that card in the {@link DeckInstance}
   * @return uncategorized list of the cards in the {@link DeckInstance} with their associated
   *         quantity
   */
  Map<String, Integer> getCardQuantities();

  /**
   * Returns a categorized hashmap of the cards (by name) that are apart of this
   * {@link DeckInstance}, categories in this {@link DeckInstance} to a list of card IDs that are
   * apart of that category.
   * @return uncategorized list of cards (by name)
   */
  Map<String, SortedSet<String>> getCardsByCategory();

  /**
   * Returns set of {@link CardPrinting} in this {@link DeckInstance}
   * @return card printings in this instance
   */
  SortedSet<CardPrinting> getCardPrintings();

  /**
   * Returns set of categories of cards that are in this {@link DeckInstance}.
   * @return set of categories that cards can be in
   */
  SortedSet<String> getCategories();

  /**
   * Returns set of card names that are in this {@link DeckInstance}
   * @return set of cards in this instance
   */
  SortedSet<String> getCards();

  /**
   * Returns mapping of specific card printings that are apart of this {@link DeckInstance}, to the
   * quantity of that card in the {@link DeckInstance}
   * @return uncategorized list of the {@link Card}s with their quantity
   */
  Map<CardPrinting, Integer> getCardPrintingQuantities();

  @Override
  boolean equals(Object other);

  @Override
  int hashCode();

  /**
   * Compares this DeckInstance with another DeckInstance for order based on their creation date &
   * time. Returns a zero if their creation times are the same, a negative int if this DeckInstance
   * was created after inputted DeckInstance, and a positive int if this DeckInstance was created
   * before the inputted DeckInstance.
   * @param other another {@link DeckInstance} to compare this {@link DeckInstance} to
   * @return int representing ordering of two DeckInstances
   * @throws IllegalArgumentException if given DeckInstance is null
   */
  @Override
  int compareTo(DeckInstance other) throws IllegalArgumentException;
}
