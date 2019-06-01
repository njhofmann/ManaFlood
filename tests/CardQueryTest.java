import static org.junit.jupiter.api.Assertions.*;

import database.access.CardChannel;
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
 * produce correct queries to database.
 */
class CardQueryTest {

  public static CardQuery cardQuery;
  public static CardChannel cardChannel;

  @BeforeAll
  public static void init() throws SQLException {
    Path pathToDatabase = Paths.get("resources\\test.db").toAbsolutePath();
    DefaultDatabaseChannel defaultChannel = new DefaultDatabaseChannel(pathToDatabase);
    cardChannel = defaultChannel;
    cardQuery = defaultChannel.getQuery();
  }

  @AfterEach
  public void clearCardQuery() {
    cardQuery.clear();
  }

  @DisplayName("No parameters")
  @Test
  public void noParameters() {
    String result = "SELECT t0.card_name card_name, t0.expansion expansion FROM CardExpansion t0";
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
          + "WHERE card_name IN ("
          + "WITH MustIncludePrintings AS ("
          + "SELECT name FROM Card WHERE (name LIKE '%cabal%')) "
          + "SELECT name FROM Card "
          + "WHERE name IN MustIncludePrintings)";
      cardQuery.byName("cabal", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Must include multiple name parameters")
    @Test
    public void includeMultipleName() {
      String result = "SELECT card_name, expansion, number FROM CardExpansion "
          + "WHERE card_name IN ("
          + "WITH MustIncludePrintings AS ("
          + "SELECT name FROM Card "
          + "WHERE (name LIKE '%bi%' AND name LIKE '%rd%' AND name LIKE '%re%')) "
          + "SELECT name FROM Card "
          + "WHERE name IN MustIncludePrintings)";
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
          + "WHERE card_name IN ("
          + "WITH MustIncludePrintings AS ("
          + "SELECT name FROM Card WHERE (name LIKE '%ah%' AND name LIKE '%re%')) "
          + "SELECT name FROM Card "
          + "WHERE name IN MustIncludePrintings "
          + "AND (name NOT LIKE '%jk%' AND name NOT LIKE '%rt%') "
          + "AND (name LIKE '%gh%' OR name LIKE '%yu%'))";
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

    }

    @DisplayName("Include multiple text parameters")
    @Test
    public void includeMultipleText() {

    }

    @DisplayName("Disallow single text parameter")
    @Test
    public void disallowSingleText() {

    }

    @DisplayName("Disallow multiple text parameters")
    @Test
    public void disallowMultipleText() {

    }

    @DisplayName("Mixed multiple text parameters")
    @Test
    public void mixedMultipleText() {

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

    }

    @DisplayName("Include multiple block parameters")
    @Test
    public void includeMultipleBlock() {

    }

    @DisplayName("Disallow single block parameter")
    @Test
    public void disallowSingleBlock() {

    }

    @DisplayName("Disallow multiple block parameters")
    @Test
    public void disallowMultipleBlock() {

    }

    @DisplayName("Mixed multiple block parameters")
    @Test
    public void mixedMultipleBlock() {

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

    }

    @DisplayName("Include multiple set parameters")
    @Test
    public void includeMultipleSet() {

    }

    @DisplayName("Disallow single set parameter")
    @Test
    public void disallowSingleSet() {

    }

    @DisplayName("Disallow multiple set parameters")
    @Test
    public void disallowMultipleSet() {

    }

    @DisplayName("Mixed multiple set parameters")
    @Test
    public void mixedMultipleSet() {

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

    @DisplayName("Disallow multiple artist parameters")
    @Test
    public void disallowMultipleArtist() {

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
      String result = "SELECT t0.card_name card_name, t0.expansion expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.flavor_text LIKE '%foo%'";
      cardQuery.byFlavorText("foo", SearchOption.MustInclude);
      assertEquals(result, cardQuery.asQuery());
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

    @DisplayName("Disallow multiple rarity parameters")
    @Test
    public void disallowMultipleRarity() {

    }

    @DisplayName("Mixed multiple rarity parameters")
    @Test
    public void mixedMultipleRarity() {

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

      @DisplayName("Single stat parameter for each stat and comparison combination")
      @Test
      public void singleStatCombo() {

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

      @DisplayName("Single stat parameter for each stat, comparison, and stat combination")
      @Test
      public void singleStatComparisonStat() {

      }


      @DisplayName("Mixed multiple stat versus stat parameters")
      @Test
      public void mixedMultipleStatVersusStat() {

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

      }

      @DisplayName("Mixed multiple mana type parameters")
      @Test
      public void mixedMultipleManaType() {

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
}