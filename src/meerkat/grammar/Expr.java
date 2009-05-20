package meerkat.grammar;

public interface Expr<T> {
  public <V> V accept(GrammarVisitor<T, V> gv);
}
