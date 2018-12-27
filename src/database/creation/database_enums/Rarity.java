package database.creation.database_enums;

/**
 * Represents the different types of rarities on a MTG card, with IDs as reflected in the CDDB.
 */
public enum Rarity {
  MYTHIC_RARE("mythic rare"),
  RARE("rare"),
  UNCOMMON("uncommon"),
  COMMON("common");

  /**
   * String version of this rarity in the CDDB.
   */
  private final String type;

  Rarity(final String type) {
    this.type = type;
  }

  /**
   * Given a string, returns the type of {@link Rarity} that the string matches.
   * @param toMatch string to compare
   * @return Rarity given String matches
   * @throws IllegalArgumentException if given String doesn't match one type of Rarity
   */
  public static Rarity matches(String toMatch) {
    if (toMatch == null) {
      throw new IllegalArgumentException("Given String can't be null!");
    }

    toMatch = toMatch.toLowerCase();

    for (Rarity rarity : Rarity.values()) {
      if (rarity.getType().equals(toMatch)) {
        return rarity;
      }
    }
    throw new IllegalArgumentException("Given String doesn't match and Type!");
  }

  /**
   * Returns the string representing this {@link Rarity}.
   * @return string representing this Rarity
   */
  public String getType() {
    return type;
  }
}
