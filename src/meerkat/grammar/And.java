package meerkat.grammar;

public interface And<T> extends Rule<T> {
  public Rule<T> getRule();
}
