package meerkat.basic;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.LinkedList;

import meerkat.Source;
import meerkat.Stream;

public class TestListSource {

  private List<BasicNode> list = new LinkedList<BasicNode>() {{
    add(new BasicNode("foo"));
    add(new BasicNode("bar"));
    add(new BasicNode("baz"));
  }};

  @Test
  public void simple() {
    Source<BasicNode> source = new ListSource<BasicNode>(list);
    assertEquals(source.currentSize(), 3);
    assertFalse(source.hasMore());
    assertEquals(source.get(0), new BasicNode("foo"));
    assertEquals(source.get(1), new BasicNode("bar"));
    assertEquals(source.get(2), new BasicNode("baz"));
  }

  @Test(expected=IndexOutOfBoundsException.class)
  public void bounds() {
    Source<BasicNode> source = new ListSource<BasicNode>(list);
    source.get(3);
  }

  @Test
  public void streams() {
    Source<BasicNode> source = new ListSource<BasicNode>(list);

    Stream<BasicNode> stream1 = source.getStream();
    assertTrue(stream1.hasMore());
    assertEquals(stream1.getNext(), new BasicNode("foo"));

    Stream<BasicNode> stream2 = stream1.getRest();
    assertTrue(stream2.hasMore());
    assertEquals(stream2.getNext(), new BasicNode("bar"));

    Stream<BasicNode> stream3 = stream2.getRest();
    assertTrue(stream3.hasMore());
    assertEquals(stream3.getNext(), new BasicNode("baz"));

    Stream<BasicNode> stream4 = stream3.getRest();
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
    assertEquals(stream1.getNext(), new BasicNode("foo"));

    assertTrue(stream2.hasMore());
    assertEquals(stream2.getNext(), new BasicNode("bar"));

    assertTrue(stream3.hasMore());
    assertEquals(stream3.getNext(), new BasicNode("baz"));

    assertFalse(stream4.hasMore());
  }
}
