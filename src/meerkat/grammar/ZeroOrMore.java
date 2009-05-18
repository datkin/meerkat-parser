package meerkat.grammar;

import meerkat.Node;

public interface ZeroOrMore<T extends Node<T>> extends Rule<T> {
  public Rule<T> getRule();
}
