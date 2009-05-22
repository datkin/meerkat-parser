package meerkat.grammar.basic;

import org.junit.Test;
import static org.junit.Assert.*;

import meerkat.grammar.Grammar;
import meerkat.grammar.Nonterminal;

public class TestBasicNonterminal {
  @Test
  public void testEquality() {
    MutableGrammar<String> g = new MutableGrammar<String>();
    Nonterminal<String> nt1 = new BasicNonterminal<String>("Foo", g);
    g.addRule(nt1, null);
    assertTrue(nt1.equals(nt1));
    assertEquals("Foo", nt1.getName());
    assertEquals(g, nt1.getGrammar());

    Nonterminal<String> nt3 = new BasicNonterminal<String>("Foo", g);
    g.addRule(nt3, null); // this in a spurious test b/c nt1 is gonna get booted from the hash
    assertTrue(nt1.equals(nt3));
    assertTrue(nt3.equals(nt1));
  }
}
