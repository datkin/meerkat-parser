package meerkat.parser.compile;

import java.lang.reflect.*;

import org.junit.Test;

import meerkat.parser.ParserTester;

//import org.objectweb.asm.*;

import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;
import meerkat.grammar.basic.UnsafeGrammarFactory;
import meerkat.parser.Parser;

public class TestGrammarCompiler {

  @Test
  public void debug() throws Exception {
    final String name = "meerkat.parser.compile.GrammarClass";
    final Grammar<String> expected = ParserTester.getSampleGrammar();
    byte [] bytes = GrammarCompilerTester.getGrammarClass(expected, name.replace('.', '/'));

    Class<? extends Parser> clazz = new BytecodeClassLoader().loadClass(bytes, Parser.class);
    Parser p = clazz.newInstance();

    System.out.println(p.getGrammar());
    /*
    Method getGrammarMethod = clazz.getMethod("getGrammar");
    Object grammar = getGrammarMethod.invoke(o);
    System.out.println(grammar);
    */
  }
}
