package meerkat.parser;

import org.junit.Test;

import meerkat.grammar.Grammar;

public class TestPackratParser {

  @Test
  public void test() {
    ParserFactory parserFactory = new ParserFactory() {
      @Override
      public <T> Parser<T> newParser(Grammar<T> grammar, Class<T> clazz) {
        return new PackratParser<T>(grammar);
      }
    };
    ParserTester.testParser(parserFactory);
    ParserTester.testCaching(parserFactory);
  }
}
