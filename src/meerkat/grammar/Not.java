package meerkat.grammar;

public interface Not<T> extends Expr<T> {
  public Expr<T> getExpr();
}
