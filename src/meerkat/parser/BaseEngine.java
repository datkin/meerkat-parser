package meerkat.parser;

import java.util.List;
import java.util.LinkedList;

import meerkat.Stream;
import meerkat.grammar.*;

class BaseEngine<T> implements Engine<T> {
  private final Parser<T> parser;
  private final Engine<T> parent;
  private Stream<T> stream;
  private final ListResult<T> FAILED = ListResult.getFailedResult();

  public BaseEngine(Parser<T> parser) {
    if (parser == null)
      throw new IllegalArgumentException();
    this.parser = parser;
    this.parent = this;
  }

  public BaseEngine(Parser<T> parser, Engine<T> parent) {
    if (parser == null || parent == null)
      throw new IllegalArgumentException();
    this.parser = parser;
    this.parent = parent;
  }

  @Override
  public Stream<T> setStream(Stream<T> stream) {
    Stream<T> oldStream = this.stream;
    this.stream = stream;
    return oldStream;
  }

  @Override
  public Stream<T> getStream() {
    return this.stream;
  }

  @Override
  public ListResult<T> visit(Rule<T> rule) {
    // Invariant assumption: even if parser relies on *this* engine
    // to do the parsing, we don't need to save the stream b/c the
    // parser will take care of saving/restoring "this.stream" if necessary
    Result<T> result = parser.parse(this.stream, rule);
    if (result.successful()) {
      this.stream = result.getRest();
      return new ListResult<T>(result.getValue(), this.stream);
    }
    return FAILED;
  }

  @Override
  public ListResult<T> visit(Sequence<T> seq) {
    // Invariant assumption: b/c we are relying on *parent/this* to
    // parse the sub expr, we also expect that at the end of the loop
    // this.stream has been foward the necessary amount by the sub-calls
    // We need to track the original stream b/c even if one of the sub-exprs
    // fails, it will only restore to the start of the call, but we need to
    // reset to the beginning of the entire sequence that was read.
    Stream<T> original = this.stream;
    List<ParseNode<T>> nodes = new LinkedList<ParseNode<T>>();
    for (Expr<T> expr : seq.getExprs()) {
      ListResult<T> result = expr.accept(parent);
      if (!result.successful()) {
        this.stream = original;
        return FAILED;
      }
      nodes.addAll(result.getNodes());
    }
    return new ListResult<T>(nodes, this.stream);
  }

  @Override
  public ListResult<T> visit(Choice<T> or) {
    for (Expr<T> expr : or.getExprs()) {
      ListResult<T> result = expr.accept(parent);
      if (result.successful()) {
        return result;
      }
    }
    return FAILED;
  }

  @Override
  public ListResult<T> visit(Optional<T> opt) {
    ListResult<T> result = opt.getExpr().accept(parent);
    if (result.successful()) {
      return result;
    }
    return new ListResult<T>(this.stream);
  }

  @Override
  public ListResult<T> visit(And<T> and) {
    Stream<T> original = this.stream;
    ListResult<T> result = and.getExpr().accept(parent);
    this.stream = original; // restore regardless, predicates don't consume input
    if (result.successful()) {
      return new ListResult<T>(this.stream);
    }
    return FAILED;
  }

  @Override
  public ListResult<T> visit(Not<T> not) {
    Stream<T> original = this.stream;
    ListResult<T> result = not.getExpr().accept(parent);
    this.stream = original; // restore regardless, predicates don't consume input
    if (result.successful()) {
      return FAILED;
    }
    return new ListResult<T>(this.stream);
  }

  @Override
  public ListResult<T> visit(OneOrMore<T> oom) {
    ListResult<T> result = oom.getExpr().accept(parent);
    if (!result.successful()) {
      return FAILED;
    }
    List<ParseNode<T>> nodes = new LinkedList<ParseNode<T>>();
    while (result.successful()) {
      nodes.addAll(result.getNodes());
      result = oom.getExpr().accept(parent);
    }
    return new ListResult<T>(nodes, this.stream);
  }

  @Override
  public ListResult<T> visit(ZeroOrMore<T> zom) {
    List<ParseNode<T>> nodes = new LinkedList<ParseNode<T>>();
    ListResult<T> result = zom.getExpr().accept(parent);
    while (result.successful()) {
      nodes.addAll(result.getNodes());
      result = zom.getExpr().accept(parent);
    }
    return new ListResult<T>(nodes, this.stream);
  }

  @Override
  public ListResult<T> visit(Class<? extends T> clazz) {
    if (this.stream.hasMore()) {
      T t = this.stream.getNext();
      if (clazz.isInstance(t)) {
        this.stream = this.stream.getRest();
        return new ListResult<T>(t, this.stream);
      }
    }
    return FAILED;
  }

  @Override
  public ListResult<T> visit(T goalT) {
    if (this.stream.hasMore()) {
      T t = this.stream.getNext();
      if (goalT.equals(t)) {
        this.stream = this.stream.getRest();
        return new ListResult<T>(t, this.stream);
      }
    }
    return FAILED;
  }
}
