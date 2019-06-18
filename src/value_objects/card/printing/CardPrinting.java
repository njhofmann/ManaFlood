package value_objects.card.printing;

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

  /**
   * Returns if the given Object is the same as this {@link CardPrinting} if and only if it is also
   * a CardPrinting, and it has the same card name, expansion, and identifying number as this
   * CardPrinting - else returns false.
   * @param other other object to compare to
   * @return if given object is the same as this CardPrinting
   */
  @Override
  boolean equals(Object other);

  /**
   * Returns the overriden hashcode of this {@link CardPrinting} as a hashing based on its name,
   * expansion, and number.
   * @return overriden hashcode of this CardPrinting
   */
  @Override
  int hashCode();

  /**
   * Returns how a given {@link CardPrinting} compares to this CardPrinting. Returns a negative
   * number if this CP comes before the given CP, a positive if after, and 0 if they are the same
   * CP. Checks for ordering based off of card name, then expansion, then number.
   * @param cardPrinting CardPrinting to compare this CardPrinting against
   * @return how given CardPrinting compares to this CardPrinting
   * @throws IllegalArgumentException if the given CardPrinting is null
   */
  @Override
  int compareTo(CardPrinting cardPrinting);
}
