package meerkat.parser.compile;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

// Represents a strategy for taking an instance of a class
// and recreating it on the stack
/* Emit the bytecode necessary to recreate a given object
 * on the operand stack of the given function */
public interface ObjectCompiler<T> {
  /* Post condition: the stack remains exactly the same
   * except for an object of type T added to the top
   */
  public void writeToStack(T t, MethodVisitor mv);
}
