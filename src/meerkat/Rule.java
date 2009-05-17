package meerkat;

public interface Rule<T extends Node<T>> {
  public Result<T> parse(Stream<T> s);
  public Id getId();
  public interface Id {
    public String getName(); // change to a special identifier type?
  }
}
