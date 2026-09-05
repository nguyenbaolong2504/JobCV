package vn.edu.eaut.recruitflow.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

class ResumeStorageUtilTest {
    @Test
    void resolvesPortableFilenameInsideOwnedDirectory() throws Exception {
        Path root = Path.of("target", "resume-storage-test", UUID.randomUUID().toString())
                .toAbsolutePath().normalize();
        Files.createDirectories(root);
        Path resume = root.resolve("resume_9_test.pdf");
        try {
            Files.writeString(resume, "%PDF-1.7\n%%EOF");

            assertEquals(resume.toRealPath(), ResumeStorageUtil.resolveStoredFile(root, resume.getFileName().toString()));
        } finally {
            Files.deleteIfExists(resume);
            Files.deleteIfExists(root);
        }
    }

    @Test
    void rejectsExistingFileOutsideOwnedDirectory() throws Exception {
        Path root = Path.of("target", "resume-storage-test", UUID.randomUUID().toString())
                .toAbsolutePath().normalize();
        Path outside = Path.of("target", "outside-" + UUID.randomUUID() + ".pdf")
                .toAbsolutePath().normalize();
        Files.createDirectories(root);
        try {
            Files.writeString(outside, "%PDF-1.7\n%%EOF");

            assertThrows(BusinessException.class,
                    () -> ResumeStorageUtil.resolveStoredFile(root, outside.toString()));
        } finally {
            Files.deleteIfExists(outside);
            Files.deleteIfExists(root);
        }
    }
}
