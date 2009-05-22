package meerkat.grammar.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import meerkat.grammar.Sequence;
import meerkat.grammar.Expr;
import meerkat.grammar.GrammarVisitor;

public class BasicSequence<T> implements Sequence<T> {
  private final List<Expr<T>> exprs;

  public BasicSequence(List<Expr<T>> exprs) {
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
