package keep.it.simple.java;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public final class IoInstruction extends Instruction {
    public enum Operation {
        PRINT_NUMBER {
            void emit(BytecodeContext context) {
                context.standardOutput();
                context.loadX();
                context.method().visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            }
        },
        PRINT_CHARACTER {
            void emit(BytecodeContext context) {
                context.standardOutput();
                context.loadX();
                context.method().visitInsn(I2C);
                context.method().visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(C)V", false);
            }
        },
        READ_CHARACTER {
            void emit(BytecodeContext context) {
                context.method().visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
                context.method().visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "read", "()I", false);
                context.storeX();
            }
        };

        abstract void emit(BytecodeContext context);
    }

    private final Operation operation;

    public IoInstruction(Operation operation, int lineNumber) {
        super(lineNumber);
        this.operation = Objects.requireNonNull(operation, "operation");
    }

    @Override
    public void emit(BytecodeContext context) { operation.emit(context); }
}
