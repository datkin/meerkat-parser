package meerkat.grammar.basic;

import meerkat.grammar.Grammar;
import meerkat.grammar.GrammarVisitor;
import meerkat.grammar.Nonterminal;

public class BasicNonterminal<T> extends AbstractRule.BasicId<T> implements Nonterminal<T> {
  private final String name;

  // The Id will end up being irrelevant in this implementation if we do not provide an accessor for it
  public BasicNonterminal(int id, String name, Grammar<T> grammar) {
    super(id, grammar);
    if (name == null)
      throw new IllegalArgumentException();
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return "<Nonterminal:" + getName() + ":" + getGrammar() + ">";
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !obj.getClass().equals(this.getClass()))
      return false;
    BasicNonterminal bnt = (BasicNonterminal)obj;
    return bnt.id == this.id &&
      bnt.getName().equals(this.getName()) &&
      bnt.getGrammar().equals(this.getGrammar());
  }

  @Override
  public <V> V accept(GrammarVisitor<T, V> gv) {
    return gv.visit(this);
  }
}
