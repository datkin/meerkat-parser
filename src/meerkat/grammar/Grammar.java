package meerkat.grammar;

public interface Grammar<T> {
  public Iterable<Rule.Id<T>> getRuleIds();
  public Rule<T> getRule(Rule.Id<T> id);
  public Nonterminal<T> getStartingRule();
}
