package meerkat.grammar.basic;

import meerkat.grammar.Not;
import meerkat.grammar.Expr;
import meerkat.grammar.GrammarVisitor;
import meerkat.grammar.Rule;

public class BasicNot<T> extends AbstractRule<T> implements Not<T> {
  private final Expr<T> expr;

  public BasicNot(Rule.Id<T> id, Expr<T> expr) {
    super(id);
    if (expr == null)
      throw new IllegalArgumentException();
    this.expr = expr;
  }

  @Override
  public <V> V accept(GrammarVisitor<T, V> gv) {
    return gv.visit(this);
  }

  @Override
  public Expr<T> getExpr() {
    return this.expr;
  }
}
