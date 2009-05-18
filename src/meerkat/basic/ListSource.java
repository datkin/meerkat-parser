package meerkat.basic;

import java.util.List;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import meerkat.Node;
import meerkat.Source;
import meerkat.Stream;

public class ListSource<T extends Node<T>> implements Source<T> {
  private final List<T> list;
  private Reference<Stream<T>> streamRef = new SoftReference<Stream<T>>(null);

  public ListSource(List<T> list) {
    if (list == null)
      throw new IllegalArgumentException();
    this.list = list;
  }

  @Override
  public boolean hasMore() {
    return false;
  }

  @Override
  public int currentSize() {
    return this.list.size();
  }

  @Override
  public T get(int i) {
    return this.list.get(i); // could throw IndexOutOfBoundsException
  }

  @Override
  public T getNext() {
    throw new IllegalStateException();
    //throw new UnsupportedOperationException();
  }

  @Override
  public Stream<T> getStream() {
    Stream<T> stream = this.streamRef.get();
    if (stream == null) {
      stream = new BasicStream<T>(this);
      this.streamRef = new SoftReference<Stream<T>>(stream);
    }
    return stream;
  }
}

class BasicStream<T extends Node<T>> implements Stream<T> {
  private final Source<T> source;
  private final int position;
  private T next = null;
  private Reference<Stream<T>> streamRef = new WeakReference<Stream<T>>(null);

  public BasicStream(Source<T> source) {
    this(source, 0);
  }

  public BasicStream(Source<T> source, int position) {
    if (source == null)
      throw new IllegalArgumentException();
    if (position > source.currentSize()) // Don't allow creation of invalid streams?
      throw new IllegalArgumentException();
    this.source = source;
    this.position = position;
    if (position < source.currentSize())
      this.next = source.get(position);
  }

  @Override
  public T getNext() {
    if (this.next != null)
      return this.next;
    if (position < source.currentSize()) {
      this.next = source.get(position);
      return this.next;
    }
    if (position == source.currentSize() && source.hasMore()) {
      this.next = source.getNext();
      return this.next;
    }
    throw new IllegalStateException();
  }

  @Override
  public Stream<T> getRest() {
    Stream<T> stream = this.streamRef.get();
    if (stream == null) {
      stream = new BasicStream<T>(this.source, position + 1);
      this.streamRef = new WeakReference<Stream<T>>(stream);
    }
    return stream;
  }

  @Override
  public boolean hasMore() {
    // this assumes !(position > source.currentSize())
    // ie that if source.hasMore() is true, that source.getNext() is the token we want
    return this.next != null || source.hasMore();
  }
}
