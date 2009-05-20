package meerkat.util;

import static org.junit.Assert.*;

import java.util.Iterator;

import meerkat.Leaf;
import meerkat.Node;
import meerkat.Tree;
import meerkat.TreeVisitor;

public class NodeChecker<T, L> implements TreeVisitor<T, L, Void> {
  private final Node<T, L> expected;
  private Node<T, L> current;

  public NodeChecker(Node<T, L> expected) {
    if (expected == null)
      throw new IllegalArgumentException();
    this.expected = expected;
    this.current = expected;
  }

  @Override
  public Void visit(Tree<T, L> tree) {
    assertTrue("Found a tree (" + tree + ") instead of a leaf", current instanceof Tree);
    Tree<T, L> currentTree = (Tree<T, L>)current;
    assertEquals(currentTree.getValue(), tree.getValue());
    Iterator<Node<T, L>> checkNodes = tree.getNodes().iterator();
    Node<T, L> prevCurrent = current;
    for (Node<T, L> expectedNode : currentTree.getNodes()) {
      current = expectedNode;
      assertTrue("Fewer nodes than expected", checkNodes.hasNext());
      checkNodes.next().accept(this);
    }
    current = prevCurrent;
    assertFalse("More nodes than expected", checkNodes.hasNext());
    return null;
  }

  @Override
  public Void visit(L leafValue) {
    assertTrue("Found leaf (" + leafValue + ") instead of tree", current instanceof Leaf);
    assertEquals(((Leaf)current).getValue(), leafValue);
    return null;
  }
}
