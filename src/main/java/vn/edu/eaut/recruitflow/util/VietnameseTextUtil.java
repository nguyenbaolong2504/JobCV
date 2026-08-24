package vn.edu.eaut.recruitflow.util;

import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/** Repairs legacy UTF-8 text that was accidentally decoded as Windows-1252/Latin-1. */
public final class VietnameseTextUtil {
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    private VietnameseTextUtil() {
    }

    public static String repairLegacyMojibake(String value) {
        if (value == null || value.isBlank() || !looksLikeMojibake(value)) {
            return value;
        }

        String best = value;
        for (int pass = 0; pass < 3; pass++) {
            String repaired = repairOnePass(best);
            if (repaired.equals(best)) {
                break;
            }
            best = repaired;
        }
        return best;
    }

    private static String repairOnePass(String value) {
        String best = value;
        int bestScore = mojibakeScore(value);
        for (Charset charset : new Charset[]{WINDOWS_1252, StandardCharsets.ISO_8859_1}) {
            String candidate = decodeStrict(value, charset);
            if (candidate == null) {
                candidate = decodeEncodableSegments(value, charset);
            }
            if (candidate == null) {
                continue;
            }
            int candidateScore = mojibakeScore(candidate);
            if (candidateScore < bestScore) {
                best = candidate;
                bestScore = candidateScore;
            }
        }
        return best;
    }

    private static String decodeEncodableSegments(String value, Charset sourceCharset) {
        StringBuilder result = new StringBuilder(value.length());
        StringBuilder segment = new StringBuilder();
        for (int index = 0; index <= value.length(); index++) {
            boolean encodable = index < value.length()
                    && sourceCharset.newEncoder().canEncode(value.charAt(index));
            if (encodable) {
                segment.append(value.charAt(index));
                continue;
            }
            if (!segment.isEmpty()) {
                String original = segment.toString();
                String decoded = looksLikeMojibake(original) ? decodeStrict(original, sourceCharset) : null;
                if (decoded == null && looksLikeMojibake(original)) {
                    decoded = decodeMarkedTokens(original, sourceCharset);
                }
                result.append(decoded != null && mojibakeScore(decoded) < mojibakeScore(original) ? decoded : original);
                segment.setLength(0);
            }
            if (index < value.length()) {
                result.append(value.charAt(index));
            }
        }
        String candidate = result.toString();
        return candidate.equals(value) ? null : candidate;
    }

    private static String decodeMarkedTokens(String value, Charset sourceCharset) {
        StringBuilder result = new StringBuilder(value.length());
        int start = 0;
        boolean changed = false;
        for (int index = 0; index <= value.length(); index++) {
            boolean delimiter = index == value.length()
                    || (value.charAt(index) <= 0x7F && Character.isWhitespace(value.charAt(index)));
            if (!delimiter) {
                continue;
            }
            if (index > start) {
                String token = value.substring(start, index);
                String decoded = looksLikeMojibake(token) ? decodeStrict(token, sourceCharset) : null;
                if (decoded != null && mojibakeScore(decoded) < mojibakeScore(token)) {
                    result.append(decoded);
                    changed = true;
                } else {
                    result.append(token);
                }
            }
            if (index < value.length()) {
                result.append(value.charAt(index));
            }
            start = index + 1;
        }
        return changed ? result.toString() : null;
    }

    private static String decodeStrict(String value, Charset sourceCharset) {
        try {
            ByteBuffer bytes = sourceCharset.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(value));
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(bytes)
                    .toString();
        } catch (CharacterCodingException | RuntimeException ex) {
            return null;
        }
    }

    private static boolean looksLikeMojibake(String value) {
        return mojibakeScore(value) > 0;
    }

    private static int mojibakeScore(String value) {
        int score = 0;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '\uFFFD' || current == '\u00C3' || current == '\u00C2'
                    || current == '\u00C4' || current == '\u00C6'
                    || current == '\u009D' || current == '\u009C'
                    || current == '\u0081' || current == '\u008F') {
                score += 3;
            } else if (current == '\u00E1' && index + 1 < value.length()
                    && (value.charAt(index + 1) == '\u00BA' || value.charAt(index + 1) == '\u00BB')) {
                score += 4;
            } else if (current >= '\u0080' && current <= '\u009F') {
                score += 2;
            } else if (current == '\u00BA' || current == '\u00BB' || current == '\u00A0' || current == '\u00A1') {
                score++;
            }
        }
        return score;
    }
}
