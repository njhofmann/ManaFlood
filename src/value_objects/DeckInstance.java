package value_objects;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a single instance in the history of a {@code Deck}. Implements Comparable so a
 * collection of {@link DeckInstance} can be sorted by their creation date & time.
 */
public interface DeckInstance extends Comparable<DeckInstance> {

  /**
   * Returns the unique deck ID of the {@link Deck} this {@link DeckInstance} is apart of.
   * @return deck ID of parent ID
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
  Map<String, Integer> getCards();

  /**
   * Returns a categorized hashmap of the cards (by name) that are apart of this
   * {@link DeckInstance}, categories in this {@link DeckInstance} to a list of card IDs that are
   * apart of that category.
   * @return uncategorized list of cards (by name)
   */
  Map<String, Set<String>> getCardsByCategory();

  /**
   * Returns mapping of specific card printings that are apart of this {@link DeckInstance}, to the
   * quantity of that card in the {@link DeckInstance}
   * @return uncategorized list of the {@link Card}s with their quantity
   */
  Map<CardPrinting, Integer> getCardPrintings();

  /**
   * Compares this DeckInstance with another DeckInstance for order based on their creation date &
   * time. Returns a zero if their creation times are the same, a negative int if this DeckInstance
   * was created after inputted DeckInstance, and a positive int if this DeckInstance was created
   * before the inputted DeckInstance.
   * @param other another {@link DeckInstance} to compare this {@link DeckInstance} to
   * @return int representing ordering of two DeckInstances
   */
  @Override
  int compareTo(DeckInstance other);
}
