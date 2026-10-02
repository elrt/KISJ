package keep.it.simple.java;

public final class CompilerConfig {
    private final int memorySize;
    private final int stackSize;

    public CompilerConfig() {
        this(1024, 256);
    }

    public CompilerConfig(int memorySize, int stackSize) {
        if (memorySize <= 0 || stackSize <= 0) {
            throw new IllegalArgumentException("Memory and stack sizes must be positive.");
        }
        this.memorySize = memorySize;
        this.stackSize = stackSize;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public int getStackSize() {
        return stackSize;
    }
}
