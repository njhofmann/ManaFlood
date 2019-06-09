package value_objects.deck.instance;

import java.lang.ProcessHandle.Info;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import value_objects.card.Card;
import value_objects.card.printing.CardPrinting;
import value_objects.card.printing.InformativeCardPrinting;

public class DefaultInformativeDeckInstance implements InformativeDeckInstance {

  private final int parentDeckID;

  private final LocalDateTime creation;

  private final SortedSet<Card> cards;

  private final Map<String, SortedSet<Card>> categoryCardContents;

  private final Map<InformativeCardPrinting, Integer> cardPrintingQuantities;

  private final Map<String, Card> cardNameToCard;

  /**
   *
   * @param parentDeckID
   * @param creation
   * @param categoryCardContents
   * @param cardPrintingQuantities
   */
  public DefaultInformativeDeckInstance(int parentDeckID, LocalDateTime creation,
      Map<String, SortedSet<Card>> categoryCardContents,
      Map<InformativeCardPrinting, Integer> cardPrintingQuantities) {

    if (creation == null) {
      throw new IllegalArgumentException("Give date and time of creation can't be null!");
    }
    else if (categoryCardContents == null) {
      throw new IllegalArgumentException("Given list of category contents can't be null!");
    }
    else if (cardPrintingQuantities == null) {
      throw new IllegalArgumentException("Given list of card printings to quantity in deck can't "
          + "be null!");
    }

    cardNameToCard = new HashMap<>();

    SortedSet<InformativeCardPrinting> cardPrintingsFromCard = new TreeSet<>();
    SortedSet<Card> cards = new TreeSet<>();
    for (Set<Card> categoryCardContent : categoryCardContents.values()) {
      cards.addAll(categoryCardContent);
      for (Card card : categoryCardContent) {
        cardPrintingsFromCard.addAll(card.getCardPrintings());
        String cardName = card.getName();
        if (cardNameToCard.containsKey(cardName)) {
          throw new IllegalArgumentException("");
        }
        cardNameToCard.put(cardName, card);
      }
    }

    // Cumulative set of card printings included from each given card should equal the card
    // printings given in map of card printings to quantities
    SortedSet<InformativeCardPrinting> cardPrintingsFromQuantity = new TreeSet<>(cardPrintingQuantities.keySet());
    if (!cardPrintingsFromCard.equals(cardPrintingsFromQuantity)) {
      throw new IllegalArgumentException("Given");
    }

    // Check for nonpositive card printing quantities
    for (int quantity : cardPrintingQuantities.values()) {
      if (quantity < 1) {
        throw new IllegalArgumentException("Given mapping of card printings to quantities contains"
            + "a non-positive quantity of a card!");
      }
    }

    this.parentDeckID = parentDeckID;
    this.creation = creation;
    this.cards = Collections.unmodifiableSortedSet(cards);
    this.categoryCardContents = Collections.unmodifiableMap(categoryCardContents);
    this.cardPrintingQuantities = Collections.unmodifiableMap(cardPrintingQuantities);
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
  public Map<Card, Integer> getCardQuantities() {
    Map<Card, Integer> toReturn = new HashMap<>();

    for (Card card : cards) {
      toReturn.put(card, 0);
    }

    for (InformativeCardPrinting cardPrinting : cardPrintingQuantities.keySet()) {
      Card associatedCard = cardNameToCard.get(cardPrinting.getCardName());
      if (associatedCard.getCardPrintings().contains(cardPrinting)) {
        int newQuantity = cardPrintingQuantities.get(cardPrinting) + toReturn.get(associatedCard);
        toReturn.put(associatedCard, newQuantity);
      }
      else {
        throw new IllegalStateException(""); // Should never be reached
      }
    }

    return toReturn;
  }

  @Override
  public Map<String, SortedSet<Card>> getCardsByCategory() {
    return categoryCardContents;
  }

  @Override
  public SortedSet<InformativeCardPrinting> getCardPrintings() {
    return Collections.unmodifiableSortedSet(new TreeSet<>(cardPrintingQuantities.keySet()));
  }

  @Override
  public SortedSet<String> getCategories() {
    return Collections.unmodifiableSortedSet(new TreeSet<>(categoryCardContents.keySet()));
  }

  @Override
  public SortedSet<Card> getCards() {
    return cards;
  }

  @Override
  public Map<InformativeCardPrinting, Integer> getCardPrintingQuantities() {
    return cardPrintingQuantities;
  }

  @Override
  public int compareTo(InformativeDeckInstance other) throws IllegalArgumentException {
    return 0;
  }
}