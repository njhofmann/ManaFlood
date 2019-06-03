import static org.junit.jupiter.api.Assertions.*;

import database.access.CardChannel;
import database.access.DefaultDatabaseChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.SortedSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import value_objects.card.Card;
import value_objects.card.printing.CardPrinting;
import value_objects.card.printing.CardPrintingInfo;
import value_objects.card.query.CardQuery;
import value_objects.card.query.Comparison;
import value_objects.card.query.SearchOption;
import value_objects.card.query.Stat;

/**
 * Tests to ensure overridden equality and implemented compareTo methods for {@link Card} work
 * properly.
 */
public class CardEqualityTest {

  static CardQuery cardQuery;
  static CardChannel cardChannel;
  Card cardA;
  Card cardB;
  Card cardC;
  SortedSet<Card> cardQueryResult;

  @BeforeAll
  public static void init() throws SQLException {
    Path pathToDatabase = Paths.get("resources\\cddb.db").toAbsolutePath();
    cardChannel = new DefaultDatabaseChannel(pathToDatabase);
    cardQuery = cardChannel.getQuery();
  }

  @BeforeEach
  public void clearQuery() {
    cardQuery.clear();
  }

  @DisplayName("Different cards, by name, before")
  @Test
  public void diffCardNamesBefore() throws SQLException {
    cardQuery.byName("Abzan", SearchOption.MustInclude);
    cardQuery.byName("Charm", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Gruul", SearchOption.MustInclude);
    cardQuery.byName("Charm", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertTrue(cardA.compareTo(cardB) < 0);
    assertTrue(cardB.compareTo(cardA) > 0);
  }

  @DisplayName("Different cards, by name, after")
  @Test
  public void diffCardNamesAfter() throws SQLException {
    cardQuery.byName("Enforcer", SearchOption.MustInclude);
    cardQuery.byName("Griffin", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Abbey", SearchOption.MustInclude);
    cardQuery.byName("Griffin", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertTrue(cardA.compareTo(cardB) > 0);
    assertTrue(cardB.compareTo(cardA) < 0);
  }

  @DisplayName("Different card, by name, transitive")
  @Test
  public void diffCardNamesTransitive() throws SQLException {
    cardQuery.byName("Firestorm", SearchOption.MustInclude);
    cardQuery.byName("Phoenix", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Kuldotha", SearchOption.MustInclude);
    cardQuery.byName("Phoenix", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Shard", SearchOption.MustInclude);
    cardQuery.byName("Phoenix", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardC = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertNotEquals(cardA, cardC);
    assertNotEquals(cardB, cardC);

    assertTrue(cardA.compareTo(cardB) < 0);
    assertTrue(cardB.compareTo(cardA) > 0);
    assertTrue(cardB.compareTo(cardC) < 0);
    assertTrue(cardC.compareTo(cardB) > 0);
    assertTrue(cardA.compareTo(cardC) < 0);
    assertTrue(cardC.compareTo(cardA) > 0);
  }

  @DisplayName("Equal cards")
  @Test
  public void equalCards() throws SQLException {
    // Should retrieve Boros Charm with all of its expansions

    cardQuery.byName("Boros", SearchOption.MustInclude);
    cardQuery.byName("Charm", SearchOption.MustInclude);
    //cardQuery.byBlock("Return to Ravnica", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byColor("R", SearchOption.MustInclude);
    cardQuery.byColor("W", SearchOption.MustInclude);
    cardQuery.byType("Instant", SearchOption.MustInclude);
    cardQuery.byStat(Stat.CMC, Comparison.EQUAL, 2);
    cardQuery.byText("indestructible", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertEquals(cardA, cardB);
    assertEquals(cardA.hashCode(), cardB.hashCode());
    assertEquals(0, cardA.compareTo(cardB));
    assertEquals(0, cardB.compareTo(cardA));
  }


  @DisplayName("Same name, same expansions with more, before")
  @Test
  public void sameNameSameExpansionsMoreBefore() throws SQLException {
    cardQuery.byName("Giant", SearchOption.MustInclude);
    cardQuery.byName("Growth", SearchOption.MustInclude);
    cardQuery.bySet("Battlebond", SearchOption.MustInclude);
    cardQuery.bySet("Magic 2014", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);
    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();

    cardQuery.bySet("Return to Ravnica", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertTrue(cardA.compareTo(cardB) < 0);
    assertTrue(cardB.compareTo(cardA) > 0);
  }

  @DisplayName("Different cards, same expansions with more, after")
  @Test
  public void sameNameSameExpansionsMoreAfter() throws SQLException {
    cardQuery.byName("Lightning", SearchOption.MustInclude);
    cardQuery.byName("Bolt", SearchOption.MustInclude);
    cardQuery.bySet("Magic 2011", SearchOption.MustInclude);
    cardQuery.bySet("Beatdown Box Set", SearchOption.MustInclude);
    cardQuery.bySet("Anthologies", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);
    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Lightning", SearchOption.MustInclude);
    cardQuery.byName("Bolt", SearchOption.MustInclude);
    cardQuery.bySet("Beatdown Box Set", SearchOption.MustInclude);
    cardQuery.bySet("Anthologies", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertTrue(cardB.compareTo(cardA) < 0);
    assertTrue(cardA.compareTo(cardB) > 0);
  }

  @DisplayName("Different cards, same name same expansions and more, transitive")
  @Test
  public void sameNameSameExpansionsMoreTransitive() throws SQLException {
    cardQuery.byName("Swords", SearchOption.MustInclude);
    cardQuery.byName("to", SearchOption.MustInclude);
    cardQuery.byName("Plowshares", SearchOption.MustInclude);
    cardQuery.bySet("Conspiracy", SearchOption.MustInclude);

    cardQueryResult = cardChannel.queryCards(cardQuery);
    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();

    cardQuery.bySet("Iconic Masters", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    cardQuery.bySet("Masters 25", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardC = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertNotEquals(cardA, cardC);
    assertNotEquals(cardB, cardC);

    assertTrue(cardA.compareTo(cardB) < 0);
    assertTrue(cardB.compareTo(cardA) > 0);
    assertTrue(cardB.compareTo(cardC) < 0);
    assertTrue(cardC.compareTo(cardB) > 0);
    assertTrue(cardA.compareTo(cardC) < 0);
    assertTrue(cardC.compareTo(cardA) > 0);
  }

  @DisplayName("Same name, different expansions, before")
  @Test
  public void sameNameDiffExpansionsBefore() throws SQLException {
    cardQuery.byName("Dark", SearchOption.MustInclude);
    cardQuery.byName("Ritual", SearchOption.MustInclude);
    cardQuery.bySet("Planechase", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Dark", SearchOption.MustInclude);
    cardQuery.byName("Ritual", SearchOption.MustInclude);
    cardQuery.bySet("Tempest Remastered", SearchOption.MustInclude);
    cardQuery.bySet("Urza's Saga", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertTrue(cardA.compareTo(cardB) < 0);
    assertTrue(cardB.compareTo(cardA) > 0);
  }

  @DisplayName("Different cards, diff expansions, after")
  @Test
  public void sameNameDiffExpansionsAfter() throws SQLException {
    cardQuery.byName("Brainstorm", SearchOption.MustInclude);
    cardQuery.bySet("Vintage Masters", SearchOption.MustInclude);
    cardQuery.bySet("Beatdown Box Set", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Brainstorm", SearchOption.MustInclude);
    cardQuery.bySet("Commander 2018", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertTrue(cardA.compareTo(cardB) < 0);
    assertTrue(cardB.compareTo(cardA) > 0);
  }

  @DisplayName("Different cards, diff expansions, transitive")
  @Test
  public void sameNameDiffExpansionsTransitive() throws SQLException {
    cardQuery.byName("Swords", SearchOption.MustInclude);
    cardQuery.byName("to", SearchOption.MustInclude);
    cardQuery.byName("Plowshares", SearchOption.MustInclude);
    cardQuery.bySet("Conspiracy", SearchOption.MustInclude);

    cardQueryResult = cardChannel.queryCards(cardQuery);
    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();

    cardQuery.bySet("Iconic Masters", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    cardQuery.bySet("Masters 25", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardC = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertNotEquals(cardA, cardC);
    assertNotEquals(cardB, cardC);

    assertTrue(cardA.compareTo(cardB) < 0);
    assertTrue(cardB.compareTo(cardA) > 0);
    assertTrue(cardB.compareTo(cardC) < 0);
    assertTrue(cardC.compareTo(cardB) > 0);
    assertTrue(cardA.compareTo(cardC) < 0);
    assertTrue(cardC.compareTo(cardA) > 0);
  }

  @DisplayName("Same name, single diff expansion, before")
  @Test
  public void sameNameSingleDiffExpansionBefore() throws SQLException {
    cardQuery.byName("Path", SearchOption.MustInclude);
    cardQuery.byName("to", SearchOption.MustInclude);
    cardQuery.byName("Exile", SearchOption.MustInclude);
    cardQuery.bySet("Archenemy", SearchOption.MustInclude);

    cardQueryResult = cardChannel.queryCards(cardQuery);
    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Path", SearchOption.MustInclude);
    cardQuery.byName("to", SearchOption.MustInclude);
    cardQuery.byName("Exile", SearchOption.MustInclude);
    cardQuery.bySet("Conflux", SearchOption.MustInclude);

    cardQueryResult = cardChannel.queryCards(cardQuery);
    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertTrue(cardA.compareTo(cardB) < 0);
    assertTrue(cardB.compareTo(cardA) > 0);
  }

  @DisplayName("Same name, single diff expansion, after")
  @Test
  public void sameNameSingleDiffExpansionAfter() throws SQLException {
    cardQuery.byName("Doom", SearchOption.MustInclude);
    cardQuery.byName("Blade", SearchOption.MustInclude);
    cardQuery.bySet("Explorers of Ixalan", SearchOption.MustInclude);

    cardQueryResult = cardChannel.queryCards(cardQuery);
    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Doom", SearchOption.MustInclude);
    cardQuery.byName("Blade", SearchOption.MustInclude);
    cardQuery.bySet("Commander 2011", SearchOption.MustInclude);

    cardQueryResult = cardChannel.queryCards(cardQuery);
    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertTrue(cardB.compareTo(cardA) < 0);
    assertTrue(cardA.compareTo(cardB) > 0);
  }

  @DisplayName("Same name, single diff expansion, transitive")
  @Test
  public void sameNameSingleDiffExpansionTransitive() throws SQLException {
    cardQuery.byName("Counterspell", SearchOption.MustInclude);
    cardQuery.bySet("Amonkhet Invocations", SearchOption.MustInclude);

    cardQueryResult = cardChannel.queryCards(cardQuery);
    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Counterspell", SearchOption.MustInclude);
    cardQuery.bySet("Classic Sixth Edition", SearchOption.MustInclude);
    cardQuery.bySet("Masters 25", SearchOption.MustInclude);
    System.out.println(cardQuery.asQuery());
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Counterspell", SearchOption.MustInclude);
    cardQuery.bySet("Vintage Masters", SearchOption.MustInclude);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardC = cardQueryResult.first();

    assertNotEquals(cardA, cardB);
    assertNotEquals(cardA, cardC);
    assertNotEquals(cardB, cardC);

    assertTrue(cardA.compareTo(cardB) < 0);
    assertTrue(cardB.compareTo(cardA) > 0);
    assertTrue(cardB.compareTo(cardC) < 0);
    assertTrue(cardC.compareTo(cardB) > 0);
    assertTrue(cardA.compareTo(cardC) < 0);
    assertTrue(cardC.compareTo(cardA) > 0);
  }
}
