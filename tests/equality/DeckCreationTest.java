package equality;

import static org.junit.jupiter.api.Assertions.*;

import database.access.DatabaseChannel;
import database.access.DefaultDatabaseChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import value_objects.card.printing.CardPrinting;
import value_objects.card.printing.DefaultCardPrinting;
import value_objects.deck.Deck;
import value_objects.deck.DefaultDeck;
import value_objects.deck.instance.DeckInstance;
import value_objects.deck.instance.DefaultDeckInstance;

/**
 * Tests ensuring the creation, entering, retrieval, editing, and deletion of {@link Deck}s and
 * {@link DeckInstance} into the Card and Deck Database (CDDB).
 */
public class DeckCreationTest {

  static DatabaseChannel deckChannel;
  static Deck oldDeck;
  static Deck newDeck;
  static DeckInstance deckInstance;

  @BeforeAll
  /**
   * Use an empty CDDB with no decks inserted prior to these tests.
   */
  public static void init() throws SQLException {
    Path pathToDatabase = Paths.get("tests\\test_cddb.db").toAbsolutePath();
    deckChannel = new DefaultDatabaseChannel(pathToDatabase);
  }

  @BeforeEach
  public void clearDatabase() throws SQLException {
    Set<Integer> deckIds = deckChannel.getDecks().keySet();
    for (Integer integer : deckIds) {
      deckChannel.deleteDeck(integer);
    }
  }

  @DisplayName("Deck with empty single deck instance insertion and deletion")
  @Test
  public void singleDeckInstance() throws SQLException {
    int deckId = 1;
    String deckName = "notebook";
    String desp = "foobar";

    // Create deck instance
    LocalDateTime localDateTime = LocalDateTime.of(2007, 3, 23, 5, 14);
    deckInstance = new DefaultDeckInstance(deckId, localDateTime, new HashMap<>(), new HashMap<>());

    SortedSet<DeckInstance> deckInstances = new TreeSet<>();
    deckInstances.add(deckInstance);

    // Create deck
    oldDeck = new DefaultDeck(deckId, deckName, desp, deckInstances);

    // Add deck
    deckChannel.addDeck(oldDeck);

    // Check deck is in database
    Map<Integer, String> idsToNames = deckChannel.getDecks();
    assertEquals(1, idsToNames.size());
    assertTrue(idsToNames.containsKey(deckId));
    assertEquals(deckName, idsToNames.get(deckId));

    // Retrieve deck again
    newDeck = deckChannel.getDeck(deckId);

    // Check that retrieved deck is same as inserted deck
    assertEquals(oldDeck, newDeck);
    assertEquals(oldDeck.hashCode(), newDeck.hashCode());

    assertEquals(oldDeck.getDeckID(), newDeck.getDeckID());
    assertEquals(oldDeck.getDeckName(), newDeck.getDeckName());
    assertEquals(oldDeck.getDescription(), newDeck.getDescription());
    assertEquals(oldDeck.getHistory(), newDeck.getHistory());

    // Delete deck
    deckChannel.deleteDeck(deckId);

    // Check deck is no longer in database
    idsToNames = deckChannel.getDecks();
    assertTrue(idsToNames.isEmpty());
  }

  @DisplayName("Update deck name")
  @Test
  public void updateDeckName() throws SQLException {
    int deckId = 1;
    String oldDeckName = "football";
    String desp = "barbung";

    // Create deck instance
    LocalDateTime localDateTime = LocalDateTime.of(2007, 3, 23, 5, 14);
    deckInstance = new DefaultDeckInstance(deckId, localDateTime, new HashMap<>(), new HashMap<>());

    SortedSet<DeckInstance> deckInstances = new TreeSet<>();
    deckInstances.add(deckInstance);

    // Create deck
    oldDeck = new DefaultDeck(deckId, oldDeckName, desp, deckInstances);

    // Add deck
    deckChannel.addDeck(oldDeck);

    // Check deck is in database
    Map<Integer, String> idsToNames = deckChannel.getDecks();
    assertEquals(1, idsToNames.size());
    assertTrue(idsToNames.containsKey(deckId));
    assertEquals(oldDeckName, idsToNames.get(deckId));

    // Change deck name
    String newDeckName = "soccer";
    deckChannel.updateDeckName(deckId, newDeckName);

    // Retrieve deck again
    newDeck = deckChannel.getDeck(deckId);

    // Check that retrieved deck is same as inserted deck, name change shouldn't effect equality
    assertEquals(oldDeck, newDeck);
    assertEquals(oldDeck.hashCode(), newDeck.hashCode());

    assertEquals(oldDeck.getDeckID(), newDeck.getDeckID());
    assertEquals(oldDeck.getDescription(), newDeck.getDescription());
    assertEquals(oldDeck.getHistory(), newDeck.getHistory());

    assertEquals(newDeck.getDeckName(), newDeckName);

    // Delete deck
    deckChannel.deleteDeck(deckId);

    // Check deck is no longer in database
    idsToNames = deckChannel.getDecks();
    assertTrue(idsToNames.isEmpty());
  }

