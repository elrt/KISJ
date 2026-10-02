package keep.it.simple.java;

import static org.objectweb.asm.Opcodes.GOTO;

public final class HaltInstruction extends Instruction {
    public HaltInstruction(int lineNumber) { super(lineNumber); }

    @Override
    public boolean endsBasicBlock() { return true; }

    @Override
    public void emit(BytecodeContext context) {
        context.method().visitJumpInsn(GOTO, context.endOfProgram());
    }
}
