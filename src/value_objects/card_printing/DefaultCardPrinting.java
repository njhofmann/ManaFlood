package value_objects.card_printing;

import java.util.Objects;

/**
 * Default implementation of {@link CardPrinting} interface, simply provides a container object
 * with private fields and null checking for provided values.
 */
public class DefaultCardPrinting implements CardPrinting {

  /**
   * Name of card this {@link DefaultCardPrinting} represents.
   */
  private final String cardName;

  /**
   * Name of expansion the card this {@link DefaultCardPrinting} represents if from.
   */
  private final String cardExpansion;

  /**
   * Identifying value of the card from the expansion this {@link DefaultCardPrinting} represents.
   */
  private final String identifyingNumber;

  /**
   *
   * @param cardName name of card
   * @param cardExpansion expansion card is apart of
   * @param identifyingNumber identifying number / value of card in given expansion
   * @throws IllegalArgumentException if any of given parameters are null
   */
  public DefaultCardPrinting(String cardName, String cardExpansion, String identifyingNumber) {
    if (cardName == null) {
      throw new IllegalArgumentException("Given card name can't be null!");
    }
    else if (cardExpansion == null) {
      throw new IllegalArgumentException("Given card expansion can't be null!");
    }
    else if (identifyingNumber == null) {
      throw new IllegalArgumentException("Given identifying number can't be null!");
    }
    this.cardName = cardName;
    this.cardExpansion = cardExpansion;
    this.identifyingNumber = identifyingNumber;
  }

  @Override
  public String getCardName() {
    return cardName;
  }

  @Override
  public String getCardExpansion() {
    return cardExpansion;
  }

  @Override
  public String getIdentifyingNumber() {
    return identifyingNumber;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof CardPrinting) {
      CardPrinting casting = (CardPrinting)other;
      return casting.getCardName().equals(cardName)
          && casting.getCardExpansion().equals(cardExpansion)
          && casting.getIdentifyingNumber().equals(identifyingNumber);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardName, cardExpansion, identifyingNumber);
  }

  @Override
  public int compareTo(CardPrinting o) {
    int nameEquality = cardName.compareTo(o.getCardName());
    if (nameEquality != 0) {
      return nameEquality;
    }

    int expansionEquality = cardExpansion.compareTo(o.getCardExpansion());
    if (expansionEquality != 0) {
      return expansionEquality;
    }

    return identifyingNumber.compareTo(o.getIdentifyingNumber());
  }
}
