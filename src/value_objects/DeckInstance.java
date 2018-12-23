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
   * Returns a uncategorized list of the {@link Card}s that are apart of this {@link DeckInstance}.
   * @return uncategorized list of the {@link Card}s
   */
  List<Card> getCards();

  /**
   * Returns a categorized hashmap of the {@link Card}s that are apart of this {@link DeckInstance},
   * of categories in this {@link DeckInstance} to a list of {@link Card} that are apart of that
   * category.
   * @return uncategorized list of the {@link Card}s
   */
  HashMap<String, List<Card>> getCardsByCategory();

}
