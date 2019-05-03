package value_objects.query;

import value_objects.card.Card;

/**
 * Represents qualities of MTG {@link Card}s that are integers - power, toughness, loyalty, and
 * converted mana cost, and  quantity of a mana type. If mana type quantity, must associate
 * with mana type separately.
 */
public enum Stat {

  /**
   * Converted mana cost of a card.
   */
  CMC("cmc"),

  /**
   * Power of a card, if it can be creature.
   */
  POWER("power"),

  /**
   * Toughness of a card, if it can be a creature.
   */
  TOUGHNESS("toughness"),

  /**
   * Loyalty of a card, if it can be a planeswalker.
   */
  LOYALTY("loyalty"),

  /**
   * Quantity of a mana type in a card, must be associated with a mana type separately.
   */
  MANA_TYPE_COUNT("mana_type_count");

  private String string;

  /**
   * String form of a given stat
   * @param string string form for a stat
   */
  private Stat(String string) {
    this.string = string;
  }
}
