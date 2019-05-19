import static org.junit.jupiter.api.Assertions.*;

import database.access.CardChannel;
import database.access.DefaultDatabaseChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.SortedSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import value_objects.card.Card;
import value_objects.card.query.CardQuery;
import value_objects.card.query.Comparison;
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
    Path pathToDatabase = Paths.get("resources\\test.db").toAbsolutePath();
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
    cardQuery.byName("Abzan", true);
    cardQuery.byName("Charm", true);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Gruul", true);
    cardQuery.byName("Charm", true);
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
    cardQuery.byName("Enforcer", true);
    cardQuery.byName("Griffin", true);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Abbey", true);
    cardQuery.byName("Griffin", true);
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
    cardQuery.byName("Firestorm", true);
    cardQuery.byName("Phoenix", true);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Kuldotha", true);
    cardQuery.byName("Phoenix", true);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byName("Shard", true);
    cardQuery.byName("Phoenix", true);
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

    cardQuery.byName("Boros", true);
    cardQuery.byName("Charm", true);
    //cardQuery.byBlock("Return to Ravnica", true);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();
    cardQuery.clear();

    cardQuery.byColor("R", true);
    cardQuery.byColor("W", true);
    cardQuery.byType("instant", true);
    cardQuery.byStat(Stat.CMC, Comparison.EQUAL, 2);
    cardQuery.byText("indestructible", true);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertEquals(cardA, cardB);
    assertEquals(0, cardA.compareTo(cardB));
    assertEquals(0, cardB.compareTo(cardA));
  }


  @DisplayName("Different cards, same name, different expansions, before")
  @Test
  public void sameNameDiffExpansionsBefore() throws SQLException {

  }

  @DisplayName("Different cards, same name, different expansions, after")
  @Test
  public void sameNameDiffExpansionsAfter() throws SQLException {

  }

  @DisplayName("Different cards, same name, different expansions, transitive")
  @Test
  public void sameNameDiffExpansionsTransitive() throws SQLException {

  }
}
