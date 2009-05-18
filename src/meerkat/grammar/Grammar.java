package meerkat.grammar;

import meerkat.Node;

public interface Grammar<T> {
  public Iterable<Rule.Id<T>> ruleIds();
  public Rule<T> getRule(Rule.Id<T> id);
}
