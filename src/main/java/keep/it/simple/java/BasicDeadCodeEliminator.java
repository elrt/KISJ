package keep.it.simple.java;

import java.util.List;

/* Local DCE */
final class BasicDeadCodeEliminator {
    boolean[] retainedInstructions(List<Instruction> instructions, int[] skipTargets) {
        boolean[] retained = new boolean[instructions.size()];
        boolean[] conditionalEntries = new boolean[instructions.size() + 1];
        boolean reachable = true;
        for (int index = 0; index < instructions.size(); index++) {
            Instruction instruction = instructions.get(index);
            if (instruction.isLabel() || conditionalEntries[index]) {
                reachable = true;
            }
            if (!reachable) {
                continue;
            }
            retained[index] = true;
            if (instruction.skipsNextInstruction()) {
                conditionalEntries[skipTargets[index]] = true;
            }
            reachable = !instruction.endsBasicBlock();
        }
        return retained;
    }
}
