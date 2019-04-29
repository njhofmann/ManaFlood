package value_objects.card;

import value_objects.card.relationship.CardRelationship;

import java.util.Map;
import java.util.Set;

/**
 * Represents all info of an individual card in the Card and Deck Database (CDDB). Uniqueness is
 * determined by the name of this {@link Card}.
 */
public interface Card extends Comparable<Card> {

  /**
   * Returns the unique, identifying name of this {@link Card}.
   * @return card's name
   */
  String getName();

  /**
   * Returns the mana symbols and their quantities that make up this {@link Card}'s mana cost
   * as an immutable map.
   * @return card's mana cost
   */
  Map<String, Integer> getManaCost();

  /**
   * Returns the colors (WUBRG or colorless) that make up this {@link Card}'s colors.
   * @return set of this card's colors
   */
  Set<String> getColors();

  /**
   * Returns the colors (WUBRG or colorless) that make up this {@link Card}'s color identity.
   * @return set of this card's color identity
   */
  Set<String> getColorIdentity();

  /**
   * Returns the text making up this {@link Card}.
   * @return card's text
   */
  String getText();

  /**
   * Returns the supertypes of this {@link Card} as an immutable set.
   * @return card's supertypes
   */
  Set<String> getSupertypes();

  /**
   * Returns the types of this {@link Card} as an immutable set.
   * @return card's types
   */
  Set<String> getTypes();

  /**
   * Returns the subtypes of this {@link Card}.
   * @return card's subtypes
   */
  Set<String> getSubtypes();

  /**
   * If this {@link Card} has either power or toughness, or loyalty, returns that info in a
   * immutable Map<String, Integer> map.
   * If card has p/t returns as map with keys "power" and "toughness".
   * If card has loyalty returns a map with key "loyalty".
   * If card has no additional info returns only an empty map.
   * @return any extra stats this card may have
   */
  Map<String, Integer> getExtraStats();

  /**
   * If this {@link Card} has a relationship with one or more other cards, returns the other
   * cards (and itself) and the nature of their relationship. If no relationship, then returned
   * {@link CardRelationship} will return "false" for its {@link CardRelationship#hasRelationship}
   * method. For example, if the card is transform card - returns the  card this card will
   * transform into (and itself), and "transform" as the nature of their relationship.
   * @return CardRelationship representing any relationship this card may have
   */
  CardRelationship getRelationships();

  /**
   * Compares this Card with another Card for order based their names. Returns a zero if their
   * names are the same, a negative int if this Card's name comes after inputted Card's name
   * alphabetically, and a positive int if this Card's name comes before the inputted Card's name
   * alphabetically.
   * @param other another {@link Card} to compare this {@link Card} to
   * @return int representing ordering of two DeckInstances
   * @throws IllegalArgumentException if given Card is null
   */
  @Override
  int compareTo(Card other) throws IllegalArgumentException;
}
