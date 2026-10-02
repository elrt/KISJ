package keep.it.simple.java;

import org.objectweb.asm.Label;

import java.util.HashMap;
import java.util.Map;

/** one compilations symbol table
 *  all declarations precede resolution */
public final class LabelRegistry {
    private final Map<String, Label> labels = new HashMap<>();

    public void declare(String name, int lineNumber) {
        if (name.isEmpty()) {
            throw new CompilationException("Label name cannot be empty.", lineNumber);
        }
        if (labels.putIfAbsent(name, new Label()) != null) {
            throw new CompilationException("Duplicate label: '" + name + "'.", lineNumber);
        }
    }

    public Label resolve(String name, int lineNumber) {
        Label label = labels.get(name);
        if (label == null) {
            throw new CompilationException("Undefined label: '" + name + "'.", lineNumber);
        }
        return label;
    }
}
