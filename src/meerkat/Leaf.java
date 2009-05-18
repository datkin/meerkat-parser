package meerkat;

// TODO: is there some way to leave the T type unspecified?
public interface Leaf<T, L> extends Node<T, L> {
  public <V> V accept(TreeVisitor<T, L, V> tv);
  public L getValue();
}
