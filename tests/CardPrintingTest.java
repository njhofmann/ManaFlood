import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import value_objects.card_printing.CardPrinting;
import value_objects.card_printing.DefaultCardPrinting;

public class CardPrintingTest {

  static CardPrinting cardPrintingA;
  static CardPrinting cardPrintingB;

  @DisplayName("Equal card printings")
  @Test
  public void equalCardPrintings() {
    String name = "foo";
    String expansion = "bar";
    String number = "four";

    cardPrintingA = new DefaultCardPrinting(name, expansion, number);
    cardPrintingB = new DefaultCardPrinting(name, expansion, number);
    assertEquals(0, cardPrintingA.compareTo(cardPrintingB));
    assertEquals(0, cardPrintingB.compareTo(cardPrintingA));
    assertEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Equal name, equal expansion, before number")
  @Test
  public void beforeNumber() {
    String name = "man";
    String expansion = "lop";

    cardPrintingA = new DefaultCardPrinting(name, expansion, "four");
    cardPrintingB = new DefaultCardPrinting(name, expansion, "six");
    assertTrue(cardPrintingA.compareTo(cardPrintingB) < 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) > 0);
    assertNotEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Equal name, equal expansion, after number")
  @Test
  public void afterNumber() {
    String name = "pop";
    String expansion = "tio";

    cardPrintingA = new DefaultCardPrinting(name, expansion, "hero");
    cardPrintingB = new DefaultCardPrinting(name, expansion, "food");
    assertTrue(cardPrintingA.compareTo(cardPrintingB) > 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) < 0);
    assertNotEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Equal name, before expansion")
  @Test
  public void beforeExpansion() {
    String name = "nero";

    cardPrintingA = new DefaultCardPrinting(name, "retu", "what");
    cardPrintingB = new DefaultCardPrinting(name, "zebr", "ever");
    assertTrue(cardPrintingA.compareTo(cardPrintingB) < 0);
    assertTrue(cardPrintingB.compareTo(cardPrintingA) > 0);
    assertNotEquals(cardPrintingA, cardPrintingB);
  }

  @DisplayName("Equal name, after expansion")
  @Test
  public void afterExpansion() {
    String name = "were";

    cardPrintingA = new DefaultCardPrinting(name, "xlk", "what");
    cardPrintingB = new DefaultCardPrinting(name, "iop", "ever");
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
    String name = "james";
    String expansion = "tony";

    cardPrintingA = new DefaultCardPrinting(name, expansion, "foo");
    cardPrintingB = new DefaultCardPrinting(name, expansion, "three");
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
