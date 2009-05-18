package meerkat.basic;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import meerkat.Tree;
import meerkat.Node;
import meerkat.TreeVisitor;

public class BasicTree<L> implements Tree<Void, L> {
  private final List<Node<Void, L>> nodes;

  public BasicTree(List<Node<Void, L>> nodes) {
    this.nodes = nodes;
  }

  @Override
  public <V> V accept(TreeVisitor<Void, L, V> tv) {
    return tv.visit(this);
  }

  @Override
  public Iterable<Node<Void, L>> getNodes() {
    return nodes; // immutable collection?
  }

  @Override
  public Void getValue() {
    return null;
  }
}
