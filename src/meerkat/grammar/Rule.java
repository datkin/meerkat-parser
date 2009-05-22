package meerkat.grammar;

public interface Rule<T> extends Expr<T> {
  public String getName();
  public Grammar<T> getGrammar();
}
