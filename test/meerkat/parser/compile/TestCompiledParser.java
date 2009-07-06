package meerkat.parser.compile;

import org.junit.Test;

import meerkat.parser.ParserTester;

public class TestCompiledParser {
  @Test
  public void test() {
    ParserTester.testParser(new CompiledParserFactory());
  }
}
