package value_objects.utility;

import java.util.Objects;

/**
 * Generic container to contain objects of any three types.
 */
public class Triple<A, B, C> {

  private final A a;

  private final B b;

  private final C c;

  /**
   * Constructs a {@link Triple} from given objects.
   * @param a object a, determines type A
   * @param b object b, determines type B
   * @param c object c, determines type C
   * @throws IllegalArgumentException if any of the given objects are null
   */
  public Triple(A a, B b, C c) {
    if (a == null || b == null || c == null) {
      throw new IllegalArgumentException("Given parameters can't be null!");
    }
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public A getA() {
    return a;
  }

  public B getB() {
    return b;
  }

  public C getC() {
    return c;
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b, c);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    else if (other == null || getClass() != other.getClass()) {
      return false;
    }
    Triple<?, ?, ?> triple = (Triple<?, ?, ?>) other;
    return a.equals(triple.a) && b.equals(triple.b) && c.equals(triple.c);
  }
}
