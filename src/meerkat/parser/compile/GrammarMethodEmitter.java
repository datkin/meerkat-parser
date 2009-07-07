package meerkat.parser.compile;

import java.util.List;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;

import meerkat.Stream;
import meerkat.grammar.*;
import meerkat.parser.Result;
import meerkat.parser.ParseNode;
import meerkat.parser.basic.BasicParseLeaf;

// GrammarMethodEmitter emits the bytecode body necessary to match a given
// grammar rule. Matches on other rules in the grammar are delegated to calls
// to parse<Name> methods where <Name> is the name of the other rule.
// The assumption is that before and after each call the operand stack is empty.
// All necessary information for the parse (result and stream objects) are kept
// in the local variables. Whatever invokes this emitter takes care of
// initializing a result object as well as appropriate failure and success code.
public class GrammarMethodEmitter<T> implements GrammarVisitor<T, Void> {
  private final String name;
  private final String descriptor;
  private final MethodWriter mw;
  private final List<T> terminals;
  private final Class<?> clazz;

  // Any function that sets its own failure label also needs to store the result tail so it can
  // rollback changes to the result object before continuing. The default failure label should
  // be passed in by the constructor and should jump us to a statement returning a failure result.
  // If a function doesn't change the failure label itself, it can simply assume that a failure
  // results in an immediate failure. If an outer expression breaks this assumption, it's responsible
  // for ensuring that modifications made to the stream and result registers by the (partial) failed
  // match are undone.
  private Label failureLabel;

  // Various registers are used to track useful variables during parsing:
  // The streamRegister holds the current stream at any point in the parse
  private int streamRegister = 1;
  // The resultRegister holds the partially constructed result object for the parse
  private int resultRegister = 2;
  // The free registers are necessary for storing backtrack information (intermediate
  // result and stream data)
  private int nextFreeRegister = 3;

