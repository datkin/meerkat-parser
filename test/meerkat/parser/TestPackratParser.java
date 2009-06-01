package meerkat.parser;

import org.junit.Test;

public class TestPackratParser {

  @Test
  public void test() {
    ParserTester.testParser(PackratParser.class);
    ParserTester.testCaching(PackratParser.class);
  }
}
