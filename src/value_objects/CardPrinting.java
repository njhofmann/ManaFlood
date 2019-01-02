package value_objects;

/**
 * Represents a specific printing of a MTG card in the CDDB, a card associated with a specific
 * expansion and identifying number (from that expansion).
 */
public interface CardPrinting {

  String getCardName();

  String getCardExpansion();

  String getIdentifyingNumber();

}
