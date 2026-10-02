package keep.it.simple.java;

import java.util.Objects;
import static org.objectweb.asm.Opcodes.*;

/* E and L skip one executable instruction; labels never count as instructions */
public final class BranchInstruction extends Instruction {

    public enum Condition {
        EQUAL(IF_ICMPEQ),
        LESS_THAN(IF_ICMPLT);

        private final int opcode;

        Condition(int opcode) {
            this.opcode = opcode;
        }
    }

    private final Condition condition;
    private final int value;

    public BranchInstruction(Condition condition, int value, int lineNumber) {
        super(lineNumber);
        this.condition = Objects.requireNonNull(condition, "condition");
        this.value = value;
    }

    @Override
    public boolean skipsNextInstruction() {
        return true;
    }

    @Override
    public void emit(BytecodeContext context) {
        context.loadX();
        context.pushInt(value);
        context.method().visitJumpInsn(condition.opcode, context.skipTarget(this));
    }
}