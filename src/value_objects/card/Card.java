package value_objects.card;

import java.util.SortedSet;
import value_objects.card.relationship.CardRelationship;

import java.util.Map;
import java.util.Set;
import value_objects.card.printing.CardPrintingInfo;

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
   * Returns the converted mana cost of this {@link Card}.
   * @return cards converted mana cost
   */
  int getConvertedManaCost();

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
   * Returns a sorted set of all card printings that this Card is associated with - based on
   * specific set of expansions and printing numbers.
   * @return set of card printings this card is associated with
   */
  SortedSet<CardPrintingInfo> getCardPrintings();

  /**
   * If this {@link Card} has either power or toughness, or loyalty, returns that info in a
   * immutable Map<String, Integer> map.
   * If card has p/t returns as map with keys "power" and "toughness".
   * If card has loyalty returns a map with key "loyalty".
   * If card has no additional info returns only an empty map.
   * @return any extra stats this card may have
   */
  Map<String, String> getExtraStats();

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
   * Compares this Card with another Card for order based their names. Returns a negative int if
   * this Card's name comes after inputted Card's name alphabetically, and a positive int if this
   * Card's name comes before the inputted Card's name alphabetically. If names are the sames,
   * checks for equality off of card printings included within each card. Returns 0 if and only
   * if this Card and given Card have the same card printings.
   * @param other another {@link Card} to compare this {@link Card} to
   * @return int representing ordering of this Card and given card
   * @throws IllegalArgumentException if given Card is null
   */
  @Override
  int compareTo(Card other) throws IllegalArgumentException;

  /**
   * Returns if given object is equal to this {@link Card} if and only if it is a Card itself, it
   * has the same name as this card, and has the same card printings as this card.
   * @param other object to compare to
   * @return if this given object equals this Card
   */
  @Override
  boolean equals(Object other);
}
