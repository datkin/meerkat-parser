package meerkat;

// Immutable view on a Source
public interface Stream<T> {
  public T getNext(); // throw exception if nothing more?
  public Stream<T> getRest();
  public int getPosition();
  public boolean hasMore(); // true if getNext is valid
}
