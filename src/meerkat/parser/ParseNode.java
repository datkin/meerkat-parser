package meerkat.parser;

import meerkat.Node;
import meerkat.grammar.Rule;

public interface ParseNode<T> extends Node<Rule<T>, T> {
}
