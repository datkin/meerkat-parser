package meerkat.parser;

import meerkat.Stream;
import meerkat.grammar.Grammar;
import meerkat.grammar.Nonterminal;

public interface Parser<L> {
  public ParseNode<L> parse(Stream<L> s, Nonterminal<L> r);
  public Grammar<L> getGrammar();
}
