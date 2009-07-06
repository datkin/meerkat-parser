package meerkat.parser;

import org.junit.Test;

import meerkat.grammar.Grammar;

public class TestBasicParser {

  @Test
  public void test() {
    ParserTester.testParser(new ParserFactory() {
      @Override
      public <T> Parser<T> newParser(Grammar<T> grammar, Class<T> clazz) {
        return new BasicParser<T>(grammar);
      }
    });
  }
}
