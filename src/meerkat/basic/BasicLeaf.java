package meerkat.basic;

import meerkat.Node;
import meerkat.Leaf;
import meerkat.TreeVisitor;

public class BasicLeaf<T, L> implements Leaf<T, L> {//, BasicNode<L> {
  private final L value;

  public BasicLeaf(L value) {
    this.value = value;
  }

  @Override
  public L getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && this.getClass().equals(obj.getClass()) &&
      ((BasicLeaf)obj).getValue().equals(this.getValue());
  }

  @Override
  public int hashCode() {
    return getValue().hashCode();
  }

  @Override
  public <V> V accept(TreeVisitor<T, L, V> tv) {
    return tv.visit(value);
  }

  @Override
  public String toString() {
    return getValue().toString();
  }
}
