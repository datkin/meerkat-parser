package meerkat.basic;

import meerkat.Node;
import meerkat.TreeVisitor;

public class BasicNode implements Node<BasicNode> {
  private final String value;

  public BasicNode(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && this.getClass().equals(obj.getClass()) &&
      ((BasicNode)obj).getValue().equals(this.getValue());
  }

  @Override
  public <V> V accept(TreeVisitor<BasicNode, V> tv) {
    return tv.visit(this);
  }
}
