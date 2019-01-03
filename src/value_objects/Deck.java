package value_objects;

import java.util.List;

/**
 * Represents the "history" of a specific deck of {@link Card}s as stored in the Card & Deck
 * Database (CDDB).
 */
public interface Deck {

  /**
   * Returns the unique deck ID of this {@link Deck}.
   * @return deck ID of this {@link Deck}
   */
  int getDeckID();

  /**
   * Returns the deck name of this {@link Deck}.
   * @return name of this {@link Deck}
   */
  String getDeckName();

  /**
   * Returns the description given for this {@link Deck}.
   * @return {@link Deck}'s description
   */
  String getDescription();

  /**
   * Returns a unmodifiable list of all {@link DeckInstance} that make up this {@link Deck}'s
   * history, sorted by oldest to newest.
   * @return sorted list of {@link DeckInstance} that make up this deck
   */
  List<DeckInstance> getHistory();
}
