package meerkat.grammar.basic;

import meerkat.grammar.ZeroOrMore;
import meerkat.grammar.Expr;
import meerkat.grammar.GrammarVisitor;
import meerkat.grammar.Rule;

public class BasicZeroOrMore<T> extends AbstractRule<T> implements ZeroOrMore<T> {
  private final Expr<T> expr;

  public BasicZeroOrMore(Rule.Id<T> id, Expr<T> expr) {
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
