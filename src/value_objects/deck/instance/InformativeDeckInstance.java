package value_objects.deck.instance;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedSet;
import value_objects.card.Card;
import value_objects.card.printing.CardPrinting;
import value_objects.card.printing.InformativeCardPrinting;
import value_objects.deck.Deck;

/**
 * An extension of the {@link DeckInstance} interface, intended to maintain the information already
 * represented by a related {@link DeckInstance}, as well as information specific to Cards and
 * {@link CardPrinting}s stored in the {@link DeckInstance} in the form of {@link Card}s and
 * {@link InformativeCardPrinting}s.
 */
public interface InformativeDeckInstance extends DeckInstance {

  /**
   * Returns a unmodifiable, uncategorized list of the {@link Card}s that are apart of this
   * {@link InformativeDeckInstance}, and the quantity of that card in the DeckInstance
   * @return uncategorized mapping of the cards in the DeckInstance with their associated
   *         quantity
   */
  Map<Card, Integer> getCardQuantities();

  /**
   * Returns a map of the {@link Card}s that are apart of this
   * {@link InformativeDeckInstance}, categories to a set of Cards that are apart of that category.
   * @return Cards in each category in this InformativeDeckInstance
   */
  Map<String, SortedSet<Card>> getCardsByCategory();

  /**
   * Returns set of {@link InformativeCardPrinting} in this {@link InformativeDeckInstance}
   * @return card printings in this instance
   */
  SortedSet<InformativeCardPrinting> getInformativeCardPrintings();

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
  Map<InformativeCardPrinting, Integer> getInformativeCardPrintingQuantities();
}
