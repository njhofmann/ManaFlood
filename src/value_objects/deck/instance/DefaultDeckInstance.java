package value_objects.deck.instance;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import value_objects.deck.Deck;
import value_objects.card.printing.CardPrinting;

/**
 * Default implementation of {@link DeckInstance}, a container for identifies of this DeckInstance
 * as well as the categories, cards, card quantities, and category contents that comprise it.
 */
public class DefaultDeckInstance implements DeckInstance {

  /**
   * Unique ID of the {@link Deck} this {@link DeckInstance}
   */
  private final int parentDeckID;

  /**
   * Time and date this {@link DeckInstance} was created.
   */
  private final LocalDateTime creation;

  /**
   * The set of cards that make up this DeckInstance, without quantities.
   */
  private final SortedSet<String> cards;

  /**
   * Mapping of categories in this DeckInstance to the cards that are apart of them.
   */
  private final Map<String, SortedSet<String>> categoryContents;

  /**
   * Mapping of specific card printings to their quantities in this deck instance.
   */
  private final Map<CardPrinting, Integer> cardPrintingQuantities;

  /**
   * Constructs a instance of some {@link Deck} from its parent Deck's unique ID, when it was
   * created, card printings to their quantities in this instance, and categories to their card
   * contents in this instance.
   * @param parentDeckID unique ID of parent deck
   * @param creation when this instance was created
   * @param categoryContents categories of this instance to their card contents
   * @param cardQuantities quantities of specific card printings that make up this instance
   * @throws IllegalArgumentException if {@param cardQuantities} contains a non-positive quantity,
   *         or if there is a card in cardQuantities that isn't in a category in
   *         {@param categoryContents}, or if any parameter is null
   */
  public DefaultDeckInstance(int parentDeckID, LocalDateTime creation,
      Map<String, SortedSet<String>> categoryContents,
      Map<CardPrinting, Integer> cardQuantities) {
    if (creation == null) {
      throw new IllegalArgumentException("Give date and time of creation can't be null!");
    }
    else if (categoryContents == null) {
      throw new IllegalArgumentException("Given list of category contents can't be null!");
    }
    else if (cardQuantities == null) {
      throw new IllegalArgumentException("Given list of card printings to quantity in deck can't "
          + "be null!");
    }

    SortedSet<String> cards = new TreeSet<>();
    for (Set<String> categoryContent : categoryContents.values()) {
      for (String card : categoryContent) {
        if (!cards.contains(card)) {
          cards.add(card);
        }
      }
    }

    for (CardPrinting cardPrinting : cardQuantities.keySet()) {
      if (cards.contains(cardPrinting.getCardName())) {
        throw new IllegalArgumentException("Given mapping of card printing to quantities contains"
            + " a card not in given mapping of categories to card!");
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
    this.cards = Collections.unmodifiableSortedSet(cards);
    this.categoryContents = Collections.unmodifiableMap(categoryContents);
    this.cardPrintingQuantities = Collections.unmodifiableMap(cardQuantities);
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
  public Map<String, Integer> getCardQuantities() {
    Map<String, Integer> toReturn = new HashMap<>();
    for (CardPrinting cardPrinting : cardPrintingQuantities.keySet()) {
      String cardName = cardPrinting.getCardName();
      int cardQuantity = cardPrintingQuantities.get(cardPrinting);
      if (toReturn.containsKey(cardName)) {
        toReturn.put(cardName, toReturn.get(cardName) + cardQuantity);
      }
      else {
        toReturn.put(cardName, cardQuantity);
      }
    }
    return toReturn;
  }

  @Override
  public Map<String, SortedSet<String>> getCardsByCategory() {
    return categoryContents;
  }

  @Override
  public SortedSet<CardPrinting> getCardPrintings() {
    return Collections.unmodifiableSortedSet(new TreeSet<>(cardPrintingQuantities.keySet()));
  }

  @Override
  public SortedSet<String> getCategories() {
    return Collections.unmodifiableSortedSet(new TreeSet<>(categoryContents.keySet()));
  }

  @Override
  public SortedSet<String> getCards() {
    return Collections.unmodifiableSortedSet(cards);
  }

  @Override
  public Map<CardPrinting, Integer> getCardPrintingQuantities() {
    return cardPrintingQuantities;
  }

  @Override
  public int compareTo(DeckInstance other) {
    if (other == null) {
      throw new IllegalArgumentException("Given deck instance can't be null!");
    }
    return this.getCreationInfo().compareTo(other.getCreationInfo());
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof DeckInstance) {
      DeckInstance casting = (DeckInstance)other;
      return casting.getParentDeckID() == parentDeckID
          && casting.getCreationInfo().equals(creation);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(parentDeckID, creation);
  }
}
