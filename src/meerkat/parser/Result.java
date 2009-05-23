package meerkat.parser;

import meerkat.Stream;
import meerkat.grammar.Rule;

public interface Result<T> {
  public boolean successful();
  public boolean hasValue(); // some may not have results, like a "failed" optional and an and predicate
  public ParseNode<T> getValue(); // ParseNode<T>?
  public Stream<T> getRest();
  //public Rule<T> getRule();
  // perhaps some extra meta data recording state for left recursion
}
