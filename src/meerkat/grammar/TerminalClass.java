package meerkat.grammar;

public interface TerminalClass<T> extends Expr<T> {
  public Class<? extends T> getTerminalClass();
}
