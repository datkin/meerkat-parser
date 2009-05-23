package meerkat.grammar.basic;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import meerkat.grammar.*;
import meerkat.grammar.util.GrammarToString;

public class ImmutableGrammar<T> implements Grammar<T> {
  private final Map<Rule<T>, Expr<T>> rules = new HashMap<Rule<T>, Expr<T>>();
  private final Rule<T> startingRule;

  public ImmutableGrammar(Grammar<T> grammar) {
    if (grammar == null)
      throw new IllegalArgumentException();
    this.startingRule = grammar.getStartingRule();
    this.startingRule.accept(new GrammarBuilder<T>(this));
  }

  @Override
  public Iterable<Rule<T>> getRules() {
    return rules.keySet();
  }

  @Override
  public Expr<T> getExpr(Rule<T> rule) {
    return rules.get(rule);
  }

  @Override
  public Rule<T> getStartingRule() {
    return this.startingRule;
  }

  @Override
  public String toString() {
    return startingRule.accept(new GrammarToString<T>());
  }

  private static class GrammarBuilder<T> implements GrammarVisitor<T, Void> {
    private final Set<Rule<T>> seen = new HashSet<Rule<T>>();
    private final ImmutableGrammar<T> outputGrammar;

    public GrammarBuilder(ImmutableGrammar<T> outputGrammar) {
      this.outputGrammar = outputGrammar;
    }

    @Override
    public Void visit(Rule<T> rule) {
      if (seen.contains(rule))
        return null;
      seen.add(rule);
      Expr<T> expr = rule.getGrammar().getExpr(rule);
      expr.accept(this);
      // Not sure why this doesn't work (when GrammarBuilder is declared non-static)
      // ImmutableGrammar.this.rules.put(new BasicRule<T>(rule.getName(), (Grammar<T>)ImmutableGrammar.this), expr);
      Rule<T> newRule = new BasicRule<T>(rule.getName(), outputGrammar);
      outputGrammar.rules.put(newRule, expr);
      return null;
    }

    @Override
    public Void visit(Sequence<T> seq) {
      for (Expr<T> e : seq.getExprs())
        e.accept(this);
      return null;
    }

    @Override
    public Void visit(Choice<T> choice) {
      for (Expr<T> e : choice.getExprs())
        e.accept(this);
      return null;
    }

    @Override
    public Void visit(Optional<T> opt) {
      return opt.getExpr().accept(this);
    }

    @Override
    public Void visit(And<T> and) {
      return and.getExpr().accept(this);
    }

    @Override
    public Void visit(Not<T> not) {
      return not.getExpr().accept(this);
    }

    @Override
    public Void visit(ZeroOrMore<T> zom) {
      return zom.getExpr().accept(this);
    }

    @Override
    public Void visit(OneOrMore<T> oom) {
      return oom.getExpr().accept(this);
    }

    @Override
    public Void visit(Class<? extends T> clazz) {
      return null;
    }

    @Override
    public Void visit(T t) {
      return null;
    }
  }
}
