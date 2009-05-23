package meerkat.parser;

import java.util.Deque;
import java.util.List;
import java.util.LinkedList;

import meerkat.basic.BasicTree;
import meerkat.basic.BasicLeaf;

import meerkat.Stream;
import meerkat.Source;
import meerkat.grammar.*;

public class PackratParser<T> implements Parser<T> {
  private final Grammar<T> grammar;
  //private final NaiveEngine<T> engine;

  public PackratParser(Grammar<T> grammar) {
    if (grammar == null)
      throw new IllegalArgumentException();
    this.grammar = grammar;
    //this.engine = new NaiveEngine(this);
  }

  public Result<T> parse(Source<T> source) {
    return parse(source.getStream(), grammar.getStartingRule());
  }

  @Override
  public Result<T> parse(Stream<T> stream, Rule<T> rule) {
    NaiveEngine<T> engine = new NaiveEngine<T>(this, stream);
    ListResult<T> r = grammar.getExpr(rule).accept(engine);
    if (r.successful()) {
      return new BasicResult<T>(new BasicParseTree<T>(rule, r.getNodes()), engine.getStream());
    }
    return new BasicResult<T>(null, null);
  }

  @Override
  public Grammar<T> getGrammar() {
    return this.grammar;
  }
}

class NaiveEngine<T> implements GrammarVisitor<T, ListResult<T>> {
  // stream management has to be done when we need to RESET to a previous position...
  private final Parser<T> parser;
  private final Deque<Stream<T>> streams = new LinkedList<Stream<T>>();
  private Stream<T> stream;
  private final ListResult<T> FAILED = new ListResult<T>(false, null);

  NaiveEngine(Parser<T> parser, Stream<T> stream) {
    this.parser = parser;
    this.stream = stream;
  }

  // "push" the current stream to the stack
  private void push() {
    this.streams.addFirst(this.stream);
  }

  // *restore* the return from the stack and return an empty success
  private ListResult<T> restoreAndSucceed() {
    this.stream = this.streams.removeFirst();
    return new ListResult<T>(true, new LinkedList<ParseNode<T>>());
  }

  private ListResult<T> succeed() {
    this.streams.removeFirst();
    return new ListResult<T>(true, new LinkedList<ParseNode<T>>());
  }

  // pop the stream from the top of the stack, we no longer care about it, and return a success
  private ListResult<T> succeed(ParseNode<T> node) {
    this.streams.removeFirst();
    List<ParseNode<T>> nodes = new LinkedList<ParseNode<T>>();
    nodes.add(node);
    return new ListResult<T>(true, nodes);
  }

  private ListResult<T> succeed(List<ParseNode<T>> nodes) {
    this.streams.removeFirst();
    return new ListResult<T>(true, nodes);
  }

  // restore the stream at the top of the stack and return a failure result
  private ListResult<T> fail() {
    this.stream = this.streams.removeFirst();
    return FAILED;
  }

  public Stream<T> getStream() { return this.stream; }

  @Override
  public ListResult<T> visit(Rule<T> rule) {
    push();
    Result<T> result = parser.parse(stream, rule);
    if (result.successful()) {
      this.stream = result.getRest();
      return succeed(result.getValue());
    }
    return fail();
  }

  @Override
  public ListResult<T> visit(Sequence<T> seq) {
    push();
    List<ParseNode<T>> nodes = new LinkedList<ParseNode<T>>();
    for (Expr<T> expr : seq.getExprs()) {
      ListResult<T> result = expr.accept(this);
      if (result.successful()) {
        nodes.addAll(result.getNodes());
      } else {
        return fail();
      }
    }
    return succeed(nodes); //new BasicResult(new ParseTree<T>(nodes), stream);
  }

  @Override
  public ListResult<T> visit(Choice<T> choice) {
    push();
    for (Expr<T> expr : choice.getExprs()) {
      ListResult<T> result = expr.accept(this);
      if (result.successful()) {
        //this.stream = result.getRest(); // we pass this new stream in the result, so the
        // upstream recipient is responsible for setting it up
        return succeed(result.getNodes());
      }
    }
    return fail();
  }

  @Override
  public ListResult<T> visit(Optional<T> opt) {
    push();
    ListResult<T> result = opt.getExpr().accept(this);
    if (result.successful()) {
      return succeed(result.getNodes());
    }
    return succeed();
  }

  @Override
  public ListResult<T> visit(And<T> and) {
    push();
    ListResult<T> result = and.getExpr().accept(this);
    if (result.successful()) {
      return restoreAndSucceed();
    }
    return fail();
  }

  @Override
  public ListResult<T> visit(Not<T> not) {
    push();
    ListResult<T> result = not.getExpr().accept(this);
    if (result.successful()) {
      return fail();
    }
    return restoreAndSucceed();
  }

  @Override
  public ListResult<T> visit(OneOrMore<T> oom) {
    push();
    List<ParseNode<T>> nodes = new LinkedList<ParseNode<T>>();
    ListResult<T> result = oom.getExpr().accept(this);
    if (!result.successful()) {
      return fail();
    }
    while (result.successful()) {
      nodes.addAll(result.getNodes());
      result = oom.getExpr().accept(this);
    }
    return succeed(nodes);
  }

  @Override
  public ListResult<T> visit(ZeroOrMore<T> zom) {
    push();
    List<ParseNode<T>> nodes = new LinkedList<ParseNode<T>>();
    ListResult<T> result = zom.getExpr().accept(this);
    while (result.successful()) {
      nodes.addAll(result.getNodes());
      result = zom.getExpr().accept(this);
    }
    return succeed(nodes);
  }

  @Override
  public ListResult<T> visit(Class<? extends T> clazz) {
    if (this.stream.hasMore()) {
      T t = stream.getNext();
      if (clazz.isInstance(t)) {
        this.stream = stream.getRest();
        List<ParseNode<T>> nodes = new LinkedList<ParseNode<T>>();
        nodes.add(new BasicParseLeaf<T>(t));
        return new ListResult<T>(true, nodes);
      }
    }
    return new ListResult<T>(false, null);
  }

  @Override
  public ListResult<T> visit(T goalT) {
    if (this.stream.hasMore()) {
      T t = stream.getNext();
      if (t.equals(goalT)) {
        this.stream = stream.getRest();
        List<ParseNode<T>> nodes = new LinkedList<ParseNode<T>>();
        nodes.add(new BasicParseLeaf<T>(t));
        return new ListResult<T>(true, nodes);
      }
    }
    return new ListResult<T>(false, null);
  }
}

class BasicResult<T> implements Result<T> {
  // private final static Result<T> FAILED = new BasicResult<T>();
  private final boolean success;
  private final ParseNode<T> value;
  private final Stream<T> rest;

  public BasicResult(ParseNode<T> value, Stream<T> rest) {
    if (value == null) {
      success = false;
      this.value = null;
      this.rest = null;
    } else {
      success = true;
      this.value = value;
      this.rest = rest;
    }
  }

  @Override
  public boolean successful() { return this.success; }

  @Override
  public boolean hasValue() { return this.value != null; }

  @Override
  public ParseNode<T> getValue() { return this.value; }

  @Override
  public Stream<T> getRest() { return this.rest; }
}

class ListResult<T> {
  private final boolean success;
  private final List<ParseNode<T>> nodes;

  public ListResult(boolean success, List<ParseNode<T>> nodes) {
    this.success = success;
    this.nodes = nodes;
  }

  public boolean successful() { return this.success; }

  public List<ParseNode<T>> getNodes() { return this.nodes; }
}
