package vn.edu.eaut.recruitflow.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VietnameseTextUtilTest {
    @Test
    void repairsLegacyMojibakeFromCandidateProfile() {
        assertEquals("Hà Nội, Nhà Riêng",
                VietnameseTextUtil.repairLegacyMojibake("HÃ  Ná»™i, NhÃ  RiÃªng"));
        assertEquals("Công nghệ thông tin",
                VietnameseTextUtil.repairLegacyMojibake("CÃ´ng nghá»‡ thÃ´ng tin"));
    }

    @Test
    void keepsCleanVietnameseTextUnchanged() {
        assertEquals("Hà Nội, Nhà Riêng",
                VietnameseTextUtil.repairLegacyMojibake("Hà Nội, Nhà Riêng"));
        assertEquals("Java, Spring Boot, MySQL",
                VietnameseTextUtil.repairLegacyMojibake("Java, Spring Boot, MySQL"));
    }

    @Test
    void repairsRepeatedMojibakeWithoutDamagingLegitimateVietnamese() {
        Charset windows1252 = Charset.forName("windows-1252");
        String once = new String("Hà Nội, Nhà Riêng".getBytes(StandardCharsets.UTF_8), windows1252);
        String twice = new String(once.getBytes(StandardCharsets.UTF_8), windows1252);

        assertEquals("Hà Nội, Nhà Riêng", VietnameseTextUtil.repairLegacyMojibake(twice));
        assertEquals("TRÍ TUỆ NHÂN TẠO",
                VietnameseTextUtil.repairLegacyMojibake("TRÍ TUỆ NHÂN TẠO"));
    }

    @Test
    void repairsMojibakeEmbeddedInsideCleanVietnameseSentence() {
        assertEquals("Đơn ứng tuyển cho vị trí Quản lý nhân sự đã được ghi nhận.",
                VietnameseTextUtil.repairLegacyMojibake(
                        "Đơn ứng tuyển cho vị trí Quáº£n lÃ½ nhÃ¢n sá»± đã được ghi nhận."));
    }
}
