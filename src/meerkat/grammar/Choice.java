package meerkat.grammar;

public interface Choice<T> extends Expr<T> {
  public Iterable<Expr<T>> getExprs();
}
