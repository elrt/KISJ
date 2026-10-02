package keep.it.simple.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Character-based scanner. Each instance owns a single source cursor. */
public final class KissLexer {
    private final String source;
    private int offset;
    private int line = 1;
    private int column = 1;
    private boolean commandExpected = true;
    private boolean labelExpected;

    public KissLexer(String source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = nextToken();
            tokens.add(token);
        } while (token.getKind() != Token.Kind.EOF);
        return Collections.unmodifiableList(tokens);
    }

    public Token nextToken() {
        while (!atEnd() && isHorizontalSpace(peek())) {
            advance();
        }
        if (!atEnd() && peek() == '#') {
            while (!atEnd() && !isNewline(peek())) {
                advance();
            }
        }
        int startLine = line;
        int startColumn = column;
        if (atEnd()) {
            return new Token(Token.Kind.EOF, "", line, column);
        }
        if (isNewline(peek())) {
            char newline = advance();
            if (newline == '\r' && !atEnd() && peek() == '\n') {
                advance();
            }
            line++;
            column = 1;
            commandExpected = true;
            labelExpected = false;
            return new Token(Token.Kind.NEWLINE, "", startLine, startColumn);
        }
        if (commandExpected) {
            commandExpected = false;
            char first = advance();
            if (first == ':') {
                labelExpected = true;
                return new Token(Token.Kind.COLON, ":", startLine, startColumn);
            }
            StringBuilder command = new StringBuilder().append(first);
            if (!atEnd() && isCommandPair(first, peek())) {
                command.append(advance());
            }
            String name = command.toString();
            labelExpected = name.equals("G") || name.equals("CL");
            return new Token(Token.Kind.COMMAND, name, startLine, startColumn);
        }
        if (labelExpected) {
            labelExpected = false;
            return readLabel(startLine, startColumn);
        }
        if (peek() == '"') {
            return readString(startLine, startColumn);
        }
        return readAtom(startLine, startColumn);
    }

    private Token readLabel(int startLine, int startColumn) {
        StringBuilder value = new StringBuilder();
        StringBuilder pendingSpace = new StringBuilder();
        while (!atEnd() && !isNewline(peek()) && peek() != '#') {
            char character = advance();
            if (isHorizontalSpace(character)) {
                pendingSpace.append(character);
            } else {
                value.append(pendingSpace).append(character);
                pendingSpace.setLength(0);
            }
        }
        return new Token(Token.Kind.LABEL_NAME, value.toString(), startLine, startColumn);
    }

    private Token readString(int startLine, int startColumn) {
        advance(); // opening quote!!
        StringBuilder value = new StringBuilder();
        while (!atEnd() && !isNewline(peek())) {
            char character = advance();
            if (character == '"') {
                return new Token(Token.Kind.STRING, value.toString(), startLine, startColumn);
            }
            if (character == '\\' && !atEnd() && !isNewline(peek())) {
                char escaped = peek();
                if (escaped == 'n') {
                    advance();
                    value.append('\n');
                } else if (escaped == 't') {
                    advance();
                    value.append('\t');
                } else {
                    value.append('\\');
                }
            } else {
                value.append(character);
            }
        }
        throw new CompilationException("Unterminated string literal.", startLine, startColumn);
    }

    private Token readAtom(int startLine, int startColumn) {
        StringBuilder value = new StringBuilder();
        boolean integer = true;
        boolean hasDigit = false;
        while (!atEnd() && !isHorizontalSpace(peek()) && !isNewline(peek())
                && peek() != '#' && peek() != '"') {
            char character = advance();
            boolean sign = value.length() == 0 && (character == '+' || character == '-');
            boolean digit = character >= '0' && character <= '9';
            integer &= sign || digit;
            hasDigit |= digit;
            value.append(character);
        }
        Token.Kind kind = integer && hasDigit ? Token.Kind.INTEGER : Token.Kind.ATOM;
        return new Token(kind, value.toString(), startLine, startColumn);
    }

    private boolean isCommandPair(char first, char second) {
        return (first == 'C' && (second == 'L' || second == 'P' || second == 'I'))
                || (first == 'P' && second == 'R')
                || ((first == 'H' || first == 'R') && second == 'T');
    }

    private boolean isHorizontalSpace(char character) {
        return character <= ' ' && !isNewline(character);
    }

    private boolean isNewline(char character) {
        return character == '\n' || character == '\r';
    }

    private boolean atEnd() { return offset == source.length(); }
    private char peek() { return source.charAt(offset); }
    private char advance() {
        column++;
        return source.charAt(offset++);
    }
}
