package meerkat.grammar;

public interface OneOrMore<T> extends Expr<T> {
  public Expr<T> getExpr();
}
