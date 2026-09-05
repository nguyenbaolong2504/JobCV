package vn.edu.eaut.recruitflow.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

class UploadUtilTest {
    @Test
    void acceptsPdfHeaderAfterLeadingLineBreak() throws Exception {
        byte[] pdf = "\n%PDF-1.7\n1 0 obj\n<<>>\nendobj\n%%EOF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        Part part = new InMemoryPart("cv.pdf", "application/pdf", pdf);

        UploadUtil.validateResumePart(part);
    }

    @Test
    void rejectsPdfMarkerAfterUnsafeContentPrefix() {
        byte[] polyglot = "<html>%PDF-1.7\n%%EOF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        Part part = new InMemoryPart("cv.pdf", "application/pdf", polyglot);

        assertThrows(BusinessException.class, () -> UploadUtil.validateResumePart(part));
    }

    @Test
    void acceptsAndStoresARealPngSignature() throws Exception {
        byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        Part part = new InMemoryPart("avatar.png", "image/png", png);
        Path temporaryDirectory = Path.of("target", "avatar-test", UUID.randomUUID().toString())
                .toAbsolutePath().normalize();

        Path stored = null;
        try {
            stored = UploadUtil.storeAvatar(part, 42, temporaryDirectory);
            assertTrue(stored.startsWith(temporaryDirectory));
            assertTrue(Files.isRegularFile(stored));
            assertTrue(stored.getFileName().toString().matches("avatar_42_.+\\.png"));
            assertEquals(png.length, Files.size(stored));
        } finally {
            if (stored != null) {
                Files.deleteIfExists(stored);
            }
            Files.deleteIfExists(temporaryDirectory);
        }
    }

    @Test
    void rejectsSpoofedOrUnsupportedAvatarFiles() {
        Part spoofedPng = new InMemoryPart("avatar.png", "image/png", "not an image".getBytes());
        Part svg = new InMemoryPart("avatar.svg", "image/svg+xml", "<svg/>".getBytes());

        assertThrows(BusinessException.class, () -> UploadUtil.validateAvatarPart(spoofedPng));
        assertThrows(BusinessException.class, () -> UploadUtil.validateAvatarPart(svg));
    }

    private static final class InMemoryPart implements Part {
        private final String fileName;
        private final String contentType;
        private final byte[] bytes;

        private InMemoryPart(String fileName, String contentType, byte[] bytes) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return "avatarFile";
        }

        @Override
        public String getSubmittedFileName() {
            return fileName;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public void write(String fileName) throws IOException {
            throw new IOException("Not supported by test part");
        }

        @Override
        public void delete() {
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return List.of();
        }

        @Override
        public Collection<String> getHeaderNames() {
            return List.of();
        }
    }
}
