package value_objects.card.query;
import value_objects.card.Card;

/**
 * Enum specificying the different types of search options usable for parameters in a
 * {@link CardQuery}.
 */
public enum SearchOption {
  /**
   * Returned {@link Card} must have parameters associated with this option.
   */
  MustInclude,

  /**
   * Returned {@link Card} must have at least one of parameters linked with this options.
   */
  OneOf,

  /**
   * Returned {@link Card} can not have parameters associated with this option.
   */
  Disallow
}
