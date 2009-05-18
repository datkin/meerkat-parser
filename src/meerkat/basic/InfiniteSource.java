package meerkat.basic;

import java.math.BigInteger;

import meerkat.Stream;
import meerkat.Source;

public class InfiniteSource implements Source<Number> {
  private BigInteger max = BigInteger.ZERO;

  @Override
  public boolean hasMore() {
    return true;
  }

  @Override
  public int currentSize() {
    return max.intValue();
  }

  @Override
  public Number get(int i) {
    return new BigInteger(new Integer(i).toString());
  }

  @Override
  public Number getNext() {
    max = max.add(BigInteger.ONE);
    return max;
  }

  @Override
  public Stream<Number> getStream() {
    return new BasicStream<Number>(this);
  }
}
