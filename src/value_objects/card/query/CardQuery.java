package value_objects.card.query;

import value_objects.card.Card;

/**
 * Represents a query for {@link Card}s in the Card and Deck Database (CDDB) based off a selected
 * number of parameters. Build up query by calling selected methods, then executing 
 * {@link CardQuery#asQuery()} to the full query.
 */
public interface CardQuery {

  /**
   * Add parameter to search for cards with given word in their name, not in their name, or one
   * of given words in their name.
   * No spaces in included word, nor non empty word.
   * @param word word to search by
   * @param searchFor to search for cards with given word in their name, not in their name, or one
   *  of given words in their name.
   * @throws IllegalArgumentException if the given word is null, has a space, or is empty
   */
  void byName(String word, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given word in their text, not in their text, or one
   * of given words in their text.
   * No spaces in included word, nor non empty word.
   * @param text text to search by
   * @param searchFor to search for cards with given word in their text, not in their text, or one
   *  of given words in their text.
   * @throws IllegalArgumentException if the given word is null, has a space, or is empty
   */
  void byText(String text, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards that are the given color, not the given color, or optionally
   * one of the given color.
   * @param color color to search by
   * @param searchFor to search for cards that are the given color, not the given color, or optionally
   * one of the given color.
   * @throws IllegalArgumentException if the given color isn't a supported color by the CDDB,
   * or a given param is null
   */
  void byColor(String color, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards that are the given color identity, not the given color
   * identity, or optionally one of the given color identity.
   * @param color color identity to search by
   * @param searchFor to search for cards that cards that are the given color identity, not the
   * given color identity, or optionally one of the given color identity.
   * @throws IllegalArgumentException if the given color isn't a supported color by the CDDB,
   * or given param is null
   */
  void byColorIdentity(String color, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given supertype, not the given supertype, or optionally
   * one of the given supertype.
   * @param type supertype to search by
   * @param searchFor to search for cards with given supertype, not the given supertype, or optionally
   * one of the given supertype.
   * @throws IllegalArgumentException if the given type isn't a supported supertype by the CDDB,
   * or is null
   */
  void bySupertype(String type, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given type, not the given type, or
   * one of the given type.
   * @param type type to search by
   * @param searchFor to search for cards with given type, not the given type, or optionally
   * one of the given type.
   * @throws IllegalArgumentException if the given type isn't a supported type by the CDDB,
   * or is null
   */
  void byType(String type, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given subtype, not the given subtype, or optionally
   * one of the given subtype.
   * @param type subtype to search by
   * @param searchFor to search for cards with given subtype, not the given subtype, or optionally
   * one of the given subtype.
   * @throws IllegalArgumentException if the given type isn't a supported subtype by the CDDB,
   * or is null
   */
  void bySubtype(String type, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for card printings printed in the given block, not in the given block,
   * or in one of given blocks.
   * @param block block to search by
   * @param searchFor to search for cards printed in the given block, or not in the given block
   * @throws IllegalArgumentException if the given block isn't a supported block by the CDDB,
   * or param is null
   */
  void byBlock(String block, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for card printings printed in the given set, not in the given set,
   * or in one of given set.
   * @param set set to search by
   * @param searchFor to search for cards printings printed in the given set, not in the given set,
   *  or in one of given set.
   * @throws IllegalArgumentException if the given set isn't a supported set by the CDDB,
   * or a given param is null
   */
  void bySet(String set, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for card printings created by the given artist, not by given artist,
   * or optionally one of the given artists.
   * @param artist artist to search by
   * @param searchFor to search for card printings created by the given artist, not by given artist,
   * or optionally one of the given artists.
   * @throws IllegalArgumentException if the given artist isn't a listed artist in the CDDB,
   * or a given param is null
   */
  void byArtist(String artist, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for cards with given flavor text , not the given flavor text,
   * or optionally one of the given flavor text.
   * @param word word to search by
   * @param searchFor to search for cards by the given flavor text or not the given flavor text
   * @throws IllegalArgumentException if given param is null, or given workd contains spaces, or is
   * empty
   */
  void byFlavorText(String word, SearchOption searchFor) throws IllegalArgumentException;

  /**
   * Add parameter to search for card printings that are the given rarity, not the given rarity,
   * or optionally one of the given rarity.
   * @param rarity rarity to search by
   * @param searchFor to search for cards that are the given rarity, not the given rarity,
   * or optionally one of the given rarity.
   * @throws IllegalArgumentException if the given rarity isn't a supported rarity by the CDDB,
   * or a given param is null
   */
  void byRarity(String rarity, SearchOption searchFor) throws IllegalArgumentException;

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
   * Returns this {@link CardQuery} as a string for for actual querying in the CDDB, as
   * determined by the parameters included so far. If no parameters have been entered, searches
   * for all Cards ever printed.
   * @return this query as a string query
   */
  String asQuery();

  /**
   * Resets this {@link CardQuery} of all entered parameters.
   */
  void clear();
}
