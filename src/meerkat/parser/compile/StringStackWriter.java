package meerkat.parser.compile;

import org.objectweb.asm.MethodVisitor;

public class StringStackWriter implements StackWriter {
  private final String str;

  public StringStackWriter(String str) {
    if (str == null)
      throw new IllegalArgumentException();
    this.str = str;
  }

  @Override
  public void writeToStack(MethodVisitor mv) {
    mv.visitLdcInsn(this.str);
  }
}
