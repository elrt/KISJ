package keep.it.simple.java;

public final class ReturnInstruction extends Instruction {
    public ReturnInstruction(int lineNumber) { super(lineNumber); }

    @Override
    public boolean endsBasicBlock() { return true; }

    @Override
    public void emit(BytecodeContext context) { context.runtime().emitReturn(); }
}
