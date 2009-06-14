package meerkat.parser.compile;

import java.lang.reflect.*;

import org.junit.Test;

import meerkat.parser.ParserTester;

//import org.objectweb.asm.*;

import meerkat.grammar.Grammar;
import meerkat.grammar.Rule;
import meerkat.grammar.basic.UnsafeGrammarFactory;

public class TestGrammarCompiler {

  @Test
  public void debug() throws Exception {
    final String name = "meerkat.parser.compile.GrammarClass_Gen";
    UnsafeGrammarFactory<String> ugf = new UnsafeGrammarFactory<String>(String.class);
    ugf.setStartingRule(ugf.orRule("a", "b", "c"));
    final Grammar<String> expected = ParserTester.getSampleGrammar();
    byte [] b = GrammarCompilerTester.getGrammarClass(expected, name.replace('.', '/'));

    /*
    Class clazz = null;
    try {
      ClassLoader loader = ClassLoader.getSystemClassLoader();
      Class cls = Class.forName("java.lang.ClassLoader");
      java.lang.reflect.Method method =
        cls.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });

      method.setAccessible(true);
      try {
        Object[] args = new Object[] { name, b, new Integer(0), new Integer(b.length)};
        clazz = (Class) method.invoke(loader, args);
      } finally {
        method.setAccessible(false);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    */

    ClassLoader cl = new ClassLoader() {
      @Override
      protected Class findClass(String name) throws ClassNotFoundException {
        if (name.endsWith("_Gen")) {
          byte [] b = GrammarCompilerTester.getGrammarClass(expected, name.replace('.', '/'));
          return defineClass(name, b, 0, b.length);
        }
        return super.findClass(name);
      }
    };

    Class<?> clazz = cl.loadClass(name);

    Object o = clazz.newInstance();

    Method getGrammarMethod = clazz.getMethod("getGrammar");
    Object grammar = getGrammarMethod.invoke(null);
    System.out.println(grammar);
  }
}
