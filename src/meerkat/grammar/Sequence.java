package meerkat.grammar;

import meerkat.Node;

public interface Sequence<T extends Node<T>> extends Rule<T> {
  public Iterable<Rule<T>> getRules();
}
