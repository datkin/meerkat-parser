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
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;

import meerkat.grammar.*;
import meerkat.grammar.basic.BasicRule;
import meerkat.grammar.util.TerminalCollector;
import meerkat.parser.Parser;
import meerkat.parser.Result;
import meerkat.parser.ParseNode;
import meerkat.parser.basic.BasicResult;
import meerkat.parser.basic.BasicParseTree;
import meerkat.parser.FailureResult;
import meerkat.Source;
import meerkat.Stream;

public class BasicParserCompiler implements ParserCompiler {
  private static final String objectIntenalName = Type.getInternalName(Object.class);
  private static final String parserInternalName = Type.getInternalName(Parser.class);

  @Override
  // Name argument represents the internal name of the class
  public <T> byte[] writeClass(String name, Grammar<T> grammar, ObjectCompiler<T> terminalCompiler, Class<T> clazz) {
    String descriptor = "L" + name + ";";
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
    //TraceClassVisitor cv = new TraceClassVisitor(cw, new java.io.PrintWriter(System.out));
    ClassVisitor cv = cw;
    FieldVisitor fv;
    MethodWriter mw;

    // public class <name> implements Parser<clazz> {
    cv.visit(
        V1_6,
        ACC_PUBLIC + ACC_SUPER,
        name,
        getParserSignature(clazz),
        objectIntenalName,
        new String[] { parserInternalName }
        );

    // private static final Grammar<clazz> grammar;
    //System.out.println(getSingleArgType(Grammar.class, clazz));
    fv = cv.visitField(
        ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
        "grammar",
        Type.getDescriptor(Grammar.class),
        getSingleArgType(Grammar.class, clazz),
        null
        );
    fv.visitEnd();

    // private static final Rule<clazz>[] rules;
    fv = cv.visitField(
        ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
        "rules",
        "[" + Type.getDescriptor(Rule.class), // 1D array
        getSingleArgArrayType(Rule.class, clazz),
        null
        );
    fv.visitEnd();

    // private static final Result<T> failed;
    fv = cv.visitField(
        ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
        "failed",
        Type.getDescriptor(Result.class),
        getSingleArgType(Result.class, clazz),
        null
        );
    fv.visitEnd();

    // Store the terminal directly, do not wrap it in a BasicTerminal object?
    // private static final Clazz[] terminals;
    //System.out.println(getSingleArgArrayType(Terminal.class, clazz));
    fv = cv.visitField(
        ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
        "terminals",
        "[" + Type.getDescriptor(clazz),
        null, //getSingleArgArrayType(Terminal.class, clazz),
        null
        );
    fv.visitEnd();

    // private static void <clinit>()
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

    // Initialize "failed"
    mw.visitTypeInsn(NEW, FailureResult.class);
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL, FailureResult.class, "<init>", Type.VOID_TYPE);
    mw.visitFieldInsn(PUTSTATIC, name, "failed", Type.getDescriptor(Result.class));

    // Push grammar to stack and store to grammar
    new GrammarCompiler<T>(terminalCompiler).writeToStack(grammar, mw);
    mw.visitFieldInsn(PUTSTATIC, name, "grammar", Type.getDescriptor(Grammar.class));

    // Go through each rule and store a copy to rules
    List<Rule<T>> rules = new LinkedList<Rule<T>>();
    for (Rule<T> rule : grammar.getRules()) {
      rules.add(rule);
    }
    mw.visitLdcInsn(rules.size()); // TODO: some of those can be optimized to ICONST_x, modify MethodWriter to do this?
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

    // Go through each terminal and store a copy to terminals
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

