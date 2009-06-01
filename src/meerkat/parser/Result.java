package meerkat.parser;

import meerkat.Stream;
import meerkat.grammar.Rule;

public interface Result<T> {
  public boolean successful();
  //public boolean hasValue(); // some may not have results, like a "failed" optional and an and predicate
  public ParseNode<T> getValue(); // ParseNode<T>?
  public Stream<T> getRest();
  //public Rule<T> getRule();
  // perhaps some extra meta data recording state for left recursion
}

class BasicResult<T> implements Result<T> {
  // private final static Result<T> FAILED = new BasicResult<T>();
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
