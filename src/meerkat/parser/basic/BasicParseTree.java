package meerkat.parser.basic;

import java.util.List;
import java.util.ArrayList;

import meerkat.Node;
import meerkat.basic.BasicTree;
import meerkat.grammar.Rule;
import meerkat.parser.ParseNode;
import meerkat.parser.ParseTree;
import meerkat.parser.compile.SpliceList; // TODO: cleaner way to support this (w/ iterable)?

public class BasicParseTree<T> extends BasicTree<Rule<T>, T> implements ParseTree<T> {
  public BasicParseTree(Rule<T> rule, List<ParseNode<T>> nodes) {
    super(rule, new ArrayList<Node<Rule<T>, T>>(nodes));
  }

  public BasicParseTree(Rule<T> rule, SpliceList<ParseNode<T>> nodes) {
    this(rule, nodes.toList());
  }
}
