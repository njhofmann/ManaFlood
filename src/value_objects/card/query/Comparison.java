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
}
