package meerkat.parser.compile;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;
import java.util.List;

import meerkat.grammar.*;
import meerkat.parser.Result;
import meerkat.parser.ParseNode;
import meerkat.parser.basic.BasicParseLeaf;
import meerkat.Stream;

// TODO: Unify the ParseNode/SpliceList data structure -
// currently every terminal is getting double wrapped, we should
// try and create a LinkedParseNode that does SpliceList's job

// TODO: inside of a "choice", we can keep the terminal on the stack
// and just run a bunch of checks, but in other situatoins we will
// want to keep the stream on the stack

/* We need some sort of stack discipline for what is on the stack
 * before and ever every visit call.
 * Assume either:
 *  - the item on the top of the stack is the stream at which to
 *    start checking
 *  - the location (stack or local var) is specified in a field
 */
public class GrammarMethodEmitter<T> implements GrammarVisitor<T, Void> {
  private final MethodWriter mw;
  private final String name; // class name
  private final String descriptor;
  private final List<T> terminals;
  private final Class<?> clazz;
  private int resultRegister = 2;
  private int streamRegister = 1;
  private int tempRegister = 3; // only to be used locally, across another visit call we can expect the value stored in this register to change
  private int nextFreeRegister = 4;
  private boolean storeResult = true; // default to true, but set to false for predicates
  // when false, we simply consume from the input stream without modifying the reuslt list
  //private boolean returnOnFailure = true;
  private Label failureLabel = null; // if the failureLabel is null, return on a failure?
  // boolean to say whether or not the expr we just handled was a rule
  // boolean to indicate whether failure to match the current expr should fail the whole match?
  // a top level failure label (which jump to a "return failed") should probably be passed as an arg
  // **Any function that sets its own failure label also needs to store the result tail so it can
  //   changes to the result object before continuing
  //   (when using the default failure label, a failure result is returned so no need to rollback)

  public GrammarMethodEmitter(String name, MethodWriter mw, List<T> terminals, Class<?> clazz, Label failureLabel) {
    if (name == null || mw == null || terminals == null || clazz == null || failureLabel == null)
      throw new IllegalArgumentException();
    this.name = name;
    this.descriptor = "L" + name + ";";
    this.mw = mw;
    this.terminals = terminals;
    this.clazz = clazz;
    this.failureLabel = failureLabel;
  }

