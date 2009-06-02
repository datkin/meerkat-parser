package meerkat.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import static meerkat.parser.ParserTester.*;

import meerkat.grammar.basic.BasicRule;
import meerkat.grammar.basic.UnsafeGrammarFactory;
import meerkat.grammar.*;

public class TestRecursiveParser {

  @Test
  public void test() {
    //testRecursionDetection(RecursiveParser.class);
    testDirectLR(RecursiveParser.class);
    testIndirectLR(RecursiveParser.class);
    ParserTester.testParser(RecursiveParser.class);
    ParserTester.testCaching(RecursiveParser.class);
  }

  public void testDirectLR(Class<? extends Parser> clazz) {
    Grammar<String> g = getDirectLRGrammar();
    Parser<String> p = getParser(clazz, g);
    Rule<String> expr = g.getStartingRule();
    Rule<String> digit = new BasicRule<String>("digit", g);
    Rule<String> num = new BasicRule<String>("num", g);

    Result<String> r;

    r = p.parse(getSourceForString("12"));
    assertTrue(r.successful());
    assertEquals(newParseTree(expr, newParseTree(num, "1", "2")), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("12-10"));
    assertTrue(r.successful());
    assertEquals(newParseTree(expr,
          newParseTree(expr,
            newParseTree(num, "1", "2")),
          "-",
          newParseTree(num, "1", "0")),
        r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("12-10x"));
    assertTrue(r.successful());
    assertEquals(newParseTree(expr,
          newParseTree(expr,
            newParseTree(num, "1", "2")),
          "-",
          newParseTree(num, "1", "0")),
        r.getValue());
    assertEquals(getSourceForString("12-10x").getStream().getRest().getRest().getRest().getRest().getRest(), r.getRest());

    r = p.parse(getSourceForString("25-12-10"));
    assertTrue(r.successful());
    assertEquals(newParseTree(expr,
          newParseTree(expr,
            newParseTree(expr,
              newParseTree(num, "2", "5")),
            "-",
            newParseTree(num, "1", "2")),
          "-",
          newParseTree(num, "1", "0")),
        r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("5-2-1xy"));
    assertTrue(r.successful());
    assertEquals(newParseTree(expr,
          newParseTree(expr,
            newParseTree(expr,
              newParseTree(num, "5")),
            "-",
            newParseTree(num, "2")),
          "-",
          newParseTree(num, "1")),
        r.getValue());
    assertEquals(getSourceForString("5-2-1xy").getStream().getRest().getRest().getRest().getRest().getRest(), r.getRest());
  }

  public void testRecursionDetection(Class<? extends Parser> clazz) {
    Grammar<String> g = getDirectLRGrammar();
    Parser<String> p = getParser(clazz, g);
    Rule<String> expr = g.getStartingRule();
    Rule<String> digit = new BasicRule<String>("digit", g);
    Rule<String> num = new BasicRule<String>("num", g);

    Result<String> r;

    r = p.parse(getSourceForString("12"));
    assertTrue(r.successful());
    assertEquals(newParseTree(expr, newParseTree(num, "1", "2")), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("12-10"));
    assertTrue(r.successful());
    assertEquals(newParseTree(expr, newParseTree(num, "1", "2")), r.getValue());
    assertEquals(getSourceForString("12-10").getStream().getRest().getRest(), r.getRest());
  }

  public static Grammar<String> getDirectLRGrammar() {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> num = ugf.orRule("num", ugf.plus(ugf.or("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")));
    Rule<String> expr = ugf.newRule("expr");
    ugf.setStartingRule(ugf.orRule(expr, ugf.seq(expr, "-", num), num));
    return ugf.getGrammar();
  }

  private static void testIndirectLR(Class<? extends Parser> clazz) {
    System.out.println("== Starting Indirect test ==");
    Grammar<String> g = getIndirectLRGrammar();
    Parser<String> p = getParser(clazz, g);
    Rule<String> x = new BasicRule<String>("x", g);
    Rule<String> expr = new BasicRule<String>("expr", g);
    Rule<String> num = new BasicRule<String>("num", g);
    Result<String> r;

    r = p.parse(getSourceForString("5-2-1"));
    assertTrue(r.successful());
    assertEquals(
        newParseTree(x,
          newParseTree(expr,
            newParseTree(x,
              newParseTree(expr,
                newParseTree(x,
                  newParseTree(expr,
                    newParseTree(num, "5"))),
                "-",
                newParseTree(num, "2"))),
            "-",
            newParseTree(num, "1"))),
        r.getValue());
    assertFalse(r.getRest().hasMore());
    //assertEquals(getSourceForString("5-2-1").getStream().getRest().getRest().getRest().getRest().getRest(), r.getRest());
    System.out.println("== Ending Indirect test ==");
  }

  public static Grammar<String> getIndirectLRGrammar() {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> expr = ugf.newRule("expr");
    Rule<String> x = ugf.seqRule("x", expr);
    Rule<String> num = ugf.orRule("num", ugf.plus(ugf.or("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")));
    ugf.setRule(expr, ugf.or(ugf.seq(x, "-", num), num));
    ugf.setStartingRule(x);
    return ugf.getGrammar();
  }

}
