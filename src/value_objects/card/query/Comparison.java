package value_objects.card.query;

/**
 * Represents possible comparisons between integers.
 */
public enum Comparison {

  UNEQUAL("!="),
  EQUAL("="),
  LESS("<"),
  LESS_EQUAL("<="),
  GREATER(">"),
  GREATER_EQUAL(">=");

  private String value;

  /**
   * String value of each type of comparison.
   * @param value string value of a comparison
   */
  private Comparison(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static Comparison getComparison(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Given value can't be null!");
    }

    for (Comparison comparison : Comparison.values()) {
      if (comparison.getValue().equals(value)) {
        return comparison;
      }
    }
    throw new IllegalArgumentException("Given string value doesn't match any value of this enum!");
  }
}
