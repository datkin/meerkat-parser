package meerkat.parser;

import meerkat.grammar.Grammar;

public class PackratParser<T> extends AbstractParser<T> {
  public PackratParser(Grammar<T> grammar) {
    super(grammar, new CachedEngineFactory<T>());
  }
}
