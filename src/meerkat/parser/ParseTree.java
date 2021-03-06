package meerkat.parser;

import java.util.List;
import java.util.ArrayList;

import meerkat.Tree;
import meerkat.grammar.Rule;

public interface ParseTree<T> extends Tree<Rule<T>, T>, ParseNode<T> {
}
