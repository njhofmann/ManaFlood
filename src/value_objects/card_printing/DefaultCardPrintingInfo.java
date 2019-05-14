package value_objects.card_printing;

public class DefaultCardPrintingInfo implements CardPrintingInfo {

  private final CardPrinting cardPrinting;

  private final String artist;

  private final String flavorText;

  private final String rarity;

  public DefaultCardPrintingInfo(String cardName, String cardExpansion, String identifyingNumber,
      String artist, String flavorText, String rarity) {
    cardPrinting = new DefaultCardPrinting(cardName, cardExpansion, identifyingNumber);

    if (artist == null) {
      throw new IllegalArgumentException("Given artist can't be null!");
    }
    else if (flavorText == null) {
      throw new IllegalArgumentException("Given flavor text can't be null!");
    }
    else if (identifyingNumber == null) {
      throw new IllegalArgumentException("Given rarity can't be null!");
    }

    this.artist = artist;
    this.flavorText = flavorText;
    this.rarity = rarity;
  }

  @Override
  public String getFlavorText() {
    return flavorText;
  }

  @Override
  public String getArtist() {
    return artist;
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
