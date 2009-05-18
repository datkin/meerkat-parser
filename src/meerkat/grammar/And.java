package meerkat.grammar;

import meerkat.Node;

public interface And<T extends Node<T>> extends Rule<T> {
  public Rule<T> getRule();
}
