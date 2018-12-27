package database.creation.database_enums;

/**
 * Represents all the possible supertypes a MTG card may have, as reflected in the CDDB.
 */
public enum Supertype {
  BASIC("basic"),
  ELITE("elite"),
  LEGENDARY("legendary"),
  ONGOING("ongoing"),
  SNOW("snow"),
  WORLD("world");

  /**
   * String version of this supertype in the CDDB.
   */
  private final String type;

  Supertype(final String type) {
    this.type = type;
  }

  /**
   * Given a string, returns the type of {@link Supertype} that the string matches.
   * @param toMatch string to compare
   * @return Supertype given String matches
   * @throws IllegalArgumentException if given String doesn't match one type of Type
   */
  public static Supertype matches(String toMatch) {
    if (toMatch == null) {
      throw new IllegalArgumentException("Given String can't be null!");
    }

    toMatch = toMatch.toLowerCase();

    for (Supertype supertype : Supertype.values()) {
      if (supertype.getType().equals(toMatch)) {
        return supertype;
      }
    }
    throw new IllegalArgumentException("Given String doesn't match any Supertype!");
  }

  /**
   * Returns the string representing this {@link Supertype}.
   * @return string representing this supertype
   */
  public String getType() {
    return type;
  }

}
