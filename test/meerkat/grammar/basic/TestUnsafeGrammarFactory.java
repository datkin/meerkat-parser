package meerkat.grammar.basic;

import org.junit.Test;
import static org.junit.Assert.*;

import meerkat.grammar.Rule;
import meerkat.grammar.util.GrammarToString;

public class TestUnsafeGrammarFactory {

  @Test
  public void sampleGrammar() {
    UnsafeGrammarFactory<String> gf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> expr = gf.newRule("Expr");
    Rule<String> value = gf.orRule("Value", gf.plus(String.class, gf.seq("(", expr, ")")));
    Rule<String> product = gf.seqRule("Product", value, gf.star(gf.or("*", "/"), value));
    Rule<String> sum = gf.seqRule("Sum", product, gf.star(gf.or("+", "-"), product));
    gf.setRule(expr, sum);
    gf.setStartingRule(expr);
    System.out.println(expr.accept(new GrammarToString<String>("'")));
  }
}
