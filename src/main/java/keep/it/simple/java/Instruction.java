package keep.it.simple.java;

/** Immutable AST node
 *  Concrete nodes own their bytecode generation */
public abstract class Instruction {
    private final int lineNumber;

    protected Instruction(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public final int getLineNumber() { return lineNumber; }

    public abstract void emit(BytecodeContext context);

    public boolean isLabel() { return false; }
    public boolean endsBasicBlock() { return false; }
    public boolean skipsNextInstruction() { return false; }

    public void declareLabels(LabelRegistry labels) { }
    public void validateLabels(LabelRegistry labels) { }
}
