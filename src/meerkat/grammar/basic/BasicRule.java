package meerkat.grammar.basic;

import meerkat.grammar.Grammar;
import meerkat.grammar.GrammarVisitor;
import meerkat.grammar.Rule;

public class BasicRule<T> implements Rule<T> {
  private final String name;
  private final Grammar<T> grammar;

  // The Id will end up being irrelevant in this implementation if we do not provide an accessor for it
  public BasicRule(String name, Grammar<T> grammar) {
    if (name == null || grammar == null)
      throw new IllegalArgumentException();
    this.name = name;
    this.grammar = grammar;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Grammar<T> getGrammar() {
    return this.grammar;
  }

  @Override
  public String toString() {
    return "<Rule:" + getName() + ">"; //":" + getGrammar() + ">";
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !obj.getClass().equals(this.getClass()))
      return false;
    BasicRule br = (BasicRule)obj;
    return
      br.getName().equals(this.getName());
    // TODO: check grammar equality here when proper grammar equality checking is implemented?
  }

  @Override
  public <V> V accept(GrammarVisitor<T, V> gv) {
    return gv.visit(this);
  }
}
