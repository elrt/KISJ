package keep.it.simple.java;

import java.util.Objects;

public final class CallInstruction extends Instruction {
    private final String target;

    public CallInstruction(String target, int lineNumber) {
        super(lineNumber);
        this.target = Objects.requireNonNull(target, "target");
    }

    @Override
    public void validateLabels(LabelRegistry labels) {
        labels.resolve(target, getLineNumber());
    }

    @Override
    public void emit(BytecodeContext context) {
        context.runtime().emitCall(context.labels().resolve(target, getLineNumber()));
    }
}
