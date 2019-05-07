package database.access;

import java.sql.SQLException;
import java.util.List;
import java.util.SortedSet;
import value_objects.card.Card;
import value_objects.query.CardQuery;

/**
 * Provides methods for accessing enumerated info about cards, and querying specific cards stored in
 * the from the Card and Deck Database (CDDB), such as card supertypes, types, subtypes, colors, etc. 
 */
public interface CardChannel {

  /**
   * Returns a {@link CardQuery} object to use for querying specific cards from the CDDB.
   * @return CardQuery object
   */
  CardQuery getQuery();

  /**
   * Returns a list of {@link Card} s from the CDDB that match the parameters given by the inputted
   * {@link CardQuery}.
   * @param cardQuery desired query parameters to match cards against
   * @return list of {@link Card}s that match given card parameters
   * @throws IllegalArgumentException if given {@link CardQuery} is null.
   * @throws SQLException if there is a failure to query card info from the CDDB
   */
  List<Card> queryCards(CardQuery cardQuery) throws IllegalArgumentException, SQLException;

  /**
   * Returns a unmodifiable sorted set of all the card types held in the CDDB. Supertypes, types,
   * and subtypes.
   * @return unmodifiable sorted set of all the card types
   * @throws SQLException if connection to CDDB has not yet been established, or if there is a
   * failure to query from the database
   */
  SortedSet<String> getTypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the card rarity types held in the CDDB.
   * @return unmodifiable sorted set of all the card rarity types
   * @throws SQLException if connection to CDDB has not yet been established, or if there is a
   * failure to query from the database
   */
  SortedSet<String> getRarityTypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all colors held in the CDDB.
   * @return unmodifiable sorted set of all the three faced card types
   * @throws SQLException if connection to CDDB has not yet been established, or if there is a
   * failure to query from the database
   */
  SortedSet<String> getColors() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the card mana types held in the CDDB.
   * @return unmodifiable sorted set of all the card mana types
   * @throws SQLException if connection to CDDB has not yet been established, or if there is a
   * failure to query from the database
   */
  SortedSet<String> getManaTypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the multifaced card types held in the CDDB.
   * @return unmodifiable sorted set of all the two faced card types
   * @throws SQLException if connection to CDDB has not yet been established, or if there is a
   * failure to query from the database
   */
  SortedSet<String> getMultifacedTypes() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the blocks held in the CDDB.
   * @return unmodifiable sorted set of all the two faced card types
   * @throws SQLException if connection to CDDB has not yet been established, or if there is a
   * failure to query from the database
   */
  SortedSet<String> getBlocks() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all the artists held in the CDDB.
   * @return unmodifiable sorted set of all artists
   * @throws SQLException if connection to CDDB has not yet been established, or if there is a
   * failure to query from the database
   */
  SortedSet<String> getArtists() throws SQLException;

  /**
   * Returns a unmodifiable sorted set of all expansions (full name) that are held in the CDDB.
   * @return unmodifiable sorted set of all expansion names
   * @throws SQLException if connection to CDDB has not yet been established, or if there is a
   * failure to query from the database
   */
  SortedSet<String> getSets() throws SQLException;

  /**
   * Returns a {@link Card} representing all the information associated with a given card name, from
   * the CDDB. Given string must match desired card name exactly
   * @return info related to a given card
   * @throws IllegalArgumentException if given card name is null, or doesn't match with an
   * associated card
   * @throws SQLException if connection to CDDB has not yet been established, or if there is a
   * failure to query from the database
   */
  Card getCard(String name) throws SQLException;
}