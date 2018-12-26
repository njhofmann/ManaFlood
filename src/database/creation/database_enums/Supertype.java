package database.creation.database_enums;

/**
 * Represents all the possible supertypes a MTG card may have, as reflected in the CDDB.
 */
public enum Supertype {
  BASIC(1, "basic"),
  ELITE(2, "elite"),
  LEGENDARY(3, "legendary"),
  ONGOING(4, "ongoing"),
  SNOW(5, "snow"),
  WORLD(6, "world");

  /**
   * Primary key ID of this supertype in the CDDB.
   */
  private final int databaseID;

  /**
   * String version of this supertype in the CDDB.
   */
  private final String type;

  Supertype(final int databaseID, final String type) {
    this.databaseID = databaseID;
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

  /**
   * Returns the primary key ID of this {@link Supertype} in the CDDB.
   * @return primary key ID in of this type
   */
  public int getDatabaseID() {
    return databaseID;
  }
}
