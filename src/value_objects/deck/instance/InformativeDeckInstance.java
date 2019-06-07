package value_objects.deck.instance;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedSet;
import value_objects.card.Card;
import value_objects.card.printing.CardPrinting;
import value_objects.card.printing.InformativeCardPrinting;
import value_objects.deck.Deck;

public interface InformativeDeckInstance extends Comparable<InformativeDeckInstance> {

  /**
   * Returns the unique deck ID of the {@link Deck} this {@link InformativeDeckInstance} is apart of.
   * @return deck ID of parent deck
   */
  int getParentDeckID();

  /**
   * Returns the date and time this {@link InformativeDeckInstance} was created.
   * @return date & time of this DeckInstance's creation
   */
  LocalDateTime getCreationInfo();

  /**
   * Returns a unmodifiable, uncategorized list of the {@link Card}s that are apart of this
   * {@link InformativeDeckInstance}, and the quantity of that card in the DeckInstance
   * @return uncategorized list of the cards in the DeckInstance with their associated
   *         quantity
   */
  Map<Card, Integer> getCardQuantities();

  /**
   * Returns a categorized map of the {@link Card}s that are apart of this
   * {@link InformativeDeckInstance}, categories to a set of cards that are apart of that category.
   * @return uncategorized list of cards (by name)
   */
  Map<String, SortedSet<Card>> getCardsByCategory();

  /**
   * Returns set of {@link InformativeCardPrinting} in this {@link InformativeDeckInstance}
   * @return card printings in this instance
   */
  SortedSet<InformativeCardPrinting> getCardPrintings();

  /**
   * Returns set of card categories that are in this {@link InformativeDeckInstance}.
   * @return set of categories that cards can be in
   */
  SortedSet<String> getCategories();

  /**
   * Returns set of {@link Card}s that are in this {@link InformativeDeckInstance}
   * @return set of cards in this instance
   */
  SortedSet<Card> getCards();

  /**
   * Returns mapping of specific {@link InformativeCardPrinting} that are apart of this
   * {@link InformativeDeckInstance}, to the quantity of that card in the DeckInstance
   * @return uncategorized list of the {@link InformativeCardPrinting}s with their quantity
   */
  Map<InformativeCardPrinting, Integer> getCardPrintingQuantities();

  @Override
  boolean equals(Object other);

  @Override
  int hashCode();

  /**
   * Compares this DeckInstance with another DeckInstance for order based on their creation date &
   * time. Returns a zero if their creation times are the same, a negative int if this DeckInstance
   * was created after inputted DeckInstance, and a positive int if this DeckInstance was created
   * before the inputted DeckInstance.
   * @param other another {@link DeckInstance} to compare this DeckInstance to
   * @return int representing ordering of two DeckInstances
   * @throws IllegalArgumentException if given DeckInstance is null
   */
  @Override
  int compareTo(InformativeDeckInstance other) throws IllegalArgumentException;

}
