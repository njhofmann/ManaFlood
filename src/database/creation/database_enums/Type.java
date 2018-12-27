package database.creation.database_enums;

public enum Type {

  ARTIFACT("artifact"),
  CONSPIRACY("conspiracy"),
  CREATURE("creature"),
  ENCHANTMENT("enchantment"),
  INSTANT("instant"),
  LAND("land"),
  PHENOMENON("phenomenon"),
  PLANE("plane"),
  PLANESWALKER("planeswalker"),
  SCHEME("scheme"),
  SORCERY("sorcery"),
  TRIBAL("tribal"),
  VANGUARD("vanguard");

  /**
   * String version of this type in the CDDB.
   */
  private final String type;

  /**
   *
   * @param type
   */
  Type(final String type) {
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
}
