package meerkat;

public interface Node<T extends Node<T>> {
  public <V> V accept(TreeVisitor<T, V> tv);
}
