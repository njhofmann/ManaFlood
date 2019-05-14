package value_objects.card_printing;

/**
 * Represents a specific printing of a MTG card in the CDDB, a card associated with a specific
 * expansion and identifying number (from that expansion).
 */
public interface CardPrinting extends Comparable<CardPrinting> {

  /**
   * Returns immutable reference name of card this {@link CardPrinting} is a printing of.
   * @return card name this printing represents
   */
  String getCardName();

  /**
   * Returns expansion this {@link CardPrinting} is from.
   * @return this printing's expansion
   */
  String getCardExpansion();

  /**
   * Returns to the print number from the above expansion this {@link CardPrinting} represents.
   * @return print number from linked expansion
   */
  String getIdentifyingNumber();

}
