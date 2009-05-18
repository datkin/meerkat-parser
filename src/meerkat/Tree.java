package meerkat;

import java.util.Iterator;

public interface Tree<T extends Node<T>> extends Node<T> {
  //public boolean successful();
  public <V> V accept(TreeVisitor<T, V> tv);
  public Iterator<Node<T>> getNodes();
  public String getName();
}
