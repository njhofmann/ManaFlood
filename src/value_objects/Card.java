package value_objects;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents all info of an individual card in the Card and Deck Database (CDDB).
 */
public interface Card {

  /**
   * Returns the unique, identifying name of this {@link Card}.
   * @return card's name
   */
  String getName();

  /**
   * Returns the mana symbols and their quantities that make up this {@link Card}'s mana cost.
   * @return card's mana cost
   */
  Map<String, Integer> getManaCost();

  /**
   * Returns the text making up this {@link Card}.
   * @return card's text
   */
  String getText();

  /**
   * Returns the supertypes of this {@link Card}.
   * @return card's supertypes
   */
  Set<String> getSupertypes();

  /**
   * Returns the types of this {@link Card}.
   * @return card's types
   */
  Set<String> getTypes();

  /**
   * Returns the subtypes of this {@link Card}.
   * @return card's subtypes
   */
  Set<String> getSubtypes();

  /**
   * If this {@link Card} has either power or toughness, or loyalty, returns that info in a int
   * array.
   * If card has p/t returns as array of length 2 in form of "[power, toughness]".
   * If card has loyalty returns a array of length 1 in form of "[loyalty]".
   * If card has no additional info returns only an empty array.
   * @return any extra stats this card may have
   */
  int[] getExtraStats();
}
