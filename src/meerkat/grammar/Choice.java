package meerkat.grammar;

import meerkat.Node;

public interface Choice<T extends Node<T>> extends Rule<T> {
  public Iterable<Rule<T>> getRules();
}
