package meerkat.grammar.basic;

import meerkat.grammar.Rule;
import meerkat.grammar.Grammar;
import meerkat.grammar.GrammarVisitor;

public abstract class AbstractRule<T> implements Rule<T> {
  private final Rule.Id id;

  public AbstractRule(Rule.Id id) {
    if (id == null)
      throw new IllegalArgumentException();
    this.id = id;
  }

  @Override
  public Rule.Id getId() {
    return this.id;
  }

  @Override
  public abstract <V> V accept(GrammarVisitor<T, V> tv);

  @Override // should this be ID indpendent?
  public boolean equals(Object obj) {
    return getId().equals(obj);
  }

  @Override // ID indpendent?
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public String toString() {
    return "<Rule:" + getId() + ">";
  }

  public static class BasicId<T> implements Rule.Id<T> {
    protected final int id;
    private final Grammar<T> grammar;

    public BasicId(int id, Grammar<T> grammar) {
      this.id = id;
      this.grammar = grammar;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && this.getClass().equals(obj.getClass())) {
        BasicId id = (BasicId)obj;
        return id.id == this.id;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return this.id;
    }

    @Override
    public Grammar<T> getGrammar() {
      return this.grammar;
    }

    @Override
    public String toString() {
      return "<Rule.Id#" + id + ":" + getGrammar() + ">";
    }
  }
}
