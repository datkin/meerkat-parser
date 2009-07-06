package meerkat.parser.compile;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;

// A linked list style collection of items where a group of items can easily be dropped
// TODO: implement Collection interface
// TODO: implement ParseTree interface
public class SpliceList<T> implements Iterable<T> {
  private final LinkedNode<T> head = new LinkedNode<T>(null);
  private LinkedNode<T> tail;

  public SpliceList() {
    this.tail = this.head;
  }

  public LinkedNode<T> add(T value) {
    LinkedNode<T> node = new LinkedNode<T>(value);
    this.tail.setNext(node);
    this.tail = node;
    return this.tail;
  }

  // drop everything *after* this node
  public void dropRest(LinkedNode<T> node) {
    this.tail = node;
    this.tail.setNext(null);
  }

  public LinkedNode<T> getTail() {
    return this.tail;
  }

  @Override
  public Iterator<T> iterator() {
    if (tail == head) // alternately, check for head.getNext() != null
      return new EmptyIterator<T>();
    return head.getNext().iterator();
  }

  public List<T> toList() {
    List<T> result = new LinkedList<T>();
    for (T t : this) {
      result.add(t);
    }
    return result;
  }

  private static class EmptyIterator<T> implements Iterator<T> {
    public boolean hasNext() {
      return false;
    }

    public T next() {
      throw new NoSuchElementException();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
