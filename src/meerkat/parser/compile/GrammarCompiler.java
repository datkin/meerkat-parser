package meerkat.parser.compile;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;

import meerkat.grammar.*;
import meerkat.grammar.basic.*;
import meerkat.grammar.basic.GrammarFactory;

/* TODO: the "<type>CtorSig" variables are really constructor descriptors, not signatures
 * (signatures are for generics, at least in ASM */

/* This class visits a grammar, and emits the bytecode to create
 * a static copy of this grammar at class initialization time */
public class GrammarCompiler<T> {
  private final Grammar<T> grammar;

  public GrammarCompiler(Grammar<T> grammar) {
    if (grammar == null)
      throw new IllegalArgumentException();
    this.grammar = grammar;
  }

  // takes the maximum used local variable upon entry
  // and returns the number of local variables added
  public int writeToStack(MethodVisitor mv, int max, Map<T, StackWriter> terminals) {
    // Create a local array of rules and store them
    List<Rule<T>> rules = new LinkedList<Rule<T>>();
    for (Rule<T> rule : grammar.getRules())
      rules.add(rule);

    String ruleType = Type.getInternalName(Rule.class);
    mv.visitLdcInsn(rules.size());
    mv.visitTypeInsn(ANEWARRAY, ruleType);
    mv.visitVarInsn(ASTORE, max); // store the new rules array in local variable |max|

    String grammarFactory = Type.getInternalName(GrammarFactory.class);
    mv.visitTypeInsn(NEW, grammarFactory);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, grammarFactory, "<init>", "()V");

    String newRuleType = Type.getMethodDescriptor(Type.getType(Rule.class), new Type[] { Type.getType(String.class) });
    for (int i = 0; i < rules.size(); i++) {
      mv.visitInsn(DUP); // dupe the grammar factory
      mv.visitVarInsn(ALOAD, max);
      mv.visitLdcInsn(i);
      mv.visitInsn(DUP2_X1); // copy the array and index below the factory
      mv.visitInsn(POP2);
      mv.visitLdcInsn(rules.get(i).getName());
      mv.visitMethodInsn(INVOKEVIRTUAL, grammarFactory, "newRule", newRuleType);
      mv.visitInsn(AASTORE);
    }

    String setRuleType = Type.getMethodDescriptor(Type.getType(Rule.class), new Type[] { Type.getType(Rule.class), Type.getType(Expr.class) });
    GrammarEmitter<T> ge = new GrammarEmitter<T>(mv, max, terminals, rules);
    for (Rule<T> r : rules) {
      mv.visitInsn(DUP);
      mv.visitVarInsn(ALOAD, max);
      mv.visitLdcInsn(rules.indexOf(r));
      mv.visitInsn(AALOAD);
      grammar.getExpr(r).accept(ge);
      mv.visitMethodInsn(INVOKEVIRTUAL, grammarFactory, "setRule", setRuleType);
      if (r.equals(grammar.getStartingRule())) {
        mv.visitInsn(SWAP);
        mv.visitInsn(DUP_X1);
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKEVIRTUAL, grammarFactory, "setStartingRule", Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Rule.class) }));
      } else {
        mv.visitInsn(POP);
      }
    }

    mv.visitMethodInsn(INVOKEVIRTUAL, grammarFactory, "getGrammar", Type.getMethodDescriptor(Type.getType(Grammar.class), new Type[] {}));

    return 1;
  }

}

