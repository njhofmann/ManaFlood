package value_objects.card.printing.info;

import java.util.SortedSet;
import value_objects.card.printing.CardPrinting;

/**
 * Represents a additional info specific printing of a MTG card in the CDDB, a card associated with
 * a specific expansion and identifying number (from that expansion).
 */
public interface CardPrintingInfo extends CardPrinting {

  /**
   * Retrieves the flavor text for the printings of this {@link CardPrinting}.
   * @return rarity for this CardPrinting
   */
  String getFlavorText();

  /**
   * Retrieves the artists who created the art for the specific instance of this {@link CardPrinting}.
   * @return artists who created the art for this card
   */
  SortedSet<String> getArtists();

  /**
   * Retrieves the rarity the printings of this {@link CardPrinting} was printed at.
   * @return rarity for this CardPrinting
   */
  String getRarity();

  /**
   * Returns the Scryfall id of this specific card printing, used to retrieve specific data not
   * stored in the CDDB such as card images and rulings.
   * @return Scryfall ID of this card
   */
  String getScryfallID();
}
