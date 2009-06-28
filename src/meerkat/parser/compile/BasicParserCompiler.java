package meerkat.parser.compile;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;

import meerkat.grammar.*;
import meerkat.grammar.basic.BasicRule;
import meerkat.grammar.util.TerminalCollector;
import meerkat.parser.Parser;
import meerkat.parser.Result;
import meerkat.Source;
import meerkat.Stream;

public class BasicParserCompiler implements ParserCompiler {
  private static final String objectIntenalName = Type.getInternalName(Object.class);
  private static final String parserInternalName = Type.getInternalName(Parser.class);

  @Override
  public <T> byte[] writeClass(String name, Grammar<T> grammar, ObjectCompiler<T> terminalCompiler, Class<T> clazz) {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
    TraceClassVisitor cv = new TraceClassVisitor(cw, new java.io.PrintWriter(System.out));
    FieldVisitor fv;
    MethodWriter mw;

    cv.visit(
        V1_6,
        ACC_PUBLIC + ACC_SUPER,
        name,
        getParserSignature(clazz),
        objectIntenalName,
        new String[] { parserInternalName }
        );

    System.out.println(getSingleArgType(Grammar.class, clazz));
    fv = cv.visitField(
        ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
        "grammar",
        Type.getDescriptor(Grammar.class),
        getSingleArgType(Grammar.class, clazz),
        null
        );
    fv.visitEnd();

    fv = cv.visitField(
        ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
        "rules",
        "[" + Type.getDescriptor(Rule.class), // 1D array
        getSingleArgArrayType(Rule.class, clazz),
        null
        );
    fv.visitEnd();

    // Store the terminal directly, do not wrap it in a BasicTerminal object?
    System.out.println(getSingleArgArrayType(Terminal.class, clazz));
    fv = cv.visitField(
        ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
        "terminals",
        "[" + Type.getDescriptor(clazz),
        null, //getSingleArgArrayType(Terminal.class, clazz),
        null
        );
    fv.visitEnd();

    // add Rule[] field
    // add Terminal[] field

    mw = new MethodWriter(
        cv.visitMethod(
          ACC_STATIC,
          "<clinit>",
          "()V",
          null,
          null
          )
        );
    mw.visitCode();
    new GrammarCompiler<T>(terminalCompiler).writeToStack(grammar, mw);
    mw.visitFieldInsn(PUTSTATIC, name, "grammar", Type.getDescriptor(Grammar.class));

    List<Rule<T>> rules = new LinkedList<Rule<T>>();
    for (Rule<T> rule : grammar.getRules()) {
      rules.add(rule);
    }
    mw.visitLdcInsn(rules.size());
    mw.visitTypeInsn(ANEWARRAY, Rule.class);
    mw.visitFieldInsn(PUTSTATIC, name, "rules", "[" + Type.getDescriptor(Rule.class));
    for (int i = 0; i < rules.size(); i++) {
      mw.visitFieldInsn(GETSTATIC, name, "rules", "[" + Type.getDescriptor(Rule.class));
      mw.visitLdcInsn(i);
      mw.visitTypeInsn(NEW, BasicRule.class);
      mw.visitInsn(DUP);
      mw.visitLdcInsn(rules.get(i).getName());
      mw.visitFieldInsn(GETSTATIC, name, "grammar", Type.getDescriptor(Grammar.class));
      mw.visitMethodInsn(INVOKESPECIAL, BasicRule.class, "<init>", Type.VOID_TYPE, String.class, Grammar.class);
      mw.visitInsn(AASTORE);
    }

    List<T> terminals = new LinkedList<T>();
    for (T t : grammar.getStartingRule().accept(new TerminalCollector<T>(grammar))) {
      terminals.add(t);
    }
    mw.visitLdcInsn(terminals.size());
    mw.visitTypeInsn(ANEWARRAY, clazz);
    mw.visitFieldInsn(PUTSTATIC, name, "terminals", "[" + Type.getDescriptor(clazz));
    for (int i = 0; i < terminals.size(); i++) {
      mw.visitFieldInsn(GETSTATIC, name, "terminals", "[" + Type.getDescriptor(clazz));
      mw.visitLdcInsn(i);
      terminalCompiler.writeToStack(terminals.get(i), mw);
      mw.visitInsn(AASTORE);
    }

    mw.visitInsn(RETURN);
    mw.visitMaxs(0, 0);
    mw.visitEnd();

    mw = new MethodWriter(cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null));
    mw.visitCode();
    mw.visitVarInsn(ALOAD, 0);
    mw.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V");
    mw.visitInsn(RETURN);
    mw.visitMaxs(0, 0);
    mw.visitEnd();

