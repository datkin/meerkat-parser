package meerkat.parser.compile;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class MethodWriter extends MethodAdapter {
  private int nextLocal = 0;

  public MethodWriter(MethodVisitor delegate) {
    super(delegate);
  }

  public void visitFieldInsn(int opcode, Class<?> owner, String name, Class<?> clazz) {
    this.visitFieldInsn(opcode, Type.getInternalName(owner), name, Type.getDescriptor(clazz));
  }

  public void visitMethodInsn(int opcode, Object owner, String name, Object returnDesc, Object... parameters) {
    Type ownerType = objectToType(owner);
    Type returnType = objectToType(returnDesc);
    Type[] paramTypes = new Type[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      paramTypes[i] = objectToType(parameters[i]);
    }
    this.visitMethodInsn(opcode, ownerType.getInternalName(), name, Type.getMethodDescriptor(returnType, paramTypes));
  }

  private static Type objectToType(Object obj) {
    if (obj instanceof Class) {
      return Type.getType((Class)obj);
    } else if (obj instanceof Type) {
      return (Type)obj;
    }
    return Type.getType(obj.toString());
  }

  public void visitTypeInsn(int opcode, Class<?> clazz) {
    this.visitTypeInsn(opcode, Type.getInternalName(clazz));
  }

  public int getNextLocal() {
    return this.nextLocal;
  }

  public void addLocal() {
    this.nextLocal++;
  }
}
