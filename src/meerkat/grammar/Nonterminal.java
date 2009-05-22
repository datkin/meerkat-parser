package meerkat.grammar;

public interface Nonterminal<T> extends Expr<T>, Rule.Id<T> {
  public String getName();
}
