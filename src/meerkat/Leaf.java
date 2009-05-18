package meerkat;

public interface Leaf<T, L> extends Node<T, L> {
  public <V> V accept(TreeVisitor<T, L, V> tv);
  public L getValue();
}
