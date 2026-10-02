package keep.it.simple.java;

import java.util.Objects;

/** a lexical token with a one-based source location */
public final class Token {
    public enum Kind { COMMAND, COLON, LABEL_NAME, INTEGER, STRING, ATOM, NEWLINE, EOF }

    private final Kind kind;
    private final String text;
    private final int line;
    private final int column;

    public Token(Kind kind, String text, int line, int column) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.text = Objects.requireNonNull(text, "text");
        this.line = line;
        this.column = column;
    }

    public Kind getKind() { return kind; }
    public String getText() { return text; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
}
