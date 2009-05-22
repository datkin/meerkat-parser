package meerkat.parser;

import meerkat.Stream;
import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;

public interface Parser<L> {
  public ParseNode<L> parse(Stream<L> s, Rule<L> r);
  public Grammar<L> getGrammar();
}
