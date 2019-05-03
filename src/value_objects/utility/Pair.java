package value_objects.utility;

import java.util.Objects;

/**
 * Generic container to contain two objects of any two type.
 */
public class Pair<A, B> {

  private final A a;

  private final B b;

  /**
   * Constructs a {@link Pair} from given objects.
   * @param a object a, determines type A
   * @param b object b, determines type B
   * @throws IllegalArgumentException if any of the given objects are null
   */
  public Pair(A a, B b) {
    if (a == null || b == null) {
      throw new IllegalArgumentException("Given parameters can't be null!");
    }
    this.a = a;
    this.b = b;
  }

  public A getA() {
    return a;
  }

  public B getB() {
    return b;
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    else if (other == null || getClass() != other.getClass()) {
      return false;
    }
    Pair<?, ?> pair = (Pair<?, ?>) other;
    return a.equals(pair.getA()) && b.equals(pair.getB());
  }
}