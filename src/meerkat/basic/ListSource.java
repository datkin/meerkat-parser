package meerkat.basic;

import java.util.List;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import meerkat.Source;
import meerkat.Stream;

public class ListSource<T> implements Source<T> {
  private final List<T> list;
  private Reference<Stream<T>> streamRef = new SoftReference<Stream<T>>(null);

  public ListSource(List<T> list) {
    if (list == null)
      throw new IllegalArgumentException();
    this.list = list;
  }

  @Override
  public int hashCode() {
    return this.list.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj.getClass().equals(this.getClass())) {
      return ((ListSource)obj).list.equals(this.list);
    }
    return false;
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
