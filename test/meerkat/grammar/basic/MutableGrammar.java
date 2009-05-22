package meerkat.grammar.basic;

import java.util.Map;
import java.util.HashMap;

import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;
import meerkat.grammar.Expr;

public class MutableGrammar<T> implements Grammar<T> {
  private final Map<Rule<T>, Expr<T>> rules = new HashMap<Rule<T>, Expr<T>>();

  public void addRule(Rule<T> id, Expr<T> expr) {
    rules.put(id, expr);
  }

  @Override
  public Iterable<Rule<T>> getRules() {
    return rules.keySet();
  }

  @Override
  public Expr<T> getExpr(Rule<T> id) {
    return rules.get(id);
  }

  @Override
  public Rule<T> getStartingRule() {
    throw new UnsupportedOperationException();
  }
}
