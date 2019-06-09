package baseline;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import value_objects.card.printing.CardPrinting;
import value_objects.card.printing.DefaultCardPrinting;
import value_objects.card.printing.InformativeCardPrinting;

/**
 * Tests to ensure overridden equality and implemented compareTo methods for {@link CardPrinting}
 * and {@link InformativeCardPrinting} work properly.
 */
public class CardPrintingEqualityTest {

  static CardPrinting cardPrintingA;
  static CardPrinting cardPrintingB;

  @DisplayName("Equal card printings")
  @Test
  public void equalCardPrintings() {
    cardPrintingA = new DefaultCardPrinting("foo", "bar", "four");
    cardPrintingB = new DefaultCardPrinting("foo", "bar", "four");

    assertEquals(cardPrintingA.hashCode(), cardPrintingB.hashCode());
    assertEquals(0, cardPrintingA.compareTo(cardPrintingB));
    assertEquals(0, cardPrintingB.compareTo(cardPrintingA));
    assertEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Equal name, equal expansion, before number")
  @Test
  public void beforeNumber() {
    cardPrintingA = new DefaultCardPrinting("man", "lop", "four");
    cardPrintingB = new DefaultCardPrinting("man", "lop", "six");
    assertTrue(cardPrintingA.compareTo(cardPrintingB) < 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) > 0);
    assertNotEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Equal name, equal expansion, after number")
  @Test
  public void afterNumber() {
    cardPrintingA = new DefaultCardPrinting("pop", "tio", "hero");
    cardPrintingB = new DefaultCardPrinting("pop", "tio", "food");
    assertTrue(cardPrintingA.compareTo(cardPrintingB) > 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) < 0);
    assertNotEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Equal name, before expansion")
  @Test
  public void beforeExpansion() {
    cardPrintingA = new DefaultCardPrinting("nero", "retu", "what");
    cardPrintingB = new DefaultCardPrinting("nero", "zebr", "ever");
    assertTrue(cardPrintingA.compareTo(cardPrintingB) < 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) > 0);
    assertNotEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Equal name, after expansion")
  @Test
  public void afterExpansion() {
    cardPrintingA = new DefaultCardPrinting("were", "xlk", "what");
    cardPrintingB = new DefaultCardPrinting("were", "iop", "ever");
    assertTrue(cardPrintingA.compareTo(cardPrintingB) > 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) < 0);
    assertNotEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Before name")
  @Test
  public void beforeName() {
    cardPrintingA = new DefaultCardPrinting("george", "xlk", "what");
    cardPrintingB = new DefaultCardPrinting("nate", "iop", "ever");
    assertTrue(cardPrintingA.compareTo(cardPrintingB) < 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) > 0);
    assertNotEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("After name")
  @Test
  public void afterName() {
    cardPrintingA = new DefaultCardPrinting("trump", "xlk", "what");
    cardPrintingB = new DefaultCardPrinting("obama", "iop", "ever");
    assertTrue(cardPrintingA.compareTo(cardPrintingB) > 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) < 0);
    assertNotEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Transitive tests")
  @Test
  public void transitiveTests() {
    cardPrintingA = new DefaultCardPrinting("james", "tony", "foo");
    cardPrintingB = new DefaultCardPrinting("james", "tony", "three");
    CardPrinting cardPrintingC = new DefaultCardPrinting("alex", "rub", "six");

    assertNotEquals(cardPrintingA, cardPrintingB);
    assertNotEquals(cardPrintingA, cardPrintingC);
    assertNotEquals(cardPrintingB, cardPrintingC);

    assertTrue(cardPrintingC.compareTo(cardPrintingA) < 0);
    assertTrue(cardPrintingA.compareTo(cardPrintingC) > 0);
    assertTrue(cardPrintingA.compareTo(cardPrintingB) < 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) > 0);
    assertTrue(cardPrintingC.compareTo(cardPrintingB) < 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingC) > 0);
  }
}
