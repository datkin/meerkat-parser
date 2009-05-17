package meerkat;

// Immutable view on a Source
public interface Stream<T extends Node<T>> {
  public T getNext(); // throw exception if nothing more?
  public Stream<T> getRest();
  public boolean hasMore(); // true if getNext is valid
}
