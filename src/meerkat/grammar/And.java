package meerkat.grammar;

public interface And<T> extends Expr<T> {
  public Expr<T> getExpr();
}
