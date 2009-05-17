package meerkat;

public interface Rule<T extends Node<T>> {
  public Result<T> parse(Stream<T> s, Parser<T> p);
  public Id getId();
  public interface Id<T> {
    public String getName();
  }
}
