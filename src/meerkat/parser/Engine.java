package meerkat.parser;

import java.util.List;
import java.util.LinkedList;

import meerkat.Stream;
import meerkat.grammar.GrammarVisitor;
import meerkat.parser.basic.BasicParseLeaf;

interface Engine<T> extends GrammarVisitor<T, ListResult<T>> {
  public Stream<T> getStream();
  public Stream<T> setStream(Stream<T> stream);
}

class ListResult<T> {
  private final boolean success;
  private final List<ParseNode<T>> nodes;
  private final Stream<T> rest;

  public static <T> ListResult<T> getFailedResult() {
    return new ListResult<T>() {
      @Override
      public List<ParseNode<T>> getNodes() { throw new UnsupportedOperationException(); }
      @Override
      public Stream<T> getRest() { throw new UnsupportedOperationException(); }
    };
  }

  public ListResult(List<ParseNode<T>> nodes, Stream<T> rest) {
    if (nodes == null || rest == null)
      throw new IllegalArgumentException();
    this.success = true;
    this.nodes = nodes;
    this.rest = rest;
  }

  public ListResult(ParseNode<T> node, Stream<T> rest) {
    if (node == null || rest == null)
      throw new IllegalArgumentException();
    this.success = true;
    this.nodes = new LinkedList<ParseNode<T>>();
    this.nodes.add(node);
    this.rest = rest;
  }

  public ListResult(Stream<T> rest) {
    if (rest == null)
      throw new IllegalArgumentException();
    this.success = true;
    this.nodes = new LinkedList<ParseNode<T>>();
    this.rest = rest;
  }

  public ListResult(T t, Stream<T> rest) {
    if (t == null || rest == null)
      throw new IllegalArgumentException();
    this.success = true;
    this.nodes = new LinkedList<ParseNode<T>>();
    this.nodes.add(new BasicParseLeaf<T>(t));
    this.rest = rest;
  }

  private ListResult() {
    this.success = false;
    this.nodes = null;
    this.rest = null;
  }

  public boolean successful() {
    return success;
  }

  public List<ParseNode<T>> getNodes() {
    return this.nodes;
  }

  public Stream<T> getRest() {
    return rest;
  }
}
