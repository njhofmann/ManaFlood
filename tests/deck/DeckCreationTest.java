package deck;

import static org.junit.jupiter.api.Assertions.*;

import database.access.DeckChannel;
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
import value_objects.deck.Deck;
import value_objects.deck.DefaultDeck;
import value_objects.deck.instance.DeckInstance;
import value_objects.deck.instance.DefaultDeckInstance;

/**
 * Tests ensuring the creation, entering, retrieval, editing, and deletion of {@link Deck}s and
 * {@link DeckInstance} into the Card and Deck Database (CDDB).
 */
public class DeckCreationTest {

  static DeckChannel deckChannel;
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

  @DisplayName("Deck with single deck instance insertion and deletion")
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

  @DisplayName("Update deck name")
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

  @DisplayName("Add multiple deck instances")
  @Test
  public void addMultipleDeckInstance() {

  }

  @DisplayName("Add multiple decks")
  @Test
  public void addMultipleNewDecks() {

  }
}
