package meerkat.parser;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import org.junit.Test;
import static org.junit.Assert.*;

import meerkat.grammar.basic.UnsafeGrammarFactory;
import meerkat.grammar.*;

import meerkat.Source;
import meerkat.Stream;
import meerkat.basic.ListSource;
import meerkat.basic.BasicStream;

public class ParserTester {

  private static Grammar<String> grammar = getGrammar();

  public static void testParser(Class<? extends Parser> parserClazz) {
    testSeq(parserClazz);
  }

  private static void testSeq(Class<? extends Parser> clazz) {
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.seqRule("start", "a", "b", "c"));
    Grammar<String> g = ugf.getGrammar();
    Parser<String> p = getParser(clazz, g);

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

  @SuppressWarnings("unchecked")
  private static ParseNode<String> newParseTree(Rule<String> rule, Object... objs) {
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

  @SuppressWarnings("unchecked")
  private static Parser<String> getParser(Class<? extends Parser> clazz, Grammar<String> grammar) {
    try {
      Constructor<? extends Parser> c = clazz.getConstructor(Grammar.class);
      return (Parser<String>)c.newInstance(grammar);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
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

  public static Grammar<String> getGrammar() {
    UnsafeGrammarFactory<String> gf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> additive = gf.newRule("Additive");
    Rule<String> multitive = gf.newRule("Multitive");
    Rule<String> decimal = gf.orRule("Decimal", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    Rule<String> primary = gf.orRule("Primary", gf.seq("(", additive, ")"), decimal);
    gf.orRule("Multitive", gf.seq(primary, "*", multitive), primary);
    gf.setStartingRule(gf.orRule("Additive", gf.seq(multitive, "+", additive), multitive));
    return gf.getGrammar();
  }
}
