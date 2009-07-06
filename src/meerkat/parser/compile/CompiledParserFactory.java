package meerkat.parser.compile;

import meerkat.grammar.Grammar;
import meerkat.parser.Parser;
import meerkat.parser.ParserFactory;

public class CompiledParserFactory implements ParserFactory {
  private static final String basename = "meerkat/parser/compile/CompiledParser";
  private final ParserCompiler compiler = new BasicParserCompiler();
  private final BytecodeClassLoader classLoader = new DebugClassLoader(); // TODO: this is not doing any good
  private static int count = 0;

  @Override
  @SuppressWarnings("unchecked")
  public <T> Parser<T> newParser(Grammar<T> grammar, Class<T> clazz) {
    if (!String.class.equals(clazz))
      throw new UnsupportedOperationException("Cannot compile Parser for non-String streams");
    String name = basename + String.valueOf(count++);
    byte[] bytes = compiler.writeClass(name, grammar, (ObjectCompiler<T>)new StringCompiler(), clazz);
    Class<? extends Parser> parserClazz = classLoader.loadClass(bytes, Parser.class);
    try {
      return (Parser<T>)parserClazz.newInstance();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
