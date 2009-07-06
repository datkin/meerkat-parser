package meerkat.parser.basic;

import meerkat.Stream;
import meerkat.parser.Result;
import meerkat.parser.ParseNode;

public class BasicResult<T> implements Result<T> {
  private final boolean success;
  private final ParseNode<T> value;
  private final Stream<T> rest;

  public BasicResult(ParseNode<T> value, Stream<T> rest) {
    if (value == null) {
      success = false;
      this.value = null;
      this.rest = null;
    } else {
      success = true;
      this.value = value;
      this.rest = rest;
    }
  }

  @Override
  public boolean successful() { return this.success; }

  @Override
  public ParseNode<T> getValue() { return this.value; }

  @Override
  public Stream<T> getRest() { return this.rest; }
}
