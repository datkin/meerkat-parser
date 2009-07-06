package meerkat.parser;

import meerkat.Stream;

public class FailureResult<T> implements Result<T> {
  @Override
  public boolean successful() {
    return false;
  }

  @Override
  public ParseNode<T> getValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Stream<T> getRest() {
    throw new UnsupportedOperationException();
  }
}
