package meerkat.grammar.basic;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import meerkat.grammar.*;
import meerkat.grammar.util.GrammarToString;

public class GrammarFactory<T> implements Grammar<T> {
  private Rule<T> startingRule = null;
  private final Map<Rule<T>, Expr<T>> rules = new HashMap<Rule<T>, Expr<T>>();
  private final Map<String, Rule<T>> names = new HashMap<String, Rule<T>>();

  public Rule<T> newRule(String name) {
    if (names.containsKey(name))
      return names.get(name);
    Rule<T> rule = new BasicRule<T>(name, this);
    names.put(name, rule);
    return rule;
  }

  // only return those rule ids that have been associated with a rule
  @Override
  public Iterable<Rule<T>> getRules() {
    return rules.keySet();
  }

  @Override
  public Expr<T> getExpr(Rule<T> rule) {
    if (rule == null || !names.containsKey(rule.getName()))
      throw new IllegalArgumentException("This grammar has no such rule");
    if (!rules.containsKey(rule))
      throw new IllegalStateException("This id is not yet associated with a rule");
    return rules.get(rule);
  }

  @Override
  public Rule<T> getStartingRule() {
    if (this.startingRule == null)
      throw new IllegalStateException();
    return this.startingRule;
  }

  @Override
  public String toString() {
    return getStartingRule().accept(new GrammarToString<T>());
  }

  public void setStartingRule(Rule<T> nt) {
    this.startingRule = nt;
  }

  public Grammar<T> getGrammar() {
    return new ImmutableGrammar<T>(this); // TODO: return a final/sane grammar from this grammar
  }

  public Expr<T> listToExpr(List<Expr<T>> exprs) {
    if (exprs.size() == 1)
      return exprs.get(0);
    return new BasicSequence<T>(exprs);
  }

  public Rule<T> setRule(Rule<T> rule, Expr<T> expr) {
    if (!rule.getGrammar().equals(this))
      throw new IllegalArgumentException();
    rules.put(rule, expr);
    return rule;
  }

  public Sequence<T> seq(List<Expr<T>> exprs) { return new BasicSequence<T>(exprs); }
  public Rule<T> seq(Rule<T> rule, List<Expr<T>> exprs) { return setRule(rule, seq(exprs)); } // listToExpr(exprs)?

  public Choice<T> or(List<Expr<T>> exprs) { return new BasicChoice<T>(exprs); }
  public Rule<T> or(Rule<T> rule, List<Expr<T>> exprs) { return setRule(rule, or(exprs)); }

  public Optional<T> opt(List<Expr<T>> exprs) { return new BasicOptional<T>(listToExpr(exprs)); }
  public Optional<T> opt(Expr<T> expr) { return new BasicOptional<T>(expr); }
  public Rule<T> opt(Rule<T> rule, List<Expr<T>> exprs) { return setRule(rule, opt(exprs)); }
  public Rule<T> opt(Rule<T> rule, Expr<T> expr) { return setRule(rule, opt(expr)); }

  public And<T> and(List<Expr<T>> exprs) { return new BasicAnd<T>(listToExpr(exprs)); }
  public And<T> and(Expr<T> expr) { return new BasicAnd<T>(expr); }
  public Rule<T> and(Rule<T> rule, List<Expr<T>> exprs) { return setRule(rule, and(exprs)); }
  public Rule<T> and(Rule<T> rule, Expr<T> expr) { return setRule(rule, and(expr)); }

  public Not<T> not(List<Expr<T>> exprs) { return new BasicNot<T>(listToExpr(exprs)); }
  public Not<T> not(Expr<T> expr) { return new BasicNot<T>(expr); }
  public Rule<T> not(Rule<T> rule, List<Expr<T>> exprs) { return setRule(rule, not(exprs)); }
  public Rule<T> not(Rule<T> rule, Expr<T> expr) { return setRule(rule, not(expr)); }

  public ZeroOrMore<T> star(List<Expr<T>> exprs) { return new BasicZeroOrMore<T>(listToExpr(exprs)); }
  public ZeroOrMore<T> star(Expr<T> expr) { return new BasicZeroOrMore<T>(expr); }
  public Rule<T> star(Rule<T> rule, List<Expr<T>> exprs) { return setRule(rule, star(exprs)); }
  public Rule<T> star(Rule<T> rule, Expr<T> expr) { return setRule(rule, star(expr)); }

  public OneOrMore<T> plus(List<Expr<T>> exprs) { return new BasicOneOrMore<T>(listToExpr(exprs)); }
  public OneOrMore<T> plus(Expr<T> expr) { return new BasicOneOrMore<T>(expr); }
  public Rule<T> plus(Rule<T> rule, List<Expr<T>> exprs) { return setRule(rule, plus(exprs)); }
  public Rule<T> plus(Rule<T> rule, Expr<T> expr) { return setRule(rule, plus(expr)); }
}
