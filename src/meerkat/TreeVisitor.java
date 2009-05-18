package meerkat;

public interface TreeVisitor<T, L, V> {
  public V visit(Tree<T, L> tree);
  public V visit(L leaf);
}
