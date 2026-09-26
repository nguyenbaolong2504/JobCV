package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.model.Interview;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterviewServiceValidationTest {
    private final InterviewService service = new InterviewService();

    @Test
    void rejectsOfflineInterviewWithoutLocation() {
        Interview interview = validInterview("OFFLINE");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateInterview(interview));

        assertEquals("Địa điểm phỏng vấn trực tiếp là bắt buộc.", exception.getMessage());
    }

    @Test
    void acceptsOfflineInterviewWithLocation() {
        Interview interview = validInterview("OFFLINE");
        interview.setLocation("Phòng 501, tòa nhà A");

        assertDoesNotThrow(() -> service.validateInterview(interview));
    }

    @Test
    void rejectsOnlineInterviewWithoutMeetingUrl() {
        Interview interview = validInterview("ONLINE");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateInterview(interview));

        assertEquals("Phỏng vấn online cần có đường dẫn cuộc họp.", exception.getMessage());
    }

    private Interview validInterview(String type) {
        Interview interview = new Interview();
        interview.setApplicationId(1);
        interview.setInterviewerId(1);
        interview.setInterviewType(type);
        interview.setInterviewDate(Date.valueOf(LocalDate.now().plusDays(2)));
        interview.setStartTime(Time.valueOf("09:00:00"));
        interview.setEndTime(Time.valueOf("10:00:00"));
        return interview;
    }
}
