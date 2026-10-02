package keep.it.simple.java;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public final class MemoryInstruction extends Instruction {
    public enum Access {
        LOAD {
            void emit(BytecodeContext context) {
                context.method().visitInsn(IALOAD);
                context.storeX();
            }
        },
        STORE {
            void emit(BytecodeContext context) {
                context.loadX();
                context.method().visitInsn(IASTORE);
            }
        };

        abstract void emit(BytecodeContext context);
    }

    private final Access access;
    private final boolean indirect;
    private final int address;

    public MemoryInstruction(Access access, int address, int lineNumber) {
        this(access, false, address, lineNumber);
    }

    public MemoryInstruction(Access access, int lineNumber) {
        this(access, true, 0, lineNumber);
    }

    private MemoryInstruction(Access access, boolean indirect, int address, int lineNumber) {
        super(lineNumber);
        this.access = Objects.requireNonNull(access, "access");
        this.indirect = indirect;
        this.address = address;
    }

    @Override
    public void emit(BytecodeContext context) {
        if (indirect) {
            context.loadY();
        } else {
            context.pushInt(address);
        }
        context.runtime().prepareMemoryAccess();
        access.emit(context);
    }
}
