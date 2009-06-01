package meerkat.parser;

import meerkat.grammar.Grammar;

public class BasicParser<T> extends AbstractParser<T> {
  public BasicParser(Grammar<T> grammar) {
    super(grammar, new BaseEngineFactory<T>());
  }
}
