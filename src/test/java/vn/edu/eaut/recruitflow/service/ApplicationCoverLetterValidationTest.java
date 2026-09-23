package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.util.BusinessException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationCoverLetterValidationTest {
    @Test
    void rejectsTooShortCoverLetterBeforeDatabaseWork() {
        ApplicationService service = new ApplicationService();
        assertThrows(BusinessException.class, () -> service.apply(1, 1, (Integer) null, "Quá ngắn"));
    }
}
