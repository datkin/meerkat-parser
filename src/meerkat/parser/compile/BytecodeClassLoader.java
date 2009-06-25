package meerkat.parser.compile;

import org.objectweb.asm.ClassReader;

public class BytecodeClassLoader extends ClassLoader {

  //private final static int[] constantSizes =
    //new int[] {-1, -1, -1, 4, 4, 8, 8, 2, 2, 4, 4, 4, 4};

  public Class<?> loadClass(byte[] bytes) {
    ClassReader reader = new ClassReader(bytes);
    String internalName = reader.getClassName();
    //String name = internalName.substring(1).replace('/', '.');
    String name = internalName.replace('/', '.');
    return defineClass(name, bytes, 0, bytes.length);
  }

  @SuppressWarnings("unchecked")
  public <T> Class<? extends T> loadClass(byte[] bytes, Class<T> clazz) {
    Class<?> newClazz = loadClass(bytes);
    if (clazz.isAssignableFrom(newClazz)) {
      return (Class<? extends T>)newClazz;
    }
    throw new ClassCastException();
  }

  /*
  private static getClassName(byte[] bytes) {
    int constPoolCount = (bytes[8] << 8) && bytes[9];
    int offset = 10;

    for (int i = 0; i < constPoolCount; i++) {
      int shift = 1;
      if (bytes[offset] == 1) {
        shift += 2;
        shift += (bytes[offset+1] << 8) && bytes[offset+2];
      } else {
        shift += constantSizes[bytes[offset]];
      }
      offset += shift;
    }
    classIndex = 
  }
  */
}