  @DisplayName("Update deck desp")
  @Test
  public void updateDeckDesp() throws SQLException {
    int deckId = 1;
    String deckName = "keyboard";
    String oldDeckDesp = "hung";

    // Create deck instance
    LocalDateTime localDateTime = LocalDateTime.of(2007, 3, 23, 5, 14);
    deckInstance = new DefaultDeckInstance(deckId, localDateTime, new HashMap<>(), new HashMap<>());

    SortedSet<DeckInstance> deckInstances = new TreeSet<>();
    deckInstances.add(deckInstance);

    // Create deck
    oldDeck = new DefaultDeck(deckId, deckName, oldDeckDesp, deckInstances);

    // Add deck
    deckChannel.addDeck(oldDeck);

    // Check deck is in database
    Map<Integer, String> idsToNames = deckChannel.getDecks();
    assertEquals(1, idsToNames.size());
    assertTrue(idsToNames.containsKey(deckId));
    assertEquals(deckName, idsToNames.get(deckId));

    // Change deck desp
    String newDeckDesp = "hung";
    deckChannel.updateDeckDesp(deckId, newDeckDesp);

    // Retrieve deck again
    newDeck = deckChannel.getDeck(deckId);

    // Check that retrieved deck is same as inserted deck, name change shouldn't effect equality
    assertEquals(oldDeck, newDeck);
    assertEquals(oldDeck.hashCode(), newDeck.hashCode());

    assertEquals(oldDeck.getDeckID(), newDeck.getDeckID());
    assertEquals(oldDeck.getDeckName(), newDeck.getDeckName());
    assertEquals(oldDeck.getHistory(), newDeck.getHistory());

    assertEquals(newDeck.getDescription(), newDeckDesp);

    // Delete deck
    deckChannel.deleteDeck(deckId);

    // Check deck is no longer in database
    idsToNames = deckChannel.getDecks();
    assertTrue(idsToNames.isEmpty());
  }

  @DisplayName("Add single deck multiple deck instances ")
  @Test
  public void addSingleDeckMultipleDeckInstance() {

  }

  @DisplayName("Add multiple decks with single empty deck instance")
  @Test
  public void addMultipleDeckWithEmptyDeckInstance() {

  }

