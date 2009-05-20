package meerkat.grammar.basic;

import meerkat.grammar.GrammarVisitor;
import meerkat.grammar.TerminalClass;

public class BasicTerminalClass<T> implements TerminalClass<T> {
  private final Class<? extends T> clazz;

  public BasicTerminalClass(Class<? extends T> clazz) {
    if (clazz == null)
      throw new IllegalArgumentException();
    this.clazz = clazz;
  }

  @Override
  public <V> V accept(GrammarVisitor<T, V> gv) {
    return gv.visit(getTerminalClass());
  }

  @Override
  public Class<? extends T> getTerminalClass() {
    return this.clazz;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && getClass().equals(obj.getClass()) &&
      ((BasicTerminalClass)obj).getTerminalClass().equals(getTerminalClass());
  }

  @Override
  public int hashCode() {
    return getTerminalClass().hashCode();
  }
}
