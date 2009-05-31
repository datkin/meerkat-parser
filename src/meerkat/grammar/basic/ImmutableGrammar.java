package meerkat.grammar.basic;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import meerkat.grammar.*;
import meerkat.grammar.util.GrammarToString;

public class ImmutableGrammar<T> implements Grammar<T> {
  private final Map<Rule<T>, Expr<T>> rules = new HashMap<Rule<T>, Expr<T>>();
  private final Rule<T> startingRule;

  public ImmutableGrammar(Grammar<T> grammar) {
    if (grammar == null)
      throw new IllegalArgumentException();
    this.startingRule = (Rule<T>)grammar.getStartingRule().accept(new GrammarBuilder<T>(this));
    /*
    for (Rule<T> r : rules.keySet()) {
      if (r.getName().equals(grammar.getStartingRule().getName())) {
        this.startingRule = r;
        return;
      }
    }
    // should probably throw an error if this doesn't get set
    this.startingRule = new BasicRule<T>(grammar.getStartingRule().getName(), this);
    */
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

  // copy an existing grammar to the output grammar
  private static class GrammarBuilder<T> implements GrammarVisitor<T, Expr<T>> {
    private final Set<Rule<T>> seen = new HashSet<Rule<T>>();
    private final Map<String, Rule<T>> newRules = new HashMap<String, Rule<T>>();
    private final ImmutableGrammar<T> outputGrammar;

    public GrammarBuilder(ImmutableGrammar<T> outputGrammar) {
      this.outputGrammar = outputGrammar;
    }

    @Override
    public Expr<T> visit(Rule<T> rule) {
      if (seen.contains(rule))
        return newRules.get(rule.getName()); // this should NOT BE NULL!
      seen.add(rule);
      Rule<T> newRule = new BasicRule<T>(rule.getName(), outputGrammar);
      newRules.put(rule.getName(), newRule);
      Expr<T> newExpr = rule.getGrammar().getExpr(rule).accept(this);
      outputGrammar.rules.put(newRule, newExpr);
      return newRule;

      /*
      // Not sure why this doesn't work (when GrammarBuilder is declared non-static)
      // ImmutableGrammar.this.rules.put(new BasicRule<T>(rule.getName(), (Grammar<T>)ImmutableGrammar.this), expr);
      Rule<T> newRule = new BasicRule<T>(rule.getName(), outputGrammar);
      outputGrammar.rules.put(newRule, expr);
      return null;
      */
    }

    @Override
    public Expr<T> visit(Sequence<T> seq) {
      List<Expr<T>> newExprs = new LinkedList<Expr<T>>();
      for (Expr<T> e : seq.getExprs())
        newExprs.add(e.accept(this));
      return new BasicSequence<T>(new ArrayList<Expr<T>>(newExprs));
    }

    @Override
    public Expr<T> visit(Choice<T> choice) {
      List<Expr<T>> newExprs = new LinkedList<Expr<T>>();
      for (Expr<T> e : choice.getExprs())
        newExprs.add(e.accept(this));
      return new BasicChoice<T>(new ArrayList<Expr<T>>(newExprs));
    }

    @Override
    public Expr<T> visit(Optional<T> opt) {
      return new BasicOptional<T>(opt.getExpr().accept(this));
    }

    @Override
    public Expr<T> visit(And<T> and) {
      return new BasicAnd<T>(and.getExpr().accept(this));
    }

    @Override
    public Expr<T> visit(Not<T> not) {
      return new BasicNot<T>(not.getExpr().accept(this));
    }

    @Override
    public Expr<T> visit(ZeroOrMore<T> zom) {
      return new BasicZeroOrMore<T>(zom.getExpr().accept(this));
    }

    @Override
    public Expr<T> visit(OneOrMore<T> oom) {
      return new BasicOneOrMore<T>(oom.getExpr().accept(this));
    }

    @Override
    public Expr<T> visit(Class<? extends T> clazz) {
      return new BasicTerminalClass<T>(clazz);
    }

    @Override
    public Expr<T> visit(T t){
      return new BasicTerminal<T>(t);
    }
  }
}
