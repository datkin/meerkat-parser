package meerkat.parser.compile;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

public class TestSpliceList {

  @Test
  public void testEmpty() {
    SpliceList<String> sl = new SpliceList<String>();

    assertFalse(sl.iterator().hasNext());
    assertTrue(sl.toList().isEmpty());
  }

  @Test
  public void testSingle() {
    SpliceList<String> sl = new SpliceList<String>();
    sl.add("foo");

    Iterator<String> it = sl.iterator();
    assertTrue(it.hasNext());
    assertEquals("foo", it.next());
    assertFalse(it.hasNext());

    List<String> l =  sl.toList();
    assertEquals(1, l.size());
    assertEquals("foo", l.get(0));
  }

  @Test
  public void testMulti() {
    SpliceList<String> sl = new SpliceList<String>();
    sl.add("foo");
    sl.add("bar");

    Iterator<String> it = sl.iterator();
    assertTrue(it.hasNext());
    assertEquals("foo", it.next());
    assertTrue(it.hasNext());
    assertEquals("bar", it.next());
    assertFalse(it.hasNext());

    List<String> l =  sl.toList();
    assertEquals(2, l.size());
    assertEquals("foo", l.get(0));
    assertEquals("bar", l.get(1));
  }

  @Test
  public void testDrop() {
    SpliceList<String> sl = new SpliceList<String>();
    sl.add("foo");
    LinkedNode<String> reset = sl.getTail();
    sl.add("baz");
    sl.add("bar");
    sl.dropRest(reset);

    Iterator<String> it = sl.iterator();
    assertTrue(it.hasNext());
    assertEquals("foo", it.next());
    assertFalse(it.hasNext());

    List<String> l =  sl.toList();
    assertEquals(1, l.size());
    assertEquals("foo", l.get(0));
  }
}