  @Override
  public Void visit(Rule<T> rule) {
    mw.visitVarInsn(ALOAD, 0);
    mw.visitVarInsn(ALOAD, streamRegister);
    mw.visitMethodInsn(INVOKEVIRTUAL, descriptor, "parse" + rule.getName(), Result.class, Stream.class);
    mw.visitVarInsn(ASTORE, tempRegister);
    mw.visitVarInsn(ALOAD, tempRegister);
    mw.visitMethodInsn(INVOKEINTERFACE, Result.class, "successful", "Z");
    mw.visitJumpInsn(IFEQ, failureLabel);
    if (storeResult) {
      mw.visitVarInsn(ALOAD, resultRegister);
      mw.visitVarInsn(ALOAD, tempRegister);
      mw.visitMethodInsn(INVOKEINTERFACE, Result.class, "getValue", ParseNode.class);
      // TODO: modify SpliceList.add to be void - avoids extra pop insns
      mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "add", LinkedNode.class, Object.class);
      mw.visitInsn(POP);
    }
    mw.visitVarInsn(ALOAD, tempRegister);
    mw.visitMethodInsn(INVOKEINTERFACE, Result.class, "getRest", Stream.class);
    mw.visitVarInsn(ASTORE, streamRegister);
    return null;
  }

  /* If this fails, seq itself does not care what happens,
   * if the caller needs to rollback on failure, it is his responsibility
   * to do so. We simply fail and jump to the failure label set by our caller.
   * (The default failure label is return)
   */
  @Override
  public Void visit(Sequence<T> sequence) {
    for (Expr<T> expr : sequence.getExprs()) {
      expr.accept(this);
      // After each success we need to take an item off the stack and store
      // it in some sort of result.
      // If any of the subexprs fail, the sequence will fail, and the expr will either emit a return
      // or it will jump out of the sequence altogether (if failureLabel != null)
    }
    // in the event of success, do we leave an object on the stack or what?
    // (everything in an expr is a flat list, only tree nesting occurs for rules)
    return null;
  }

  private void save() {
    mw.visitVarInsn(ASTORE, nextFreeRegister);
    this.nextFreeRegister++;
  }

  private void restore() {
    this.nextFreeRegister--;
    mw.visitVarInsn(ALOAD, nextFreeRegister);
  }

  @Override
  public Void visit(Choice<T> choice) {
    Label outerFailureLabel = this.failureLabel;
    Label successLabel = new Label();

    // Save the end of the result
    mw.visitVarInsn(ALOAD, resultRegister);
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "getTail", LinkedNode.class);
    save();
    // Save the stream
    mw.visitVarInsn(ALOAD, streamRegister);
    save();

    for (Expr<T> expr : choice.getExprs()) {
      this.failureLabel = new Label();
      expr.accept(this);
      mw.visitJumpInsn(GOTO, successLabel);
      mw.visitLabel(this.failureLabel);
      // Restore the stream
      restore();
      mw.visitVarInsn(ASTORE, streamRegister);
      // Drop any new results
      mw.visitVarInsn(ALOAD, resultRegister);
      restore();
      mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "dropRest", Type.VOID_TYPE, LinkedNode.class);
      // reset the nextFreeRegister
      this.nextFreeRegister += 2;
    }
    this.nextFreeRegister -= 2;
    this.failureLabel = outerFailureLabel;
    mw.visitJumpInsn(GOTO, this.failureLabel);
    mw.visitLabel(successLabel);
    return null;
  }

  @Override
  public Void visit(And<T> and) {
    boolean outerStoreResult = this.storeResult;
    this.storeResult = false;

    mw.visitVarInsn(ALOAD, streamRegister);
    save();
    and.getExpr().accept(this);
    restore();
    mw.visitVarInsn(ASTORE, streamRegister);

    this.storeResult = outerStoreResult;
    return null;
  }

  @Override
  public Void visit(Not<T> not) {
    boolean outerStoreResult = this.storeResult;
    this.storeResult = false;

    Label outerFailureLabel = this.failureLabel;
    this.failureLabel = new Label();

    mw.visitVarInsn(ALOAD, streamRegister);
    save();
    not.getExpr().accept(this);
    // this restore will not happen if the subexpr fails - but this is what we want, right?
    restore();
    mw.visitVarInsn(ASTORE, streamRegister);

    mw.visitJumpInsn(GOTO, outerFailureLabel); // if the parse succeeds, jump to the "real" failure label
    mw.visitLabel(failureLabel); // if the parse fails, it jumps to *our* failure label, which is just a "continue"
    this.failureLabel = outerFailureLabel;
    this.storeResult = outerStoreResult;
    return null;
  }

  @Override
  public Void visit(Optional<T> optional) {
    Label continueLabel = new Label();
    Label outerFailureLabel = this.failureLabel;
    this.failureLabel = new Label();

    // Save the end of the result
    mw.visitVarInsn(ALOAD, resultRegister);
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "getTail", LinkedNode.class);
    save();
    // Save the stream
    mw.visitVarInsn(ALOAD, streamRegister);
    save();
    optional.getExpr().accept(this);
    mw.visitJumpInsn(GOTO, continueLabel); // skip the stream/result restoration if the opt parse succeeded

    mw.visitLabel(this.failureLabel);
    // Restore the stream
    restore();
    mw.visitVarInsn(ASTORE, streamRegister);
    // Drop any new results
    mw.visitVarInsn(ALOAD, resultRegister);
    restore();
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "dropRest", Type.VOID_TYPE, LinkedNode.class);
    this.failureLabel = outerFailureLabel;

    mw.visitLabel(continueLabel);

    return null;
  }

  @Override
  public Void visit(ZeroOrMore<T> zom) {
    Label outerFailureLabel = this.failureLabel;
    this.failureLabel = new Label();

    Label startLabel = new Label();
    mw.visitLabel(startLabel);
    // Save the end of the result at the beginning of reach iteration
    mw.visitVarInsn(ALOAD, resultRegister);
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "getTail", LinkedNode.class);
    save();
    // Save the stream
    mw.visitVarInsn(ALOAD, streamRegister);
    save();
    zom.getExpr().accept(this);
    mw.visitJumpInsn(GOTO, startLabel);

    mw.visitLabel(failureLabel); // rollback on failure
    this.failureLabel = outerFailureLabel;
    // Restore the stream
    restore();
    mw.visitVarInsn(ASTORE, streamRegister);
    // Drop any new results
    mw.visitVarInsn(ALOAD, resultRegister);
    restore();
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "dropRest", Type.VOID_TYPE, LinkedNode.class);
    return null;
  }

  @Override
  public Void visit(OneOrMore<T> oom) {
    oom.getExpr().accept(this);

    Label outerFailureLabel = this.failureLabel;
    this.failureLabel = new Label();

    Label startLabel = new Label();
    mw.visitLabel(startLabel);
    // Save the end of the result at the beginning of reach iteration
    mw.visitVarInsn(ALOAD, resultRegister);
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "getTail", LinkedNode.class);
    save();
    // Save the stream
    mw.visitVarInsn(ALOAD, streamRegister);
    save();
    oom.getExpr().accept(this);
    mw.visitJumpInsn(GOTO, startLabel);

    mw.visitLabel(failureLabel); // rollback on failure
    this.failureLabel = outerFailureLabel;
    // Restore the stream
    restore();
    mw.visitVarInsn(ASTORE, streamRegister);
    // Drop any new results
    mw.visitVarInsn(ALOAD, resultRegister);
    restore();
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "dropRest", Type.VOID_TYPE, LinkedNode.class);
    return null;
  }

  @Override
  public Void visit(T t) {
    int index = terminals.indexOf(t);
    if (index < 0)
      throw new RuntimeException("could find " + t + " in terminals array");

    preCompare();
    mw.visitFieldInsn(GETSTATIC, name, "terminals", "[" + Type.getDescriptor(clazz));
    mw.visitLdcInsn(index);
    mw.visitInsn(AALOAD);
    mw.visitInsn(SWAP); // we want to invoke "equals" on the terminal, no the value from the stream which might be null
    mw.visitMethodInsn(INVOKEVIRTUAL, Object.class, "equals", "Z", Object.class);
    postCompare();

    return null;
  }

  @Override
  public Void visit(Class<? extends T> clazz) {
    preCompare();
    mw.visitTypeInsn(INSTANCEOF, clazz);
    postCompare();
    return null;
  }

  private void preCompare() {
    mw.visitVarInsn(ALOAD, streamRegister);
    mw.visitMethodInsn(INVOKEINTERFACE, Stream.class, "hasMore", "Z");
    mw.visitJumpInsn(IFEQ, failureLabel);
    mw.visitVarInsn(ALOAD, streamRegister);
    mw.visitMethodInsn(INVOKEINTERFACE, Stream.class, "getNext", Object.class);
  }

  private void postCompare() {
    mw.visitJumpInsn(IFEQ, failureLabel);

    // If storing results, create a ParseLeaf with the token and add it to the SpliceList
    if (storeResult) {
      mw.visitVarInsn(ALOAD, resultRegister); // this is where the splicelist is stored
      mw.visitTypeInsn(NEW, BasicParseLeaf.class);
      mw.visitInsn(DUP);
      mw.visitVarInsn(ALOAD, streamRegister);
      mw.visitMethodInsn(INVOKEINTERFACE, Stream.class, "getNext", Object.class);
      mw.visitMethodInsn(INVOKESPECIAL, BasicParseLeaf.class, "<init>", Type.VOID_TYPE, Object.class);
      mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "add", LinkedNode.class, Object.class);
      mw.visitInsn(POP);
      // create a new node, pass the terminal to it, then push the node to the splicelist
    }

    // Progress the stream
    mw.visitVarInsn(ALOAD, streamRegister);
    mw.visitMethodInsn(INVOKEINTERFACE, Stream.class, "getRest", Stream.class);
    // TODO: we should delay emitting this insn until we know if the stream register is going to be the next thing we access? Maybe not b/c the stream register needs to be updated anyway (do we expect a dup, store to be faster than a store, load?)
    mw.visitVarInsn(ASTORE, streamRegister);
  }
}
