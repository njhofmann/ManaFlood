import static org.junit.jupiter.api.Assertions.*;

import database.access.CardChannel;
import database.access.DefaultDatabaseChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import value_objects.card.Card;
import value_objects.card.printing.CardPrintingInfo;
import value_objects.card.query.CardQuery;

/**
 * Tests to ensure that built up {@link CardQuery}s retrieve the correct {@link Card}s from the
 * Card and Deck Database (CDDB).
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

  // Check card with differing color and color identity
  @DisplayName("Card with different color and color identity")
  @Test
  public void differentColorColorIdentity() throws SQLException {
    cardQuery.byName("Jodah", true);
    cardQuery.byName("Archmage", true);
    cardQuery.byName("Eternal", true);
    SortedSet<Card> queryResult = cardChannel.queryCards(cardQuery);

    // Check size
    assertEquals(1, queryResult.size());

    Card soleResult = queryResult.first();

    // Check name
    assertEquals("Jodah, Archmage Eternal", soleResult.getName());

    // Check cmc
    assertEquals(4, soleResult.getConvertedManaCost());

    // Check mana cost
    Map<String, Integer> expectedManaCost = new HashMap<>();
    expectedManaCost.put("{1}", 1);
    expectedManaCost.put("{R}", 1);
    expectedManaCost.put("{W}", 1);
    expectedManaCost.put("{U}", 1);
    assertEquals(expectedManaCost, soleResult.getManaCost());


    // Check colors
    Set<String> colors = new HashSet<>();
    colors.add("R");
    colors.add("W");
    colors.add("U");
    assertEquals(colors, soleResult.getColors());

    // Check color identity
    Set<String> colorIdentity = new HashSet<>();
    colorIdentity.add("R");
    colorIdentity.add("W");
    colorIdentity.add("U");
    colorIdentity.add("G");
    colorIdentity.add("B");
    assertEquals(colorIdentity, soleResult.getColorIdentity());


    // Check text
    String expectedText = "Flying\nYou may pay {W}{U}{B}{R}{G} rather than pay the mana cost for "
        + "spells that you cast.";
    assertEquals(expectedText, soleResult.getText());

    // Check supertypes
    Set<String> supertypes = new HashSet<>();
    supertypes.add("legendary");
    assertEquals(supertypes, soleResult.getSupertypes());

    // Check types
    Set<String> types = new HashSet<>();
    types.add("creature");
    assertEquals(types, soleResult.getTypes());

    // Check subtypes
    Set<String> subtypes = new HashSet<>();
    subtypes.add("human");
    subtypes.add("wizard");
    assertEquals(subtypes, soleResult.getSubtypes());

    // Check extra stats
    HashMap<String, String> stats = new HashMap<>();
    stats.put("power", "4");
    stats.put("toughness", "3");
    assertEquals(stats, soleResult.getExtraStats());

    // Check relationship
    assertFalse(soleResult.getRelationships().hasRelationship());

    // Check card printings
    SortedSet<CardPrintingInfo> cardPrintings = soleResult.getCardPrintings();
    assertEquals(2, cardPrintings.size());

    Iterator<CardPrintingInfo> iterator = cardPrintings.iterator();

    CardPrintingInfo firstPrinting = iterator.next();
    assertEquals(soleResult.getName(), firstPrinting.getCardName()); // Check name
    assertEquals("Dominaria", firstPrinting.getCardExpansion()); // Check expansion
    assertEquals("198", firstPrinting.getIdentifyingNumber()); // Check identifying number
    assertEquals("rare", firstPrinting.getRarity()); // Check rarity
    assertEquals("\"Chronicles across the ages describe Jodah. They likely refer not to "
        + "one mage, but to a family or an arcane title.\" — Arkol, Argivian scholar",
        firstPrinting.getFlavorText()); // Check flavor text
    assertEquals("Yongjae Choi", firstPrinting.getArtist()); // Check artist

    CardPrintingInfo secondPrinting = iterator.next();
    assertEquals(soleResult.getName(), secondPrinting.getCardName()); // Check name
    assertEquals("Dominaria Promos", secondPrinting.getCardExpansion()); // Check expansion
    assertEquals("198s", secondPrinting.getIdentifyingNumber()); // Check identifying number
    assertEquals("rare", secondPrinting.getRarity()); // Check rarity
    assertEquals("\"Chronicles across the ages describe Jodah. They likely refer not to "
            + "one mage, but to a family or an arcane title.\" — Arkol, Argivian scholar",
        secondPrinting.getFlavorText()); // Check flavor text
    assertEquals("Yongjae Choi", secondPrinting.getArtist()); // Check artist
  }

  // Single non-creature, non-planeswalker card from single expansion with no relationship
  @DisplayName("Non-creature, non-planeswalker card from single expansion with no relationship")
  @Test
  public void nonCreatureNonPlaneswalkerSingleExpansionNoRelationship() throws SQLException {
    cardQuery.byName("Drastic", true);
    cardQuery.byName("Revelation", true);
    SortedSet<Card> queryResult = cardChannel.queryCards(cardQuery);

    // Check size
    assertEquals(1, queryResult.size());

    Card soleResult = queryResult.first();

    // Check name
    assertEquals("Drastic Revelation", soleResult.getName());

    // Check cmc
    assertEquals(5, soleResult.getConvertedManaCost());

    // Check mana cost
    Map<String, Integer> expectedManaCost = new HashMap<>();
    expectedManaCost.put("{1}", 2);
    expectedManaCost.put("{R}", 1);
    expectedManaCost.put("{B}", 1);
    expectedManaCost.put("{U}", 1);
    assertEquals(expectedManaCost, soleResult.getManaCost());


    Set<String> colors = new HashSet<>();
    colors.add("R");
    colors.add("U");
    colors.add("B");
    // Check colors
    assertEquals(colors, soleResult.getColors());

    // Check color identity
    assertEquals(colors, soleResult.getColorIdentity());

    // Check text
    String expectedText = "Discard your hand. Draw seven cards, then discard three cards at random.";
    assertEquals(expectedText, soleResult.getText());

    // Check supertypes
    assertTrue(soleResult.getSupertypes().isEmpty());

    // Check types
    Set<String> types = new HashSet<>();
    types.add("sorcery");
    assertEquals(types, soleResult.getTypes());

    // Check subtypes
    assertTrue(soleResult.getSubtypes().isEmpty());

    // Check extra stats
    assertTrue(soleResult.getExtraStats().isEmpty());

    // Check relationship
    assertFalse(soleResult.getRelationships().hasRelationship());

    // Check card printings
    SortedSet<CardPrintingInfo> cardPrintings = soleResult.getCardPrintings();
    assertEquals(1, cardPrintings.size());

    CardPrintingInfo firstPrinting = cardPrintings.first();
    assertEquals(soleResult.getName(), firstPrinting.getCardName()); // Check name
    assertEquals("Alara Reborn", firstPrinting.getCardExpansion()); // Check expansion
    assertEquals("111", firstPrinting.getIdentifyingNumber()); // Check identifying number
    assertEquals("uncommon", firstPrinting.getRarity()); // Check rarity
    assertEquals("Every disaster holds mystery, for lack of a sane witness.",
        firstPrinting.getFlavorText()); // Check flavor text
    assertEquals("Trevor Claxton", firstPrinting.getArtist()); // Check artist
  }

  // Multiple cards from single expansion with no relationship
  @DisplayName("Mulitiple cards with no relationship from single expansion")
  @Test
  public void multipleCardsNoRelationshipSingleExpansion() throws SQLException {
    cardQuery.byType("instant", true);
    cardQuery.byColor("G", true);
    cardQuery.byColor("B", true);
    cardQuery.byBlock("Return to Ravnica", true);
    SortedSet<Card> queryResult = cardChannel.queryCards(cardQuery);

    // Check size
    assertEquals(4, queryResult.size());

    Card soleResult = queryResult.first();

    // Check name
    assertEquals("Drastic Revelation", soleResult.getName());

    // Check cmc
    assertEquals(5, soleResult.getConvertedManaCost());

    // Check mana cost
    Map<String, Integer> expectedManaCost = new HashMap<>();
    expectedManaCost.put("{1}", 2);
    expectedManaCost.put("{R}", 1);
    expectedManaCost.put("{B}", 1);
    expectedManaCost.put("{U}", 1);
    assertEquals(expectedManaCost, soleResult.getManaCost());


    Set<String> colors = new HashSet<>();
    colors.add("R");
    colors.add("U");
    colors.add("B");
    // Check colors
    assertEquals(colors, soleResult.getColors());

    // Check color identity
    assertEquals(colors, soleResult.getColorIdentity());

    // Check text
    String expectedText = "Discard your hand. Draw seven cards, then discard three cards at random.";
    assertEquals(expectedText, soleResult.getText());

    // Check supertypes
    assertTrue(soleResult.getSupertypes().isEmpty());

    // Check types
    Set<String> types = new HashSet<>();
    types.add("sorcery");
    assertEquals(types, soleResult.getTypes());

    // Check subtypes
    assertTrue(soleResult.getSubtypes().isEmpty());

    // Check extra stats
    assertTrue(soleResult.getExtraStats().isEmpty());

    // Check relationship
    assertFalse(soleResult.getRelationships().hasRelationship());

    // Check card printings
    SortedSet<CardPrintingInfo> cardPrintings = soleResult.getCardPrintings();
    assertEquals(1, cardPrintings.size());

    CardPrintingInfo firstPrinting = cardPrintings.first();
    assertEquals(soleResult.getName(), firstPrinting.getCardName()); // Check name
    assertEquals("Alara Reborn", firstPrinting.getCardExpansion()); // Check expansion
    assertEquals("111", firstPrinting.getIdentifyingNumber()); // Check identifying number
    assertEquals("uncommon", firstPrinting.getRarity()); // Check rarity
    assertEquals("Every disaster holds mystery, for lack of a sane witness.",
        firstPrinting.getFlavorText()); // Check flavor text
    assertEquals("Trevor Claxton", firstPrinting.getArtist()); // Check artist
  }

  // Single card from single expansion with two card relationship

  // Single card from single expansion with three card relationship

}
