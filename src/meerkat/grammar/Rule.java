package meerkat.grammar;

import meerkat.Node;

public interface Rule<T> {
  public <V> V accept(GrammarVisitor<T, V> gv);
  public Id getId();
  public interface Id<T> {
    public String getName();
    public Grammar<T> getGrammar();
  }
}
