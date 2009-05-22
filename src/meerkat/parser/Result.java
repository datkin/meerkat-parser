package meerkat.parser;

import meerkat.grammar.Nonterminal;

public interface Result<T> {
  public boolean successful();
  public ParseTree<T> getValue(); // ParseNode<T>?
  public Nonterminal<T> getNonterminal();
  // perhaps some extra meta data recording state for left recursion
}
