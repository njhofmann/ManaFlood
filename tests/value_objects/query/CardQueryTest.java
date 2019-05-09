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
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byName("", true);
      });
    }

    @DisplayName("Throws if name parameter with space")
    @Test
    public void nameWithSpace() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byName(" ", true);
      });
    }

    @DisplayName("Throws if null name parameter")
    @Test
    public void nullName() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byName(null, true);
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
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byText("", true);
      });
    }

    @DisplayName("Throws if text parameter with space")
    @Test
    public void textWithSpace() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byText(" ", true);
      });
    }

    @DisplayName("Throws if null text parameter")
    @Test
    public void nullText() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byText(null, true);
      });
    }

    @DisplayName("Include single text parameter")
    @Test
    public void includeSingleText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT name "
          + "FROM Card "
          + "WHERE text LIKE '%ro%'"
          + ")";
      cardQuery.byText("ro", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple text parameters")
    @Test
    public void includeMultipleText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT name "
          + "FROM Card "
          + "WHERE text LIKE '%io%' "
          + "AND text LIKE '%hj%'"
          + ")";
      cardQuery.byText("io", true);
      cardQuery.byText("hj", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single text parameter")
    @Test
    public void disallowSingleText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT name "
          + "FROM Card "
          + "WHERE text NOT LIKE '%wkj%'"
          + ")";
      cardQuery.byText("wkj", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple text parameters")
    @Test
    public void disallowMultipleText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT name "
          + "FROM Card "
          + "WHERE text NOT LIKE '%fol%' "
          + "AND text NOT LIKE '%nv%'"
          + ")";
      cardQuery.byText("fol", false);
      cardQuery.byText("nv", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple text parameters")
    @Test
    public void mixedMultipleText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT name "
          + "FROM Card "
          + "WHERE text NOT LIKE '%lk%' "
          + "AND text NOT LIKE '%ds%' "
          + "AND text LIKE '%y%' "
          + "AND text NOT LIKE '%ou%'"
          + ")";
      cardQuery.byText("lk", false);
      cardQuery.byText("ds", false);
      cardQuery.byText("y", true);
      cardQuery.byText("ou", false);
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
        cardQuery.byColor("P", true);
      });
    }

    @DisplayName("Throws if null color parameter")
    @Test
    public void nullColor() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byColor(null, true);
      });
    }

    @DisplayName("Include single color parameter")
    @Test
    public void includeSingleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Color t0 "
          + "WHERE t0.color = 'G'"
          + ")";
      cardQuery.byColor("G", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple color parameters")
    @Test
    public void includeMultipleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Color t0 "
          + "JOIN Color t1 "
          + "ON t0.card_name = t1.card_name "
          + "WHERE t0.color = 'G' "
          + "AND t1.color = 'B'"
          + ")";
      cardQuery.byColor("G", true);
      cardQuery.byColor("B", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single color parameter")
    @Test
    public void disallowSingleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Color t0 "
          + "WHERE t0.color != 'C'"
          + ")";
      cardQuery.byColor("C", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple color parameters")
    @Test
    public void disallowMultipleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Color t0 "
          + "JOIN Color t1 "
          + "ON t0.card_name = t1.card_name "
          + "JOIN Color t2 "
          + "ON t0.card_name = t2.card_name "
          + "WHERE t0.color != 'U' "
          + "AND t1.color != 'R' "
          + "AND t2.color != 'B'"
          + ")";
      cardQuery.byColor("U", false);
      cardQuery.byColor("R", false);
      cardQuery.byColor("B", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple color parameters")
    @Test
    public void mixedMultipleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Color t0 "
          + "JOIN Color t1 "
          + "ON t0.card_name = t1.card_name "
          + "JOIN Color t2 "
          + "ON t0.card_name = t2.card_name "
          + "JOIN Color t3 "
          + "ON t0.card_name = t3.card_name "
          + "WHERE t0.color = 'U' "
          + "AND t1.color != 'C' "
          + "AND t2.color != 'B' "
          + "AND t3.color = 'G'"
          + ")";
      cardQuery.byColor("U", true);
      cardQuery.byColor("C", false);
      cardQuery.byColor("B", false);
      cardQuery.byColor("G", true);
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
        cardQuery.byColorIdentity("M", true);
      });
    }

    @DisplayName("Throws if null color parameter")
    @Test
    public void nullColor() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byColorIdentity(null, true);
      });
    }

    @DisplayName("Include single color parameter")
    @Test
    public void includeSingleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM ColorIdentity t0 "
          + "WHERE t0.color = 'R'"
          + ")";
      cardQuery.byColorIdentity("R", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple color parameters")
    @Test
    public void includeMultipleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM ColorIdentity t0 "
          + "JOIN ColorIdentity t1 "
          + "ON t0.card_name = t1.card_name "
          + "WHERE t0.color = 'R' "
          + "AND t1.color = 'U'"
          + ")";
      cardQuery.byColorIdentity("R", true);
      cardQuery.byColorIdentity("U", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single color parameter")
    @Test
    public void disallowSingleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM ColorIdentity t0 "
          + "WHERE t0.color != 'W'"
          + ")";
      cardQuery.byColorIdentity("W", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple color parameters")
    @Test
    public void disallowMultipleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM ColorIdentity t0 "
          + "JOIN ColorIdentity t1 "
          + "ON t0.card_name = t1.card_name "
          + "WHERE t0.color != 'G' "
          + "AND t1.color != 'W'"
          + ")";
      cardQuery.byColorIdentity("G", false);
      cardQuery.byColorIdentity("W", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple color parameters")
    @Test
    public void mixedMultipleColor() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM ColorIdentity t0 "
          + "JOIN ColorIdentity t1 "
          + "ON t0.card_name = t1.card_name "
          + "JOIN ColorIdentity t2 "
          + "ON t0.card_name = t2.card_name "
          + "JOIN ColorIdentity t3 "
          + "ON t0.card_name = t3.card_name "
          + "WHERE t0.color = 'R' "
          + "AND t1.color != 'W' "
          + "AND t2.color != 'B' "
          + "AND t3.color = 'U'"
          + ")";
      cardQuery.byColorIdentity("R", true);
      cardQuery.byColorIdentity("W", false);
      cardQuery.byColorIdentity("B", false);
      cardQuery.byColorIdentity("U", true);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("Type Parameter tests")
  class TypeParameterTests {

    @DisplayName("Throws if unsupported type parameter")
    @Test
    public void unsupportedType() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byType("foodbar", false);
      });
    }

    @DisplayName("Throws if null type parameter")
    @Test
    public void nullType() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byType(null, false);
      });
    }

    @DisplayName("Include single type parameter")
    @Test
    public void includeSingleType() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Type t0 "
          + "WHERE t0.type = 'soldier'"
          + ")";
      cardQuery.byType("soldier", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple type parameters")
    @Test
    public void includeMultipleType() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Type t0 "
          + "JOIN Type t1 "
          + "ON t0.card_name = t1.card_name "
          + "WHERE t0.type = 'goblin' "
          + "AND t1.type = 'warrior'"
          + ")";
      cardQuery.byType("goblin", true);
      cardQuery.byType("warrior", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single type parameter")
    @Test
    public void disallowSingleType() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Type t0 "
          + "WHERE t0.type != 'human'"
          + ")";
      cardQuery.byType("human", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple type parameters")
    @Test
    public void disallowMultipleType() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Type t0 "
          + "JOIN Type t1 "
          + "ON t0.card_name = t1.card_name "
          + "WHERE t0.type != 'creature' "
          + "AND t1.type != 'instant'"
          + ")";
      cardQuery.byType("creature", false);
      cardQuery.byType("instant", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple type parameters")
    @Test
    public void mixedMultipleType() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Type t0 "
          + "JOIN Type t1 "
          + "ON t0.card_name = t1.card_name "
          + "JOIN Type t2 "
          + "ON t0.card_name = t2.card_name "
          + "WHERE t0.type != 'goblin' "
          + "AND t1.type != 'human' "
          + "AND t2.type = 'warrior'"
          + ")";
      cardQuery.byType("goblin", false);
      cardQuery.byType("human", false);
      cardQuery.byType("warrior", true);
      assertEquals(result, cardQuery.asQuery());
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
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.bySet("sticky note", true);
      });
    }

    @DisplayName("Throws if null set parameter")
    @Test
    public void nullSet() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.bySet(null, true);
      });
    }

    @DisplayName("Include single set parameter")
    @Test
    public void includeSingleSet() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.expansion = 'Guilds of Ravnica'";
      cardQuery.bySet("Guilds of Ravnica", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple set parameters")
    @Test
    public void includeMultipleSet() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "WHERE t0.expansion = 'Guilds of Ravnica' "
          + "AND t1.expansion = 'Ravnica Allegiance'";
      cardQuery.bySet("Guilds of Ravnica", true);
      cardQuery.bySet("Ravnica Allegiance", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single set parameter")
    @Test
    public void disallowSingleSet() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.expansion != 'Theros'";
      cardQuery.bySet("Theros", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple set parameters")
    @Test
    public void disallowMultipleSet() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "WHERE t0.expansion != 'Fate Reforged' "
          + "AND t1.expansion != 'Dark Ascension'";
      cardQuery.bySet("Fate Reforged", false);
      cardQuery.bySet("Dark Ascension", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple set parameters")
    @Test
    public void mixedMultipleSet() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "JOIN CardExpansion t2 "
          + "ON t0.card_name = t2.card_name "
          + "AND t0.expansion = t2.expansion "
          + "JOIN CardExpansion t3 "
          + "ON t0.card_name = t3.card_name "
          + "AND t0.expansion = t3.expansion "
          + "WHERE t0.expansion != 'Amonkhet' "
          + "AND t1.expansion = 'Ixalan' "
          + "AND t2.expansion != 'Dominaria' "
          + "AND t3.expansion = 'Battlebond'";

      cardQuery.bySet("Amonkhet", false);
      cardQuery.bySet("Ixalan", true);
      cardQuery.bySet("Dominaria", false);
      cardQuery.bySet("Battlebond", true);
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
        cardQuery.byArtist("ghor", true);
      });
    }

    @DisplayName("Throws if null artist parameter")
    @Test
    public void nullArtist() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byArtist(null, true);
      });
    }

    @DisplayName("Include single artist parameter")
    @Test
    public void includeSingleArtist() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.artist = 'John Avon'";
      cardQuery.byArtist("John Avon", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple artist parameters")
    @Test
    public void includeMultipleArtist() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "JOIN CardExpansion t2 "
          + "ON t0.card_name = t2.card_name "
          + "AND t0.expansion = t2.expansion "
          + "WHERE t0.artist = 'Kev Walker' "
          + "AND t1.artist = 'Mark Zug' "
          + "AND t2.artist = 'Nils Hamm'";
      cardQuery.byArtist("Kev Walker", true);
      cardQuery.byArtist("Mark Zug", true);
      cardQuery.byArtist("Nils Hamm", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single artist parameter")
    @Test
    public void disallowSingleArtist() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.artist != 'Izzy'";
      cardQuery.byArtist("Izzy", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple artist parameters")
    @Test
    public void disallowMultipleArtist() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "WHERE t0.artist != 'Adam Rex' "
          + "AND t1.artist != 'Chippy'";
      cardQuery.byArtist("Adam Rex", false);
      cardQuery.byArtist("Chippy", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple artist parameters")
    @Test
    public void mixedMultipleArtist() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "JOIN CardExpansion t2 "
          + "ON t0.card_name = t2.card_name "
          + "AND t0.expansion = t2.expansion "
          + "JOIN CardExpansion t3 "
          + "ON t0.card_name = t3.card_name "
          + "AND t0.expansion = t3.expansion "
          + "JOIN CardExpansion t4 "
          + "ON t0.card_name = t4.card_name "
          + "AND t0.expansion = t4.expansion "
          + "WHERE t0.artist != 'Winona Nelson' "
          + "AND t1.artist = 'Titus Lunter' "
          + "AND t2.artist = 'Nils Hamm' "
          + "AND t3.artist != 'Jung Park' "
          + "AND t4.artist != 'Jeff Miracola'";
      cardQuery.byArtist("Winona Nelson", false);
      cardQuery.byArtist("Titus Lunter", true);
      cardQuery.byArtist("Nils Hamm", true);
      cardQuery.byArtist("Jung Park", false);
      cardQuery.byArtist("Jeff Miracola", false);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("FlavorText Parameter tests")
  class FlavorTextParameterTests {

    @DisplayName("Throws if flavor text parameter is empty")
    @Test
    public void emptyFlavorText() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byFlavorText("", true);
      });
    }

    @DisplayName("Throws if flavor text parameter has a space")
    @Test
    public void flavorTextWithSpace() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byFlavorText(" ", true);
      });
    }

    @DisplayName("Throws if null flavor text parameter")
    @Test
    public void nullFlavorText() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byFlavorText(null, true);
      });
    }

    @DisplayName("Include single flavor text parameter")
    @Test
    public void includeSingleFlavorText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.flavor_text LIKE '%foo%'";
      cardQuery.byFlavorText("foo", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple flavor text parameters")
    @Test
    public void includeMultipleFlavorText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "WHERE t0.flavor_text LIKE '%foo%' "
          + "AND t1.flavor_text LIKE '%pies%'";
      cardQuery.byFlavorText("foo", true);
      cardQuery.byFlavorText("pies", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single flavor text parameter")
    @Test
    public void disallowSingleFlavorText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.flavor_text NOT LIKE '%kop%'";
      cardQuery.byFlavorText("kop", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple flavor text parameters")
    @Test
    public void disallowMultipleFlavorText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "WHERE t0.flavor_text NOT LIKE '%jfk%' "
          + "AND t1.flavor_text NOT LIKE '%aoun%'";
      cardQuery.byFlavorText("jfk", false);
      cardQuery.byFlavorText("aoun", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple flavor text parameters")
    @Test
    public void mixedMultipleFlavorText() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "JOIN CardExpansion t2 "
          + "ON t0.card_name = t2.card_name "
          + "AND t0.expansion = t2.expansion "
          + "JOIN CardExpansion t3 "
          + "ON t0.card_name = t3.card_name "
          + "AND t0.expansion = t3.expansion "
          + "WHERE t0.flavor_text NOT LIKE '%jfk%' "
          + "AND t1.flavor_text NOT LIKE '%aoun%' "
          + "AND t2.flavor_text LIKE '%asd%' "
          + "AND t3.flavor_text LIKE '%man%'";
      cardQuery.byFlavorText("jfk", false);
      cardQuery.byFlavorText("aoun", false);
      cardQuery.byFlavorText("asd", true);
      cardQuery.byFlavorText("man", true);
      assertEquals(result, cardQuery.asQuery());
    }
  }

  @Nested
  @DisplayName("Rarity Parameter tests")
  class RarityParameterTests {

    @DisplayName("Throws if unsupported rarity parameter")
    @Test
    public void unsupportedRarity() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byRarity("supercommon", true);
      });
    }

    @DisplayName("Throws if null rarity parameter")
    @Test
    public void nullRarity() {
      assertThrows(IllegalArgumentException.class, () -> {
        cardQuery.byRarity(null, true);
      });
    }

    @DisplayName("Include single rarity parameter")
    @Test
    public void includeSingleRarity() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.rarity = 'common'";
      cardQuery.byRarity("common", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Include multiple rarity parameters")
    @Test
    public void includeMultipleRarity() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "WHERE t0.rarity = 'rare' "
          + "AND t1.rarity = 'mythic'";
      cardQuery.byRarity("rare", true);
      cardQuery.byRarity("mythic", true);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow single rarity parameter")
    @Test
    public void disallowSingleRarity() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.rarity != 'rare'";
      cardQuery.byRarity("rare", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Disallow multiple rarity parameters")
    @Test
    public void disallowMultipleRarity() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "WHERE t0.rarity != 'common' "
          + "AND t1.rarity != 'mythic'";
      cardQuery.byRarity("common", false);
      cardQuery.byRarity("mythic", false);
      assertEquals(result, cardQuery.asQuery());
    }

    @DisplayName("Mixed multiple rarity parameters")
    @Test
    public void mixedMultipleRarity() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "JOIN CardExpansion t1 "
          + "ON t0.card_name = t1.card_name "
          + "AND t0.expansion = t1.expansion "
          + "JOIN CardExpansion t2 "
          + "ON t0.card_name = t2.card_name "
          + "AND t0.expansion = t2.expansion "
          + "WHERE t0.rarity = 'uncommon' "
          + "AND t1.rarity != 'mythic' "
          + "AND t2.rarity != 'rare'";
      cardQuery.byRarity("uncommon", true);
      cardQuery.byRarity("mythic", false);
      cardQuery.byRarity("rare", false);
      assertEquals(result, cardQuery.asQuery());
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

    @DisplayName("Single stat parameter for each stat and comparison combination")
    @Test
    public void singleStatCombo() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN "
          + "("
          + "SELECT t0.card_name "
          + "FROM Stat t0 "
          + "WHERE t0.category = '%s' "
          + "AND t0.base_value %s 3"
          + ")";

      for (Stat stat : Stat.values()) {
        for (Comparison comparison : Comparison.values()) {
          cardQuery.byStat(stat, comparison, 3);
          String resultFormatted = String.format(result, stat.getValue(), comparison.getValue());
          assertEquals(resultFormatted, cardQuery.asQuery());
          cardQuery.clear();
        }
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

    @DisplayName("Single stat parameter for each stat, comparison, and stat combination")
    @Test
    public void singleStatComparisonStat() {
      String result = "SELECT t0.card_name, t0.expansion "
          + "FROM CardExpansion t0 "
          + "WHERE t0.card_name IN ("
          + ""
          + "SELECT t0.card_name "
          + "FROM Stat t0 "
          + "JOIN Stat t1 "
          + "ON t0.card_name = t1.card_name "
          + "WHERE t0.category = '%s' "
          + "AND t1.category = '%s' "
          + "AND t0.base_value %s t1.base_value"
          + ")";

      for (Stat stat : Stat.values()) {
        for (Comparison comparison : Comparison.values()) {
          for (Stat otherStat : Stat.values()) {
            if (otherStat.equals(stat)) {
              assertThrows(IllegalArgumentException.class, () -> {
                cardQuery.byStatVersusStat(stat, comparison, otherStat);
              });
            }
            else {
              cardQuery.byStatVersusStat(stat, comparison, otherStat);
              String resultFormatted = String.format(result, stat.getValue(), otherStat.getValue(), comparison.getValue());
              assertEquals(resultFormatted, cardQuery.asQuery());
              cardQuery.clear();
            }
          }
        }
      }
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