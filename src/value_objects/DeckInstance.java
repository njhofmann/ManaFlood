package value_objects;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a single instance in the history of a {@code Deck}.
 */
public interface DeckInstance {

  /**
   * Returns the unique deck ID of the {@link Deck} this {@link DeckInstance} is apart of.
   * @return
   */
  int getParentDeckID();

  /**
   * Returns the date and time this {@link DeckInstance} was created.
   * @return
   */
  LocalDateTime getCreationInfo();

  /**
   * Returns a uncategorized list of the cards (by name) that are apart of this {@link DeckInstance},
   * and
   * the quantity of that card in the {@link DeckInstance}
   * @return uncategorized list of the cards in the {@link DeckInstance} with their associated
   *         quantity
   */
  HashMap<String, Integer> getCards();

  /**
   * Returns a categorized hashmap of the cards (by name) that are apart of this
   * {@link DeckInstance}, categories in this {@link DeckInstance} to a list of card IDs that are
   * apart of that category.
   * @return uncategorized list of cards (by name)
   */
  HashMap<String, List<String>> getCardsByCategory();

  /**
   * Returns mapping of specific card printings that are apart of this {@link DeckInstance}, to the
   * quantity of that card in the {@link DeckInstance}
   * @return uncategorized list of the {@link Card}s with their quantity
   */
  HashMap<CardPrinting, Integer> getCardPrintings();
}
