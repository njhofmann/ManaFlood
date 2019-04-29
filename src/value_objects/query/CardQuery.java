package value_objects.query;

import value_objects.card.Card;

/**
 * Represents a query for {@link Card}s in the Card and Deck Database (CDDB) based off a selected
 * number of parameters.
 */
public interface CardQuery {

  void byName(String word, boolean include);

  void byText(String text, boolean include);

  void byColors(String color, boolean include);

  void byColorIdentity(String color, boolean include);

  void byType(String type, boolean include);

  void byBlock(String block, boolean include);

  void bySet(String set, boolean include);

  void byArtist(String artist, boolean include);

  void byStat(Stat stat, Comparison comparison, int quantity);

  void byRarity(String rarity, boolean include);

  void byManaType(String type, boolean quantity);

  /**
   *
   * @return
   */
  String asQuery();

}