    // TODO: add Override annotation?
    // public name() // parser default constructor
    mw = new MethodWriter(cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null));
    mw.visitCode();
    mw.visitVarInsn(ALOAD, 0);
    mw.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V");
    mw.visitInsn(RETURN);
    mw.visitMaxs(0, 0);
    mw.visitEnd();

    // public Grammar<clazz> getGrammar()
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

    // public Result<clazz> parse(Source<clazz>)
    mw = new MethodWriter(
        cv.visitMethod(
          ACC_PUBLIC,
          "parse",
          Type.getMethodDescriptor(Type.getType(Result.class), new Type[] {Type.getType(Source.class)}),
          getMethodSignature(clazz, Result.class, Source.class),
          null
          )
        );
    mw.visitCode();
    mw.visitVarInsn(ALOAD, 0);
    mw.visitVarInsn(ALOAD, 1);
    // TODO: difference between INVOKEINTERFACE and INVOKEVIRTUAL?
    mw.visitMethodInsn(INVOKEINTERFACE, Source.class, "getStream", Stream.class);
    // the API's version takes internal names, but if we pass Strings to Type.getType, they need to be descriptors
    mw.visitMethodInsn(INVOKEVIRTUAL, descriptor, "parse" + grammar.getStartingRule().getName(), Result.class, Stream.class);
    mw.visitInsn(ARETURN);
    mw.visitMaxs(0, 0);
    mw.visitEnd();

    // public Result<clazz> parse(Stream<clazz>, Rule<clazz>)
    mw = new MethodWriter(
        cv.visitMethod(
          ACC_PUBLIC,
          "parse",
          Type.getMethodDescriptor(Type.getType(Result.class), new Type[] {Type.getType(Rule.class), Type.getType(Stream.class)}),
          getMethodSignature(clazz, Result.class, Rule.class, Stream.class),
          null
          )
        );
    mw.visitCode();
    for (int i = 0; i < rules.size(); i++) {
      mw.visitFieldInsn(GETSTATIC, name, "rules", "[" + Type.getDescriptor(Rule.class));
      mw.visitLdcInsn(i);
      mw.visitInsn(AALOAD);
      mw.visitVarInsn(ALOAD, 2);
      mw.visitMethodInsn(INVOKEVIRTUAL, Object.class, "equals", "Z", Object.class);
      Label label = new Label();
      // ifeq, stay here, otherwise jump to label
      mw.visitJumpInsn(IFEQ, label);
      mw.visitVarInsn(ALOAD, 0);
      mw.visitVarInsn(ALOAD, 1);
      mw.visitMethodInsn(INVOKEVIRTUAL, descriptor, "parse" + rules.get(i).getName(), Result.class, Stream.class);
      mw.visitInsn(ARETURN);
      mw.visitLabel(label);
    }
    mw.visitTypeInsn(NEW, IllegalArgumentException.class);
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL, IllegalArgumentException.class, "<init>", Type.VOID_TYPE);
    mw.visitInsn(ATHROW);
    mw.visitMaxs(0, 0);
    mw.visitEnd();

    for (Rule<T> rule : rules) {
      mw = new MethodWriter(
          cv.visitMethod(
            ACC_PRIVATE,
            "parse" + rule.getName(),
            Type.getMethodDescriptor(Type.getType(Result.class), new Type[] { Type.getType(Stream.class) }),
            getMethodSignature(clazz, Result.class, Stream.class),
            null
            )
          );
      mw.visitCode();
      mw.visitTypeInsn(NEW, SpliceList.class);
      mw.visitInsn(DUP);
      mw.visitMethodInsn(INVOKESPECIAL, SpliceList.class, "<init>", Type.VOID_TYPE);
      mw.visitVarInsn(ASTORE, 2); // first free variable?
      // we'll just use var 1 as the stream register?
      //mw.visitVarInsn(ALOAD, 1); // push the stream to the stack to setup the stack discipline
      Label failureLabel = new Label();
      grammar.getExpr(rule).accept(new GrammarMethodEmitter<T>(name, mw, terminals, clazz, failureLabel));
      mw.visitTypeInsn(NEW, BasicResult.class);
      mw.visitInsn(DUP);
      mw.visitTypeInsn(NEW, BasicParseTree.class);
      mw.visitInsn(DUP);
      mw.visitFieldInsn(GETSTATIC, name, "rules", "[" + Type.getDescriptor(Rule.class));
      int ruleIndex = rules.indexOf(rule);
      if (ruleIndex < 0)
        throw new RuntimeException("Could not find rule " + rule);
      mw.visitLdcInsn(ruleIndex);
      mw.visitInsn(AALOAD);
      mw.visitVarInsn(ALOAD, 2); // resultRegister
      mw.visitMethodInsn(INVOKESPECIAL, BasicParseTree.class, "<init>", Type.VOID_TYPE, Rule.class, SpliceList.class);
      mw.visitVarInsn(ALOAD, 1); // streamRegister
      mw.visitMethodInsn(INVOKESPECIAL, BasicResult.class, "<init>", Type.VOID_TYPE, ParseNode.class, Stream.class);
      mw.visitInsn(ARETURN);
      mw.visitLabel(failureLabel);
      mw.visitFieldInsn(GETSTATIC, name, "failed", Type.getDescriptor(Result.class));
      mw.visitInsn(ARETURN);
      mw.visitMaxs(0, 0);
      mw.visitEnd();
    }

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

    // do "recursive" calls with a trampoline to keep the stack down?
    // need to track all intermediate streams regardless... make trampolining a build option?
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
