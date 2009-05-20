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
    private final int id;
    private final String name;
    private final Grammar<T> grammar;

    public BasicId(int id, String name, Grammar<T> grammar) {
      this.id = id;
      this.name = name;
      this.grammar = grammar;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && this.getClass().equals(obj.getClass())) {
        BasicId id = (BasicId)obj;
        return id.id == this.id && id.name.equals(this.name) && id.grammar.equals(this.grammar);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return this.id;
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
      return getGrammar().toString() + ": " + this.getName() + " (" + id + ")";
    }
  }
}
