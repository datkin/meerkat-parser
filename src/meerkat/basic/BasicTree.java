package meerkat.basic;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import meerkat.Tree;
import meerkat.Node;
import meerkat.TreeVisitor;

public class BasicTree<T, L> implements Tree<T, L> {
  private final T t;
  private final List<Node<T, L>> nodes;

  public BasicTree(T t, List<Node<T, L>> nodes) {
    if (nodes == null)
      throw new IllegalArgumentException();
    this.t = t;
    this.nodes = new ArrayList<Node<T, L>>(nodes);
  }

  public BasicTree(T t, Node<T, L>... nodes) {
    this(t, Arrays.asList(nodes));
  }

  @Override
  public <V> V accept(TreeVisitor<T, L, V> tv) {
    return tv.visit(this);
  }

  @Override
  public Iterable<Node<T, L>> getNodes() {
    return Collections.unmodifiableList(this.nodes);
  }

  @Override
  public T getValue() {
    return t;
  }

  @Override
  public int hashCode() {
    return 31 * getValue().hashCode() + nodes.size();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Tree))
      return false;
    if (obj == this) // short circuit to avoid cycles!
      return true;
    Tree otherTree = (Tree)obj;
    if (!otherTree.getValue().equals(getValue()))
      return false;
    //Iterator<Node<T, L>> myNodes = getNodes().iterator();
    Iterator<Node> otherNodes = (Iterator<Node>)otherTree.getNodes().iterator(); // Why does getNodes().iterator() not work?
    for(Node<T, L> myNode : getNodes()) {
      if (!otherNodes.hasNext() || !myNode.equals(otherNodes.next()))
        return false;
    }
    return !otherNodes.hasNext();
  }

  @Override
  public String toString() {
    return new NodeToString<T, L>().visit(this);
  }
}
