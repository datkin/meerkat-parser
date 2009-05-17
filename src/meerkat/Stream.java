package meerkat;

// Immutable view on a Source
public interface Stream<T extends Node<T>> {
  //public Result<T> parse(Rule<T> r);
  //public Source<T> getSource();
  public T getNext(); // throw exception if nothing more?
  public Stream<T> getRest();
  public boolean hasMore();
}
