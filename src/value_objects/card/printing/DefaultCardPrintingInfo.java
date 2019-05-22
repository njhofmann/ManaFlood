package value_objects.card.printing;

import java.util.SortedSet;

/**
 * Default implementation of {@link CardPrintingInfo}, represents additional info for a given
 * {@link CardPrinting}.
 */
public class DefaultCardPrintingInfo implements CardPrintingInfo {

  /**
   * Card Printing this {@link CardPrintingInfo} represents.
   */
  private final CardPrinting cardPrinting;

  private final SortedSet<String> artists;

  private final String flavorText;

  private final String rarity;

  public DefaultCardPrintingInfo(String cardName, String cardExpansion, String identifyingNumber,
      SortedSet<String> artists, String flavorText, String rarity) {
    this(new DefaultCardPrinting(cardName, cardExpansion, identifyingNumber),
        artists, flavorText, rarity);
  }

  public DefaultCardPrintingInfo(CardPrinting cardPrinting, SortedSet<String> artists,
      String flavorText, String rarity) {

    if (artists == null || artists.isEmpty()) {
      throw new IllegalArgumentException("Given artists set can't be null or empty!");
    }
    else if (flavorText == null) {
      throw new IllegalArgumentException("Given flavor text can't be null!");
    }
    else if (rarity == null) {
      throw new IllegalArgumentException("Given rarity can't be null!");
    }
    else if (cardPrinting == null) {
      throw new IllegalArgumentException("Given CardPrinting can't be null!");
    }

    this.artists = artists;
    this.flavorText = flavorText;
    this.rarity = rarity;
    this.cardPrinting = cardPrinting;
  }

  @Override
  public String getFlavorText() {
    return flavorText;
  }

  @Override
  public SortedSet<String> getArtists() {
    return artists;
  }

  @Override
  public String getRarity() {
    return rarity;
  }

  @Override
  public String getCardName() {
    return cardPrinting.getCardName();
  }

  @Override
  public String getCardExpansion() {
    return cardPrinting.getCardExpansion();
  }

  @Override
  public String getIdentifyingNumber() {
    return cardPrinting.getIdentifyingNumber();
  }

  @Override
  public int compareTo(CardPrinting o) {
    return cardPrinting.compareTo(o);
  }
}
