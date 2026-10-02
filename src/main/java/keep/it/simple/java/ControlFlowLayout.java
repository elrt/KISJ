package keep.it.simple.java;

import org.objectweb.asm.Label;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class ControlFlowLayout {
    private final Label[] entries;
    private final Label end = new Label();
    private final Map<Instruction, Label> skipLabels = new IdentityHashMap<>();
    private final boolean[] retained;

    ControlFlowLayout(List<Instruction> instructions) {
        int size = instructions.size();
        entries = new Label[size];
        int[] nextExecutable = new int[size];
        int next = size;
        for (int index = size - 1; index >= 0; index--) {
            entries[index] = new Label();
            nextExecutable[index] = next;
            if (!instructions.get(index).isLabel()) {
                next = index;
            }
        }
        int[] skipTargets = new int[size];
        for (int index = 0; index < size; index++) {
            Instruction instruction = instructions.get(index);
            if (instruction.skipsNextInstruction()) {
                int skipped = nextExecutable[index];
                int target = skipped == size ? size : nextExecutable[skipped];
                skipTargets[index] = target;
                skipLabels.put(instruction, target == size ? end : entries[target]);
            }
        }
        retained = new BasicDeadCodeEliminator().retainedInstructions(instructions, skipTargets);
    }

    boolean shouldEmit(int index) { return retained[index]; }
    Label entryAt(int index) { return entries[index]; }
    Label endOfProgram() { return end; }

    Label skipTarget(Instruction instruction) {
        Label label = skipLabels.get(instruction);
        if (label == null) {
            throw new IllegalStateException("No conditional target for instruction.");
        }
        return label;
    }
}
