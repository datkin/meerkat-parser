package meerkat.parser.compile;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;

import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;

import meerkat.grammar.*;
import meerkat.grammar.basic.*;
import meerkat.grammar.basic.GrammarFactory;

/* This class visits a grammar, and emits the bytecode to create
 * a static copy of this grammar at class initialization time */
public class GrammarCompiler<T> {
  private final static Type V = Type.VOID_TYPE; // TODO: move this to a constants inteface?

  private final Grammar<T> grammar;

  public GrammarCompiler(Grammar<T> grammar) {
    if (grammar == null)
      throw new IllegalArgumentException();
    this.grammar = grammar;
  }

  // takes the maximum used local variable upon entry
  // and returns the number of local variables added
  public int writeToStack(MethodWriter mw, Map<T, StackWriter> terminals) {
    // Create a local array of rules and store them
    List<Rule<T>> rules = new LinkedList<Rule<T>>();
    for (Rule<T> rule : grammar.getRules())
      rules.add(rule);

    int rulesLocal = mw.getNextLocal();
    mw.visitLdcInsn(rules.size());
    mw.visitTypeInsn(ANEWARRAY, Rule.class);
    mw.visitVarInsn(ASTORE, rulesLocal);

    mw.visitTypeInsn(NEW, GrammarFactory.class);
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL, GrammarFactory.class, "<init>", V);

    for (int i = 0; i < rules.size(); i++) {
      mw.visitInsn(DUP); // dupe the grammar factory
      mw.visitVarInsn(ALOAD, rulesLocal);
      mw.visitLdcInsn(i);
      mw.visitInsn(DUP2_X1); // copy the array and index below the factory
      mw.visitInsn(POP2);
      mw.visitLdcInsn(rules.get(i).getName());
      mw.visitMethodInsn(INVOKEVIRTUAL, GrammarFactory.class, "newRule", Rule.class, String.class);
      mw.visitInsn(AASTORE);
    }

    GrammarEmitter<T> ge = new GrammarEmitter<T>(mw, rulesLocal, terminals, rules);
    for (Rule<T> r : rules) {
      mw.visitInsn(DUP);
      mw.visitVarInsn(ALOAD, rulesLocal);
      mw.visitLdcInsn(rules.indexOf(r));
      mw.visitInsn(AALOAD);
      grammar.getExpr(r).accept(ge);
      mw.visitMethodInsn(INVOKEVIRTUAL, GrammarFactory.class, "setRule", Rule.class, Rule.class, Expr.class);
      if (r.equals(grammar.getStartingRule())) {
        mw.visitInsn(SWAP);
        mw.visitInsn(DUP_X1);
        mw.visitInsn(SWAP);
        mw.visitMethodInsn(INVOKEVIRTUAL, GrammarFactory.class, "setStartingRule", V, Rule.class);
      } else {
        mw.visitInsn(POP);
      }
    }

    mw.visitMethodInsn(INVOKEVIRTUAL, GrammarFactory.class, "getGrammar", Grammar.class);

    return 1;
  }

}

/* Leave an Expr on the stack */
class GrammarEmitter<T> implements GrammarVisitor<T, Void> {
  private final static Type V = Type.VOID_TYPE;

  private final MethodWriter mw;
  private final int rulesLocal;
  private final Map<T, StackWriter> terminals;
  private final List<Rule<T>> rules;

  public GrammarEmitter(MethodWriter mw, int rulesLocal, Map<T, StackWriter> terminals, List<Rule<T>> rules) {
    if (mw == null || terminals == null || rules == null)
      throw new IllegalArgumentException();
    this.mw = mw;
    this.rulesLocal = rulesLocal;
    this.terminals = terminals;
    this.rules = rules;
  }

  @Override
  public Void visit(Rule<T> rule) {
    mw.visitVarInsn(ALOAD, rulesLocal);
    mw.visitLdcInsn(rules.indexOf(rule));
    mw.visitInsn(AALOAD);
    return null;
  }

  @Override
  public Void visit(Sequence<T> sequence) {
    // 0. Create a seq object
    mw.visitTypeInsn(NEW, BasicSequence.class);
    mw.visitInsn(DUP);

    // 1. create a linked list
    mw.visitTypeInsn(NEW, LinkedList.class);
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL, LinkedList.class, "<init>", V);

