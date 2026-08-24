package vn.edu.eaut.recruitflow.encoding;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class TextEncodingTest {
    private static final Pattern MOJIBAKE = Pattern.compile(
            "Ã[\\u00A0-\\u00BF]|Â[\\u00A0-\\u00BF]|Ä[\\u00A0-\\u00BF]|Æ[\\u00A0-\\u00BF]|"
                    + "Ă[\\u00A0-\\u00BF]|á[»º]|â€|ðŸ|ï¿½|�");
    private static final List<String> TEXT_EXTENSIONS = List.of(".java", ".jsp", ".js", ".sql");

    @Test
    void userFacingSourceDoesNotContainKnownMojibake() throws IOException {
        assertNoMojibake(Path.of("src", "main"));
        assertNoMojibake(Path.of("schema.sql"));
    }

    @Test
    void everyJspDeclaresUtf8PageEncoding() throws IOException {
        Path views = Path.of("src", "main", "webapp", "WEB-INF", "views");
        try (Stream<Path> paths = Files.walk(views)) {
            paths.filter(path -> path.toString().endsWith(".jsp")).forEach(path -> {
                String content = read(path);
                assertTrue(content.contains("pageEncoding=\"UTF-8\""),
                        () -> path + " must declare UTF-8 page encoding");
            });
        }
    }

    @Test
    void sharedJavascriptIsAsciiSafeForWindowsBrowsers() throws IOException {
        Path appJs = Path.of("src", "main", "webapp", "assets", "js", "app.js");
        String content = Files.readString(appJs, StandardCharsets.UTF_8);

        assertTrue(content.codePoints().allMatch(codePoint -> codePoint <= 127),
                "app.js must use Unicode escapes for non-ASCII interface text");
        assertTrue(content.contains("\\u0110"), "Vietnamese labels must remain encoded as Unicode escapes");
    }

    private static void assertNoMojibake(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            assertClean(path);
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                    .filter(TextEncodingTest::isTextSource)
                    .forEach(TextEncodingTest::assertClean);
        }
    }

    private static boolean isTextSource(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return TEXT_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private static void assertClean(Path path) {
        String content = read(path);
        assertFalse(MOJIBAKE.matcher(content).find(), () -> "Mojibake detected in " + path);
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + path, exception);
        }
    }
}
