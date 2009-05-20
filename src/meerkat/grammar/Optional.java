package meerkat.grammar;

public interface Optional<T> extends Rule<T> {
  public Expr<T> getExpr();
}
