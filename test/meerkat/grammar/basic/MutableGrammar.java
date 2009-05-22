package meerkat.grammar.basic;

import java.util.Map;
import java.util.HashMap;

import meerkat.grammar.Grammar;
import meerkat.grammar.Nonterminal;
import meerkat.grammar.Expr;

public class MutableGrammar<T> implements Grammar<T> {
  private final Map<Nonterminal<T>, Expr<T>> rules = new HashMap<Nonterminal<T>, Expr<T>>();

  public void addRule(Nonterminal<T> id, Expr<T> expr) {
    rules.put(id, expr);
  }

  @Override
  public Iterable<Nonterminal<T>> getNonterminals() {
    return rules.keySet();
  }

  @Override
  public Expr<T> getRule(Nonterminal<T> id) {
    return rules.get(id);
  }

  @Override
  public Nonterminal<T> getStartingNonterminal() {
    throw new UnsupportedOperationException();
  }
}