  @DisplayName("Deck instance with card insertion and retrieval")
  @Test
  public void singleDeckWithDeckInstanceWithContent() throws SQLException {
    // Set up categories
    String removalCategory = "removal";
    String discardCategory = "discard";
    String massDamageCategory = "mass damage";
    String cardAdvantageCategory = "card advantage";
    String landsCategory = "lands";
    String beatstickCategory = "beatstick";

    // Expansions
    String therosSet = "Theros";
    String lorywnSet = "Lorwyn";
    String morningtideSet = "Morningtide";
    String bornOfTheGodsSet = "Born of the Gods";
    String gatecrashSet = "Gatecrash";
    String gatecrashPromosSet = "Gatecrash Promos";
    String m14Set = "Magic 2014";
    String returnToRavnicaSet = "Return to Ravnica";

    // Add card printing quantities
    Map<CardPrinting, Integer> cardPrintingQuantities = new HashMap<>();

    // Set up cards, card printings, and quantities
    String desecrationDemonCard = "Desecration Demon";
    CardPrinting desecrationDemonCardPrinting = new DefaultCardPrinting(desecrationDemonCard, returnToRavnicaSet, "63");
    int desecrationDemonCardPrintingQuantity = 4;
    cardPrintingQuantities.put(desecrationDemonCardPrinting, desecrationDemonCardPrintingQuantity);

    String grayMerchantCard = "Gray Merchant of Asphodel";
    CardPrinting grayMerchantCardPrinting = new DefaultCardPrinting(grayMerchantCard, therosSet, "89");
    int grayMerchantCardPrintingQuantity = 4;
    cardPrintingQuantities.put(grayMerchantCardPrinting, grayMerchantCardPrintingQuantity);

    String lifebaneZombieCard = "Lifebane Zombie";
    CardPrinting lifebaneZombieCardPrinting = new DefaultCardPrinting(lifebaneZombieCard, m14Set, "101");
    int lifebaneZombieCardPrintingQuantity = 4;
    cardPrintingQuantities.put(lifebaneZombieCardPrinting, lifebaneZombieCardPrintingQuantity);

    String nightveilSpecterCard = "Nightveil Specter";
    CardPrinting nightveilSpecterCardPrintingA = new DefaultCardPrinting(nightveilSpecterCard, gatecrashSet, "222");
    int nightveilSpecterCardPrintingAQuantity = 3;
    cardPrintingQuantities.put(nightveilSpecterCardPrintingA, nightveilSpecterCardPrintingAQuantity);

    CardPrinting nightveilSpecterCardPrintingB = new DefaultCardPrinting(nightveilSpecterCard, gatecrashPromosSet, "*222");
    int nightveilSpecterCardPrintingBQuantity = 1;
    cardPrintingQuantities.put(nightveilSpecterCardPrintingB, nightveilSpecterCardPrintingBQuantity);

    String packRatCard = "Pack Rat";
    CardPrinting packRatCardPrinting = new DefaultCardPrinting(packRatCard, returnToRavnicaSet, "73");
    int packRatPrintingQuantity = 4;
    cardPrintingQuantities.put(packRatCardPrinting, packRatPrintingQuantity);

    String underworldConnectionsCard = "Underworld Connections";
    CardPrinting underworldConnectionsCardPrinting = new DefaultCardPrinting(underworldConnectionsCard, returnToRavnicaSet, "83");
    int underworldConnectionsPrintingQuantity = 4;
    cardPrintingQuantities.put(underworldConnectionsCardPrinting, underworldConnectionsPrintingQuantity);

    String devourFleshCard = "Devour Flesh";
    CardPrinting devourFleshCardPrinting = new DefaultCardPrinting(devourFleshCard, gatecrashSet, "63");
    int devourFleshCardPrintingQuantity = 4;
    cardPrintingQuantities.put(devourFleshCardPrinting, devourFleshCardPrintingQuantity);

    String bileBlightCard = "Bile Blight";
    CardPrinting bileBlightfallCardPrinting = new DefaultCardPrinting(bileBlightCard, bornOfTheGodsSet, "61");
    int bileBlightCardPrintingQuantity = 4;
    cardPrintingQuantities.put(bileBlightfallCardPrinting, bileBlightCardPrintingQuantity);

    String herosDownfallCard = "Hero's Downfall";
    CardPrinting herosDownfallCardPrinting = new DefaultCardPrinting(herosDownfallCard, therosSet, "90");
    int herosDownfallCardPrintingQuantity = 4;
    cardPrintingQuantities.put(herosDownfallCardPrinting, herosDownfallCardPrintingQuantity);

    String thoughtseizeCard = "Thoughtseize";
    CardPrinting thoughtseizeCardPrintingA = new DefaultCardPrinting(thoughtseizeCard, therosSet, "107");
    int thoughtseizeCardPrintingAQuantity = 2;
    cardPrintingQuantities.put(thoughtseizeCardPrintingA, thoughtseizeCardPrintingAQuantity);

    CardPrinting thoughtseizeCardPrintingB = new DefaultCardPrinting(thoughtseizeCard, lorywnSet, "145");
    int thoughtseizeCardPrintingBQuantity = 2;
    cardPrintingQuantities.put(thoughtseizeCardPrintingB, thoughtseizeCardPrintingBQuantity);

    String swampCard = "Swamp";
    CardPrinting swampCardPrintingA = new DefaultCardPrinting(swampCard, returnToRavnicaSet, "260");
    int swampCardPrintingAQuantity = 10;
    cardPrintingQuantities.put(swampCardPrintingA, swampCardPrintingAQuantity);

    CardPrinting swampCardPrintingB = new DefaultCardPrinting(swampCard, therosSet, "240");
    int swampCardPrintingBQuantity = 7;
    cardPrintingQuantities.put(swampCardPrintingB, swampCardPrintingBQuantity);

    CardPrinting swampCardPrintingC = new DefaultCardPrinting(swampCard, m14Set, "240");
    int swampCardPrintingCQuantity = 4;
    cardPrintingQuantities.put(swampCardPrintingC, swampCardPrintingCQuantity);

    String mutavaultCard = "Mutavault";
    CardPrinting mutavaultCardPrintingA = new DefaultCardPrinting(mutavaultCard, m14Set, "228");
    int mutavaultCardPrintingAQuantity = 2;
    cardPrintingQuantities.put(mutavaultCardPrintingA, mutavaultCardPrintingAQuantity);

    CardPrinting mutavaultCardPrintingB = new DefaultCardPrinting(mutavaultCard, morningtideSet, "148");
    int mutavaultCardPrintingBQuantity = 2;
    cardPrintingQuantities.put(mutavaultCardPrintingB, mutavaultCardPrintingBQuantity);

    // Add card categories
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();

    SortedSet<String> removalCards = new TreeSet<>();
    removalCards.add(devourFleshCard);
    removalCards.add(bileBlightCard);
    removalCards.add(herosDownfallCard);
    categoryContents.put(removalCategory, removalCards);

    SortedSet<String> discardCards = new TreeSet<>();
    discardCards.add(lifebaneZombieCard);
    discardCards.add(thoughtseizeCard);
    categoryContents.put(discardCategory, discardCards);

    SortedSet<String> landCards = new TreeSet<>();
    landCards.add(swampCard);
    landCards.add(mutavaultCard);
    categoryContents.put(landsCategory, landCards);

    SortedSet<String> beatstickCards = new TreeSet<>();
    beatstickCards.add(desecrationDemonCard);
    beatstickCards.add(lifebaneZombieCard);
    beatstickCards.add(nightveilSpecterCard);
    categoryContents.put(beatstickCategory, beatstickCards);

    SortedSet<String> massDamageCards = new TreeSet<>();
    massDamageCards.add(packRatCard);
    massDamageCards.add(grayMerchantCard);
    categoryContents.put(massDamageCategory, massDamageCards);

    SortedSet<String> cardAdvantageCards = new TreeSet<>();
    cardAdvantageCards.add(underworldConnectionsCard);
    cardAdvantageCards.add(nightveilSpecterCard);
    categoryContents.put(cardAdvantageCategory, cardAdvantageCards);

    // Deck info
    int deckId = 10;
    String deckName = "instance";
    String deckDesp = "deck";

    // Create deck instance
    LocalDateTime localDateTime = LocalDateTime.of(2004, 8, 3, 1, 9);
    deckInstance = new DefaultDeckInstance(deckId, localDateTime, categoryContents, cardPrintingQuantities);

    SortedSet<DeckInstance> deckInstances = new TreeSet<>();
    deckInstances.add(deckInstance);

    // Create deck
    oldDeck = new DefaultDeck(deckId, deckName, deckDesp, deckInstances);

    // Add deck
    deckChannel.addDeck(oldDeck);

    // Check deck is in database
    Map<Integer, String> idsToNames = deckChannel.getDecks();
    assertEquals(1, idsToNames.size());
    assertTrue(idsToNames.containsKey(deckId));
    assertEquals(deckName, idsToNames.get(deckId));

    // Retrieve deck again
    newDeck = deckChannel.getDeck(deckId);

    // Check that retrieved deck is same as inserted deck, name change shouldn't effect equality
    assertEquals(oldDeck, newDeck);
    assertEquals(oldDeck.hashCode(), newDeck.hashCode());

    assertEquals(oldDeck.getDeckID(), newDeck.getDeckID());
    assertEquals(oldDeck.getDeckName(), newDeck.getDeckName());
    assertEquals(oldDeck.getDescription(), newDeck.getDescription());

    // Check that deck instances are the same
    SortedSet<DeckInstance> newDeckHistory = newDeck.getHistory();
    DeckInstance newDeckInstance = newDeckHistory.first();
    assertEquals(1, newDeckHistory.size());
    assertEquals(deckInstance, newDeckInstance);
    assertEquals(deckInstance.hashCode(), newDeckInstance.hashCode());
    assertEquals(deckInstance.getCardNamesByCategory(), newDeckInstance.getCardNamesByCategory());
    assertEquals(deckInstance.getCardNames(), newDeckInstance.getCardNames());
    assertEquals(deckInstance.getCardPrintings(), newDeckInstance.getCardPrintings());
    assertEquals(deckInstance.getCardPrintingQuantities(), newDeckInstance.getCardPrintingQuantities());
    assertEquals(deckInstance.getCardNameQuantities(), newDeckInstance.getCardNameQuantities());

    assertEquals(oldDeck.getHistory(), newDeck.getHistory());

    // Delete deck
    deckChannel.deleteDeck(deckId);

    // Check deck is no longer in database
    idsToNames = deckChannel.getDecks();
    assertTrue(idsToNames.isEmpty());
  }
}
