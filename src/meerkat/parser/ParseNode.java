package meerkat.parser;

import meerkat.Node;
import meerkat.grammar.Rule;

public interface ParseNode<L> extends Node<Rule.Id<L>, L> {
}
