package equality;

import static org.junit.jupiter.api.Assertions.*;

import database.access.DatabaseChannel;
import database.access.DefaultDatabaseChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Random;
import java.util.SortedSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import value_objects.card.query.CardQuery;
import value_objects.card.query.Comparison;
import value_objects.card.query.SearchOption;
import value_objects.card.query.Stat;

/**
 * Tests to verify that the queries produced by {@link CardQuery} and its main implementation
 * produce correct queries to database. Brittle set of tests that are likely to change as they are
 * implementation specific.
 */
class CardQueryTest {

  public static CardQuery cardQuery;
  public static DatabaseChannel deckChannel;

  @BeforeAll
  public static void init() throws SQLException {
    Path pathToDatabase = Paths.get("tests\\test_cddb.db").toAbsolutePath();
    deckChannel = new DefaultDatabaseChannel(pathToDatabase);
    cardQuery = deckChannel.getQuery();
  }

  @AfterEach
  public void clearCardQuery() {
    cardQuery.clear();
  }

  @DisplayName("No parameters")
  @Test
  public void noParameters() {
    String result = "SELECT card_name, expansion, number FROM CardExpansion";
    assertEquals(result, cardQuery.asQuery());
  }

  @Nested
  @DisplayName("Name Parameter tests")
  class NameParameterTests {

