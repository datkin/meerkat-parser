package meerkat.grammar;

public interface GrammarVisitor<T, V> {
  public <V> V visit(Sequence<T> seq);
  public <V> V visit(Choice<T> choice);
  public <V> V visit(Optional<T> opt);
  public <V> V visit(Not<T> not);
  public <V> V visit(And<T> and);
  public <V> V visit(ZeroOrMore<T> zom);
  public <V> V visit(OneOrMore<T> oom);
  public <V> V visit(Class<? extends T> clazz); // for class match (.equals())
  public <V> V visit(T t); // for exact match (.equals())
  //public <V> V visit(Comparator<T> c);
}
