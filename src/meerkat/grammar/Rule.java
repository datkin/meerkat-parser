package meerkat.grammar;

import meerkat.Node;

public interface Rule<T> extends Expr<T> {
  public Id getId();
  public interface Id<T> {
    public String getName();
    // Id should be lightweight, do we really want to hold this reference?
    public Grammar<T> getGrammar();
  }
}
