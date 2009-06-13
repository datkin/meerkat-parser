package meerkat.grammar.basic;

import meerkat.grammar.*;

public abstract class AbstractGrammarVisitor<T, V> implements GrammarVisitor<T, V> {

  protected abstract V visit(Iterable<Expr<T>> exprs);
  protected abstract V visit(Expr<T> expr);

  @Override
  public V visit(Sequence<T> s) {
    return this.visit(s.getExprs());
  }

  @Override
  public V visit(Choice<T> c) {
    return this.visit(c.getExprs());
  }

  @Override
  public V visit(Optional<T> opt) {
    return this.visit(opt.getExpr());
  }

  @Override
  public V visit(And<T> and) {
    return this.visit(and.getExpr());
  }

  @Override
  public V visit(Not<T> not) {
    return this.visit(not.getExpr());
  }

  @Override
  public V visit(ZeroOrMore<T> zom) {
    return this.visit(zom.getExpr());
  }

  @Override
  public V visit(OneOrMore<T> oom) {
    return this.visit(oom.getExpr());
  }

  @Override
  public abstract V visit(T t);

  @Override
  public abstract V visit(Class<? extends T> clazz);
}
