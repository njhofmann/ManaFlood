import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import value_objects.card.printing.CardPrinting;
import value_objects.card.printing.DefaultCardPrinting;
import value_objects.deck.instance.DeckInstance;
import value_objects.deck.instance.DefaultDeckInstance;

/**
 * Tests for ensuring that created {@link DeckInstance} implementations work correctly.
 */
public class DeckInstanceCreationTest {

  DeckInstance deckInstance;

  @DisplayName("Null creation time")
  @Test
  public void nullCreationTime() {
    int deckId = 1;
    LocalDateTime creation = null;
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();
    Map<CardPrinting, Integer> cardQuantities = new HashMap<>();

    assertThrows(IllegalArgumentException.class, () -> {
      new DefaultDeckInstance(deckId, creation, categoryContents, cardQuantities);
    });
  }

  @DisplayName("Null category contents")
  @Test
  public void nullCategoryContents() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(2009, 3, 2, 1, 4);
    Map<String, SortedSet<String>> categoryContents = null;
    Map<CardPrinting, Integer> cardQuantities = new HashMap<>();

    assertThrows(IllegalArgumentException.class, () -> {
      new DefaultDeckInstance(deckId, creation, categoryContents, cardQuantities);
    });
  }


  @DisplayName("Null card quantities")
  @Test
  public void nullCardQuantities() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(2045, 3, 4, 5, 4);
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();
    Map<CardPrinting, Integer> cardQuantities = null;

    assertThrows(IllegalArgumentException.class, () -> {
      new DefaultDeckInstance(deckId, creation, categoryContents, cardQuantities);
    });
  }

  @DisplayName("Non positive quantity of cards entered into the deck instance")
  @Test
  public void nonPositiveQuantity() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(2045, 3, 4, 5, 4);
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();

    Map<CardPrinting, Integer> cardQuantities = new HashMap<>();
    cardQuantities.put(new DefaultCardPrinting("foo", "bard", "boo"), -2);

    assertThrows(IllegalArgumentException.class, () -> {
      new DefaultDeckInstance(deckId, creation, categoryContents, cardQuantities);
    });
  }

  @DisplayName("Given card printing not in given set of cards")
  @Test
  public void cardPrintingsButNoCard() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(2045, 3, 4, 5, 4);
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();

    Map<CardPrinting, Integer> cardQuantities = new HashMap<>();
    cardQuantities.put(new DefaultCardPrinting("foo", "bard", "boo"), 3);

    assertThrows(IllegalArgumentException.class, () -> {
      new DefaultDeckInstance(deckId, creation, categoryContents, cardQuantities);
    });
  }

  @DisplayName("Given card has not associated card printing(s)")
  @Test
  public void cardButNoCardPrinting() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(2045, 3, 4, 5, 4);
    Map<CardPrinting, Integer> cardQuantities = new HashMap<>();

    Map<String, SortedSet<String>> categoryContents = new HashMap<>();
    SortedSet<String> cards = new TreeSet<>();
    cards.add("gard");
    categoryContents.put("hum", cards);

    assertThrows(IllegalArgumentException.class, () -> {
      new DefaultDeckInstance(deckId, creation, categoryContents, cardQuantities);
    });
  }

  @DisplayName("Empty deck instance")
  @Test
  public void emptyDeckInstance() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(5, 3, 2, 1, 4);
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();
    Map<CardPrinting, Integer> cardQuantities = new HashMap<>();

    deckInstance = new DefaultDeckInstance(deckId, creation, categoryContents, cardQuantities);

    // Check card quantities
    assertTrue(deckInstance.getCardQuantities().isEmpty());

    // Check cards by category
    assertTrue(deckInstance.getCardsByCategory().isEmpty());

    // Check card printings
    assertTrue(deckInstance.getCardPrintings().isEmpty());

    // Check card categories
    assertTrue(deckInstance.getCardsByCategory().isEmpty());

    // Check cards
    assertTrue(deckInstance.getCards().isEmpty());

    // Check card printing quantities
    assertTrue(deckInstance.getCardPrintingQuantities().isEmpty());
  }

  @DisplayName("Single category, single card, single card printing per card")
  @Test
  public void singleCategorySingleCardSingleCardPrinting() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(5, 3, 2, 1, 4);

    // Set up card and card printings
    String card = "hof";
    String category = "removal";
    CardPrinting cardPrinting = new DefaultCardPrinting(card, "george", "youtube");
    int cardPrintingQuantity = 5;

    // Add card and card printings
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();
    SortedSet<String> cardsInCategory = new TreeSet<>();
    cardsInCategory.add(card);
    categoryContents.put(category, cardsInCategory);

    Map<CardPrinting, Integer> cardPrintingQuantities = new HashMap<>();
    cardPrintingQuantities.put(cardPrinting, cardPrintingQuantity);

    // Create deck instance
    deckInstance = new DefaultDeckInstance(deckId, creation, categoryContents,
        cardPrintingQuantities);

    // Check card quantities
    Map<String, Integer> cardQuantities = deckInstance.getCardQuantities();
    assertEquals(1, cardQuantities.size());
    assertTrue(cardQuantities.containsKey(card));
    assertEquals(cardPrintingQuantity, cardQuantities.get(card));

    // Check cards by category
    assertEquals(categoryContents, deckInstance.getCardsByCategory());

    // Check card printings
    SortedSet<CardPrinting> cardPrintings = deckInstance.getCardPrintings();
    assertEquals(1, cardPrintings.size());
    assertTrue(cardPrintings.contains(cardPrinting));

    // Check card categories
    SortedSet<String> categories = deckInstance.getCategories();
    assertEquals(1, categories.size());
    assertTrue(categories.contains(category));

    // Check cardsInCategory
    SortedSet<String> cards = deckInstance.getCards();
    assertEquals(1, cards.size());
    assertTrue(cards.contains(card));

    // Check card printing quantities
    assertEquals(cardPrintingQuantities, deckInstance.getCardPrintingQuantities());
  }

  @DisplayName("Single category, single card, multiple card printing per card")
  @Test
  public void singleCategorySingleCardMultipleCardPrinting() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(5, 3, 2, 1, 4);

    // Set up card and card printings
    String card = "hof";
    String category = "removal";

    CardPrinting cardPrintingA = new DefaultCardPrinting(card, "george", "youtube");
    int cardPrintingAQuantity = 4;

    CardPrinting cardPrintingB = new DefaultCardPrinting(card, "foreman", "grill");
    int cardPrintingBQuantity = 3;

    // Add card categories
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();
    SortedSet<String> cardsInCategory = new TreeSet<>();
    cardsInCategory.add(card);
    categoryContents.put(category, cardsInCategory);

    // Add card printing categories
    Map<CardPrinting, Integer> cardPrintingQuantities = new HashMap<>();
    cardPrintingQuantities.put(cardPrintingA, cardPrintingAQuantity);
    cardPrintingQuantities.put(cardPrintingB, cardPrintingBQuantity);

    // Create deck instance
    deckInstance = new DefaultDeckInstance(deckId, creation, categoryContents,
        cardPrintingQuantities);

    // Check card quantities
    Map<String, Integer> cardQuantities = deckInstance.getCardQuantities();
    assertEquals(1, cardQuantities.size());
    assertTrue(cardQuantities.containsKey(card));
    assertEquals(cardPrintingAQuantity + cardPrintingBQuantity, cardQuantities.get(card));

    // Check cards by category
    assertEquals(categoryContents, deckInstance.getCardsByCategory());

    // Check card printings
    SortedSet<CardPrinting> cardPrintings = deckInstance.getCardPrintings();
    assertEquals(2, cardPrintings.size());
    assertTrue(cardPrintings.contains(cardPrintingA));
    assertTrue(cardPrintings.contains(cardPrintingB));

    // Check card categories
    SortedSet<String> categories = deckInstance.getCategories();
    assertEquals(1, categories.size());
    assertTrue(categories.contains(category));

    // Check cardsInCategory
    SortedSet<String> cards = deckInstance.getCards();
    assertEquals(1, cards.size());
    assertTrue(cards.contains(card));

    // Check card printing quantities
    assertEquals(cardPrintingQuantities, deckInstance.getCardPrintingQuantities());
  }

  @DisplayName("Single category, multiple cards, single card printing per card")
  @Test
  public void singleCategoryMultipleCardMultipleCardPrinting() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(5, 3, 2, 1, 4);

    // Set up card and card printings
    String category = "discard";

    String cardA = "hoof";
    CardPrinting cardAPrinting = new DefaultCardPrinting(cardA, "kil", "u");
    int cardAPrintingQuantity = 3;

    String cardB = "jum";
    CardPrinting cardBPrinting = new DefaultCardPrinting(cardB, "me", "too");
    int cardBPrintingQuantity = 2;

    // Add card categories
    Map<String, SortedSet<String>> categoryContents = new HashMap<>();
    SortedSet<String> cardsInCategory = new TreeSet<>();
    cardsInCategory.add(cardA);
    cardsInCategory.add(cardB);
    categoryContents.put(category, cardsInCategory);

    // Add card printing categories
    Map<CardPrinting, Integer> cardPrintingQuantities = new HashMap<>();
    cardPrintingQuantities.put(cardAPrinting, cardAPrintingQuantity);
    cardPrintingQuantities.put(cardBPrinting, cardBPrintingQuantity);

    // Create deck instance
    deckInstance = new DefaultDeckInstance(deckId, creation, categoryContents,
        cardPrintingQuantities);

    // Check card quantities
    Map<String, Integer> cardQuantities = deckInstance.getCardQuantities();
    assertEquals(2, cardQuantities.size());

    assertTrue(cardQuantities.containsKey(cardA));
    assertEquals(cardAPrintingQuantity, cardQuantities.get(cardA));

    assertTrue(cardQuantities.containsKey(cardB));
    assertEquals(cardBPrintingQuantity, cardQuantities.get(cardB));

    // Check cards by category
    assertEquals(categoryContents, deckInstance.getCardsByCategory());

    // Check card printings
    SortedSet<CardPrinting> cardPrintings = deckInstance.getCardPrintings();
    assertEquals(2, cardPrintings.size());
    assertTrue(cardPrintings.contains(cardAPrinting));
    assertTrue(cardPrintings.contains(cardBPrinting));

    // Check card categories
    SortedSet<String> categories = deckInstance.getCategories();
    assertEquals(1, categories.size());
    assertTrue(categories.contains(category));

    // Check cards
    SortedSet<String> cards = deckInstance.getCards();
    assertEquals(2, cards.size());
    assertTrue(cards.contains(cardA));
    assertTrue(cards.contains(cardB));

    // Check card printing quantities
    assertEquals(cardPrintingQuantities, deckInstance.getCardPrintingQuantities());
  }

  @DisplayName("Multiple category, single card, single card printing per card")
  @Test
  public void multipleCategorySingleCardSingleCardPrinting() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(5, 3, 2, 1, 4);

    // Set up card and card printings
    String card = "many";
    String categoryA = "board";
    String categoryB = "card draw";
    CardPrinting cardPrinting = new DefaultCardPrinting(card, "george", "youtube");
    int cardPrintingQuantity = 9;

    // Add card categories
    SortedSet<String> cardsInCategoryA = new TreeSet<>();
    cardsInCategoryA.add(card);

    SortedSet<String> cardsInCategoryB = new TreeSet<>();
    cardsInCategoryB.add(card);

    Map<String, SortedSet<String>> categoryContents = new HashMap<>();
    categoryContents.put(categoryA, cardsInCategoryA);
    categoryContents.put(categoryB, cardsInCategoryB);

    // Add card printing quantities
    Map<CardPrinting, Integer> cardPrintingQuantities = new HashMap<>();
    cardPrintingQuantities.put(cardPrinting, cardPrintingQuantity);

    // Create deck instance
    deckInstance = new DefaultDeckInstance(deckId, creation, categoryContents,
        cardPrintingQuantities);

    // Check card quantities
    Map<String, Integer> cardQuantities = deckInstance.getCardQuantities();
    assertEquals(1, cardQuantities.size());
    assertTrue(cardQuantities.containsKey(card));
    assertEquals(cardPrintingQuantity, cardQuantities.get(card));

    // Check cards by category, same as inserted mapping
    assertEquals(categoryContents, deckInstance.getCardsByCategory());

    // Check card printings
    SortedSet<CardPrinting> cardPrintings = deckInstance.getCardPrintings();
    assertEquals(1, cardPrintings.size());
    assertTrue(cardPrintings.contains(cardPrinting));

    // Check card categories
    SortedSet<String> categories = deckInstance.getCategories();
    assertEquals(2, categories.size());
    assertTrue(categories.contains(categoryA));
    assertTrue(categories.contains(categoryB));

    // Check cards
    SortedSet<String> cards = deckInstance.getCards();
    assertEquals(1, cards.size());
    assertTrue(cards.contains(card));

    // Check card printing quantities, same as inserted mapping
    assertEquals(cardPrintingQuantities, deckInstance.getCardPrintingQuantities());
  }

  @DisplayName("Multiple category, multiple cards, single card printing per card")
  @Test
  public void multipleCategoryMultipleCardSingleCardPrinting() {

  }

  @DisplayName("A whole deck - multiple category, multiple cards, multiple card printing per card")
  @Test
  public void wholeDeck() {
    int deckId = 1;
    LocalDateTime creation = LocalDateTime.of(2013, 4, 23, 13, 44);

    // Set up categories
    String removalCategory = "removal";
    String discardCategory = "discard";
    String massDamageCategory = "mass damage";
    String cardAdvantageCategory = "card advantage";
    String landsCategory = "lands";
    String beatstickCategory = "beatstick";

    // Expansions
    String therosSet = "Theros";
    String lorywnSet = "Lorywn";
    String morningtideSet = "MorningTide";
    String bornOfTheGodsSet = "Born of the Gods";
    String gatecrashSet = "Gatecrash";
    String gatecrashPromosSet = "GatecrashPromos";
    String m14Set = "M14";
    String returnToRavnicaSet = "Return to Ravnica";

    // Add card printing quantities
    Map<CardPrinting, Integer> cardPrintingQuantities = new HashMap<>();

    // Set up cards, card printings, and quantities
    String desecrationDemonCard = "Desecration Demon";
    CardPrinting desecrationDemonCardPrinting = new DefaultCardPrinting(desecrationDemonCard, returnToRavnicaSet, "66");
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
    CardPrinting packRatCardPrinting = new DefaultCardPrinting(packRatCard, gatecrashSet, "73");
    int packRatPrintingQuantity = 4;
    cardPrintingQuantities.put(packRatCardPrinting, packRatPrintingQuantity);

    String underworldConnectionsCard = "Underworld Connections";
    CardPrinting underworldConnectionsCardPrinting = new DefaultCardPrinting(underworldConnectionsCard, gatecrashSet, "83");
    int underworldConnectionsPrintingQuantity = 4;
    cardPrintingQuantities.put(underworldConnectionsCardPrinting, underworldConnectionsPrintingQuantity);

    String devourFleshCard = "Devour Flesh";
    CardPrinting devourFleshCardPrinting = new DefaultCardPrinting(devourFleshCard, gatecrashSet, "63");
    int devourFleshCardPrintingQuantity = 4;
    cardPrintingQuantities.put(devourFleshCardPrinting, devourFleshCardPrintingQuantity);

    String bileBlightCard = "Bile Blight";
    CardPrinting bileBlightfallCardPrinting = new DefaultCardPrinting(bileBlightCard, bornOfTheGodsSet, "90");
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
    CardPrinting swampCardPrintingA = new DefaultCardPrinting(swampCard, gatecrashSet, "56");
    int swampCardPrintingAQuantity = 10;
    cardPrintingQuantities.put(swampCardPrintingA, swampCardPrintingAQuantity);

    CardPrinting swampCardPrintingB = new DefaultCardPrinting(swampCard, therosSet, "71");
    int swampCardPrintingBQuantity = 7;
    cardPrintingQuantities.put(swampCardPrintingB, swampCardPrintingBQuantity);

    CardPrinting swampCardPrintingC = new DefaultCardPrinting(swampCard, m14Set, "24");
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

    // Create deck instance
    deckInstance = new DefaultDeckInstance(deckId, creation, categoryContents,
        cardPrintingQuantities);

    // Check card quantities
    Map<String, Integer> cardQuantities = deckInstance.getCardQuantities();
    assertEquals(12, cardQuantities.size());
    // ToDo check card quantities

    // Check cards by category, same as inserted mapping
    assertEquals(categoryContents, deckInstance.getCardsByCategory());

    // Check card printings
    SortedSet<CardPrinting> cardPrintings = deckInstance.getCardPrintings();
    assertEquals(17, cardPrintings.size());
    assertTrue(cardPrintings.contains(desecrationDemonCardPrinting));
    assertTrue(cardPrintings.contains(grayMerchantCardPrinting));
    assertTrue(cardPrintings.contains(lifebaneZombieCardPrinting));
    assertTrue(cardPrintings.contains(nightveilSpecterCardPrintingA));
    assertTrue(cardPrintings.contains(nightveilSpecterCardPrintingB));
    assertTrue(cardPrintings.contains(packRatCardPrinting));
    assertTrue(cardPrintings.contains(underworldConnectionsCardPrinting));
    assertTrue(cardPrintings.contains(devourFleshCardPrinting));
    assertTrue(cardPrintings.contains(bileBlightfallCardPrinting));
    assertTrue(cardPrintings.contains(herosDownfallCardPrinting));
    assertTrue(cardPrintings.contains(thoughtseizeCardPrintingA));
    assertTrue(cardPrintings.contains(thoughtseizeCardPrintingB));
    assertTrue(cardPrintings.contains(swampCardPrintingA));
    assertTrue(cardPrintings.contains(swampCardPrintingB));
    assertTrue(cardPrintings.contains(swampCardPrintingC));
    assertTrue(cardPrintings.contains(mutavaultCardPrintingA));
    assertTrue(cardPrintings.contains(mutavaultCardPrintingB));

    // Check card categories
    SortedSet<String> categories = deckInstance.getCategories();
    assertEquals(6, categories.size());
    assertTrue(categories.contains(removalCategory));
    assertTrue(categories.contains(discardCategory));
    assertTrue(categories.contains(beatstickCategory));
    assertTrue(categories.contains(landsCategory));
    assertTrue(categories.contains(massDamageCategory));
    assertTrue(categories.contains(cardAdvantageCategory));

    // Check cards
    SortedSet<String> cards = deckInstance.getCards();
    assertEquals(12, cards.size());
    assertTrue(cards.contains(desecrationDemonCard));
    assertTrue(cards.contains(grayMerchantCard));
    assertTrue(cards.contains(lifebaneZombieCard));
    assertTrue(cards.contains(nightveilSpecterCard));
    assertTrue(cards.contains(packRatCard));
    assertTrue(cards.contains(underworldConnectionsCard));
    assertTrue(cards.contains(devourFleshCard));
    assertTrue(cards.contains(bileBlightCard));
    assertTrue(cards.contains(herosDownfallCard));
    assertTrue(cards.contains(thoughtseizeCard));
    assertTrue(cards.contains(swampCard));
    assertTrue(cards.contains(mutavaultCard));

    // Check card printing quantities, same as inserted mapping
    assertEquals(cardPrintingQuantities, deckInstance.getCardPrintingQuantities());
  }
}
