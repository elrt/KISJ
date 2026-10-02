package keep.it.simple.java;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/** emits standalone jvm rc, with no dependency on compiler classes */
final class RuntimeEmitter {
    private static final int MEMORY = 3;
    private static final int CALL_STACK = 4;
    private static final int STACK_POINTER = 5;
    private static final int ADDRESS = 6;

    private final BytecodeContext context;
    private final MethodVisitor method;
    private final CompilerConfig config;
    private final Label returnDispatcher = new Label();
    private final Label memoryViolation = new Label();
    private final Label stackOverflow = new Label();
    private final Label stackUnderflow = new Label();
    private final Label invalidReturn = new Label();
    private final List<Label> returnTargets = new ArrayList<>();
    private boolean memoryUsed;
    private boolean returnUsed;

    RuntimeEmitter(BytecodeContext context, CompilerConfig config) {
        this.context = context;
        this.method = context.method();
        this.config = config;
    }

    void initialize() {
        context.pushInt(0);
        context.storeX();
        context.pushInt(0);
        context.storeY();
        allocateArray(config.getMemorySize(), MEMORY);
        allocateArray(config.getStackSize(), CALL_STACK);
        context.pushInt(0);
        method.visitVarInsn(ISTORE, STACK_POINTER);
    }

    private void allocateArray(int size, int slot) {
        context.pushInt(size);
        method.visitIntInsn(NEWARRAY, T_INT);
        method.visitVarInsn(ASTORE, slot);
    }

    void prepareMemoryAccess() {
        memoryUsed = true;
        method.visitVarInsn(ISTORE, ADDRESS);
        checkIndex(ADDRESS, config.getMemorySize(), memoryViolation, memoryViolation);
        method.visitVarInsn(ALOAD, MEMORY);
        method.visitVarInsn(ILOAD, ADDRESS);
    }

    void emitCall(Label target) {
        checkIndex(STACK_POINTER, config.getStackSize(), stackUnderflow, stackOverflow);
        Label continuation = new Label();
        int returnId = returnTargets.size();
        returnTargets.add(continuation);
        method.visitVarInsn(ALOAD, CALL_STACK);
        method.visitVarInsn(ILOAD, STACK_POINTER);
        context.pushInt(returnId);
        method.visitInsn(IASTORE);
        method.visitIincInsn(STACK_POINTER, 1);
        method.visitJumpInsn(GOTO, target);
        method.visitLabel(continuation);
    }

    void emitReturn() {
        returnUsed = true;
        method.visitJumpInsn(GOTO, returnDispatcher);
    }

    private void checkIndex(int slot, int size, Label belowZero, Label aboveLimit) {
        method.visitVarInsn(ILOAD, slot);
        context.pushInt(0);
        method.visitJumpInsn(IF_ICMPLT, belowZero);
        method.visitVarInsn(ILOAD, slot);
        context.pushInt(size);
        method.visitJumpInsn(IF_ICMPGE, aboveLimit);
    }

    void finish() {
        method.visitJumpInsn(GOTO, context.endOfProgram());
        if (returnUsed) {
            emitReturnDispatcher();
        }
        if (memoryUsed) {
            emitError(memoryViolation, "KISS Runtime Error: Memory Access Violation");
        }
        if (returnUsed || !returnTargets.isEmpty()) {
            emitError(stackUnderflow, "KISS Runtime Error: Stack Underflow");
            emitError(stackOverflow, "KISS Runtime Error: Stack Overflow");
        }
        if (returnUsed && !returnTargets.isEmpty()) {
            emitError(invalidReturn, "KISS Runtime Error: Invalid Return Address");
        }
        method.visitLabel(context.endOfProgram());
        method.visitInsn(RETURN);
    }

    private void emitReturnDispatcher() {
        method.visitLabel(returnDispatcher);
        method.visitVarInsn(ILOAD, STACK_POINTER);
        context.pushInt(1);
        method.visitJumpInsn(IF_ICMPLT, stackUnderflow);
        method.visitIincInsn(STACK_POINTER, -1);
        checkIndex(STACK_POINTER, config.getStackSize(), stackUnderflow, stackOverflow);
        if (returnTargets.isEmpty()) {
            method.visitJumpInsn(GOTO, stackUnderflow);
            return;
        }
        method.visitVarInsn(ALOAD, CALL_STACK);
        method.visitVarInsn(ILOAD, STACK_POINTER);
        method.visitInsn(IALOAD);
        method.visitTableSwitchInsn(0, returnTargets.size() - 1, invalidReturn,
                returnTargets.toArray(new Label[0]));
    }

    private void emitError(Label label, String message) {
        method.visitLabel(label);
        method.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
        method.visitLdcInsn(message);
        method.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        method.visitJumpInsn(GOTO, context.endOfProgram());
    }
}
