package meerkat;

public interface Parser<T extends Node<T>> {
  public Result<T> parse(Stream<T> s, Rule.Id<T> r);
}
