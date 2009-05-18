package meerkat.basic;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import meerkat.Tree;
import meerkat.Node;
import meerkat.TreeVisitor;

public class BasicTree implements Tree<BasicNode> {
  private final String name;
  private final List<Node<BasicNode>> nodes;

  public BasicTree(String name, List<Node<BasicNode>> nodes) {
    this.name = name;
    this.nodes = nodes;
  }

  @Override
  public <V> V accept(TreeVisitor<BasicNode, V> tv) {
    return tv.visit(this);
  }

  @Override
  public Iterator<Node<BasicNode>> getNodes() {
    return nodes.iterator();
  }

  @Override
  public String getName() {
    return this.name;
  }
}
