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
import value_objects.query.CardQuery;
import value_objects.query.Comparison;
import value_objects.query.Stat;

public class CardTest {

  static SortedSet<Card> cardQueryResult;
  static CardQuery cardQuery;
  static Card cardA;
  static Card cardB;
  static CardChannel cardChannel;

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

  @DisplayName("Equal cards")
  @Test
  public void equalCards() throws SQLException {
    cardQuery.byName("Boros", true);
    cardQuery.byName("Charm", true);
    cardQuery.byBlock("Return to Ravnica", true);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardA = cardQueryResult.first();

    cardQuery.clear();
    cardQuery.bySet("Gatecrash", true);
    cardQuery.byColor("R", true);
    cardQuery.byColor("W", true);
    cardQuery.byType("instant", true);
    cardQuery.byStat(Stat.CMC, Comparison.EQUAL, 2);
    cardQuery.byText("indestructible", true);
    cardQueryResult = cardChannel.queryCards(cardQuery);

    assertEquals(1, cardQueryResult.size());
    cardB = cardQueryResult.first();

    assertEquals(cardA, cardB);
  }


  @DisplayName("Before card")
  @Test
  public void beforeCard() {

  }

  @DisplayName("After cards")
  @Test
  public void afterCard() {

  }
}
