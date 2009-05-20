package meerkat.grammar;

public interface OneOrMore<T> extends Rule<T> {
  public Expr<T> getExpr();
}
