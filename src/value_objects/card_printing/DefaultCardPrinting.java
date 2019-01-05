package value_objects.card_printing;

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
}
