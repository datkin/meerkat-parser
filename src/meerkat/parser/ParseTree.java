package meerkat.parser;

import meerkat.Tree;
import meerkat.grammar.Rule;

public interface ParseTree<T> extends Tree<Rule<T>, T>, ParseNode<T> {
}
