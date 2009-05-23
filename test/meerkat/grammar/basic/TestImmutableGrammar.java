package meerkat.grammar.basic;

import org.junit.Test;
import static org.junit.Assert.*;

import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;
import example.sexp.Token;

public class TestImmutableGrammar {

  @Test
  public void properPruning() {
    UnsafeGrammarFactory<Token> gf = new UnsafeGrammarFactory<Token>(Token.class);
    Rule<Token> expr = gf.newRule("Expr");
    gf.setStartingRule(
        gf.orRule(expr,
          gf.seqRule("Identifier", gf.plus(Token.Letter), gf.star(Token.Space)),
          gf.seq(
            gf.seqRule("Open", Token.Open, gf.star(Token.Space)),
            gf.star(expr),
            gf.seqRule("Close", gf.star(Token.Space), Token.Close))));
    Grammar<Token> g = new ImmutableGrammar<Token>(gf);
    System.out.println(g);
  }

  @Test
  public void arith() {
    UnsafeGrammarFactory<String> gf = new UnsafeGrammarFactory<String>(String.class);
    Rule<String> additive = gf.newRule("Additive");
    Rule<String> multitive = gf.newRule("Multitive");
    Rule<String> decimal = gf.orRule("Decimal", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    Rule<String> primary = gf.orRule("Primary", gf.seq("(", additive, ")"), decimal);
    gf.orRule("Multitive", gf.seq(primary, "*", multitive), primary);
    gf.setStartingRule(gf.orRule("Additive", gf.seq(multitive, "+", additive), multitive));
    Grammar<String> g = new ImmutableGrammar<String>(gf);
    System.out.println(g);
  }
}
