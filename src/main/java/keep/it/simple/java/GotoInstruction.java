package keep.it.simple.java;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.GOTO;

public final class GotoInstruction extends Instruction {
    private final String target;

    public GotoInstruction(String target, int lineNumber) {
        super(lineNumber);
        this.target = Objects.requireNonNull(target, "target");
    }

    @Override
    public boolean endsBasicBlock() { return true; }

    @Override
    public void validateLabels(LabelRegistry labels) {
        labels.resolve(target, getLineNumber());
    }

    @Override
    public void emit(BytecodeContext context) {
        context.method().visitJumpInsn(GOTO, context.labels().resolve(target, getLineNumber()));
    }
}
