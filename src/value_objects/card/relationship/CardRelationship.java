package value_objects.card.relationship;

import java.util.SortedSet;
import value_objects.card.Card;

/**
 * Represents the relationship between a group of {@link Card}s - the cards in the relationship,
 * the nature of the relationship, and if it even represents a relationship.
 */
public interface CardRelationship {

  /**
   * Returns if this {@link CardRelationship} actually represents a relationship, or if it is
   * "empty".
   * @return if this CardRelationship represnets a relationship or is empty
   */
  boolean hasRelationship();

  /**
   * Returns the cards apart of the relationship this {@link CardRelationship} represents. Returns
   * an empty set if this CardRelationship doesn't represent any relationship.
   * @return cards in this relationship
   */
  SortedSet<String> getCards();

  /**
   * Returns the nature of the relationship between the cards this {@link CardRelationship} holds.
   * Returns an empty string if this CardRelationship doesn't represent any relationship.
   * @return nature of this relationship
   */
  String getRelationship();
}
