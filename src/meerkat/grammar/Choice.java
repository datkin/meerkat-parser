package meerkat.grammar;

public interface Choice<T> extends Rule<T> {
  public Iterable<Rule<T>> getRules();
}
