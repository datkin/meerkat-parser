package meerkat.parser;

import meerkat.grammar.Rule;

public interface Result<T> {
  public boolean successful();
  public ParseTree<T> getValue(); // ParseNode<T>?
  public Rule.Id<T> getId();
  // perhaps some extra meta data recording state for left recursion
}