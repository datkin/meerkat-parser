package meerkat.grammar;

public interface ZeroOrMore<T> extends Expr<T> {
  public Expr<T> getExpr();
}
