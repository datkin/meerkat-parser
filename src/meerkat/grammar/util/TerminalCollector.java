package meerkat.grammar.util;

import java.util.Set;
import java.util.HashSet;

import meerkat.grammar.*;
import meerkat.grammar.basic.AbstractGrammarVisitor;

public class TerminalCollector<T> extends AbstractGrammarVisitor<T, Set<T>> {
  private final Set<Rule<T>> seen;
  private final Grammar<T> grammar; // TODO: do not need the grammar here, b/c rule has getGrammar?

  public TerminalCollector(Grammar<T> grammar) {
    this(grammar, new HashSet<Rule<T>>());
  }

  public TerminalCollector(Grammar<T> grammar, Set<Rule<T>> seen) {
    if (grammar == null || seen == null)
      throw new IllegalArgumentException();
    this.grammar = grammar;
    this.seen = seen;
  }

  @Override
  public Set<T> visit(Rule<T> r) {
    if (seen.contains(r)) {
      return new HashSet<T>();
    }
    Set<Rule<T>> newSeen = new HashSet<Rule<T>>(seen);
    newSeen.add(r);
    return grammar.getExpr(r).accept(new TerminalCollector<T>(grammar, newSeen));
  }

  @Override
  public Set<T> visit(Iterable<Expr<T>> exprs) {
    Set<T> terminals = new HashSet<T>();
    for (Expr<T> expr : exprs)
      terminals.addAll(expr.accept(this));
    return terminals;
  }

  @Override
  public Set<T> visit(Expr<T> expr) {
    return expr.accept(this);
  }

  @Override
  public Set<T> visit(Class<? extends T> clazz) {
    return new HashSet<T>();
  }

  @Override
  public Set<T> visit(T t) {
    Set<T> terminals = new HashSet<T>(1);
    terminals.add(t);
    return terminals;
  }
}
