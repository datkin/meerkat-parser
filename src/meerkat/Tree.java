package meerkat;

import java.util.Iterator;

public interface Tree<T, L> extends Node<T, L> {
  public <V> V accept(TreeVisitor<T, L, V> tv);
  public Iterable<Node<T, L>> getNodes();
  public T getValue();
}