    mw = new MethodWriter(
        cv.visitMethod(
          ACC_PUBLIC,
          "getGrammar",
          Type.getMethodDescriptor(Type.getType(Grammar.class), new Type[] {}),
          getMethodSignature(clazz, Grammar.class),
          null
          )
        );
    mw.visitCode();
    mw.visitFieldInsn(GETSTATIC, name, "grammar", Type.getDescriptor(Grammar.class));
    mw.visitInsn(ARETURN);
    mw.visitMaxs(0, 0);
    mw.visitEnd();
    /*
    mw = new MethodWriter(
        cv.visitMethod(
          ACC_PUBLIC,
          "parse",
          Type.getMethodDescriptor(Type.getType(Result.class), new Type[] { Type.getType(Source.class) }),
          getParseSignature(Source.class, clazz),
          null
          )
        );
        */


    cv.visitEnd();

    // new classwriter
    // write grammar to static field with <clinit>
    // write rules to static array with <clinit>
    // write terms to static array with <clinit>
    // write parse loop by traversing rules
    // write rule parser by visiting each rule
    //  (to "recursive" calls with a trampoline to keep the stack down?)
    //  need to track all intermediate streams regardless... make trampolining a build option?
    return cw.toByteArray();
  }

  private static String getParserSignature(Class<?> clazz) {
    // NOTE: sw follows the Class Signature grammar, which does *not* call visitEnd at the
    // end of the signature. the CS grammar involes a visit to the super class and all
    // implemented interfaces using the Type Signature grammar.
    SignatureWriter sw = new SignatureWriter();
    {
      SignatureVisitor sv = sw.visitSuperclass();
      sv.visitClassType(objectIntenalName);
      sv.visitEnd();
    }
    {
      SignatureVisitor sv = sw.visitInterface();
      sv.visitClassType(parserInternalName);
      withArgument(sv, clazz);
      sv.visitEnd();
    }
    //System.out.println(sw);
    return sw.toString();
  }

  // Assumes every parameter and the return type is paramterized by class
  private static String getMethodSignature(Class<?> clazz, Class<?> returnClazz, Class... parameters) {
    SignatureWriter sw = new SignatureWriter();
    {
      SignatureVisitor sv;
      for (int i = 0; i < parameters.length; i++) {
        sv = sw.visitParameterType();
        sv.visitClassType(Type.getInternalName(parameters[i]));
        withArgument(sv, clazz);
        sv.visitEnd();
      }
      sv = sw.visitReturnType();
      sv.visitClassType(Type.getInternalName(returnClazz));
      withArgument(sv, clazz);
      sv.visitEnd();
    }
    //System.out.println(sw);
    return sw.toString();
  }

  private static String getSingleArgType(Class<?> clazz, Class<?> arg) {
    SignatureWriter sw = new SignatureWriter();
    sw.visitClassType(Type.getInternalName(clazz));
    withArgument(sw, arg);
    sw.visitEnd();
    return sw.toString();
  }

  private static String getSingleArgArrayType(Class<?> clazz, Class<?> arg) {
    SignatureWriter sw = new SignatureWriter();
    SignatureVisitor sv = sw.visitArrayType();
    sv.visitClassType(Type.getInternalName(clazz));
    withArgument(sv, arg);
    sv.visitEnd();
    return sw.toString();
  }

  private static void withArgument(SignatureVisitor sv, Class<?> argument) {
    SignatureVisitor argSv = sv.visitTypeArgument(SignatureVisitor.INSTANCEOF);
    argSv.visitClassType(Type.getInternalName(argument));
    argSv.visitEnd();
  }
}
