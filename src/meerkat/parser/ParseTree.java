package meerkat.parser;

import java.util.List;
import java.util.ArrayList;

import meerkat.Node;
import meerkat.Tree;
import meerkat.TreeVisitor;
import meerkat.grammar.Rule;

import meerkat.basic.BasicTree;
import meerkat.basic.BasicLeaf;

public interface ParseTree<T> extends Tree<Rule<T>, T>, ParseNode<T> {
}

class BasicParseTree<T> extends BasicTree<Rule<T>, T> implements ParseTree<T> {
  public BasicParseTree(Rule<T> rule, List<ParseNode<T>> nodes) {
    super(rule, new ArrayList<Node<Rule<T>, T>>(nodes));
  }

/*
  public BasicParseTree(Rule<T> rule, List<ParseNode<T>> nodes) {
    super(rule, (List<Node<Rule<T>, T>>)nodes);
  }
*/
}

class BasicParseLeaf<T> extends BasicLeaf<Rule<T>, T> implements ParseNode<T> {
  public BasicParseLeaf(T t) {
    super(t);
  }
}

/*
class BasicParseTree<T> implements ParseTree<T> {
  private final Rule<T> rule;
  private final List<Node<Rule<T>, T>> nodes;

  public BasicParseTree(Rule<T> rule, List<ParseNode<T>> nodes) {
    if (nodes == null)
      throw new IllegalArgumentException();
    this.rule = rule;
    this.nodes = new ArrayList<Node<Rule<T>, T>>(nodes);
  }

  @Override
  public <V> V accept(TreeVisitor<Rule<T>, T, V> tv) {
    return tv.visit(this);
  }

  @Override
  public Iterable<Node<Rule<T>, T>> getNodes() {
    return nodes;
  }

  @Override
  public Rule<T> getValue() {
    return rule;
  }
}
*/
