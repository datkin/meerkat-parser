package meerkat.grammar;

import meerkat.Node;

public interface GrammarVisitor<T extends Node<T>, V> {
  public <V> V visit(Rule<T> r);
  public <V> V visit(Sequence<T> a);
  public <V> V visit(Choice<T> c);
  public <V> V visit(Optional<T> o);
  public <V> V visit(Not<T> n);
  public <V> V visit(And<T> a);
  public <V> V visit(ZeroOrMore<T> zom);
  public <V> V visit(OneOrMore<T> oom);
  public <V> V visit(Class<T> c); // for class match (.equals())
  public <V> V visit(T t); // for exact match (.equals())
}
