package meerkat.parser;

import meerkat.Stream;
import meerkat.grammar.Rule;

public interface Result<T> {
  public boolean successful();
  public ParseNode<T> getValue();
  public Stream<T> getRest();
}