    // 2. create each sub expr and add it to the list
    for (Expr<T> expr : sequence.getExprs()) {
      mw.visitInsn(DUP);
      expr.accept(this); // e.g. push the expr to the stack
      mw.visitMethodInsn(INVOKEVIRTUAL, LinkedList.class, "add", Type.BOOLEAN_TYPE, Type.getType(Object.class));
      mw.visitInsn(POP);
    }

    // 3. pass the linked list to a seq constructor
    mw.visitMethodInsn(INVOKESPECIAL, BasicSequence.class, "<init>", V, List.class);

    return null;
  }

  @Override
  public Void visit(Choice<T> choice) {
    // 0. Create a choice object
    mw.visitTypeInsn(NEW, BasicChoice.class);
    mw.visitInsn(DUP);

    // 1. create a linked list
    mw.visitTypeInsn(NEW, LinkedList.class);
    mw.visitInsn(DUP);
    mw.visitMethodInsn(INVOKESPECIAL, LinkedList.class, "<init>", V);

    // 2. create each sub expr and add it to the list
    for (Expr<T> expr : choice.getExprs()) {
      mw.visitInsn(DUP);
      expr.accept(this); // e.g. push the expr to the stack
      mw.visitMethodInsn(INVOKEVIRTUAL, LinkedList.class, "add", Type.BOOLEAN_TYPE, Object.class);
      mw.visitInsn(POP);
    }

    // 3. pass the linked list to a choice constructor
    mw.visitMethodInsn(INVOKESPECIAL, BasicChoice.class, "<init>", V, List.class);
    return null;
  }

  @Override
  public Void visit(Optional<T> optional) {
    mw.visitTypeInsn(NEW, BasicOptional.class);
    mw.visitInsn(DUP);
    optional.getExpr().accept(this);
    mw.visitMethodInsn(INVOKESPECIAL, BasicOptional.class, "<init>", V, Expr.class);
    return null;
  }

  @Override
  public Void visit(And<T> and) {
    mw.visitTypeInsn(NEW, BasicAnd.class);
    mw.visitInsn(DUP);
    and.getExpr().accept(this);
    mw.visitMethodInsn(INVOKESPECIAL, BasicAnd.class, "<init>", V, Expr.class);
    return null;
  }

  @Override
  public Void visit(Not<T> not) {
    mw.visitTypeInsn(NEW, BasicNot.class);
    mw.visitInsn(DUP);
    not.getExpr().accept(this);
    mw.visitMethodInsn(INVOKESPECIAL, BasicNot.class, "<init>", V, Expr.class);
    return null;
  }

  @Override
  public Void visit(ZeroOrMore<T> zom) {
    mw.visitTypeInsn(NEW, BasicZeroOrMore.class);
    mw.visitInsn(DUP);
    zom.getExpr().accept(this);
    mw.visitMethodInsn(INVOKESPECIAL, BasicZeroOrMore.class, "<init>", V, Expr.class);
    return null;
  }

  @Override
  public Void visit(OneOrMore<T> oom) {
    mw.visitTypeInsn(NEW, BasicOneOrMore.class);
    mw.visitInsn(DUP);
    oom.getExpr().accept(this);
    mw.visitMethodInsn(INVOKESPECIAL, BasicOneOrMore.class, "<init>", V, Expr.class);
    return null;
  }

  @Override
  public Void visit(T t) {
    mw.visitTypeInsn(NEW, BasicTerminal.class);
    mw.visitInsn(DUP);
    terminals.get(t).writeToStack(mw);
    mw.visitMethodInsn(INVOKESPECIAL, BasicTerminal.class, "<init>", V, Object.class);
    return null;
  }

  @Override
  public Void visit(Class<? extends T> clazz) {
    mw.visitTypeInsn(NEW, BasicTerminalClass.class);
    mw.visitInsn(DUP);
    mw.visitLdcInsn(Type.getType(clazz));
    mw.visitMethodInsn(INVOKESPECIAL, BasicTerminalClass.class, "<init>", V, Class.class);
    return null;
  }
}
