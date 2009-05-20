package meerkat.parser;

import meerkat.Tree;
import meerkat.Node;
import meerkat.grammar.Rule;

public interface ParseTree<T> extends Tree<Rule.Id<T>, T>, ParseNode<T> {
}
