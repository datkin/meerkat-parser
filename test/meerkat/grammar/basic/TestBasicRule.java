package meerkat.grammar.basic;

import org.junit.Test;
import static org.junit.Assert.*;

import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;

public class TestBasicRule {
  @Test
  public void testEquality() {
    MutableGrammar<String> g = new MutableGrammar<String>();
    Rule<String> nt1 = new BasicRule<String>("Foo", g);
    g.addRule(nt1, null);
    assertTrue(nt1.equals(nt1));
    assertEquals("Foo", nt1.getName());
    assertEquals(g, nt1.getGrammar());

    Rule<String> nt3 = new BasicRule<String>("Foo", g);
    g.addRule(nt3, null); // this in a spurious test b/c nt1 is gonna get booted from the hash
    assertTrue(nt1.equals(nt3));
    assertTrue(nt3.equals(nt1));
  }
}
