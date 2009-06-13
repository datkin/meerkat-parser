package meerkat.grammar.util;

import java.util.Map;
import java.util.HashMap;

import meerkat.grammar.*;
import meerkat.grammar.basic.AbstractGrammarVisitor;

public class DependencyGraph<T> {
  private final Map<Rule<T>, DependencyNode<T>> graph =
    new HashMap<Rule<T>, DependencyNode<T>>();

  public DependencyGraph() {}

  public DependencyGraph(Grammar<T> grammar) {
    for (Rule<T> rule : grammar.getRules())
      this.add(new DependencyNode<T>(rule));
    for (Rule<T> rule : grammar.getRules())
      grammar.getExpr(rule).accept(new GraphBuilder<T>(rule, this));
  }

  public DependencyNode<T> get(Rule<T> rule) {
    return graph.get(rule);
  }

  public void add(DependencyNode<T> node) {
    graph.put(node.getRule(), node);
  }

  public boolean equals(Object obj) {
    if (obj != null && obj.getClass().equals(this.getClass())) {
      return ((DependencyGraph)obj).graph.equals(this.graph);
    }
    return false;
  }
}

class GraphBuilder<T> extends AbstractGrammarVisitor<T, Void> {
  private final Rule<T> rule;
  private final DependencyGraph<T> graph;

  public GraphBuilder(Rule<T> rule, DependencyGraph<T> graph) {
    if (rule == null || graph == null)
      throw new IllegalArgumentException();
    this.rule = rule;
    this.graph = graph;
  }

  @Override
  public Void visit(Expr<T> expr) {
    return expr.accept(this);
  }

  @Override
  public Void visit(Iterable<Expr<T>> exprs) {
    for (Expr<T> expr : exprs)
      expr.accept(this);
    return null;
  }

  @Override
  public Void visit(Rule<T> r) {
    graph.get(this.rule).addRequirement(r);
    graph.get(r).addDependent(this.rule);
    return null;
  }

  @Override
  public Void visit(T t) {
    return null;
  }

  @Override
  public Void visit(Class<? extends T> clazz) {
    return null;
  }
}
