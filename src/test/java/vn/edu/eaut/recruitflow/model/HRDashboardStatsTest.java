package vn.edu.eaut.recruitflow.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HRDashboardStatsTest {

    @Test
    void percentageValuesAreKeptInsideValidRange() {
        HRDashboardStats stats = new HRDashboardStats();

        stats.setHiringRate(125);
        stats.setOfferAcceptanceRate(-8);

        assertEquals(100, stats.getHiringRate());
        assertEquals(0, stats.getOfferAcceptanceRate());
    }

    @Test
    void chartDataIsDefensivelyCopied() {
        HRDashboardStats stats = new HRDashboardStats();
        Map<String, Long> source = new LinkedHashMap<>();
        source.put("08/2026", 12L);

        stats.setApplicationsByMonth(source);
        source.put("09/2026", 20L);

        assertEquals(Map.of("08/2026", 12L), stats.getApplicationsByMonth());
        assertNotSame(source, stats.getApplicationsByMonth());
    }

    @Test
    void nullChartDataBecomesAnEmptyMap() {
        HRDashboardStats stats = new HRDashboardStats();

        stats.setApplicationsByMonth(null);
        stats.setApplicationStatus(null);
        stats.setRecruitmentFunnel(null);

        assertTrue(stats.getApplicationsByMonth().isEmpty());
        assertTrue(stats.getApplicationStatus().isEmpty());
        assertTrue(stats.getRecruitmentFunnel().isEmpty());
    }
}
