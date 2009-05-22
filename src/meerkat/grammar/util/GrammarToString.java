package meerkat.grammar.util;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;


import meerkat.grammar.*;

public class GrammarToString<T> implements GrammarVisitor<T, String> {
  private final Set<Rule<T>> seen = new HashSet<Rule<T>>();
  private final List<Rule<T>> queue = new LinkedList<Rule<T>>();
  private boolean emitRule = true;

  private final String padding;

  public GrammarToString() {
    this("");
  }

  public GrammarToString(String padding) {
    if (padding == null)
      throw new IllegalArgumentException();
    this.padding = padding;
  }

  @Override
  public String visit(Rule<T> rule) {
    if (!emitRule) {
      enqueue(rule);
      return rule.getName();
    }
    seen.add(rule);
    StringBuilder sb = new StringBuilder(rule.getName()).append(" <- ");
    emitRule = false;
    sb.append(rule.getGrammar().getExpr(rule).accept(this));
    emitRule = true;
    sb.append("\n");
    for (int i = 0; i < queue.size(); i++) {
      sb.append(queue.remove(i).accept(this));
    }
    return sb.toString();
  }

  @Override
  public String visit(Sequence<T> seq) {
    StringBuilder sb = new StringBuilder();
    Iterator<Expr<T>> exprs = seq.getExprs().iterator();
    while (exprs.hasNext()) {
      sb.append(exprs.next().accept(this));
      if (exprs.hasNext()) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  @Override
  public String visit(Choice<T> choice) {
    StringBuilder sb = new StringBuilder();
    Iterator<Expr<T>> exprs = choice.getExprs().iterator();
    while (exprs.hasNext()) {
      sb.append(exprs.next().accept(this));
      if (exprs.hasNext()) {
        sb.append(" / ");
      }
    }
    return sb.toString();
  }

  @Override
  public String visit(Optional<T> opt) {
    return "(" + opt.getExpr().accept(this) + ")?";
  }

  @Override
  public String visit(And<T> and) {
    return "&(" + and.getExpr().accept(this) + ")";
  }

  @Override
  public String visit(Not<T> not) {
    return "!(" + not.getExpr().accept(this) + ")";
  }

  @Override
  public String visit(ZeroOrMore<T> zom) {
    return "(" + zom.getExpr().accept(this) + ")*";
  }

  @Override
  public String visit(OneOrMore<T> oom) {
    return "(" + oom.getExpr().accept(this) + ")+";
  }

  @Override
  public String visit(Class<? extends T> clazz) {
    return clazz.getName();
  }

  @Override
  public String visit(T t) {
    return padding + t.toString() + padding;
  }

  private void enqueue(Rule<T> rule) {
    if (!seen.contains(rule) && !queue.contains(rule)) {
      queue.add(rule);
    }
  }
}
