package meerkat.grammar.basic;

import org.junit.Test;
import static org.junit.Assert.*;

//import meerkat.grammar.Rule;
import meerkat.grammar.Rule;

public class TestGrammarFactory {
  @Test
  public void testNewRule() {
    GrammarFactory<String> gf = new GrammarFactory<String>();
    Rule<String> nt1 = gf.newRule("Foo");
    assertTrue(nt1.equals(nt1));
    assertEquals(gf, nt1.getGrammar());
    assertEquals("Foo", nt1.getName());
    Rule<String> nt2 = gf.newRule("Foo");
    assertTrue(nt2.equals(nt2));
    assertEquals(gf, nt2.getGrammar());
    assertEquals("Foo", nt2.getName());
    assertTrue(nt1.equals(nt2));
    assertTrue(nt2.equals(nt1));
  }
}
