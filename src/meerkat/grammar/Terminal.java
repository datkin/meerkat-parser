package meerkat.grammar;

public interface Terminal<T> extends Expr<T> {
  public T getTerminal();
}
