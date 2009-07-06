package meerkat.parser;

import meerkat.grammar.Grammar;

public interface ParserFactory {
  // Alternately, the factory class could be parameterized by T
  public <T> Parser<T> newParser(Grammar<T> grammar, Class<T> clazz);
}
