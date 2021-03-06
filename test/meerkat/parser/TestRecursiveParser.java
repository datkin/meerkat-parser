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
    ParserFactory parserFactory = new ParserFactory() {
      @Override
      public <T> Parser<T> newParser(Grammar<T> grammar, Class<T> clazz) {
        return new RecursiveParser<T>(grammar);
      }
    };
    //testRecursionDetection(RecursiveParser.class);
    testDirectLR(parserFactory);
    testIndirectLR(parserFactory);
    //testJavaGrammar(RecursiveParser.class);
    ParserTester.testParser(parserFactory);
    ParserTester.testCaching(parserFactory);
  }

  public void testDirectLR(ParserFactory parserFactory) {
    Grammar<String> g = getDirectLRGrammar();
    Parser<String> p = parserFactory.newParser(g, String.class);
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

  public void testRecursionDetection(ParserFactory parserFactory) {
    Grammar<String> g = getDirectLRGrammar();
    Parser<String> p = parserFactory.newParser(g, String.class);
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

  private static void testIndirectLR(ParserFactory parserFactory) {
    Grammar<String> g = getIndirectLRGrammar();
    Parser<String> p = parserFactory.newParser(g, String.class);
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
  }

  public static Grammar<String> getIndirectLRGrammar() {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> expr = ugf.newRule("expr");
    Rule<String> x = ugf.seqRule("x", expr);
    Rule<String> num = ugf.plusRule("num", ugf.or("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"));
    ugf.setRule(expr, ugf.or(ugf.seq(x, "-", num), num));
    ugf.setStartingRule(x);
    return ugf.getGrammar();
  }

  private static void testJavaGrammar(ParserFactory parserFactory) {
    Grammar<String> g = getJavaGrammar();
    Parser<String> p = parserFactory.newParser(g, String.class);

    Result<String> r;

    r = p.parse(getSource("this", ".", "x", ".", "m", "(", ")"));
    System.out.println(r.getValue());

    r = p.parse(getSource("x", "[", "i", "]", "[", "j", "]", ".", "y"));
    System.out.println(r.getValue());
  }

  public static Grammar<String> getJavaGrammar() {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> expr = ugf.orRule("Expr", "i", "j");
    Rule<String> id = ugf.newRule("Id");
    Rule<String> exprName = ugf.seqRule("ExprName", id);
    Rule<String> methodName = ugf.orRule("MethodName", "m", "n");
    Rule<String> type = ugf.newRule("Type");
    ugf.orRule(id, "x", "y", type, methodName);
    Rule<String> interfaceName = ugf.orRule("InterfaceName", "I", "J");
    Rule<String> className = ugf.orRule("ClassName", "C", "D");
    ugf.orRule(type, className, interfaceName);
    Rule<String> primary = ugf.newRule("Primary");
    Rule<String> arrayAccess = ugf.orRule("ArrayAccess", ugf.seq(primary, "[", expr, "]"), ugf.seq(exprName, "[", expr, "]"));
    Rule<String> fieldAccess = ugf.orRule("FieldAccess", ugf.seq(primary, ".", id), ugf.seq("super", ".", id));
    Rule<String> methodInvocation = ugf.orRule("MethodInvocation", ugf.seq(primary, ".", id, "(", ")"), ugf.seq(id, "(", ")"));
    ugf.setStartingRule(ugf.orRule(primary, methodInvocation, fieldAccess, arrayAccess, "this"));
    return ugf.getGrammar();
  }

}
