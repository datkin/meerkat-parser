package meerkat.grammar;

public interface Rule<T> extends Expr<T> {
  public Id getId();
  public interface Id<T> {
    // Id should be lightweight, do we really want to hold this reference?
    public Grammar<T> getGrammar();
  }
}
