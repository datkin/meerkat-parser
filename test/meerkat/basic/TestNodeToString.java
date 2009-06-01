package meerkat.basic;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import meerkat.Node;
import meerkat.Tree;
import meerkat.TreeVisitor;

public class TestNodeToString {
  @Test
  public void cycle() {
    final MutableTree<String, String> tree = new MutableTree<String, String>("root", new LinkedList<Node<String, String>>());
    tree.setNodes(new LinkedList<Node<String, String>>() {{
      add(tree);
    }});
    System.out.println(tree);
  }
}

class MutableTree<T, L> implements Tree<T, L> {
  private final T t;
  private List<Node<T, L>> nodes;

  public MutableTree(T t, List<Node<T, L>> nodes) {
    if (nodes == null)
      throw new IllegalArgumentException();
    this.t = t;
    this.nodes = nodes;
  }

  @Override
  public <V> V accept(TreeVisitor<T, L, V> tv) {
    return tv.visit(this);
  }

  @Override
  public Iterable<Node<T, L>> getNodes() {
    return nodes; // immutable collection?
  }

  public void setNodes(List<Node<T, L>> newNodes) {
    if (newNodes == null)
      throw new IllegalArgumentException();
    this.nodes = newNodes;
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
