package meerkat.grammar;

public interface Sequence<T> extends Rule<T> {
  public Iterable<Expr<T>> getExprs();
}
