package value_objects.query;

import static org.junit.jupiter.api.Assertions.*;

import database.access.DefaultDatabaseChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify that the queries produced by {@link CardQuery} and its main implementation
 * produce correct queries to database.
 */
class CardQueryTest {

  public static CardQuery cardQuery;

  @BeforeAll
  public static void init() throws SQLException {
    Path pathToDatabase = Paths.get("resources\\test.db").toAbsolutePath();
    cardQuery = new DefaultDatabaseChannel(pathToDatabase).getQuery();
  }

  @AfterEach
  public void clearCardQuery() {
    cardQuery.clear();
  }

  @DisplayName("No parameters")
  @Test
  public void noParameters() {
    String result = "SELECT t0.card_name, t0.expansion FROM CardExpansion t0";
    assertEquals(result, cardQuery.asQuery());
  }

  @Nested
  @DisplayName("Name Parameter tests")
  class NameParameterTests {

    @DisplayName("Throws if empty name parameter")
    @Test
    public void emptyName() {
      String name = "";
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byName(name, true);
      });
    }

    @DisplayName("Throws if null name parameter")
    @Test
    public void nullName() {
      String name = null;
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byName(name, true);
      });
    }

    @DisplayName("Include single name parameter")
    @Test
    public void includeSingleName() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT name "
          + "FROM Card "
          + "WHERE name LIKE '%sel%'"
          + ")";
      String name = "sel";
      cardQuery.byName(name, true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple name parameters")
    @Test
    public void includeMultipleName() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN ("
          + ""
          + "SELECT name "
          + "FROM Card "
          + "WHERE name LIKE '%pr%' "
          + "AND name LIKE '%to%'"
          + ")";
      cardQuery.byName("pr", true);
      cardQuery.byName("to", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single name parameter")
    @Test
    public void disallowSingleName() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT name "
          + "FROM Card "
          + "WHERE name NOT LIKE '%ba%'"
          + ")";
      cardQuery.byName("ba", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple name parameters")
    @Test
    public void disallowMultipleName() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT name "
          + "FROM Card "
          + "WHERE name NOT LIKE '%ba%' "
          + "AND name NOT LIKE '%ho%'"
          + ")";
      cardQuery.byName("ba", false);
      cardQuery.byName("ho", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple name parameters")
    @Test
    public void mixedMultipleName() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT name "
          + "FROM Card "
          + "WHERE name NOT LIKE '%ga%' "
          + "AND name LIKE '%fo%' "
          + "AND name LIKE '%mo%' "
          + "AND name NOT LIKE '%k%' "
          + "AND name LIKE '%b%'"
          + ")";
      cardQuery.byName("ga", false);
      cardQuery.byName("fo", true);
      cardQuery.byName("mo", true);
      cardQuery.byName("k", false);
      cardQuery.byName("b", true);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("Text Parameter tests")
  class TextParameterTests {

    @DisplayName("Throws if empty text parameter")
    @Test
    public void emptyText() {

    }

    @DisplayName("Throws if null text parameter")
    @Test
    public void nullText() {

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

    }

    @DisplayName("Throws if null color parameter")
    @Test
    public void nullColor() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

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

    }

    @DisplayName("Throws if null color parameter")
    @Test
    public void nullColor() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

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

    }

    @DisplayName("Throws if null type parameter")
    @Test
    public void nullType() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

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

    }

    @DisplayName("Throws if null block parameter")
    @Test
    public void nullBlock() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

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

    }

    @DisplayName("Throws if null set parameter")
    @Test
    public void nullSet() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

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

    }

    @DisplayName("Throws if null artist parameter")
    @Test
    public void nullArtist() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

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

    @DisplayName("Throws if unsupported flavor text parameter")
    @Test
    public void unsupportedFlavorText() {

    }

    @DisplayName("Throws if null flavor text parameter")
    @Test
    public void nullFlavorText() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

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

    }

    @DisplayName("Throws if null rarity parameter")
    @Test
    public void nullRarity() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

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
  }

  @Nested
  @DisplayName("Stat Parameter tests")
  class StatParameterTests {

    @DisplayName("Throws if unsupported stat parameter")
    @Test
    public void unsupportedStat() {

    }

    @DisplayName("Throws if null stat parameter")
    @Test
    public void nullStat() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

    }

    @DisplayName("Include single stat parameter")
    @Test
    public void includeSingleStat() {

    }

    @DisplayName("Include multiple stat parameters")
    @Test
    public void includeMultipleStat() {

    }

    @DisplayName("Disallow single stat parameter")
    @Test
    public void disallowSingleStat() {

    }

    @DisplayName("Disallow multiple stat parameters")
    @Test
    public void disallowMultipleStat() {

    }

    @DisplayName("Mixed multiple stat parameters")
    @Test
    public void mixedMultipleStat() {

    }
  }

  @Nested
  @DisplayName("StatVersusStat Parameter tests")
  class StatVersusStatParameterTests {

    @DisplayName("Throws if unsupported stat versus stat parameter")
    @Test
    public void unsupportedStatVersusStat() {

    }

    @DisplayName("Throws if null stat versus stat parameter")
    @Test
    public void nullStatVersusStat() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

    }

    @DisplayName("Include single stat versus stat parameter")
    @Test
    public void includeSingleStatVersusStat() {

    }

    @DisplayName("Include multiple stat versus stat parameters")
    @Test
    public void includeMultipleStatVersusStat() {

    }

    @DisplayName("Disallow single stat versus stat parameter")
    @Test
    public void disallowSingleStatVersusStat() {

    }

    @DisplayName("Disallow multiple stat versus stat parameters")
    @Test
    public void disallowMultipleStatVersusStat() {

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

    }

    @DisplayName("Throws if null mana type parameter")
    @Test
    public void nullManaType() {

    }

    @DisplayName("No parameters")
    @Test
    public void noParameters() {

    }

    @DisplayName("Include single mana type parameter")
    @Test
    public void includeSingleManaType() {

    }

    @DisplayName("Include multiple mana type parameters")
    @Test
    public void includeMultipleManaType() {

    }

    @DisplayName("Disallow single mana type parameter")
    @Test
    public void disallowSingleManaType() {

    }

    @DisplayName("Disallow multiple mana type parameters")
    @Test
    public void disallowMultipleManaType() {

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

    @DisplayName("Empty card expansion parameter with multiple card parameters")
    @Test
    public void emptyCardExpansionMultipleCard() {

    }

    @DisplayName("One of each type of parameter")
    @Test
    public void singleEverything() {

    }

    @DisplayName("Multiple of each type of parameter")
    @Test
    public void multipleEverything() {

    }
  }
}