/* Leave an Expr on the stack */
class GrammarEmitter<T> implements GrammarVisitor<T, Void> {
  private final static String exprCtorSig = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Expr.class) });

  private final MethodVisitor mv;
  private final int rulesVar;
  private final Map<T, StackWriter> terminals;
  private final List<Rule<T>> rules;

  public GrammarEmitter(MethodVisitor mv, int rulesVar, Map<T, StackWriter> terminals, List<Rule<T>> rules) {
    if (mv == null || terminals == null || rules == null)
      throw new IllegalArgumentException();
    this.mv = mv;
    this.rulesVar = rulesVar;
    this.terminals = terminals;
    this.rules = rules;
  }

  @Override
  public Void visit(Rule<T> rule) {
    mv.visitVarInsn(ALOAD, rulesVar);
    mv.visitLdcInsn(rules.indexOf(rule));
    mv.visitInsn(AALOAD);
    return null;
  }

  @Override
  public Void visit(Sequence<T> sequence) {
    String seqType = Type.getInternalName(BasicSequence.class);
    // 0. Create a seq object
    mv.visitTypeInsn(NEW, seqType);
    mv.visitInsn(DUP);

    String llType = Type.getInternalName(LinkedList.class);
    // 1. create a linked list
    mv.visitTypeInsn(NEW, llType);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, llType, "<init>", "()V");

    String llAddSig = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] { Type.getType(Object.class) });
    // 2. create each sub expr and add it to the list
    for (Expr<T> expr : sequence.getExprs()) {
      mv.visitInsn(DUP);
      expr.accept(this); // e.g. push the expr to the stack
      mv.visitMethodInsn(INVOKEVIRTUAL, llType, "add", llAddSig);
      mv.visitInsn(POP);
    }

    String seqCtorSig = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(List.class) });
    // 3. pass the linked list to a seq constructor
    mv.visitMethodInsn(INVOKESPECIAL, seqType, "<init>", seqCtorSig);

    return null;
  }

  @Override
  public Void visit(Choice<T> choice) {
    String orType = Type.getInternalName(BasicChoice.class);
    // 0. Create a choice object
    mv.visitTypeInsn(NEW, orType);
    mv.visitInsn(DUP);

    String llType = Type.getInternalName(LinkedList.class);
    // 1. create a linked list
    mv.visitTypeInsn(NEW, llType);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, llType, "<init>", "()V");

    String llAddSig = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] { Type.getType(Object.class) });
    // 2. create each sub expr and add it to the list
    for (Expr<T> expr : choice.getExprs()) {
      mv.visitInsn(DUP);
      expr.accept(this); // e.g. push the expr to the stack
      mv.visitMethodInsn(INVOKEVIRTUAL, llType, "add", llAddSig);
      mv.visitInsn(POP);
    }

    String orCtorSig = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(List.class) });
    // 3. pass the linked list to a choice constructor
    mv.visitMethodInsn(INVOKESPECIAL, orType, "<init>", orCtorSig);
    return null;
  }

  @Override
  public Void visit(Optional<T> optional) {
    String optionalType = Type.getInternalName(BasicOptional.class);
    mv.visitTypeInsn(NEW, optionalType);
    mv.visitInsn(DUP);
    optional.getExpr().accept(this);
    mv.visitMethodInsn(INVOKESPECIAL, optionalType, "<init>", exprCtorSig);
    return null;
  }

  @Override
  public Void visit(And<T> and) {
    String andType = Type.getInternalName(BasicAnd.class);
    mv.visitTypeInsn(NEW, andType);
    mv.visitInsn(DUP);
    and.getExpr().accept(this);
    mv.visitMethodInsn(INVOKESPECIAL, andType, "<init>", exprCtorSig);
    return null;
  }

  @Override
  public Void visit(Not<T> not) {
    String notType = Type.getInternalName(BasicNot.class);
    mv.visitTypeInsn(NEW, notType);
    mv.visitInsn(DUP);
    not.getExpr().accept(this);
    mv.visitMethodInsn(INVOKESPECIAL, notType, "<init>", exprCtorSig);
    return null;
  }

  @Override
  public Void visit(ZeroOrMore<T> zom) {
    String zomType = Type.getInternalName(BasicZeroOrMore.class);
    mv.visitTypeInsn(NEW, zomType);
    mv.visitInsn(DUP);
    zom.getExpr().accept(this);
    mv.visitMethodInsn(INVOKESPECIAL, zomType, "<init>", exprCtorSig);
    return null;
  }

  @Override
  public Void visit(OneOrMore<T> oom) {
    String oomType = Type.getInternalName(BasicOneOrMore.class);
    mv.visitTypeInsn(NEW, oomType);
    mv.visitInsn(DUP);
    oom.getExpr().accept(this);
    mv.visitMethodInsn(INVOKESPECIAL, oomType, "<init>", exprCtorSig);
    return null;
  }

  @Override
  public Void visit(T t) {
    String termType = Type.getInternalName(BasicTerminal.class);
    mv.visitTypeInsn(NEW, termType);
    mv.visitInsn(DUP);
    terminals.get(t).writeToStack(mv);

    String termCtorSig = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Object.class) });
    mv.visitMethodInsn(INVOKESPECIAL, termType, "<init>", termCtorSig);

    return null;
  }

  @Override
  public Void visit(Class<? extends T> clazz) {
    String termClassType = Type.getInternalName(BasicTerminalClass.class);
    mv.visitTypeInsn(NEW, termClassType);
    mv.visitInsn(DUP);
    mv.visitLdcInsn(Type.getType(clazz));

    String termClassCtorSig = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Class.class) });
    mv.visitMethodInsn(INVOKESPECIAL, termClassType, "<init>", termClassCtorSig);

    return null;
  }
}