  // Set to false for predicates when parsed tokens aren't consumed and thus don't need
  // to be stored to the result
  private boolean storeResult = true;

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
    // NB: We don't bother to increment the nextFreeRegister here b/c
    // we are done with it before emitting any other code that might want a register.
    mw.visitVarInsn(ASTORE, nextFreeRegister);
    mw.visitVarInsn(ALOAD, nextFreeRegister);
    mw.visitMethodInsn(INVOKEINTERFACE, Result.class, "successful", "Z");
    mw.visitJumpInsn(IFEQ, failureLabel);
    if (storeResult) {
      mw.visitVarInsn(ALOAD, resultRegister);
      mw.visitVarInsn(ALOAD, nextFreeRegister);
      mw.visitMethodInsn(INVOKEINTERFACE, Result.class, "getValue", ParseNode.class);
      mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "add", Type.VOID_TYPE, Object.class);
    }
    mw.visitVarInsn(ALOAD, nextFreeRegister);
    mw.visitMethodInsn(INVOKEINTERFACE, Result.class, "getRest", Stream.class);
    mw.visitVarInsn(ASTORE, streamRegister);
    return null;
  }

  // If this fails, seq itself does not care what happens,
  // if the caller needs to rollback on failure, it is his responsibility
  // to do so. We simply fail and jump to the failure label set by our caller.
  @Override
  public Void visit(Sequence<T> sequence) {
    for (Expr<T> expr : sequence.getExprs()) {
      expr.accept(this);
    }
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
      // If the check succeeded, jump past the other tests
      mw.visitJumpInsn(GOTO, successLabel);
      // Otherwise, execution will jump to this label and we'll rollback
      // the stream and result objects before attempting another match
      mw.visitLabel(this.failureLabel);
      restore();
      mw.visitVarInsn(ASTORE, streamRegister);
      mw.visitVarInsn(ALOAD, resultRegister);
      restore();
      mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "dropRest", Type.VOID_TYPE, LinkedNode.class);
      // restore "unallocates" the registers but we want them to remain allocated until the loop is over
      this.nextFreeRegister += 2;
    }

    this.nextFreeRegister -= 2;
    this.failureLabel = outerFailureLabel;
    // We arrive here if one of the choices has not already jumped us to the success label
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
    // This store only happens when the subexpr fails. If the subexpr fails,
    // we don't need to take care of restoring it ourselves. If an outer expr needs
    // the stream restored on failure, they will do it themselves.
    restore();
    mw.visitVarInsn(ASTORE, streamRegister);

    // If the parse succeeds, jump to the actual failure label
    mw.visitJumpInsn(GOTO, outerFailureLabel);
    // If the parse fails, it jumps to *our* failure label, which is just a "continue"
    mw.visitLabel(failureLabel);
    this.failureLabel = outerFailureLabel;
    this.storeResult = outerStoreResult;
    return null;
  }

  @Override
  public Void visit(Optional<T> optional) {
    Label continueLabel = new Label();
    Label outerFailureLabel = this.failureLabel;
    this.failureLabel = new Label();

    // Save the current result and stream
    mw.visitVarInsn(ALOAD, resultRegister);
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "getTail", LinkedNode.class);
    save();
    mw.visitVarInsn(ALOAD, streamRegister);
    save();

    optional.getExpr().accept(this);
    mw.visitJumpInsn(GOTO, continueLabel); // skip the stream/result restoration if the opt parse succeeded

    // If the optional match failed restore the stream and result before continuing
    mw.visitLabel(this.failureLabel);
    restore();
    mw.visitVarInsn(ASTORE, streamRegister);
    mw.visitVarInsn(ALOAD, resultRegister);
    restore();
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "dropRest", Type.VOID_TYPE, LinkedNode.class);

    mw.visitLabel(continueLabel);
    this.failureLabel = outerFailureLabel;
    return null;
  }

  @Override
  public Void visit(ZeroOrMore<T> zom) {
    Label startLabel = new Label();
    Label outerFailureLabel = this.failureLabel;
    this.failureLabel = new Label();

    mw.visitLabel(startLabel);
    // Save the end of the result at the beginning of each iteration
    mw.visitVarInsn(ALOAD, resultRegister);
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "getTail", LinkedNode.class);
    save();
    // Save the stream
    mw.visitVarInsn(ALOAD, streamRegister);
    save();

    zom.getExpr().accept(this);
    mw.visitJumpInsn(GOTO, startLabel);

    // Every execution path passes through here once the match has failed (unless it matches forever)
    // at which point we clean up the changes made during the last parse attempt
    // and then continue the rest of the parse.
    mw.visitLabel(failureLabel);
    restore();
    mw.visitVarInsn(ASTORE, streamRegister);
    mw.visitVarInsn(ALOAD, resultRegister);
    restore();
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "dropRest", Type.VOID_TYPE, LinkedNode.class);
    this.failureLabel = outerFailureLabel;
    return null;
  }

  @Override
  public Void visit(OneOrMore<T> oom) {
    // This code is as above (the ZeroOrMore case) with the exception that we have
    // one mandatory match for which we simply duplicate the necessary code.
    // TODO: using an extra free register we could avoid the duplication and turn
    // this into a loop that increments after every match and does and IFEQ check
    // to determine if we failed after the first match attempt. Is this worthwhile?
    oom.getExpr().accept(this);

    Label startLabel = new Label();
    Label outerFailureLabel = this.failureLabel;
    this.failureLabel = new Label();

    mw.visitLabel(startLabel);
    mw.visitVarInsn(ALOAD, resultRegister);
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "getTail", LinkedNode.class);
    save();
    mw.visitVarInsn(ALOAD, streamRegister);
    save();
    oom.getExpr().accept(this);
    mw.visitJumpInsn(GOTO, startLabel);

    mw.visitLabel(failureLabel);
    restore();
    mw.visitVarInsn(ASTORE, streamRegister);
    mw.visitVarInsn(ALOAD, resultRegister);
    restore();
    mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "dropRest", Type.VOID_TYPE, LinkedNode.class);
    this.failureLabel = outerFailureLabel;
    return null;
  }

  @Override
  public Void visit(T t) {
    int index = terminals.indexOf(t);
    if (index < 0)
      throw new RuntimeException("Could find " + t + " in terminals array");

    preCompare();
    mw.visitFieldInsn(GETSTATIC, name, "terminals", "[" + Type.getDescriptor(clazz));
    mw.visitLdcInsn(index);
    mw.visitInsn(AALOAD);
    // Swap the terminal from the grammar with the terminal from the stream.
    // We want to invoke Object.equals on the object that we know not to be null.
    // NB: this code does not actually ensure the grammar's terminal is non null!
    mw.visitInsn(SWAP);
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
      mw.visitVarInsn(ALOAD, resultRegister);
      // Create a new node (leaf), pass the terminal to it, then push the node to the splicelist
      mw.visitTypeInsn(NEW, BasicParseLeaf.class);
      mw.visitInsn(DUP);
      mw.visitVarInsn(ALOAD, streamRegister);
      mw.visitMethodInsn(INVOKEINTERFACE, Stream.class, "getNext", Object.class);
      mw.visitMethodInsn(INVOKESPECIAL, BasicParseLeaf.class, "<init>", Type.VOID_TYPE, Object.class);
      mw.visitMethodInsn(INVOKEVIRTUAL, SpliceList.class, "add", Type.VOID_TYPE, Object.class);
    }
    // Progress the stream
    mw.visitVarInsn(ALOAD, streamRegister);
    mw.visitMethodInsn(INVOKEINTERFACE, Stream.class, "getRest", Stream.class);
    // Even thouh in some cases (ie for Sequences) the next instruction will be loading the stream
    // register, we don't simply want to push this to the stack:
    //  * In the general case, we want this stored to the stream register in the cases
    //    where we need to back it up, etc.
    //  * If we don't store it to a regsiter, we will need to DUP it anyway, and the cost of
    //    doing that is presumably roughly equal to storing it.
    mw.visitVarInsn(ASTORE, streamRegister);
  }
}
