package meerkat.basic;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.LinkedList;

import meerkat.Source;
import meerkat.Stream;

public class TestListSource {

  private List<String> list = new LinkedList<String>() {{
    add("foo");
    add("bar");
    add("baz");
  }};

  @Test
  public void simple() {
    Source<String> source = new ListSource<String>(list);
    assertEquals(3, source.currentSize());
    assertFalse(source.hasMore());
    assertEquals("foo", source.get(0));
    assertEquals("bar", source.get(1));
    assertEquals("baz", source.get(2));
  }

  @Test(expected=IndexOutOfBoundsException.class)
  public void bounds() {
    Source<String> source = new ListSource<String>(list);
    source.get(3);
  }

  @Test
  public void streams() {
    Source<String> source = new ListSource<String>(list);

    Stream<String> stream1 = source.getStream();
    assertTrue(stream1.hasMore());
    assertEquals("foo", stream1.getNext());

    Stream<String> stream2 = stream1.getRest();
    assertTrue(stream2.hasMore());
    assertEquals("bar", stream2.getNext());

    Stream<String> stream3 = stream2.getRest();
    assertTrue(stream3.hasMore());
    assertEquals("baz", stream3.getNext());

    Stream<String> stream4 = stream3.getRest();
    assertFalse(stream4.hasMore());

    Exception getNextError = null;
    try {
      stream4.getNext();
    } catch (Exception error) {
      getNextError = error;
    }
    assertNotNull(getNextError);

    Exception getRestError = null;
    try {
      stream4.getRest();
    } catch(Exception error) {
      getRestError = error;
    }
    assertNotNull(getRestError);

    // Now check again, for idempotency
    assertTrue(stream1.hasMore());
    assertEquals("foo", stream1.getNext());

    assertTrue(stream2.hasMore());
    assertEquals("bar", stream2.getNext());

    assertTrue(stream3.hasMore());
    assertEquals("baz", stream3.getNext());

    assertFalse(stream4.hasMore());
  }
}
