package meerkat.grammar;

import meerkat.Node;

public interface Rule<T extends Node<T>> {
  public <V> V accept(GrammarVisitor<T, V> gv);
  public Id getId();
  public interface Id<T extends Node<T>> {
    public String getName();
    public Grammar<T> getGrammar();
  }
}
