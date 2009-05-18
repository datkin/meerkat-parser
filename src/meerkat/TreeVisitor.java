package meerkat;

public interface TreeVisitor<T extends Node<T>, V> {
  public V visit(Tree<T> r);
  public V visit(T t);
}
