package meerkat.grammar.util;

import org.junit.Test;
import static org.junit.Assert.*;
import static meerkat.parser.ParserTester.getSampleGrammar;

import java.util.Set;
import java.util.HashSet;

import meerkat.grammar.util.TerminalCollector;
import meerkat.grammar.Grammar;

public class TestTerminalCollector {

  @Test
  public void testSampleGrammar() {
    Grammar<String> g = getSampleGrammar();
    Set<String> terminals = g.getStartingRule().accept(new TerminalCollector<String>(g));

    Set<String> expectedTerminals = new HashSet<String>();
    expectedTerminals.add("0");
    expectedTerminals.add("1");
    expectedTerminals.add("2");
    expectedTerminals.add("3");
    expectedTerminals.add("4");
    expectedTerminals.add("5");
    expectedTerminals.add("6");
    expectedTerminals.add("7");
    expectedTerminals.add("8");
    expectedTerminals.add("9");
    expectedTerminals.add("+");
    expectedTerminals.add("*");
    expectedTerminals.add("(");
    expectedTerminals.add(")");

    assertEquals(expectedTerminals, terminals);
  }
}
