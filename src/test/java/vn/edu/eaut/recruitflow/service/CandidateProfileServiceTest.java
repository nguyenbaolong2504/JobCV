package vn.edu.eaut.recruitflow.service;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.model.CandidateProfile;

import java.math.BigDecimal;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CandidateProfileServiceTest {
    private final CandidateProfileService service = new CandidateProfileService();

    @Test
    void emptyProfileStartsAtZeroPercent() {
        assertEquals(0, service.completion(new CandidateProfile()));
    }

    @Test
    void completeCareerProfileReachesOneHundredPercent() {
        CandidateProfile profile = new CandidateProfile();
        profile.setDateOfBirth(Date.valueOf("2000-01-01"));
        profile.setAddress("Hà Nội");
        profile.setUniversity("EAUT");
        profile.setMajor("Công nghệ thông tin");
        profile.setSkills("Java, MySQL");
        profile.setSummary("Kỹ sư phần mềm");
        profile.setExperienceYears(2);
        profile.setAvatarPath("avatar.webp");
        profile.setPhone("0912345678");
        profile.setTargetPosition("Java Developer");
        profile.setTargetLocation("Hà Nội");
        profile.setExpectedSalary(new BigDecimal("18000000"));
        profile.setCareerGoal("Phát triển sản phẩm có giá trị cho người dùng.");
        profile.setCertificates("TOEIC 800");

        assertEquals(100, service.completion(profile));
    }
}
