import static org.junit.jupiter.api.Assertions.*;

import database.access.CardChannel;
import database.access.DefaultDatabaseChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.print.DocFlavor.STRING;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import value_objects.card.Card;
import value_objects.card_printing.CardPrintingInfo;
import value_objects.query.CardQuery;

/**
 *
 */
public class CardRetrievalTest {

  static CardChannel cardChannel;
  static CardQuery cardQuery;
  static SortedSet<Card> cardQueryResult;

  @BeforeAll
  public static void init() throws SQLException {
    Path pathToDatabase = Paths.get("resources\\test.db").toAbsolutePath();
    cardChannel = new DefaultDatabaseChannel(pathToDatabase);
    cardQuery = cardChannel.getQuery();
    cardQueryResult = new TreeSet<>();
  }

  @AfterEach
  public void afterEach() {
    cardQuery.clear();
    cardQueryResult.clear();
  }

  // Single creature card from single expansion with no relationship
  @DisplayName("Single creature card with expansion with no relaitonship")
  @Test
  public void singleCreatureSingleExpansionNoRelationship() throws SQLException {
    cardQuery.byName("Fabled", true);
    cardQuery.byName("Hero", true);
    SortedSet<Card> queryResult = cardChannel.queryCards(cardQuery);

    // Check size
    assertEquals(1, queryResult.size());

    Card soleResult = queryResult.iterator().next();

    // Check name
    assertEquals("Fabled Hero", soleResult.getName());

    // Check cmc
    assertEquals(3, soleResult.getConvertedManaCost());

    // Check mana cost
    Map<String, Integer> expectedManaCost = new HashMap<>();
    expectedManaCost.put("{1}", 1);
    expectedManaCost.put("{W}", 2);
    assertEquals(expectedManaCost, soleResult.getManaCost());

    Set<String> colors = new HashSet<>();
    colors.add("W");
    // Check colors
    assertEquals(colors, soleResult.getColors());

    // Check color identity
    assertEquals(colors, soleResult.getColorIdentity());

    // Check text
    String expectedText = "Double strike\nHeroic — Whenever you cast a spell that targets Fabled Hero, put a +1/+1 counter on Fabled Hero.";
    assertEquals(expectedText, soleResult.getText());

    // Check supertypes
    assertTrue(soleResult.getSupertypes().isEmpty());

    // Check types
    Set<String> types = new HashSet<>();
    types.add("creature");
    assertEquals(types, soleResult.getTypes());

    // Check subtypes
    Set<String> subtypes = new HashSet<>();
    subtypes.add("human");
    subtypes.add("soldier");
    assertEquals(subtypes, soleResult.getSubtypes());

    // Check extra stats
    HashMap<String, String> stats = new HashMap<>();
    stats.put("power", "2");
    stats.put("toughness", "2");
    assertEquals(stats, soleResult.getExtraStats());

    // Check relationship
    assertFalse(soleResult.getRelationships().hasRelationship());

    // Check card printings
    SortedSet<CardPrintingInfo> cardPrintings = soleResult.getCardPrintings();
    assertEquals(1, cardPrintings.size());

    CardPrintingInfo solePrinting = cardPrintings.first();
    assertEquals(soleResult.getName(), solePrinting.getCardName()); // Check name
    assertEquals("Theros", solePrinting.getCardExpansion()); // Check expansion
    assertEquals("12", solePrinting.getIdentifyingNumber()); // Check identifying number
    assertEquals("rare", solePrinting.getRarity()); // Check rarity
    assertEquals("\"You. Poet. Be sure to write this down.\"", solePrinting.getFlavorText());
    assertEquals("Aaron Miller", solePrinting.getArtist());
  }

  // Single planeswalker card from single expansion with no relationship
  @DisplayName("Single creature card with single expansion with no relationship")
  @Test
  public void singlePlaneswalkerSingleExpansionNoRelationship() throws SQLException {
    cardQuery.byName("Ajani", true);
    cardQuery.byName("Valiant", true);
    cardQuery.byName("Protector", true);
    SortedSet<Card> queryResult = cardChannel.queryCards(cardQuery);
    System.out.println(cardQuery.asQuery());
    // Check size
    assertEquals(1, queryResult.size());

    Card soleResult = queryResult.first();

    // Check name
    assertEquals("Ajani, Valiant Protector", soleResult.getName());

    // Check cmc
    assertEquals(6, soleResult.getConvertedManaCost());

    // Check mana cost
    Map<String, Integer> expectedManaCost = new HashMap<>();
    expectedManaCost.put("{1}", 4);
    expectedManaCost.put("{G}", 1);
    expectedManaCost.put("{W}", 1);
    assertEquals(expectedManaCost, soleResult.getManaCost());

    Set<String> colors = new HashSet<>();
    colors.add("G");
    colors.add("W");

    // Check colors
    assertEquals(colors, soleResult.getColors());

    // Check color identity
    assertEquals(colors, soleResult.getColorIdentity());

    // Check text
    String expectedText = "+2: Put two +1/+1 counters on up to one target creature."
        + "\n+1: Reveal cards from the top of your library until you reveal a creature card. "
        + "Put that card into your hand and the rest on the bottom of your library in a random order."
        + "\n−11: Put X +1/+1 counters on target creature, where X is your life total. "
        + "That creature gains trample until end of turn.";
    assertEquals(expectedText, soleResult.getText());

    // Check supertypes
    Set<String> supertypes = new HashSet<>();
    supertypes.add("legendary");
    assertEquals(supertypes, soleResult.getSupertypes());

    // Check types
    Set<String> types = new HashSet<>();
    types.add("planeswalker");
    assertEquals(types, soleResult.getTypes());

    // Check subtypes
    Set<String> subtypes = new HashSet<>();
    subtypes.add("ajani");
    assertEquals(subtypes, soleResult.getSubtypes());

    // Check extra stats
    HashMap<String, String> stats = new HashMap<>();
    stats.put("loyalty", "4");
    assertEquals(stats, soleResult.getExtraStats());

    // Check relationship
    assertFalse(soleResult.getRelationships().hasRelationship());

    // Check card printings
    SortedSet<CardPrintingInfo> cardPrintings = soleResult.getCardPrintings();
    assertEquals(1, cardPrintings.size());

    CardPrintingInfo solePrinting = cardPrintings.first();
    assertEquals(soleResult.getName(), solePrinting.getCardName()); // Check name
    assertEquals("Aether Revolt", solePrinting.getCardExpansion()); // Check expansion
    assertEquals("185", solePrinting.getIdentifyingNumber()); // Check identifying number
    assertEquals("mythic", solePrinting.getRarity()); // Check rarity
    assertTrue(solePrinting.getFlavorText().isEmpty());
    assertEquals("Anna Steinbauer", solePrinting.getArtist());
  }
  // Check card with differing color and coloridentity


  // Single non-creature, non-planeswalker card from single expansion with no relationship

  // Multiple cards from single expansion with no relationship

  // Single card from single expansion with two card relationship

  // Single card from single expansion with three card relationship

}
