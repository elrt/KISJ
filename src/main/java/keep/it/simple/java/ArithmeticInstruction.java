package keep.it.simple.java;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public final class ArithmeticInstruction extends Instruction {
    /** per operation strategies*/
    public enum Operation {
        ASSIGN {
            void emit(BytecodeContext context, int value) { context.pushInt(value); }
        },
        ADD {
            void emit(BytecodeContext context, int value) { context.binary(IADD, value); }
        },
        SUBTRACT {
            void emit(BytecodeContext context, int value) { context.binary(ISUB, value); }
        },
        MULTIPLY {
            void emit(BytecodeContext context, int value) { context.binary(IMUL, value); }
        },
        DIVIDE {
            void emit(BytecodeContext context, int value) { context.binary(IDIV, value); }
        },
        REMAINDER {
            void emit(BytecodeContext context, int value) { context.binary(IREM, value); }
        },
        SQUARE {
            void emit(BytecodeContext context, int ignored) {
                context.loadX();
                context.loadX();
                context.method().visitInsn(IMUL);
            }
        },
        SQRT {
            void emit(BytecodeContext context, int ignored) {
                context.loadX();
                context.method().visitInsn(I2D);
                context.method().visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
                context.method().visitInsn(D2I);
            }
        },
        RANDOM {
            void emit(BytecodeContext context, int value) {
                context.method().visitMethodInsn(INVOKESTATIC, "java/lang/Math", "random", "()D", false);
                context.pushInt(value);
                context.method().visitInsn(I2D);
                context.method().visitInsn(DMUL);
                context.method().visitInsn(D2I);
            }
        },
        NEGATE {
            void emit(BytecodeContext context, int ignored) {
                context.loadX();
                context.method().visitInsn(INEG);
            }
        },
        COMPLEMENT {
            void emit(BytecodeContext context, int ignored) { context.binary(IXOR, -1); }
        },
        CLEAR {
            void emit(BytecodeContext context, int ignored) { context.pushInt(0); }
        };

        abstract void emit(BytecodeContext context, int value);
    }

    private final Operation operation;
    private final int value;

    public ArithmeticInstruction(Operation operation, int value, int lineNumber) {
        super(lineNumber);
        this.operation = Objects.requireNonNull(operation, "operation");
        this.value = value;
    }

    @Override
    public void emit(BytecodeContext context) {
        operation.emit(context, value);
        context.storeX();
    }
}
