package meerkat.parser;

import meerkat.Source;
import meerkat.Stream;
import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;

public interface Parser<L> {
  public Result<L> parse(Stream<L> s, Rule<L> r);
  public Result<L> parse(Source<L> s);
  public Grammar<L> getGrammar();
}
