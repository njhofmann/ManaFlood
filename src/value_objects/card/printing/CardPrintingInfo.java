package value_objects.card.printing;

/**
 * Represents a additional info specific printing of a MTG card in the CDDB, a card associated with a specific
 * expansion and identifying number (from that expansion).
 */
public interface CardPrintingInfo extends CardPrinting {

  String getFlavorText();

  String getArtist();

  String getRarity();
}
