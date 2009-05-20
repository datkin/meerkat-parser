package meerkat.grammar.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import meerkat.grammar.Sequence;
import meerkat.grammar.Expr;
import meerkat.grammar.GrammarVisitor;
import meerkat.grammar.Rule;

public class BasicSequence<T> extends AbstractRule<T> implements Sequence<T> {
  private final List<Expr<T>> exprs;

  public BasicSequence(Rule.Id<T> id, List<Expr<T>> exprs) {
    super(id);
    if (exprs == null)
      throw new IllegalArgumentException();
    this.exprs = new ArrayList<Expr<T>>(exprs);
  }

  @Override
  public <V> V accept(GrammarVisitor<T, V> gv) {
    return gv.visit(this);
  }

  @Override
  public Iterable<Expr<T>> getExprs() {
    return Collections.unmodifiableList(this.exprs);
  }
}
