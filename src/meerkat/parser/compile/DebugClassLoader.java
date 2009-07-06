package meerkat.parser.compile;

import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

public class DebugClassLoader extends BytecodeClassLoader {
  public Class<?> loadClass(byte[] bytes) {
    ClassReader reader = new ClassReader(bytes);
    reader.accept(new CheckClassAdapter(new EmptyVisitor()), 0);
    return super.loadClass(bytes);
  }
}