    @DisplayName("Throws if empty name parameter")
    @Test
    public void emptyName() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byName("", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if name parameter with space")
    @Test
    public void nameWithSpace() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byName(" ", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null name parameter")
    @Test
    public void nullName() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byName(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Must include single name parameter")
    @Test
    public void includeSingleName() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT name FROM Card WHERE (name LIKE '%cabal%'))";
      cardQuery.byName("cabal", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Must include multiple name parameters")
    @Test
    public void includeMultipleName() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT name FROM Card "
          + "WHERE (name LIKE '%bi%' AND name LIKE '%rd%' AND name LIKE '%re%'))";
      cardQuery.byName("bi", SearchOption.MustInclude);
      cardQuery.byName("re", SearchOption.MustInclude);
      cardQuery.byName("rd", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single name parameter")
    @Test
    public void disallowSingleName() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN ("
          + "SELECT name FROM Card "
          + "WHERE (name NOT LIKE '%woke%'))";
      cardQuery.byName("woke", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple name parameters")
    @Test
    public void disallowMultipleName() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN ("
          + "SELECT name FROM Card "
          + "WHERE (name NOT LIKE '%ao%' AND name NOT LIKE '%po%' AND name NOT LIKE '%zi%'))";
      cardQuery.byName("ao", SearchOption.Disallow);
      cardQuery.byName("zi", SearchOption.Disallow);
      cardQuery.byName("po", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("One of single name parameter")
    @Test
    public void oneOfSingleName() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN ("
          + "SELECT name FROM Card "
          + "WHERE (name LIKE '%gy%'))";
      cardQuery.byName("gy", SearchOption.OneOf);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("One of multiple name parameters")
    @Test
    public void oneOfMultipleName() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN ("
          + "SELECT name FROM Card "
          + "WHERE (name LIKE '%io%' OR name LIKE '%ui%' OR name LIKE '%wj%'))";
      cardQuery.byName("io", SearchOption.OneOf);
      cardQuery.byName("wj", SearchOption.OneOf);
      cardQuery.byName("ui", SearchOption.OneOf);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple name parameters")
    @Test
    public void mixedMultipleName() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT name FROM Card "
          + "WHERE (name NOT LIKE '%jk%' AND name NOT LIKE '%rt%') "
          + "AND (name LIKE '%gh%' OR name LIKE '%yu%') "
          + "AND (name LIKE '%ah%' AND name LIKE '%re%'))";
      cardQuery.byName("ah", SearchOption.MustInclude);
      cardQuery.byName("gh", SearchOption.OneOf);
      cardQuery.byName("yu", SearchOption.OneOf);
      cardQuery.byName("re", SearchOption.MustInclude);
      cardQuery.byName("rt", SearchOption.Disallow);
      cardQuery.byName("jk", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("Text Parameter tests")
  class TextParameterTests {

    @DisplayName("Throws if empty text parameter")
    @Test
    public void emptyText() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byText("", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if text parameter with space")
    @Test
    public void textWithSpace() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byText(" ", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null text parameter")
    @Test
    public void nullText() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byText(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Include single text parameter")
    @Test
    public void includeSingleText() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN "
          + "(SELECT name FROM Card "
          + "WHERE (text LIKE '%flying%'))";
      cardQuery.byText("flying", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple text parameters")
    @Test
    public void includeMultipleText() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT name FROM Card "
          + "WHERE (text LIKE '%island%' AND text LIKE '%mer%' AND text LIKE '%walk%'))";
      cardQuery.byText("island", SearchOption.MustInclude);
      cardQuery.byText("walk", SearchOption.MustInclude);
      cardQuery.byText("mer", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single text parameter")
    @Test
    public void disallowSingleText() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN "
          + "(SELECT name FROM Card "
          + "WHERE (text NOT LIKE '%haste%'))";
      cardQuery.byText("haste", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple text parameters")
    @Test
    public void disallowMultipleText() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN "
          + "(SELECT name FROM Card "
          + "WHERE (text NOT LIKE '%men%' AND text NOT LIKE '%raid%' AND text NOT LIKE '%trample%'))";
      cardQuery.byText("men", SearchOption.Disallow);
      cardQuery.byText("trample", SearchOption.Disallow);
      cardQuery.byText("raid", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single text parameter")
    @Test
    public void oneOfSingleText() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN "
          + "(SELECT name FROM Card "
          + "WHERE (text LIKE '%star%'))";
      cardQuery.byText("star", SearchOption.OneOf);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple text parameters")
    @Test
    public void oneOfMultipleText() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN "
          + "(SELECT name FROM Card "
          + "WHERE (text LIKE '%jump%' OR text LIKE '%load%' OR text LIKE '%over%'))";
      cardQuery.byText("over", SearchOption.OneOf);
      cardQuery.byText("load", SearchOption.OneOf);
      cardQuery.byText("jump", SearchOption.OneOf);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple text parameters")
    @Test
    public void mixedMultipleText() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN "
          + "(SELECT name FROM Card "
          + "WHERE (text NOT LIKE '%ar%' AND text NOT LIKE '%ma%') "
          + "AND (text LIKE '%er%' OR text LIKE '%io%') "
          + "AND (text LIKE '%fl%' AND text LIKE '%im%'))";
      cardQuery.byText("fl", SearchOption.MustInclude);
      cardQuery.byText("im", SearchOption.MustInclude);
      cardQuery.byText("er", SearchOption.OneOf);
      cardQuery.byText("io", SearchOption.OneOf);
      cardQuery.byText("ma", SearchOption.Disallow);
      cardQuery.byText("ar", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("Color Parameter tests")
  class ColorParameterTests {

    @DisplayName("Throws if unsupported color parameter")
    @Test
    public void unsupportedColor() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byColor("P", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null color parameter")
    @Test
    public void nullColor() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byColor(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Include single color parameter")
    @Test
    public void includeSingleColor() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT card_name FROM Color "
          + "WHERE color IN ('R') AND card_name IN (SELECT card_name FROM Color "
          + "WHERE color IN ('R') GROUP BY card_name HAVING COUNT(DISTINCT(color)) = 1))";
      cardQuery.byColor("R", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple color parameters")
    @Test
    public void includeMultipleColor() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT card_name FROM Color "
          + "WHERE color IN ('B', 'U') AND card_name IN (SELECT card_name FROM Color "
          + "WHERE color IN ('B', 'U') GROUP BY card_name HAVING COUNT(DISTINCT(color)) = 2))";
      cardQuery.byColor("U", SearchOption.MustInclude);
      cardQuery.byColor("B", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single color parameter")
    @Test
    public void disallowSingleColor() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT card_name FROM Color "
          + "WHERE color NOT IN ('W'))";
      cardQuery.byColor("W", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple color parameters")
    @Test
    public void disallowMultipleColor() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT card_name FROM Color "
          + "WHERE color NOT IN ('G', 'R'))";
      cardQuery.byColor("G", SearchOption.Disallow);
      cardQuery.byColor("R", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("One of single color parameter")
    @Test
    public void oneOfSingleColor() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT card_name FROM Color "
          + "WHERE color IN ('R'))";
      cardQuery.byColor("R", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("One of multiple color parameters")
    @Test
    public void oneOfMultipleColor() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT card_name FROM Color "
          + "WHERE color IN ('U', 'W'))";
      cardQuery.byColor("W", SearchOption.OneOf);
      cardQuery.byColor("U", SearchOption.OneOf);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple color parameters")
    @Test
    public void mixedMultipleColor() {
      String result = "SELECT card_name, expansion, number "
          + "FROM CardExpansion WHERE card_name IN (SELECT card_name FROM Color "
          + "WHERE color NOT IN ('B') AND color IN ('G', 'R', 'U', 'W') "
          + "AND card_name IN (SELECT card_name FROM Color "
          + "WHERE color IN ('R', 'U') GROUP BY card_name HAVING COUNT(DISTINCT(color)) = 2))";
      cardQuery.byColor("W", SearchOption.OneOf);
      cardQuery.byColor("U", SearchOption.MustInclude);
      cardQuery.byColor("R", SearchOption.MustInclude);
      cardQuery.byColor("G", SearchOption.OneOf);
      cardQuery.byColor("B", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("Color Identity Parameter tests")
  class ColorIdentityParameterTests {

    @DisplayName("Throws if unsupported color parameter")
    @Test
    public void unsupportedColor() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byColorIdentity("M", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null color parameter")
    @Test
    public void nullColor() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byColorIdentity(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Include single color parameter")
    @Test
    public void includeSingleColor() {

    }

    @DisplayName("Include multiple color parameters")
    @Test
    public void includeMultipleColor() {

    }

    @DisplayName("Disallow single color parameter")
    @Test
    public void disallowSingleColor() {

    }

    @DisplayName("Disallow multiple color parameters")
    @Test
    public void disallowMultipleColor() {

    }

    @DisplayName("Mixed multiple color parameters")
    @Test
    public void mixedMultipleColor() {

    }
  }

  @Nested
  @DisplayName("Type Parameter tests")
  class TypeParameterTests {

    @DisplayName("Throws if unsupported type parameter")
    @Test
    public void unsupportedType() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byType("foodbar", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null type parameter")
    @Test
    public void nullType() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byType(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Include single type parameter")
    @Test
    public void includeSingleType() {

    }

    @DisplayName("Include multiple type parameters")
    @Test
    public void includeMultipleType() {

    }

    @DisplayName("Disallow single type parameter")
    @Test
    public void disallowSingleType() {

    }

    @DisplayName("Disallow multiple type parameters")
    @Test
    public void disallowMultipleType() {

    }

    @DisplayName("Mixed multiple type parameters")
    @Test
    public void mixedMultipleType() {

    }
  }

  @Nested
  @DisplayName("Block Parameter tests")
      /**
       * Very similar to how sets are implemented, so fewer tests here.
       */
  class BlockParameterTests {

    @DisplayName("Throws if unsupported block parameter")
    @Test
    public void unsupportedBlock() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byBlock("Kahns of Fartkir", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null block parameter")
    @Test
    public void nullBlock() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byBlock(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Include single block parameter")
    @Test
    public void includeSingleBlock() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN ("
          + "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE expansion IN ('Dragon''s Maze', 'Dragon''s Maze Promos', 'Gatecrash', "
          + "'Gatecrash Promos', 'Return to Ravnica', 'Return to Ravnica Promos') "
          + "AND card_name IN (SELECT card_name FROM CardExpansion "
          + "WHERE expansion IN ('Dragon''s Maze', 'Dragon''s Maze Promos', 'Gatecrash', "
          + "'Gatecrash Promos', 'Return to Ravnica', 'Return to Ravnica Promos') "
          + "GROUP BY card_name HAVING COUNT(DISTINCT(expansion)) = 6))";
      cardQuery.byBlock("Return to Ravnica", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single block parameter")
    @Test
    public void disallowSingleBlock() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN (SELECT card_name, expansion, number "
          + "FROM CardExpansion WHERE expansion NOT IN ('Battle the Horde', 'Born of the Gods', "
          + "'Born of the Gods Hero''s Path', 'Born of the Gods Promos', 'Defeat a God', "
          + "'Face the Hydra', 'Journey into Nyx', 'Journey into Nyx Hero''s Path', "
          + "'Journey into Nyx Promos', 'Theros', 'Theros Hero''s Path', 'Theros Promos'))";
      cardQuery.byBlock("Theros", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("One of single block parameters")
    @Test
    public void oneOfSingleBlock() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN (SELECT card_name, expansion, number "
          + "FROM CardExpansion WHERE expansion IN ('Exodus', 'Stronghold', "
          + "'Tempest'))";
      cardQuery.byBlock("Tempest", SearchOption.OneOf);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("Set Parameter tests")
  class SetParameterTests {

    @DisplayName("Throws if unsupported set parameter")
    @Test
    public void unsupportedSet() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.bySet("sticky note", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null set parameter")
    @Test
    public void nullSet() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.bySet(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Include single set parameter")
    @Test
    public void includeSingleSet() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN ("
          + "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE expansion IN ('Return to Ravnica') "
          + "AND card_name IN (SELECT card_name FROM CardExpansion "
          + "WHERE expansion IN ('Return to Ravnica') "
          + "GROUP BY card_name HAVING COUNT(DISTINCT(expansion)) = 1))";
      cardQuery.bySet("Return to Ravnica", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple set parameters")
    @Test
    public void includeMultipleSet() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN ("
          + "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE expansion IN ('Khans of Tarkir', 'Return to Ravnica', 'Theros') "
          + "AND card_name IN (SELECT card_name FROM CardExpansion "
          + "WHERE expansion IN ('Khans of Tarkir', 'Return to Ravnica', 'Theros') "
          + "GROUP BY card_name HAVING COUNT(DISTINCT(expansion)) = 3))";
      cardQuery.bySet("Return to Ravnica", SearchOption.MustInclude);
      cardQuery.bySet("Khans of Tarkir", SearchOption.MustInclude);
      cardQuery.bySet("Theros", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single set parameter")
    @Test
    public void disallowSingleSet() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN (SELECT card_name, expansion, number "
          + "FROM CardExpansion WHERE expansion NOT IN ('Magic 2012'))";
      cardQuery.bySet("Magic 2012", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple set parameters")
    @Test
    public void disallowMultipleSet() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN (SELECT card_name, expansion, number "
          + "FROM CardExpansion WHERE expansion NOT IN ('Gatecrash', 'Scourge', 'Visions'))";
      cardQuery.bySet("Visions", SearchOption.Disallow);
      cardQuery.bySet("Gatecrash", SearchOption.Disallow);
      cardQuery.bySet("Scourge", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("One of single set parameters")
    @Test
    public void oneOfSingleSet() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN (SELECT card_name, expansion, number "
          + "FROM CardExpansion WHERE expansion IN ('Tempest'))";
      cardQuery.bySet("Tempest", SearchOption.OneOf);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("One of multiple set parameters")
    @Test
    public void oneOfMultipleSet() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN (SELECT card_name, expansion, number "
          + "FROM CardExpansion WHERE expansion IN ('Homelands', 'Ixalan', 'Scars of Mirrodin'))";
      cardQuery.bySet("Ixalan", SearchOption.OneOf);
      cardQuery.bySet("Homelands", SearchOption.OneOf);
      cardQuery.bySet("Scars of Mirrodin", SearchOption.OneOf);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple set parameters")
    @Test
    public void mixedMultipleSet() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE (card_name, expansion, number) IN (SELECT card_name, expansion, number "
          + "FROM CardExpansion WHERE expansion NOT IN ('Nemesis', 'Shards of Alara') "
          + "AND expansion IN ('Guildpact', 'New Phyrexia', 'Prophecy', 'Zendikar') "
          + "AND card_name IN (SELECT card_name FROM CardExpansion "
          + "WHERE expansion IN ('New Phyrexia', 'Zendikar') "
          + "GROUP BY card_name HAVING COUNT(DISTINCT(expansion)) = 2))";
      cardQuery.bySet("New Phyrexia", SearchOption.MustInclude);
      cardQuery.bySet("Zendikar", SearchOption.MustInclude);
      cardQuery.bySet("Guildpact", SearchOption.OneOf);
      cardQuery.bySet("Prophecy", SearchOption.OneOf);
      cardQuery.bySet("Nemesis", SearchOption.Disallow);
      cardQuery.bySet("Shards of Alara", SearchOption.Disallow);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("Artist Parameter tests")
  class ArtistParameterTests {

    @DisplayName("Throws if unsupported artist parameter")
    @Test
    public void unsupportedArtist() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byArtist("ghor", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null artist parameter")
    @Test
    public void nullArtist() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byArtist(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Include single artist parameter")
    @Test
    public void includeSingleArtist() {

    }

    @DisplayName("Include multiple artist parameters")
    @Test
    public void includeMultipleArtist() {

    }

    @DisplayName("Disallow single artist parameter")
    @Test
    public void disallowSingleArtist() {

    }

    @DisplayName("One of single artist parameters")
    @Test
    public void oneOfSingleArtist() {

    }

    @DisplayName("One of multiple artist parameters")
    @Test
    public void oneOfMultipleArtist() {

    }

    @DisplayName("Mixed multiple artist parameters")
    @Test
    public void mixedMultipleArtist() {

    }
  }

  @Nested
  @DisplayName("FlavorText Parameter tests")
  class FlavorTextParameterTests {

    @DisplayName("Throws if flavor text parameter is empty")
    @Test
    public void emptyFlavorText() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byFlavorText("", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if flavor text parameter has a space")
    @Test
    public void flavorTextWithSpace() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byFlavorText(" ", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null flavor text parameter")
    @Test
    public void nullFlavorText() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byFlavorText(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Include single flavor text parameter")
    @Test
    public void includeSingleFlavorText() {
    }

    @DisplayName("Include multiple flavor text parameters")
    @Test
    public void includeMultipleFlavorText() {

    }

    @DisplayName("Disallow single flavor text parameter")
    @Test
    public void disallowSingleFlavorText() {

    }

    @DisplayName("Disallow multiple flavor text parameters")
    @Test
    public void disallowMultipleFlavorText() {

    }

    @DisplayName("One of single flavor text parameter")
    @Test
    public void oneOfSingleFlavorText() {

    }

    @DisplayName("One of multiple flavor text parameters")
    @Test
    public void oneOfMultipleFlavorText() {

    }

    @DisplayName("Mixed multiple flavor text parameters")
    @Test
    public void mixedMultipleFlavorText() {

    }
  }

  @Nested
  @DisplayName("Rarity Parameter tests")
  class RarityParameterTests {

    @DisplayName("Throws if unsupported rarity parameter")
    @Test
    public void unsupportedRarity() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byRarity("supercommon", SearchOption.MustInclude);
      });
    }

    @DisplayName("Throws if null rarity parameter")
    @Test
    public void nullRarity() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byRarity(null, SearchOption.MustInclude);
      });
    }

    @DisplayName("Include single rarity parameter")
    @Test
    public void includeSingleRarity() {

    }

    @DisplayName("Include multiple rarity parameters")
    @Test
    public void includeMultipleRarity() {

    }

    @DisplayName("Disallow single rarity parameter")
    @Test
    public void disallowSingleRarity() {

    }

    @DisplayName("One of single rarity parameters")
    @Test
    public void oneOfSingleRarity() {

    }

    @DisplayName("One of multiple rarity parameters")
    @Test
    public void oneOfMultipleRarity() {

    }

    @DisplayName("Mixed multiple rarity parameters")
    @Test
    public void mixedMultipleRarity() {

    }

  }

  @Nested
  @DisplayName("Stat Parameter tests")
  class StatParameterTests {

    @DisplayName("Throws if null comparison stat parameter")
    @Test
    public void nullComparison() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byStat(Stat.CMC, null, 3);
      });
    }

    @DisplayName("Throws if null stat parameter")
    @Test
    public void nullStat() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byStat(null, Comparison.EQUAL, 5);
      });
    }

    @DisplayName("Single stat parameter for loyalty stat against each comparison combination")
    @Test
    public void loyaltyStatEachComparison() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM Loyalty t0 WHERE t0.loyalty_value %s %d)";
      for (Comparison comparison : Comparison.values()) {
        int quantity = new Random().nextInt(20);
        cardQuery.byStat(Stat.LOYALTY, comparison, quantity);
        String formattedResult = String.format(result, comparison.getValue(), quantity);
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Single stat parameter for toughness stat against each comparison combination")
    @Test
    public void toughnessStatEachComparison() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM PowerToughness t0 WHERE t0.toughness_value %s %d)";

      for (Comparison comparison : Comparison.values()) {
        int quantity = new Random().nextInt(20);
        cardQuery.byStat(Stat.TOUGHNESS, comparison, quantity);
        String formattedResult = String.format(result, comparison.getValue(), quantity);
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Single stat parameter for power stat against each comparison combination")
    @Test
    public void powerStatEachComparison() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM PowerToughness t0 WHERE t0.power_value %s %d)";

      for (Comparison comparison : Comparison.values()) {
        int quantity = new Random().nextInt(20);
        cardQuery.byStat(Stat.POWER, comparison, quantity);
        String formattedResult = String.format(result, comparison.getValue(), quantity);
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Single stat parameter for cmc stat against each comparison combination")
    @Test
    public void cmcStatEachComparison() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.name FROM Card t0 WHERE t0.cmc %s %d)";

      for (Comparison comparison : Comparison.values()) {
        int quantity = new Random().nextInt(20);
        cardQuery.byStat(Stat.CMC, comparison, quantity);
        String formattedResult = String.format(result, comparison.getValue(), quantity);
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Mixed multiple stat parameters")
    @Test
    public void mixedMultipleStat() {

    }
  }

  @Nested
  @DisplayName("StatVersusStat Parameter tests")
  class StatVersusStatParameterTests {

    @DisplayName("Throws if null stat")
    @Test
    public void nullStat() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byStatVersusStat(null, Comparison.GREATER, Stat.LOYALTY);
      });
    }

    @DisplayName("Throws if null comparison")
    @Test
    public void nullComparison() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byStatVersusStat(Stat.POWER, null, Stat.TOUGHNESS);
      });
    }

    @DisplayName("Throws if null stat")
    @Test
    public void nullOtherStat() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byStatVersusStat(Stat.CMC, Comparison.UNEQUAL, null);
      });
    }

    @DisplayName("Throws null if same stat")
    @Test
    public void sameStat() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byStatVersusStat(Stat.CMC, Comparison.UNEQUAL, Stat.CMC);
      });

      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byStatVersusStat(Stat.POWER, Comparison.UNEQUAL, Stat.POWER);
      });

      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byStatVersusStat(Stat.TOUGHNESS, Comparison.UNEQUAL, Stat.TOUGHNESS);
      });

      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byStatVersusStat(Stat.POWER, Comparison.UNEQUAL, Stat.POWER);
      });
    }

    @DisplayName("Power stat vs toughness stat")
    @Test
    public void powerVsToughness() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM PowerToughness t0 JOIN PowerToughness t1 "
          + "ON t0.card_name = t1.card_name WHERE t0.power_value %s t1.toughness_value)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.POWER, comparison, Stat.TOUGHNESS);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Toughness stat vs power stat")
    @Test
    public void toughnessVsPower() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM PowerToughness t0 JOIN PowerToughness t1 "
          + "ON t0.card_name = t1.card_name WHERE t0.toughness_value %s t1.power_value)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.TOUGHNESS, comparison, Stat.POWER);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Power stat vs CMC stat")
    @Test
    public void powerVsCMC() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM PowerToughness t0 JOIN Card t1 "
          + "ON t0.card_name = t1.name WHERE t0.power_value %s t1.cmc)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.POWER, comparison, Stat.CMC);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("CMC stat vs Power stat")
    @Test
    public void cmcVsPower() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.name FROM Card t0 JOIN PowerToughness t1 "
          + "ON t0.name = t1.card_name WHERE t0.cmc %s t1.power_value)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.CMC, comparison, Stat.POWER);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Power stat vs loyalty stat")
    @Test
    public void powerVsLoyalty() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM PowerToughness t0 JOIN Loyalty t1 "
          + "ON t0.card_name = t1.card_name WHERE t0.power_value %s t1.loyalty_value)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.POWER, comparison, Stat.LOYALTY);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Power stat vs loyalty stat")
    @Test
    public void loyaltyVsPower() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM Loyalty t0 JOIN PowerToughness t1 "
          + "ON t0.card_name = t1.card_name WHERE t0.loyalty_value %s t1.power_value)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.LOYALTY, comparison, Stat.POWER);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("CMC stat vs loyalty stat")
    @Test
    public void cmcVsLoyalty() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.name FROM Card t0 JOIN Loyalty t1 "
          + "ON t0.name = t1.card_name WHERE t0.cmc %s t1.loyalty_value)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.CMC, comparison, Stat.LOYALTY);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Loyalty stat vs CMC stat")
    @Test
    public void loyaltyVsCMC() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM Loyalty t0 JOIN Card t1 "
          + "ON t0.card_name = t1.name WHERE t0.loyalty_value %s t1.cmc)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.LOYALTY, comparison, Stat.CMC);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Toughness stat vs CMC stat")
    @Test
    public void toughnessVsCMC() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM PowerToughness t0 JOIN Card t1 "
          + "ON t0.card_name = t1.name WHERE t0.toughness_value %s t1.cmc)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.TOUGHNESS, comparison, Stat.CMC);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("CMC stat vs toughness stat")
    @Test
    public void cmcVsToughness() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.name FROM Card t0 JOIN PowerToughness t1 "
          + "ON t0.name = t1.card_name WHERE t0.cmc %s t1.toughness_value)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.CMC, comparison, Stat.TOUGHNESS);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Loyalty stat vs toughness stat")
    @Test
    public void loyaltyVsToughness() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.name FROM Card t0 JOIN PowerToughness t1 "
          + "ON t0.name = t1.card_name WHERE t0.cmc %s t1.toughness_value)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.CMC, comparison, Stat.TOUGHNESS);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Toughness stat vs loyalty Stat")
    @Test
    public void toughnessVsLoyalty() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM PowerToughness t0 JOIN Loyalty t1 "
          + "ON t0.card_name = t1.card_name WHERE t0.toughness_value %s t1.loyalty_value)";
      for (Comparison comparison : Comparison.values()) {
        cardQuery.byStatVersusStat(Stat.TOUGHNESS, comparison, Stat.LOYALTY);
        String formattedResult = String.format(result, comparison.getValue());
        assertEquals(formattedResult, cardQuery.asQuery());
        cardQuery.clear();
      }
    }

    @DisplayName("Mixed multiple stat versus stat parameters")
    @Test
    public void mixedMultipleStatVersusStat() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM PowerToughness t0 "
          + "JOIN PowerToughness t1 ON t0.card_name = t1.card_name "
          + "JOIN PowerToughness t2 ON t0.card_name = t2.card_name "
          + "JOIN Card t3 ON t0.card_name = t3.name "
          + "WHERE t0.power_value >= t1.toughness_value "
          + "AND t2.toughness_value = t3.cmc)";
      cardQuery.byStatVersusStat(Stat.POWER, Comparison.GREATER_EQUAL, Stat.TOUGHNESS);
      cardQuery.byStatVersusStat(Stat.TOUGHNESS, Comparison.EQUAL, Stat.CMC);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("ManaType Parameter tests")
  class ManaTypeParameterTests {

    @DisplayName("Throws if unsupported mana type parameter")
    @Test
    public void unsupportedManaType() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byManaType("{O}", Comparison.LESS_EQUAL, 5);
      });
    }

    @DisplayName("Throws if null mana type parameter")
    @Test
    public void nullManaType() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byManaType(null, Comparison.LESS_EQUAL, 5);
      });
    }

    @DisplayName("Single mana type parameter for each type of mana type and comparison")
    @Test
    public void includeSingleManaType() throws SQLException {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM Mana t0 "
          + "WHERE t0.mana_type = '%s' AND t0.quantity %s %d)";
      Random random = new Random();
      int bound = 20;
      SortedSet<String> manaTypes = deckChannel.getQuery().getAvailableManaTypes();
      for (String manaType : manaTypes) {
        for (Comparison comparison : Comparison.values()) {
          int nextRandom = random.nextInt(bound);
          String formattedResult = String.format(result, manaType, comparison.getValue(), nextRandom);
          cardQuery.byManaType(manaType, comparison, nextRandom);
          assertEquals(formattedResult, cardQuery.asQuery());
          cardQuery.clear();
        }
      }
    }

    @DisplayName("Mixed multiple mana type parameters")
    @Test
    public void mixedMultipleManaType() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN (SELECT t0.card_name FROM Mana t0 "
          + "JOIN Mana t1 ON t0.card_name = t1.card_name "
          + "WHERE t0.mana_type = '{R}' AND t0.quantity = 2"
          + " AND t1.mana_type = '{1}' AND t1.quantity > 1)";
      cardQuery.byManaType("{R}", Comparison.EQUAL, 2);
      cardQuery.byManaType("{1}", Comparison.GREATER, 1);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @DisplayName("Mixed queries")
  @Nested
  class MixedQueries {

    @DisplayName("Multiple card expansion parameters")
    @Test
    public void multipleCardExpansionParameters() {

    }

    @DisplayName("Name and text parameters")
    @Test
    public void nameAndText() {

    }

    @DisplayName("Color and color identity parameters")
    @Test
    public void colorAndColorIdentity() {

    }

    @DisplayName("Stat and stat vs stat parameters")
    @Test
    public void statAndStatVsStat() {

    }

    @DisplayName("Single card and block parameters")
    @Test
    public void singleCardAndBlock() {

    }

    @DisplayName("Multiple card and block parameters")
    @Test
    public void multipleCardAndBlock() {

    }

    @DisplayName("One of each type of parameter")
    @Test
    public void singleEverything() {

    }
  }
}