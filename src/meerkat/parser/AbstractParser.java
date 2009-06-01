package meerkat.parser;

import meerkat.Stream;
import meerkat.Source;
import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;

public abstract class AbstractParser<T> implements Parser<T> {
  private final Grammar<T> grammar;
  private final Engine<T> engine;

  public AbstractParser(Grammar<T> grammar, EngineFactory<T> engineFactory) {
    if (grammar == null || engineFactory == null)
      throw new IllegalArgumentException();
    this.grammar = grammar;
    this.engine = engineFactory.newEngine(this);
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
