package meerkat.parser.compile;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import static org.objectweb.asm.Opcodes.*;

import meerkat.grammar.*;
import meerkat.grammar.util.TerminalCollector;

public class GrammarCompilerTester {

  public static byte[] getGrammarClass(Grammar<String> grammar, String name) {
    Map<String, StackWriter> terminals = new HashMap<String, StackWriter>();
    Set<String> termSet = grammar.getStartingRule().accept(new TerminalCollector<String>(grammar));
    for (String s : termSet) {
      terminals.put(s, new StringStackWriter(s));
    }

    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS); // + ClassWriter.COMPUTE_FRAMES);
    TraceClassVisitor tcv = new TraceClassVisitor(cw, new java.io.PrintWriter(System.out));
    ClassVisitor cv = cw; // new CheckClassAdapter(tcv);
    FieldVisitor fv;
    MethodVisitor mv;

    cv.visit(V1_6, ACC_PUBLIC + ACC_SUPER, name, null, Type.getInternalName(Object.class), null);

    String grammarType = Type.getDescriptor(Grammar.class);
    fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "grammar", grammarType, null, null); // first null arg would be the generic signature, if any
    fv.visitEnd();

    mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    int maxLocal = new GrammarCompiler<String>(grammar).writeToStack(new MethodWriter(mv), terminals);
    mv.visitFieldInsn(PUTSTATIC, name, "grammar", grammarType);
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, "getGrammar", Type.getMethodDescriptor(Type.getType(Grammar.class), new Type[] {}), null, null);
    mv.visitCode();
    mv.visitFieldInsn(GETSTATIC, name, "grammar", grammarType);
    mv.visitInsn(ARETURN);
    //mv.visitMaxs(1, 0);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    cv.visitEnd();

    return cw.toByteArray();
  }
}
