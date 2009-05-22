package meerkat.grammar;

public interface Nonterminal<T> extends Expr<T> {
  public String getName();
  public Grammar<T> getGrammar();
}
