package meerkat;

public interface Result<T extends Node<T>> {
  public boolean successful();
  public <V> V accept(TreeVisitor<T, V> tv);
  public Iterable<Node<T>> getNodes();
  public String getName();
}
