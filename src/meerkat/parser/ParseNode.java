package meerkat.parser;

import meerkat.Node;
import meerkat.grammar.Nonterminal;

public interface ParseNode<T> extends Node<Nonterminal<T>, T> {
}
