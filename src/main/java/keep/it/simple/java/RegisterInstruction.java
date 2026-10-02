package keep.it.simple.java;

import java.util.Objects;

public final class RegisterInstruction extends Instruction {
    public enum Operation {
        COPY_TO_Y {
            void emit(BytecodeContext context, int ignored) {
                context.loadX();
                context.storeY();
            }
        },
        COPY_TO_X {
            void emit(BytecodeContext context, int ignored) {
                context.loadY();
                context.storeX();
            }
        },
        SAVE_AND_ASSIGN {
            void emit(BytecodeContext context, int value) {
                context.loadX();
                context.storeY();
                context.pushInt(value);
                context.storeX();
            }
        };

        abstract void emit(BytecodeContext context, int value);
    }

    private final Operation operation;
    private final int value;

    public RegisterInstruction(Operation operation, int value, int lineNumber) {
        super(lineNumber);
        this.operation = Objects.requireNonNull(operation, "operation");
        this.value = value;
    }

    @Override
    public void emit(BytecodeContext context) {
        operation.emit(context, value);
    }
}
