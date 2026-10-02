package keep.it.simple.java;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/** a reusable compiler all mutable backend state belongs to a compile call */
public final class KissCompiler {
    private final CompilerConfig config;
    private final KissParser parser;

    public KissCompiler(CompilerConfig config) {
        this(new KissParser(config));
    }

    public KissCompiler(KissParser parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.config = parser.getConfig();
    }

    public byte[] compile(List<String> sourceLines, String targetClassName) {
        return generate(parser.parse(sourceLines), targetClassName);
    }

    public byte[] compile(String source, String targetClassName) {
        return generate(parser.parse(source), targetClassName);
    }

    private byte[] generate(List<Instruction> instructions, String targetClassName) {
        Objects.requireNonNull(targetClassName, "targetClassName");
        LabelRegistry labels = new LabelRegistry();
        for (Instruction instruction : instructions) {
            instruction.declareLabels(labels);
        }
        for (Instruction instruction : instructions) {
            instruction.validateLabels(labels);
        }
        ControlFlowLayout layout = new ControlFlowLayout(instructions);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_8, ACC_PUBLIC | ACC_SUPER, targetClassName, null, "java/lang/Object", null);
        generateConstructor(writer);

        MethodVisitor method = writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        method.visitCode();
        BytecodeContext context = new BytecodeContext(method, config, labels, layout);
        context.runtime().initialize();
        for (int index = 0; index < instructions.size(); index++) {
            if (!layout.shouldEmit(index)) {
                continue;
            }
            method.visitLabel(layout.entryAt(index));
            instructions.get(index).emit(context);
        }
        context.runtime().finish();
        method.visitMaxs(0, 0);
        method.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private void generateConstructor(ClassWriter writer) {
        MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        method.visitInsn(RETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
    }
}
