package meerkat;

public interface Node<T, L> {
  public <V> V accept(TreeVisitor<T, L, V> tv);
}
