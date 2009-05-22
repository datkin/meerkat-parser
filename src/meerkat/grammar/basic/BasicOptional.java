package meerkat.grammar.basic;

import meerkat.grammar.Optional;
import meerkat.grammar.Expr;
import meerkat.grammar.GrammarVisitor;

public class BasicOptional<T> implements Optional<T> {
  private final Expr<T> expr;

  public BasicOptional(Expr<T> expr) {
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
