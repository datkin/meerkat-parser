package meerkat;

public interface Source<T> {
  public boolean hasMore(); // indicates whether or not the end of the source has been reached
  public int currentSize();
  public T get(int i);
  public T getNext(); // blocking operation to get the next token
  public Stream<T> getStream(); // get a stream pointing to the head of the source
}
