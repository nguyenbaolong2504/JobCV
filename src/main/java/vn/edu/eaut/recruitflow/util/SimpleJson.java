package vn.edu.eaut.recruitflow.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small dependency-free JSON reader/writer for the OpenAI-compatible integration.
 * It supports the JSON data types needed by this application and deliberately has no
 * object-to-bean magic or reflection.
 */
public final class SimpleJson {
    private SimpleJson() {
    }

    public static String stringify(Object value) {
        StringBuilder result = new StringBuilder();
        appendJson(result, value);
        return result.toString();
    }

    public static Object parse(String source) {
        if (source == null) {
            throw new JsonParseException("JSON source is missing.");
        }
        Parser parser = new Parser(source);
        Object value = parser.readValue(0);
        parser.skipWhitespace();
        if (!parser.isEnd()) {
            throw parser.error("Unexpected content after JSON value.");
        }
        return value;
    }

    private static void appendJson(StringBuilder target, Object value) {
        if (value == null) {
            target.append("null");
        } else if (value instanceof String text) {
            appendString(target, text);
        } else if (value instanceof Number || value instanceof Boolean) {
            target.append(value);
        } else if (value instanceof Map<?, ?> map) {
            target.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw new IllegalArgumentException("JSON object keys must be strings.");
                }
                if (!first) {
                    target.append(',');
                }
                appendString(target, key);
                target.append(':');
                appendJson(target, entry.getValue());
                first = false;
            }
            target.append('}');
        } else if (value instanceof Iterable<?> values) {
            target.append('[');
            boolean first = true;
            for (Object item : values) {
                if (!first) {
                    target.append(',');
                }
                appendJson(target, item);
                first = false;
            }
            target.append(']');
        } else {
            throw new IllegalArgumentException("Unsupported JSON value type: " + value.getClass().getName());
        }
    }

    private static void appendString(StringBuilder target, String value) {
        target.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> target.append("\\\"");
                case '\\' -> target.append("\\\\");
                case '\b' -> target.append("\\b");
                case '\f' -> target.append("\\f");
                case '\n' -> target.append("\\n");
                case '\r' -> target.append("\\r");
                case '\t' -> target.append("\\t");
                default -> {
                    if (character < 0x20) {
                        target.append(String.format("\\u%04x", (int) character));
                    } else {
                        target.append(character);
                    }
                }
            }
        }
        target.append('"');
    }

    public static final class JsonParseException extends IllegalArgumentException {
        public JsonParseException(String message) {
            super(message);
        }
    }

    private static final class Parser {
        private static final int MAX_DEPTH = 64;
        private final String source;
        private int position;

        private Parser(String source) {
            this.source = source;
        }

        private Object readValue(int depth) {
            if (depth > MAX_DEPTH) {
                throw error("JSON nesting is too deep.");
            }
            skipWhitespace();
            if (isEnd()) {
                throw error("Unexpected end of JSON.");
            }
            return switch (source.charAt(position)) {
                case '{' -> readObject(depth + 1);
                case '[' -> readArray(depth + 1);
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject(int depth) {
            expect('{');
            skipWhitespace();
            Map<String, Object> result = new LinkedHashMap<>();
            if (consume('}')) {
                return result;
            }
            while (true) {
                skipWhitespace();
                if (isEnd() || source.charAt(position) != '"') {
                    throw error("Expected a JSON object key.");
                }
                String key = readString();
                skipWhitespace();
                expect(':');
                Object value = readValue(depth);
                result.put(key, value);
                skipWhitespace();
                if (consume('}')) {
                    return result;
                }
                expect(',');
            }
        }

        private List<Object> readArray(int depth) {
            expect('[');
            skipWhitespace();
            List<Object> result = new ArrayList<>();
            if (consume(']')) {
                return result;
            }
            while (true) {
                result.add(readValue(depth));
                skipWhitespace();
                if (consume(']')) {
                    return result;
                }
                expect(',');
            }
        }

        private String readString() {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (!isEnd()) {
                char character = source.charAt(position++);
                if (character == '"') {
                    return result.toString();
                }
                if (character == '\\') {
                    if (isEnd()) {
                        throw error("Unterminated escape sequence.");
                    }
                    char escaped = source.charAt(position++);
                    switch (escaped) {
                        case '"', '\\', '/' -> result.append(escaped);
                        case 'b' -> result.append('\b');
                        case 'f' -> result.append('\f');
                        case 'n' -> result.append('\n');
                        case 'r' -> result.append('\r');
                        case 't' -> result.append('\t');
                        case 'u' -> result.append(readUnicodeEscape());
                        default -> throw error("Invalid escape sequence.");
                    }
                } else {
                    if (character < 0x20) {
                        throw error("Control characters must be escaped in strings.");
                    }
                    result.append(character);
                }
            }
            throw error("Unterminated JSON string.");
        }

        private char readUnicodeEscape() {
            if (position + 4 > source.length()) {
                throw error("Incomplete unicode escape.");
            }
            int codePoint = 0;
            for (int index = 0; index < 4; index++) {
                char hex = source.charAt(position++);
                int digit = Character.digit(hex, 16);
                if (digit < 0) {
                    throw error("Invalid unicode escape.");
                }
                codePoint = (codePoint << 4) | digit;
            }
            return (char) codePoint;
        }

        private Object readLiteral(String literal, Object value) {
            if (!source.startsWith(literal, position)) {
                throw error("Invalid JSON literal.");
            }
            position += literal.length();
            return value;
        }

        private BigDecimal readNumber() {
            int start = position;
            if (consume('-') && isEnd()) {
                throw error("Invalid JSON number.");
            }
            if (consume('0')) {
                // A leading zero must be the entire integer part.
            } else {
                readDigits();
            }
            if (consume('.')) {
                int beforeFraction = position;
                readDigits();
                if (beforeFraction == position) {
                    throw error("Invalid JSON number.");
                }
            }
            if (consume('e') || consume('E')) {
                consume('+');
                consume('-');
                int beforeExponent = position;
                readDigits();
                if (beforeExponent == position) {
                    throw error("Invalid JSON number.");
                }
            }
            if (start == position) {
                throw error("Expected JSON value.");
            }
            try {
                return new BigDecimal(source.substring(start, position));
            } catch (NumberFormatException exception) {
                throw error("Invalid JSON number.");
            }
        }

        private void readDigits() {
            int initial = position;
            while (!isEnd() && Character.isDigit(source.charAt(position))) {
                position++;
            }
            if (initial == position) {
                throw error("Invalid JSON number.");
            }
        }

        private void expect(char expected) {
            skipWhitespace();
            if (isEnd() || source.charAt(position) != expected) {
                throw error("Expected '" + expected + "'.");
            }
            position++;
        }

        private boolean consume(char expected) {
            if (!isEnd() && source.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void skipWhitespace() {
            while (!isEnd() && Character.isWhitespace(source.charAt(position))) {
                position++;
            }
        }

        private boolean isEnd() {
            return position >= source.length();
        }

        private JsonParseException error(String message) {
            return new JsonParseException(message + " At position " + position + '.');
        }
    }
}
