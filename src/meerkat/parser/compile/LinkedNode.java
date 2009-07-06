package meerkat.parser.compile;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedNode<T> implements Iterable<T> {
  private final T value;
  private LinkedNode<T> next;

  public LinkedNode(T value) {
    this(value, null);
  }

  public LinkedNode(T value, LinkedNode<T> next) {
    this.value = value;
    this.next = next;
  }

  private T getValue() {
    return this.value;
  }

  public void setNext(LinkedNode<T> next) {
    this.next = next;
  }

  public LinkedNode<T> getNext() {
    return this.next;
  }

  @Override
  public Iterator<T> iterator() {
    return new LinkedNodeIterator<T>(this);
  }

  private static class LinkedNodeIterator<T> implements Iterator<T> {
    private LinkedNode<T> current;

    public LinkedNodeIterator(LinkedNode<T> start) {
      this.current = start;
    }

    @Override
    public boolean hasNext() {
      return current != null;
    }

    @Override
    public T next() {
      if (current == null) {
        throw new NoSuchElementException();
      }
      T value = current.getValue();
      current = current.getNext();
      return value;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
