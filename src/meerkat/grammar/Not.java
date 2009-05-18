package meerkat.grammar;

public interface Not<T> extends Rule<T> {
  public Rule<T> getRule();
}
