package value_objects.card.query;

import value_objects.card.Card;

/**
 * Represents a query for {@link Card}s in the Card and Deck Database (CDDB) based off a selected
 * number of parameters. Build up query by calling selected methods, then executing 
 * {@link CardQuery#asQuery()} to get full query.
 */
public interface CardQuery {

  /**
   * Add parameter to search for cards with given word in their name, or not in their name.
   * No spaces in included word, nor non empty word.
   * @param word word to search by
   * @param searchFor to search for cards with given word in their name, or not in their name
   * @throws IllegalArgumentException if the given word is null, has a space, or is empty
   */
  void byName(String word, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given word in their text, or not in their text.
   * No spaces in included word, nor non empty word.
   * @param text text to search by
   * @param searchFor to search for cards with given word in their text, or not in their text
   * @throws IllegalArgumentException if the given word is null, has a space, or is empty
   */
  void byText(String text, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards that are the given color, or not the given color.
   * @param color color to search by
   * @param searchFor to search for cards with given color in their color, or not in their color
   * @throws IllegalArgumentException if the given color isn't a supported color by the CDDB,
   * or is null
   */
  void byColor(String color, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given color in their color identity, or not in their
   * color identity.
   * @param color color to search by
   * @param searchFor to search for cards with given color in their color identity,
   * or not in their color identity
   * @throws IllegalArgumentException if the given color isn't a supported color by the CDDB,
   * or is null
   */
  void byColorIdentity(String color, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given supertype, or not the given type.
   * @param type supertype to search by
   * @param searchFor to search for cards with the given type, or not the given type
   * @throws IllegalArgumentException if the given type isn't a supported supertype by the CDDB,
   * or is null
   */
  void bySupertype(String type, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given type, or not the given type.
   * @param type type to search by
   * @param searchFor to search for cards with the given type, or not the given type
   * @throws IllegalArgumentException if the given type isn't a supported type by the CDDB,
   * or is null
   */
  void byType(String type, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given subtype, or not the given subtype.
   * @param type subtype to search by
   * @param searchFor to search for cards with the given type, or not the given type
   * @throws IllegalArgumentException if the given type isn't a supported subtype by the CDDB,
   * or is null
   */
  void bySubtype(String type, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards printed in the given block, or not in the given block.
   * @param block to search for cards printed in the given block, or not in the given block
   * @param searchFor to search for cards printed in the given block, or not in the given block
   * @throws IllegalArgumentException if the given block isn't a supported block by the CDDB,
   * or is null
   */
  void byBlock(String block, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards printed in the given set, or not printed in the given block.
   * @param set set to search by
   * @param searchFor to search for cards printed in the given set, or not in the given set
   * @throws IllegalArgumentException if the given set isn't a supported set by the CDDB,
   * or is null
   */
  void bySet(String set, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with art by the given artist, or not by the given artist.
   * @param artist artist to search by
   * @param searchFor to search for cards by the given artist, or not by the given artist
   * @throws IllegalArgumentException if the given artist isn't a listed artist in the CDDB,
   * or is null
   */
  void byArtist(String artist, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given flavor text, or with not the given flavor text.
   * No spaces in included word, nor non empty word.
   * @param word word to search by
   * @param searchFor to search for cards by the given flavor text or not the given flavor text
   * @throws IllegalArgumentException if given word is null, contains spaces, or is empty
   */
  void byFlavorText(String word, boolean searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given rarity, or not in their name.
   * @param rarity rarity to search by
   * @param searchFor to search for cards with given rarity type, or not the given rarity type
   * @throws IllegalArgumentException if the given rarity isn't a supported rarity by the CDDB,
   * or is null
   */
  void byRarity(String rarity, boolean searchFor) throws IllegalArgumentException;

  /**
   * Adds parameter to search for cards by the given stat, compared to given quantity, where
   * comparison is done by given comparison
   * @param stat card stat to compare to
   * @param comparison how to compare given stat and quantity
   * @param quantity integer to compare given stat against
   */
  void byStat(Stat stat, Comparison comparison, int quantity) throws IllegalArgumentException;

  /**
   * Adds parameter to search for cards by the given stat, compared to another given stat, where
   * comparison is done by given comparison. Given stats must be of different types.
   * @param thisStat card stat to compare
   * @param comparison how to compare given stats
   * @param otherStat other card stat to compare against
   * @throws IllegalArgumentException if any of the given parameters are null, stats are of same type
   */
  void byStatVersusStat(Stat thisStat, Comparison comparison, Stat otherStat) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given quantity of the given mana type in their casting
   * cost.
   * @param type mana type to search by
   * @param comparison how to compare quantity of mana type to given quantity
   * @param quantity how many of the given mana type to search for
   * @throws IllegalArgumentException if the given mana type isn't a supported mana type in the CDDB,
   * or is null
   */
  void byManaType(String type, Comparison comparison, int quantity) throws IllegalArgumentException;

  /**
   * Returns this {@link CardQuery} as a string for for actual seaching in the CDDB, as
   * determined by the parameters included so far. If no parameters have been entered, searches
   * for all cards.
   * @return this query as a string query
   */
  String asQuery();

  /**
   * Reset the CardQuery of all entered parameters.
   */
  void clear();
}
