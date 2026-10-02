package keep.it.simple.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Token based frontend
 * The rule table describes only existing KISS commands */
public final class KissParser {

    private final CompilerConfig config;
    private final Map<String, Rule> rules;

    public KissParser(CompilerConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.rules = createRules();
    }

    public CompilerConfig getConfig() {
        return config;
    }

    public List<Instruction> parse(List<String> sourceLines) {
        return parse(String.join("\n", sourceLines));
    }

    public List<Instruction> parse(String source) {
        Cursor cursor = new Cursor(new KissLexer(source));
        List<Instruction> instructions = new ArrayList<>();
        while (!cursor.is(Token.Kind.EOF)) {
            if (cursor.is(Token.Kind.NEWLINE)) {
                cursor.take();
                continue;
            }
            Token command = cursor.take();
            if (command.getKind() == Token.Kind.COLON) {
                instructions.add(new LabelInstruction(cursor.label(), command.getLine()));
            } else {
                Rule rule = rules.get(command.getText());
                if (rule == null) {
                    throw cursor.error(command, "Unknown command: '" + command.getText() + "'.");
                }
                instructions.add(rule.parse(cursor, command));
            }
            cursor.endOfLine();
        }
        return Collections.unmodifiableList(instructions);
    }

    private Map<String, Rule> createRules() {
        Map<String, Rule> result = new HashMap<>();
        arithmetic(result, "A", ArithmeticInstruction.Operation.ASSIGN, true);
        arithmetic(result, "I", ArithmeticInstruction.Operation.ADD, true);
        arithmetic(result, "D", ArithmeticInstruction.Operation.SUBTRACT, true);
        arithmetic(result, "M", ArithmeticInstruction.Operation.MULTIPLY, true);
        arithmetic(result, "Q", ArithmeticInstruction.Operation.DIVIDE, true);
        arithmetic(result, "R", ArithmeticInstruction.Operation.REMAINDER, true);
        arithmetic(result, "^", ArithmeticInstruction.Operation.SQUARE, true);
        arithmetic(result, "t", ArithmeticInstruction.Operation.SQRT, true);
        arithmetic(result, "?", ArithmeticInstruction.Operation.RANDOM, true);
        arithmetic(result, "C", ArithmeticInstruction.Operation.CLEAR, false);
        arithmetic(result, "N", ArithmeticInstruction.Operation.NEGATE, false);
        arithmetic(result, "B", ArithmeticInstruction.Operation.COMPLEMENT, false);
        result.put("Y", (cursor, command) -> new RegisterInstruction(
                RegisterInstruction.Operation.COPY_TO_Y, 0, command.getLine()));
        result.put("V", (cursor, command) -> new RegisterInstruction(
                RegisterInstruction.Operation.COPY_TO_X, 0, command.getLine()));
        result.put("S", (cursor, command) -> new RegisterInstruction(
                RegisterInstruction.Operation.SAVE_AND_ASSIGN, cursor.integer(), command.getLine()));
        result.put(">", (cursor, command) -> cursor.memory(MemoryInstruction.Access.STORE, command));
        result.put("<", (cursor, command) -> cursor.memory(MemoryInstruction.Access.LOAD, command));
        result.put("E", (cursor, command) -> new BranchInstruction(
                BranchInstruction.Condition.EQUAL, cursor.integer(), command.getLine()));
        result.put("L", (cursor, command) -> new BranchInstruction(
                BranchInstruction.Condition.LESS_THAN, cursor.integer(), command.getLine()));
        result.put("G", (cursor, command) -> new GotoInstruction(cursor.label(), command.getLine()));
        result.put("CL", (cursor, command) -> new CallInstruction(cursor.label(), command.getLine()));
        result.put("RT", (cursor, command) -> new ReturnInstruction(command.getLine()));
        result.put("HT", (cursor, command) -> new HaltInstruction(command.getLine()));
        result.put("P", (cursor, command) -> new IoInstruction(IoInstruction.Operation.PRINT_NUMBER, command.getLine()));
        result.put("CP", (cursor, command) -> new IoInstruction(IoInstruction.Operation.PRINT_CHARACTER, command.getLine()));
        result.put("CI", (cursor, command) -> new IoInstruction(IoInstruction.Operation.READ_CHARACTER, command.getLine()));
        result.put("PR", (cursor, command) -> new PrintStringInstruction(
                cursor.require(Token.Kind.STRING, "PR requires a double-quoted string.").getText(), command.getLine()));
        return Collections.unmodifiableMap(result);
    }

    private void arithmetic(Map<String, Rule> result, String name,
                            ArithmeticInstruction.Operation operation, boolean takesInteger) {
        result.put(name, (cursor, command) -> new ArithmeticInstruction(
                operation, takesInteger ? cursor.integer() : 0, command.getLine()));
    }

    @FunctionalInterface
    private interface Rule {
        Instruction parse(Cursor cursor, Token command);
    }

    private static final class Cursor {
        private final KissLexer lexer;
        private Token current;

        private Cursor(KissLexer lexer) {
            this.lexer = lexer;
            current = lexer.nextToken();
        }

        private boolean is(Token.Kind kind) {
            return current.getKind() == kind;
        }

        private Token take() {
            Token result = current;
            current = lexer.nextToken();
            return result;
        }

        private Token require(Token.Kind kind, String message) {
            if (!is(kind)) {
                throw error(current, message);
            }
            return take();
        }

        private int integer() {
            if (is(Token.Kind.ATOM)) {
                String text = current.getText();
                if (text.startsWith("'") && text.endsWith("'") && text.length() >= 3) {
                    take();
                    String inner = text.substring(1, text.length() - 1);
                    if (inner.equals("\\n")) return '\n';
                    if (inner.equals("\\t")) return '\t';
                    if (inner.equals("\\r")) return '\r';
                    if (inner.equals("\\0")) return 0;
                    if (inner.equals("\\\\")) return '\\';
                    if (inner.equals("\\'")) return '\'';
                    if (inner.length() == 1) return inner.charAt(0);
                    throw error(current, "Invalid character literal: " + text);
                }
            }
            Token token = require(Token.Kind.INTEGER, "Expected a signed 32-bit integer or character literal.");
            try {
                return Integer.parseInt(token.getText());
            } catch (NumberFormatException exception) {
                throw error(token, "Integer is outside the signed 32-bit range: '" + token.getText() + "'.");
            }
        }

        private String label() {
            return require(Token.Kind.LABEL_NAME, "Expected a non-empty label name.").getText();
        }

        private Instruction memory(MemoryInstruction.Access access, Token command) {
            if (is(Token.Kind.ATOM) && current.getText().equals("Y")) {
                take();
                return new MemoryInstruction(access, command.getLine());
            }
            return new MemoryInstruction(access, integer(), command.getLine());
        }

        private void endOfLine() {
            if (!is(Token.Kind.NEWLINE) && !is(Token.Kind.EOF)) {
                throw error(current, "Unexpected argument: '" + current.getText() + "'.");
            }
        }

        private CompilationException error(Token token, String message) {
            return new CompilationException(message, token.getLine(), token.getColumn());
        }
    }
}