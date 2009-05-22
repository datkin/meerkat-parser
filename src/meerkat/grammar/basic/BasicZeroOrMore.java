package meerkat.grammar.basic;

import meerkat.grammar.ZeroOrMore;
import meerkat.grammar.Expr;
import meerkat.grammar.GrammarVisitor;

public class BasicZeroOrMore<T> implements ZeroOrMore<T> {
  private final Expr<T> expr;

  public BasicZeroOrMore(Expr<T> expr) {
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
