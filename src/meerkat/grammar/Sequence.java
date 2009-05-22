package meerkat.grammar;

public interface Sequence<T> extends Expr<T> {
  public Iterable<Expr<T>> getExprs();
}
