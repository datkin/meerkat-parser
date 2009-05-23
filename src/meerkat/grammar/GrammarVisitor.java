package meerkat.grammar;

public interface GrammarVisitor<T, V> {
  public V visit(Sequence<T> seq);
  public V visit(Choice<T> choice);
  public V visit(Optional<T> opt);
  public V visit(And<T> and);
  public V visit(Not<T> not);
  public V visit(ZeroOrMore<T> zom);
  public V visit(OneOrMore<T> oom);
  public V visit(Class<? extends T> clazz); // for class match (.equals())
  public V visit(T t); // for exact match (.equals())
  public V visit(Rule<T> rule); // consider overloading this with a boolean descend arg
  //public V visit(Comparator<T> c);
}
