package value_objects.query;

import value_objects.card.Card;

/**
 * Represents qualities of MTG {@link Card}s that are integers - power, toughness, loyalty, and
 * converted mana cost.
 */
public enum Stat {

  CMC("cmc"), // Converted mana cost of a card
  POWER("power"), // Power of a card, if it can be creature
  TOUGHNESS("toughness"), // Toughness of a card, if it can be a creature
  LOYALTY("loyalty"); // Loyalty of a card, if it can be a planeswalker

  private String string;

  /**
   * String form of a given stat
   * @param string string form for a stat
   */
  private Stat(String string) {
    this.string = string;
  }
}
