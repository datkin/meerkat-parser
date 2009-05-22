package meerkat.grammar;

public interface Grammar<T> {
  public Iterable<Rule<T>> getRules();
  public Expr<T> getExpr(Rule<T> rule);
  public Rule<T> getStartingRule();
}
