package meerkat.parser.compile;

// Represents a strategy for taking an instance of a class
// and recreating it on the stack
/* Emit the bytecode necessary to recreate a given object
 * on the operand stack of the given function */
public interface ObjectCompiler<T> {
  /* Post condition: the stack remains exactly the same
   * except for an object of type T added to the top
   */
  public void writeToStack(T t, MethodWriter mw); // do we want this to be a methodvisitor?
  //public ObjectWriter getObjectWriter(T t);
}
