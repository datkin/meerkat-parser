package meerkat.grammar.basic;

import meerkat.grammar.GrammarVisitor;
import meerkat.grammar.Terminal;

public class BasicTerminal<T> implements Terminal<T> {
  private final T terminal;

  public BasicTerminal(T terminal) {
    if (terminal == null)
      throw new IllegalArgumentException();
    this.terminal = terminal;
  }

  @Override
  public <V> V accept(GrammarVisitor<T, V> gv) {
    return gv.visit(getTerminal());
  }

  @Override
  public T getTerminal() {
    return this.terminal;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && getClass().equals(obj.getClass()) &&
      ((BasicTerminal)obj).getTerminal().equals(getTerminal());
  }

  @Override
  public int hashCode() {
    return getTerminal().hashCode();
  }
}
