package meerkat.parser;

import meerkat.Tree;
import meerkat.grammar.Nonterminal;

public interface ParseTree<T> extends Tree<Nonterminal<T>, T>, ParseNode<T> {
}
