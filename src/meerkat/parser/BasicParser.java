package meerkat.parser;

import java.util.Deque;
import java.util.List;
import java.util.LinkedList;

import meerkat.basic.BasicTree;
import meerkat.basic.BasicLeaf;

import meerkat.Stream;
import meerkat.Source;
import meerkat.grammar.*;

public class BasicParser<T> implements Parser<T> {
  private final Grammar<T> grammar;
  private final Engine<T> engine;

  public BasicParser(Grammar<T> grammar) {
    if (grammar == null)
      throw new IllegalArgumentException();
    this.grammar = grammar;
    this.engine = new BaseEngine<T>(this);
  }

  @Override
  public Result<T> parse(Source<T> source) {
    return parse(source.getStream(), grammar.getStartingRule());
  }

  @Override
  public Result<T> parse(Stream<T> stream, Rule<T> rule) {
    Stream<T> original = engine.setStream(stream);
    ListResult<T> r = grammar.getExpr(rule).accept(engine);
    engine.setStream(original);
    if (r.successful()) {
      return new BasicResult<T>(new BasicParseTree<T>(rule, r.getNodes()), r.getRest());
    }
    return new BasicResult<T>(null, null);
  }

  @Override
  public Grammar<T> getGrammar() {
    return this.grammar;
  }
}
