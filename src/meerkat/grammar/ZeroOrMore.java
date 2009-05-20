package meerkat.grammar;

public interface ZeroOrMore<T> extends Rule<T> {
  public Expr<T> getExpr();
}
