package value_objects.card.relationship;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import value_objects.card.Card;

/**
 * Default implementation of the {@link CardRelationship} interface, a simple value object to
 * represent the nature of the relationship between {@link Card}s.
 */
public class DefaultCardRelationship implements CardRelationship {

  /**
   * Cards making up this {@link CardRelationship}.
   */
  private final SortedSet<String> cards;

  /**
   * Returns the nature of the relationship between the cards this {@link CardRelationship}
   * represents.
   */
  private final String relationship;

  public DefaultCardRelationship(SortedSet<String> cards, String relationship) {
    if (cards == null || cards.size() < 2) {
      throw new IllegalArgumentException("Given set of cards for this relationship can't be null "
          + "and must contain two cards or more!");
    }
    else if (relationship == null || relationship.isBlank()) {
      throw new IllegalArgumentException("Give nature of this relationship can't be null nor "
          + "be empty!");
    }
    this.cards = cards;
    this.relationship = relationship;
  }

  /**
   * Constructs an empty {@link CardRelationship}, that doesn't represent any relationship between
   * cards. Used to represent that an associated {@link Card} has no relationships with other Cards.
   */
  public DefaultCardRelationship() {
    this.cards = new TreeSet<>();
    this.relationship = "";
  }

  @Override
  public boolean hasRelationship() {
    return !cards.isEmpty();
  }

  @Override
  public SortedSet<String> getCards() {
    return Collections.unmodifiableSortedSet(cards);
  }

  @Override
  public String getRelationship() {
    return relationship;
  }
}
