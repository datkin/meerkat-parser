package meerkat.parser;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import org.junit.Test;
import static org.junit.Assert.*;

import meerkat.grammar.basic.UnsafeGrammarFactory;
import meerkat.grammar.basic.BasicRule;
import meerkat.grammar.*;

import meerkat.Source;
import meerkat.Stream;
import meerkat.basic.ListSource;
import meerkat.basic.BasicStream;

import meerkat.parser.basic.BasicParseLeaf;
import meerkat.parser.basic.BasicParseTree;

public class ParserTester {

  public static void testParser(ParserFactory parserFactory) {
    testSeq(parserFactory);
    testChoice(parserFactory);
    testOptional(parserFactory);
    testAnd(parserFactory);
    testNot(parserFactory);
    testOneOrMore(parserFactory);
    testZeroOrMore(parserFactory);
    testClass(parserFactory);
    testAnBnCn(parserFactory);
  }

  private static void testSeq(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.seqRule("start", "a", "b", "c"));
    Grammar<String> g = ugf.getGrammar();
    Parser<String> p = parserFactory.newParser(g, String.class);

    Result<String> r;

    r = p.parse(getSourceForString("abc"));
    ParseNode<String> expected = newParseTree(g.getStartingRule(), "a", "b", "c");

    assertTrue(r.successful());
    assertEquals(expected, r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("ab"));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("abcd"));
    assertTrue(r.successful());
    assertEquals(expected, r.getValue());
    assertEquals(getSourceForString("abcd").getStream().getRest().getRest().getRest(), r.getRest());
  }

  private static void testChoice(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.orRule("start", ugf.seq("a", "b", "c"), ugf.seq("a", "b")));
    Grammar<String> g = ugf.getGrammar();
    Parser<String> p = parserFactory.newParser(g, String.class);

    Result<String> r;

    r = p.parse(getSourceForString("ab"));
    assertTrue(r.successful());
    assertEquals(newParseTree(g.getStartingRule(), "a", "b"), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("abc"));
    assertTrue(r.successful());
    assertEquals(newParseTree(g.getStartingRule(), "a", "b", "c"), r.getValue());
    assertFalse(r.getRest().hasMore());

    // change the order
    ugf.setStartingRule(ugf.orRule("newStart", ugf.seq("a", "b"), ugf.seq("a", "b", "c")));
    g = ugf.getGrammar();
    p = parserFactory.newParser(g, String.class);

    r = p.parse(getSourceForString("ab"));
    assertTrue(r.successful());
    assertEquals(newParseTree(g.getStartingRule(), "a", "b"), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("abc"));
    assertTrue(r.successful());
    assertEquals(newParseTree(g.getStartingRule(), "a", "b"), r.getValue());
    assertEquals(getSourceForString("abc").getStream().getRest().getRest(), r.getRest());
  }

  private static void testOptional(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.optRule("start", "x"));
    Parser<String> p = parserFactory.newParser(ugf.getGrammar(), String.class);

    Result<String> r;

    r = p.parse(getSourceForString(""));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule()), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("x"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule(), "x"), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("a"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule()), r.getValue());
    assertEquals(getSourceForString("a").getStream(), r.getRest());

    r = p.parse(getSourceForString("xa"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule(), "x"), r.getValue());
    assertEquals(getSourceForString("xa").getStream().getRest(), r.getRest());
  }

  private static void testAnd(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.andRule("start", "x"));
    Parser<String> p = parserFactory.newParser(ugf.getGrammar(), String.class);

    Result<String> r;

    r = p.parse(getSourceForString("a"));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("x"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule()), r.getValue());
    assertEquals(getSourceForString("x").getStream(), r.getRest());
  }

  private static void testNot(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.notRule("start", "x"));
    Parser<String> p = parserFactory.newParser(ugf.getGrammar(), String.class);

    Result<String> r;

    r = p.parse(getSourceForString("x"));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("a"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule()), r.getValue());
    assertEquals(getSourceForString("a").getStream(), r.getRest());
  }

  private static void testOneOrMore(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.plusRule("start", "x"));
    Parser<String> p = parserFactory.newParser(ugf.getGrammar(), String.class);

    Result<String> r;

    r = p.parse(getSourceForString(""));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("axxx"));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("x"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule(), "x"), r.getValue());
    assertEquals(getSourceForString("x").getStream().getRest(), r.getRest());

    r = p.parse(getSourceForString("xx"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule(), "x", "x"), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("xxax"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule(), "x", "x"), r.getValue());
    assertEquals(getSourceForString("xxax").getStream().getRest().getRest(), r.getRest());
  }

  private static void testZeroOrMore(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.starRule("start", "x"));
    Parser<String> p = parserFactory.newParser(ugf.getGrammar(), String.class);

    Result<String> r;

    r = p.parse(getSourceForString("ax"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule()), r.getValue());
    assertEquals(getSourceForString("ax").getStream(), r.getRest());

    r = p.parse(getSourceForString(""));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule()), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("x"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule(), "x"), r.getValue());
    assertEquals(getSourceForString("x").getStream().getRest(), r.getRest());

    r = p.parse(getSourceForString("xx"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule(), "x", "x"), r.getValue());
    assertFalse(r.getRest().hasMore());
  }

  private static void testClass(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.seqRule("start", String.class));
    Parser<String> p = parserFactory.newParser(ugf.getGrammar(), String.class);

    Result<String> r;

    r = p.parse(getSourceForString(""));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("a"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule(), "a"), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("ab"));
    assertTrue(r.successful());
    assertEquals(newParseTree(p.getGrammar().getStartingRule(), "a"), r.getValue());
    assertEquals(getSourceForString("ab").getStream().getRest(), r.getRest());
  }

  private static void testAnBnCn(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> A = ugf.newRule("A");
    Rule<String> B = ugf.newRule("B");
    ugf.setStartingRule(ugf.seqRule("S", ugf.and(A, "c"), ugf.plus("a"), B, ugf.not(ugf.or("a", "b", "c"))));
    ugf.seqRule(A, "a", ugf.opt(A), "b");
    ugf.seqRule(B, "b", ugf.opt(B), "c");
    Grammar<String> g = ugf.getGrammar();
    Rule<String> gS = g.getStartingRule();
    Rule<String> gA = null;
    Rule<String> gB = null;
    for (Rule<String> r : g.getRules()) {
      if (r.getName().equals("A"))
        gA = r;
      else if (r.getName().equals("B"))
        gB = r;
    }
    Parser<String> p = parserFactory.newParser(g, String.class);

    Result<String> r;

    r = p.parse(getSourceForString(""));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("aabbccc"));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("abc"));
    assertTrue(r.successful());
    assertEquals(newParseTree(gS, "a", newParseTree(gB, "b", "c")), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("aabbcc"));
    assertTrue(r.successful());
    assertEquals(newParseTree(gS, "a", "a", newParseTree(gB, "b", newParseTree(gB, "b", "c"), "c")), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("aaabbccc"));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("aabbbccc"));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("aaaccc"));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("bbbccc"));
    assertFalse(r.successful());

    r = p.parse(getSourceForString("aabbcca"));
    assertFalse(r.successful());

    /*
    Source<String> source = getSourceForString("aaabbbccc");
    long startTime = System.nanoTime();
    r = p.parse(source);
    long time = System.nanoTime() - startTime;
    System.out.println("Processing time: " + time + " ns");
    */
  }

  public static void testCaching(ParserFactory parserFactory) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> seq = ugf.seqRule("seq", "a", "b", "c");
    ugf.setStartingRule(ugf.orRule("start", ugf.seq(seq, "x"), seq));
    Grammar<String> g = ugf.getGrammar();
    Parser<String> p = parserFactory.newParser(g, String.class);
    Rule<String> gStart = g.getStartingRule();
    Rule<String> gSeq = new BasicRule<String>("seq", g);

    Result<String> r;

    r = p.parse(getSourceForString("abc"));
    assertTrue(r.successful());
    assertEquals(newParseTree(gStart, newParseTree(gSeq, "a", "b", "c")), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("abcy"));
    assertTrue(r.successful());
    assertEquals(newParseTree(gStart, newParseTree(gSeq, "a", "b", "c")), r.getValue());
    assertEquals(getSourceForString("abcy").getStream().getRest().getRest().getRest(), r.getRest());

    r = p.parse(getSourceForString("abcx"));
    assertTrue(r.successful());
    assertEquals(newParseTree(gStart, newParseTree(gSeq, "a", "b", "c"), "x"), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("abcxy"));
    assertTrue(r.successful());
    assertEquals(newParseTree(gStart, newParseTree(gSeq, "a", "b", "c"), "x"), r.getValue());
    assertEquals(getSourceForString("abcxy").getStream().getRest().getRest().getRest().getRest(), r.getRest());

    ugf.setStartingRule(ugf.orRule("start2", ugf.seq(seq, "y"), ugf.seq(seq, "x")));
    g = ugf.getGrammar();
    p = parserFactory.newParser(g, String.class);
    gStart = g.getStartingRule();
    gSeq = new BasicRule<String>("seq", g);

    r = p.parse(getSourceForString("abcx"));
    assertTrue(r.successful());
    assertEquals(newParseTree(gStart, newParseTree(gSeq, "a", "b", "c"), "x"), r.getValue());
    assertFalse(r.getRest().hasMore());

    r = p.parse(getSourceForString("abcy"));
    assertTrue(r.successful());
    assertEquals(newParseTree(gStart, newParseTree(gSeq, "a", "b", "c"), "y"), r.getValue());
    assertFalse(r.getRest().hasMore());
  }

  @SuppressWarnings("unchecked")
  public static ParseNode<String> newParseTree(Rule<String> rule, Object... objs) {
    List<ParseNode<String>> nodes = new LinkedList<ParseNode<String>>();
    for (int i = 0; i < objs.length; i++) {
      if (objs[i] instanceof String) {
        nodes.add(new BasicParseLeaf<String>((String)objs[i]));
      } else if (objs[i] instanceof ParseNode) { // assume ParseNode<String> was found
        nodes.add((ParseNode<String>)objs[i]);
      } else {
        throw new RuntimeException("Unexpected parse tree member: " + objs[i]);
      }
    }
    return new BasicParseTree<String>(rule, nodes);
  }

  public static Source<String> getSource(String... string) {
    return new ListSource<String>(Arrays.asList(string));
  }

  public static Source<String> getSourceForString(String string) {
    char[] chars = string.toCharArray();
    String[] strings = new String[chars.length];
    for (int i = 0; i < chars.length; i++)
      strings[i] = new Character(chars[i]).toString();
    return getSource(strings);
  }

  public static Grammar<String> getSampleGrammar() {
    UnsafeGrammarFactory<String> gf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> additive = gf.newRule("Additive");
    Rule<String> multitive = gf.newRule("Multitive");
    Rule<String> number = gf.plusRule("Number", gf.or("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
    Rule<String> primary = gf.orRule("Primary", gf.seq("(", additive, ")"), number);
    gf.orRule("Multitive", gf.seq(primary, "*", multitive), primary);
    gf.setStartingRule(gf.orRule("Additive", gf.seq(multitive, "+", additive), multitive));
    return gf.getGrammar();
  }

  public static <T> Rule<T> getRule(Grammar<T> grammar, String name) {
    for (Rule<T> rule : grammar.getRules()) {
      if (rule.getName().equals(name))
        return rule;
    }
    return null;
  }
}
