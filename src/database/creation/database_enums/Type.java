package database.creation.database_enums;

public enum Type {

  ARTIFACT(1, "artifact"),
  CONSPIRACY(2, "conspiracy"),
  CREATURE(3, "creature"),
  ENCHANTMENT(4, "enchantment"),
  INSTANT(5, "instant"),
  LAND(6, "land"),
  PHENOMENON(7, "phenomenon"),
  PLANE(8, "plane"),
  PLANESWALKER(9, "planeswalker"),
  SCHEME(10, "scheme"),
  SORCERY(11, "sorcery"),
  TRIBAL(12, "tribal"),
  VANGUARD(13, "vanguard");

  /**
   * Primary key ID of this type in the CDDB.
   */
  private final int databaseID;

  /**
   * String version of this type in the CDDB.
   */
  private final String type;

  /**
   *
   * @param databaseID
   * @param type
   */
  Type(final int databaseID, final String type) {
    this.databaseID = databaseID;
    this.type = type;
  }

  /**
   * Given a string, returns the type of {@link Type} that the string matches.
   * @param toMatch string to compare
   * @return Type given String matches
   * @throws IllegalArgumentException if given String doesn't match one type of Type
   */
  public static Type matches(String toMatch) {
    if (toMatch == null) {
      throw new IllegalArgumentException("Given String can't be null!");
    }

    toMatch = toMatch.toLowerCase();

    for (Type type : Type.values()) {
      if (type.getType().equals(toMatch)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Given String doesn't match and Type!");
  }

  /**
   * Returns the string representing this {@link Type}.
   * @return string representing this type
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the primary key ID of this {@link Type} in the CDDB.
   * @return primary key ID in of this type
   */
  public int getDatabaseID() {
    return databaseID;
  }
}
