package keep.it.simple.java;

import java.util.Objects;

public final class LabelInstruction extends Instruction {
    private final String name;

    public LabelInstruction(String name, int lineNumber) {
        super(lineNumber);
        this.name = Objects.requireNonNull(name, "name");
    }

    @Override
    public boolean isLabel() { return true; }

    @Override
    public void declareLabels(LabelRegistry labels) {
        labels.declare(name, getLineNumber());
    }

    @Override
    public void emit(BytecodeContext context) {
        context.method().visitLabel(context.labels().resolve(name, getLineNumber()));
    }
}
