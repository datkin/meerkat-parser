package meerkat.parser.compile;

public class StringCompiler implements ObjectCompiler<String> {
  @Override
  public void writeToStack(String str, MethodWriter mw) {
    mw.visitLdcInsn(str);
  }
}
