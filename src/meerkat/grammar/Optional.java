package meerkat.grammar;

public interface Optional<T> extends Expr<T> {
  public Expr<T> getExpr();
}
