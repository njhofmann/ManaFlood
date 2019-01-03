package value_objects;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link DeckInstance}, a container for identifies of this DeckInstance
 * as well as the categories, cards, card quantities, and category contents that comprise it.
 */
public class DefaultDeckInstance implements DeckInstance {

  private final int parentDeckID;

  private final LocalDateTime creation;

  private final Set<String> cards;

  private final Set<String> categories;

  private final Map<String, Set<String>> categoryContents;

  private final Map<CardPrinting, Integer> cardQuantities;

  public DefaultDeckInstance(int parentDeckID, LocalDateTime creation, Set<String> cards,
      Set<String> categories, Map<String, Set<String>> categoryContents,
      Map<CardPrinting, Integer> cardQuantities) {
    if (creation == null) {
      throw new IllegalArgumentException("Give date and time of creation can't be null!");
    }
    else if (cards == null) {
      throw new IllegalArgumentException("Given list of cards can't be null!");
    }
    else if (categories == null) {
      throw new IllegalArgumentException("Given list of categories can't be null!");
    }
    else if (categoryContents == null) {
      throw new IllegalArgumentException("Given list of category contents can't be null!");
    }
    else if (cardQuantities == null) {
      throw new IllegalArgumentException("Given list of card printings to quantity in deck can't "
          + "be null!");
    }

   for (String category : categoryContents.keySet()) {
     if (!categories.contains(category)) {
       throw new IllegalArgumentException("Given set of deck categories doesn't match set of "
           + "categories in given mapping of category contents!");
     }
   }

    for (Set<String> categoryContent : categoryContents.values()) {
      for (String card : categoryContent) {
        if (!cards.contains(card)) {
          throw new IllegalArgumentException("Given mapping of category contents contains a card"
              + "that isn't in given list of cards!");
        }
      }
    }

    for (CardPrinting cardPrinting : cardQuantities.keySet()) {
      if (!cards.contains(cardPrinting.getCardName())) {
        throw new IllegalArgumentException("Given mapping of card printings to quantities contains"
            + "contains a card that isn't in given set of cards!");
      }
    }

    for (int quantity : cardQuantities.values()) {
      if (quantity < 1) {
        throw new IllegalArgumentException("Given mapping of card printings to quantities contains"
            + "a non-positive quantity of a card!");
      }
    }

    this.parentDeckID = parentDeckID;
    this.creation = creation;
    this.cards = Collections.unmodifiableSet(cards);
    this.categories = Collections.unmodifiableSet(categories);
    this.categoryContents = Collections.unmodifiableMap(categoryContents);
    this.cardQuantities = Collections.unmodifiableMap(cardQuantities);
  }

  @Override
  public int getParentDeckID() {
    return parentDeckID;
  }

  @Override
  public LocalDateTime getCreationInfo() {
    return creation;
  }

  @Override
  public Map<String, Integer> getCards() {
    return null;
  }

  @Override
  public Map<String, Set<String>> getCardsByCategory() {
    return null;
  }

  @Override
  public Map<CardPrinting, Integer> getCardPrintings() {
    return cardQuantities;
  }

  @Override
  public int compareTo(DeckInstance other) {
    return 0;
  }
}
