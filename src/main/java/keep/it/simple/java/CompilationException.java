package keep.it.simple.java;

public class CompilationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CompilationException(String message, int lineNumber) {
        super(String.format("Syntax Error at line %d: %s", lineNumber, message));
    }

    public CompilationException(String message, int lineNumber, int column) {
        super(String.format("Syntax Error at line %d, column %d: %s", lineNumber, column, message));
    }
}
