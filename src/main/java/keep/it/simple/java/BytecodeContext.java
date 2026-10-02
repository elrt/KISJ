package keep.it.simple.java;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/** Per method backend services
 *  No state is shared across compilations */
public final class BytecodeContext {
    private static final int X = 1;
    private static final int Y = 2;

    private final MethodVisitor method;
    private final LabelRegistry labels;
    private final ControlFlowLayout layout;
    private final RuntimeEmitter runtime;

    BytecodeContext(MethodVisitor method, CompilerConfig config,
                    LabelRegistry labels, ControlFlowLayout layout) {
        this.method = method;
        this.labels = labels;
        this.layout = layout;
        this.runtime = new RuntimeEmitter(this, config);
    }

    public MethodVisitor method() { return method; }
    public LabelRegistry labels() { return labels; }
    public Label endOfProgram() { return layout.endOfProgram(); }
    public Label skipTarget(Instruction instruction) { return layout.skipTarget(instruction); }
    RuntimeEmitter runtime() { return runtime; }

    public void loadX() { method.visitVarInsn(ILOAD, X); }
    public void storeX() { method.visitVarInsn(ISTORE, X); }
    public void loadY() { method.visitVarInsn(ILOAD, Y); }
    public void storeY() { method.visitVarInsn(ISTORE, Y); }

    public void standardOutput() {
        method.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    }

    public void binary(int opcode, int value) {
        loadX();
        pushInt(value);
        method.visitInsn(opcode);
    }

    public void pushInt(int value) {
        if (value >= -1 && value <= 5) {
            method.visitInsn(ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            method.visitIntInsn(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            method.visitIntInsn(SIPUSH, value);
        } else {
            method.visitLdcInsn(value);
        }
    }
}
