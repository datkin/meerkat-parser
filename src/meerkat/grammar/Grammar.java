package meerkat.grammar;

public interface Grammar<T> {
  public Iterable<Nonterminal<T>> getNonterminals();
  public Expr<T> getRule(Nonterminal<T> id);
  public Nonterminal<T> getStartingNonterminal();
}
