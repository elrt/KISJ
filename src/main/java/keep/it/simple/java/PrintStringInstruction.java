package keep.it.simple.java;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public final class PrintStringInstruction extends Instruction {
    private final String text;

    public PrintStringInstruction(String text, int lineNumber) {
        super(lineNumber);
        this.text = Objects.requireNonNull(text, "text");
    }

    @Override
    public void emit(BytecodeContext context) {
        context.standardOutput();
        context.method().visitLdcInsn(text);
        context.method().visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V", false);
    }
}
