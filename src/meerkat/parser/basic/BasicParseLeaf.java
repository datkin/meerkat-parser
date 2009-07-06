package meerkat.parser.basic;

import meerkat.grammar.Rule;
import meerkat.basic.BasicLeaf;
import meerkat.parser.ParseNode;

public class BasicParseLeaf<T> extends BasicLeaf<Rule<T>, T> implements ParseNode<T> {
  public BasicParseLeaf(T t) {
    super(t);
  }
}
