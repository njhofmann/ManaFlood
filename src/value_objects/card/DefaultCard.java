package value_objects.card;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import value_objects.card.Card;
import value_objects.card.relationship.CardRelationship;

/**
 * Default implementation of the {@link Card} interface, a simple container to hold all the
 * information pertaining to a given card.
 */
public class DefaultCard implements Card {

  /**
   * Name of the Card as it appears in the CDDB.
   */
  private final String name;

  /**
   * Manacosts and their associated manacosts as they appear in the CDDB for this Card.
   */
  private final Map<String, Integer> manaCosts;

  /**
   * Text making up the Card as it appears in the CDDB.
   */
  private final String text;

  /**
   * Supertypes of the Card as it appears in the CDDB.
   */
  private final Set<String> supertypes;

  /**
   * Types of the Card as it appears in the CDDB.
   */
  private final Set<String> types;

  /**
   * Subtypes of the Card as it appears in the CDDB.
   */
  private final Set<String> subtypes;

  /**
   * Set of colors making up this Card's colors.
   */
  private final Set<String> colors;

  /**
   * Set of colors making up this Card's color identity.
   */
  private final Set<String> colorIdentity;

  /**
   * Relationship this Card has with other Cards, if any.
   */
  private final CardRelationship relationship;

  /**
   * Any additional info of the Card as it appears in the CDDB.
   */
  private final Map<String, Integer> additionalinfo;

  /**
   * Creates a {@link Card} as represented in the CDDB from the given parameters.
   * @param name name of the card
   * @param manaCosts mana costs and their associated quantities
   * @param text text making up what the card does
   * @param supertypes supertypes of the card
   * @param types types of the card
   * @param subtypes subtypes of the card
   * @param colors colors making up this card's colors
   * @param colorIdentity colors making up this card's color identity
   * @param relationship the relationship this card has with other cards, if any
   * @param additionalInfo any extra info the card may have, depending on its types and subtypes
   * @throws IllegalArgumentException if any of the given parameters are null, if name contains no
   * content, if set of types is empty (must have at least one type), or if additionalInfo doesn't
   * contain required info based off of given type (creature or vehicle --> power and toughness, planeswalker
   * --> loyalty)
   */
  public DefaultCard(String name, Map<String, Integer> manaCosts, String text,
      Set<String> colors, Set<String> colorIdentity, CardRelationship relationship,
      Set<String> supertypes, Set<String> types, Set<String> subtypes,
      Map<String, Integer> additionalInfo) {

    if (name == null || name.length() == 0) {
      throw new IllegalArgumentException("Given name can't be null or empty!");
    }
    else if (manaCosts == null) {
      throw new IllegalArgumentException("Given map of mana costs can't be null!");
    }
    else if (text == null) {
      throw new IllegalArgumentException("Given text can't be null!");
    }
    else if (supertypes == null) {
      throw new IllegalArgumentException("Given set of supertypes can't be null!");
    }
    else if (types == null || types.isEmpty()) {
      throw new IllegalArgumentException("Given set of supertypes can't be null or empty!");
    }
    else if (subtypes == null) {
      throw new IllegalArgumentException("Given set of subtypes can't be null!");
    }
    else if (colors == null || colors.isEmpty()) {
      throw new IllegalArgumentException("Given colors can't be null or have no associated colors!");
    }
    else if (colorIdentity == null || colorIdentity.isEmpty()) {
      throw new IllegalArgumentException("Given color identity can't be null or have no associated"
          + " colors!");
    }
    else if (relationship == null) {
      throw new IllegalArgumentException("Give relationship of cards cant' be null!");
    }
    else if (additionalInfo == null) {
      throw new IllegalArgumentException("Given map of additional info can't be null!");
    }
    else if (types.contains("creature") || subtypes.contains("vehicle")) {
      if (additionalInfo.size() != 2 || !additionalInfo.containsKey("power")
          || !additionalInfo.containsKey("toughness")) {
        throw new IllegalArgumentException("If card is a creature or vehicle, must contain exactly"
            + " two items, one for power and toughness!");
      }
    }
    else if (types.contains("planeswalker")) {
      if (additionalInfo.size() != 1 || !additionalInfo.containsKey("loyalty")) {
        throw new IllegalArgumentException("If card is a planeswalker, additional info should "
            + "contain exactly one item for loyalty!");
      }
    }

    this.name = name;
    this.manaCosts = manaCosts;
    this.text = text;
    this.supertypes = supertypes;
    this.types = types;
    this.subtypes = subtypes;
    this.colors = colors;
    this.colorIdentity = colorIdentity;
    this.relationship = relationship;
    this.additionalinfo = additionalInfo;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, Integer> getManaCost() {
    return Collections.unmodifiableMap(manaCosts);
  }

  @Override
  public Set<String> getColors() {
    return Collections.unmodifiableSet(colors);
  }

  @Override
  public Set<String> getColorIdentity() {
    return Collections.unmodifiableSet(colorIdentity);
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public Set<String> getSupertypes() {
    return Collections.unmodifiableSet(supertypes);
  }

  @Override
  public Set<String> getTypes() {
    return Collections.unmodifiableSet(types);
  }

  @Override
  public Set<String> getSubtypes() {
    return Collections.unmodifiableSet(subtypes);
  }

  @Override
  public Map<String, Integer> getExtraStats() {
    return Collections.unmodifiableMap(additionalinfo);
  }

  @Override
  public CardRelationship getRelationships() {
    return relationship;
  }

  @Override
  public int compareTo(Card other) throws IllegalArgumentException {
    if (other == null) {
      throw new IllegalArgumentException("Given card can't be null!");
    }
    return getName().compareTo(other.getName());
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof Card) {
      return name.equals(((Card) other).getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
