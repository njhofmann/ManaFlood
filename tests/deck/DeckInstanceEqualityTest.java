package deck;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import value_objects.deck.instance.DeckInstance;
import value_objects.deck.instance.DefaultDeckInstance;

/**
 * Tests to ensure overridden equality, hashcode, and comparison methods for the implementation
 * of {@link DeckInstance} work properly.
 */
public class DeckInstanceEqualityTest {

  LocalDateTime deckInstanceDateTime;
  DeckInstance deckInstanceA;
  DeckInstance deckInstanceB;
  DeckInstance deckInstanceC;

  @DisplayName("Equal oldDeck instances")
  @Test
  public void equalDeckInstances() {
    deckInstanceDateTime = LocalDateTime.of(1999, 7, 23, 5, 30);
    deckInstanceA = new DefaultDeckInstance(1, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    deckInstanceDateTime = LocalDateTime.of(1999, 7, 23, 5, 30);
    deckInstanceB = new DefaultDeckInstance(1, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    assertEquals(deckInstanceA, deckInstanceB);
    assertEquals(deckInstanceA.hashCode(), deckInstanceB.hashCode());
  }

  @DisplayName("Same parent oldDeck, before creation date")
  @Test
  public void sameParentBeforeDate() {
    deckInstanceDateTime = LocalDateTime.of(2010, 1, 4, 6, 24);
    deckInstanceA = new DefaultDeckInstance(1, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    deckInstanceDateTime = LocalDateTime.of(2017, 9, 3, 4, 1);
    deckInstanceB = new DefaultDeckInstance(1, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    assertNotEquals(deckInstanceA, deckInstanceB);
    assertTrue(deckInstanceA.compareTo(deckInstanceB) < 0);
    assertTrue(deckInstanceB.compareTo(deckInstanceA) > 0);
  }

  @DisplayName("Same parent oldDeck, after creation date")
  @Test
  public void sameParentAfterDate() {
    deckInstanceDateTime = LocalDateTime.of(2005, 10, 10, 5, 3);
    deckInstanceA = new DefaultDeckInstance(1, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    deckInstanceDateTime = LocalDateTime.of(2003, 3, 25, 11, 3);
    deckInstanceB = new DefaultDeckInstance(1, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    assertNotEquals(deckInstanceA, deckInstanceB);
    assertTrue(deckInstanceB.compareTo(deckInstanceA) < 0);
    assertTrue(deckInstanceA.compareTo(deckInstanceB) > 0);
  }

  @DisplayName("Same parent oldDeck, transitive test")
  @Test
  public void sameParentTransitive() {
    deckInstanceDateTime = LocalDateTime.of(2004, 10, 10, 5, 3);
    deckInstanceA = new DefaultDeckInstance(1, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    deckInstanceDateTime = LocalDateTime.of(2008, 8, 23, 6, 8);
    deckInstanceB = new DefaultDeckInstance(1, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    deckInstanceDateTime = LocalDateTime.of(2013, 3, 25, 11, 3);
    deckInstanceC = new DefaultDeckInstance(1, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    assertNotEquals(deckInstanceA, deckInstanceB);
    assertNotEquals(deckInstanceA, deckInstanceC);
    assertNotEquals(deckInstanceC, deckInstanceB);
    assertTrue(deckInstanceA.compareTo(deckInstanceB) < 0);
    assertTrue(deckInstanceB.compareTo(deckInstanceA) > 0);
    assertTrue(deckInstanceB.compareTo(deckInstanceC) < 0);
    assertTrue(deckInstanceC.compareTo(deckInstanceB) > 0);
    assertTrue(deckInstanceA.compareTo(deckInstanceC) < 0);
    assertTrue(deckInstanceC.compareTo(deckInstanceA) > 0);
  }

  @DisplayName("Different parent oldDeck, before")
  @Test
  public void diffParentBefore() {
    deckInstanceDateTime = LocalDateTime.of(2010, 1, 4, 6, 24);
    deckInstanceA = new DefaultDeckInstance(10, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    deckInstanceDateTime = LocalDateTime.of(2017, 9, 3, 4, 1);
    deckInstanceB = new DefaultDeckInstance(23, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    assertNotEquals(deckInstanceA, deckInstanceB);
    assertTrue(deckInstanceA.compareTo(deckInstanceB) < 0);
    assertTrue(deckInstanceB.compareTo(deckInstanceA) > 0);
  }

  @DisplayName("Different parent oldDeck, after")
  @Test
  public void diffParentAfter() {
    deckInstanceDateTime = LocalDateTime.of(2005, 10, 10, 5, 3);
    deckInstanceA = new DefaultDeckInstance(45, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    deckInstanceDateTime = LocalDateTime.of(2003, 3, 25, 11, 3);
    deckInstanceB = new DefaultDeckInstance(23, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    assertNotEquals(deckInstanceA, deckInstanceB);
    assertTrue(deckInstanceB.compareTo(deckInstanceA) < 0);
    assertTrue(deckInstanceA.compareTo(deckInstanceB) > 0);

  }

  @DisplayName("Different parent oldDeck, transitive")
  @Test
  public void diffParentTransitive() {
    deckInstanceDateTime = LocalDateTime.of(2004, 10, 10, 5, 3);
    deckInstanceA = new DefaultDeckInstance(4, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    deckInstanceDateTime = LocalDateTime.of(2008, 8, 23, 6, 8);
    deckInstanceB = new DefaultDeckInstance(6, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    deckInstanceDateTime = LocalDateTime.of(2013, 3, 25, 11, 3);
    deckInstanceC = new DefaultDeckInstance(10, deckInstanceDateTime,
        new HashMap<>(), new HashMap<>());

    assertNotEquals(deckInstanceA, deckInstanceB);
    assertNotEquals(deckInstanceA, deckInstanceC);
    assertNotEquals(deckInstanceC, deckInstanceB);
    assertTrue(deckInstanceA.compareTo(deckInstanceB) < 0);
    assertTrue(deckInstanceB.compareTo(deckInstanceA) > 0);
    assertTrue(deckInstanceB.compareTo(deckInstanceC) < 0);
    assertTrue(deckInstanceC.compareTo(deckInstanceB) > 0);
    assertTrue(deckInstanceA.compareTo(deckInstanceC) < 0);
    assertTrue(deckInstanceC.compareTo(deckInstanceA) > 0);
  }
}
