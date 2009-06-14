package meerkat.parser.compile;

import org.objectweb.asm.MethodVisitor;

public interface StackWriter {
  public void writeToStack(MethodVisitor mv);
}
