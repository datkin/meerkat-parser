package meerkat.basic;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import meerkat.Leaf;
import meerkat.Node;
import meerkat.Tree;

public class TestBasicNode {

  public static Node<String, String> empty() {
    return new BasicTree<String, String>("empty", new LinkedList<Node<String, String>>());
  }

  public static Node<String, String> tree(String name, Node<String, String>... nodes) {
    return new BasicTree<String, String>(name, nodes);
  }

  public static Node<String, String> leaf() {
    return leaf("");
  }

  public static Node<String, String> leaf(String name) {
    return new BasicLeaf<String, String>("leaf" + name);
  }

  @Test
  public void testLeaf() {
    Leaf<String, String> l = new BasicLeaf<String, String>("foo");
    assertEquals("foo", l.getValue());

    // equal nodes should have equal hash codes
    assertEquals(leaf("1").hashCode(), leaf("1").hashCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTree() {
    List<Node<String, String>> nodes = new LinkedList<Node<String, String>>();
    nodes.add(leaf("1"));
    nodes.add(leaf("2"));

    Tree<String, String> t = new BasicTree<String, String>("foo", nodes);
    assertEquals("foo", t.getValue());
    Iterator<Node<String, String>> it = t.getNodes().iterator();

    // Ensure we created a copy of the list passed to the constructor
    nodes.add(leaf("3"));

    assertEquals(leaf("1"), it.next());
    assertEquals(leaf("2"), it.next());
    assertFalse(it.hasNext());

    assertEquals(tree("a", leaf("1"), leaf("2")).hashCode(),
        tree("a", leaf("1"), leaf("2")).hashCode());
  }

  @Test
  public void testLeafEquals() {
    assertEquals(leaf(), leaf());
    assertFalse(leaf("1").equals(leaf()));
    assertFalse(leaf().equals("1"));
    assertFalse(leaf().equals(empty()));
    assertFalse(empty().equals(leaf()));
  }

  @Test
  @SuppressWarnings("unchecked") // see http://codeidol.com/java/javagenerics/Reification/Array-Creation-and-Varargs/
  public void testSimpleTreeEquals() {
    Node<String, String> tree = tree("simple", leaf("1"), leaf("2"), leaf("3"));
    assertTrue(tree.equals(tree));

    assertFalse(tree.equals(empty()));
    assertFalse(empty().equals(tree));

    assertFalse(tree.equals(leaf()));
    assertFalse(leaf().equals(tree));

    Node<String, String> verify = tree("simple", leaf("1"), leaf("2"), leaf("3"));
    assertTrue(tree.equals(verify));
    assertTrue(verify.equals(tree));

    // Different tree names
    Node<String, String> diff1 = tree("broken", leaf("1"), leaf("2"), leaf("3"));
    assertFalse(tree.equals(diff1));
    assertFalse(diff1.equals(tree));

    Node<String, String> diff2 = tree("broken", leaf("1"), leaf("2"), leaf("3"), leaf("3"));
    assertFalse(tree.equals(diff2));
    assertFalse(diff2.equals(tree));

    Node<String, String> diff3 = tree("broken", leaf("1"), leaf("2"));
    assertFalse(tree.equals(diff3));
    assertFalse(diff3.equals(tree));

    Node<String, String> diff4 = tree("broken", leaf("1"), leaf("3"), leaf("2"));
    assertFalse(tree.equals(diff4));
    assertFalse(diff4.equals(tree));
  }

  @Test
  @SuppressWarnings("unchecked") // see http://codeidol.com/java/javagenerics/Reification/Array-Creation-and-Varargs/
  public void testDeepTreeEquals() {
    Node<String, String> tree =
      tree("top",
        leaf("a"),
        tree("sub1",
          leaf("b"),
          leaf("c")),
        leaf("d"),
        tree("sub2",
          leaf("e"),
          leaf("f"),
          tree("sub3",
            leaf("g"))),
        tree("sub4"));
    assertTrue(tree.equals(tree));

    assertFalse(tree.equals(empty()));
    assertFalse(empty().equals(tree));

    assertFalse(tree.equals(leaf()));
    assertFalse(leaf().equals(tree));

    Node<String, String> verify =
      tree("top",
        leaf("a"),
        tree("sub1",
          leaf("b"),
          leaf("c")),
        leaf("d"),
        tree("sub2",
          leaf("e"),
          leaf("f"),
          tree("sub3",
            leaf("g"))),
        tree("sub4"));
    assertTrue(tree.equals(verify));
    assertTrue(verify.equals(tree));

    Node<String, String> diff1 =
      tree("top",
        leaf("a"),
        tree("sub1",
          leaf("b"),
          leaf("c")),
        leaf("d"),
        tree("sub2",
          leaf("e"),
          leaf("f"),
          tree("sub3",
            leaf("h"))), // change this node name
        tree("sub4"));
    assertFalse(tree.equals(diff1));
    assertFalse(diff1.equals(tree));

    Node<String, String> diff2 =
      tree("top",
        leaf("a"),
        tree("sub1",
          leaf("b"),
          leaf("c")),
        leaf("d"),
        tree("sub2",
          // swap positions of f and e
          leaf("f"),
          leaf("e"),
          tree("sub3",
            leaf("h"))),
        tree("sub4"));
    assertFalse(tree.equals(diff2));
    assertFalse(diff2.equals(tree));

    Node<String, String> diff3 =
      tree("top",
        leaf("a"),
        // remove sub tree 1
        leaf("d"),
        tree("sub2",
          leaf("e"),
          leaf("f"),
          tree("sub3",
            leaf("h"))),
        tree("sub4"));
    assertFalse(tree.equals(diff3));
    assertFalse(diff3.equals(tree));

    Node<String, String> diff4 =
      tree("top",
        leaf("a"),
        tree("sub1",
          leaf("b"),
          leaf("c")),
        leaf("d"),
        tree("sub2",
          leaf("e"),
          leaf("f"),
          tree("sub3",
            leaf("h"),
            leaf("g"))), // add leaf g
        tree("sub4"));
    assertFalse(tree.equals(diff4));
    assertFalse(diff4.equals(tree));
  }
}
