package meerkat.parser.compile;

import meerkat.grammar.Grammar;

public interface ParserCompiler {
  public <T> byte[] writeClass(String name, Grammar<T> grammar, ObjectCompiler<T> terminalCompiler, Class<T> clazz);
}
