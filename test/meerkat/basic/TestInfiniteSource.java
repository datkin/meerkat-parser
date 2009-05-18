package meerkat.basic;

import org.junit.Test;
import static org.junit.Assert.*;

import meerkat.Source;
import meerkat.Stream;

public class TestInfiniteSource {

  @Test // simply ensure this terminates
  public void creation() {
    Source<Number> source = new InfiniteSource();
  }

  //@Test // RUNNING TO COMPLETION TAKES A WHILE
  // Create a ton of BigIntegers and see if Java falls over.
  // A 5MB(?) heap bottoms out around i = 820,000 when caching with strong refs
  public void caching() {
    //System.out.println(Runtime.getRuntime().totalMemory());
    Source<Number> source = new InfiniteSource();
    Stream<Number> stream = source.getStream();
    for (int i = 1; i < Integer.MAX_VALUE; i++) {
      assertTrue(stream.hasMore());
      assertEquals(i, stream.getNext().intValue());
      stream = stream.getRest();
    }
  }
}
