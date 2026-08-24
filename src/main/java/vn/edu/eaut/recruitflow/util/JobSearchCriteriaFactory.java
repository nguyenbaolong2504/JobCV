package vn.edu.eaut.recruitflow.util;

import vn.edu.eaut.recruitflow.enums.EmploymentType;
import vn.edu.eaut.recruitflow.model.JobSearchCriteria;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Parses and validates the public/candidate job-search query string once. */
public final class JobSearchCriteriaFactory {
    private static final BigDecimal MAX_SALARY = new BigDecimal("9999999999999.99");
    private static final int MAX_KEYWORD_LENGTH = 150;
    private static final int MAX_TITLE_LENGTH = 150;
    private static final int MAX_LOCATION_LENGTH = 100;
    private static final int MAX_EXPERIENCE_YEARS = 100;

    private JobSearchCriteriaFactory() {
    }

    public static JobSearchCriteria fromRequest(HttpServletRequest request) throws BusinessException {
        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setKeyword(boundedText(request, "keyword", "Từ khóa", MAX_KEYWORD_LENGTH));
        criteria.setTitle(boundedText(request, "title", "Vị trí", MAX_TITLE_LENGTH));
        criteria.setDepartmentId(optionalPositiveInt(request, "departmentId", "Phòng ban"));
        criteria.setCategoryId(optionalPositiveInt(request, "categoryId", "Danh mục nghề nghiệp"));
        criteria.setLocation(boundedText(request, "location", "Địa điểm", MAX_LOCATION_LENGTH));
        criteria.setEmploymentType(optionalEmploymentType(RequestUtil.text(request, "employmentType")));
        criteria.setSalaryMin(optionalSalary(request, "salaryMin", "Mức lương từ"));
        criteria.setSalaryMax(optionalSalary(request, "salaryMax", "Mức lương đến"));
        criteria.setExperienceMin(optionalExperience(request, "experienceMin", "Kinh nghiệm từ"));
        criteria.setExperienceMax(optionalExperience(request, "experienceMax", "Kinh nghiệm đến"));
        criteria.setDeadlineFrom(optionalDate(request, "deadlineFrom", "Hạn nộp từ"));
        criteria.setDeadlineTo(optionalDate(request, "deadlineTo", "Hạn nộp đến"));

        if (criteria.getSalaryMin() != null && criteria.getSalaryMax() != null
                && criteria.getSalaryMin().compareTo(criteria.getSalaryMax()) > 0) {
            throw new BusinessException("Mức lương từ không được lớn hơn mức lương đến.");
        }
        if (criteria.getExperienceMin() != null && criteria.getExperienceMax() != null
                && criteria.getExperienceMin() > criteria.getExperienceMax()) {
            throw new BusinessException("Kinh nghiệm từ không được lớn hơn kinh nghiệm đến.");
        }
        if (criteria.getDeadlineFrom() != null && criteria.getDeadlineTo() != null
                && criteria.getDeadlineFrom().isAfter(criteria.getDeadlineTo())) {
            throw new BusinessException("Hạn nộp từ không được sau hạn nộp đến.");
        }
        return criteria;
    }

    private static String boundedText(HttpServletRequest request, String field, String label, int maxLength)
            throws BusinessException {
        String value = RequestUtil.text(request, field);
        if (value.length() > maxLength) {
            throw new BusinessException(label + " không được vượt quá " + maxLength + " ký tự.");
        }
        return value;
    }

    private static Integer optionalPositiveInt(HttpServletRequest request, String field, String label)
            throws BusinessException {
        return RequestUtil.text(request, field).isEmpty()
                ? null
                : RequestUtil.requiredPositiveInt(request, field, label);
    }

    private static String optionalEmploymentType(String rawEmploymentType) throws BusinessException {
        if (rawEmploymentType.isEmpty()) {
            return null;
        }
        try {
            return EmploymentType.fromValue(rawEmploymentType).name();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Loại hình làm việc không hợp lệ.");
        }
    }

    private static BigDecimal optionalSalary(HttpServletRequest request, String field, String label)
            throws BusinessException {
        String value = RequestUtil.text(request, field);
        if (value.isEmpty()) {
            return null;
        }
        try {
            BigDecimal salary = new BigDecimal(value);
            if (salary.compareTo(BigDecimal.ZERO) < 0 || salary.compareTo(MAX_SALARY) > 0) {
                throw new BusinessException(label + " không hợp lệ.");
            }
            return salary;
        } catch (NumberFormatException exception) {
            throw new BusinessException(label + " không hợp lệ.");
        }
    }

    private static Integer optionalExperience(HttpServletRequest request, String field, String label)
            throws BusinessException {
        String value = RequestUtil.text(request, field);
        if (value.isEmpty()) {
            return null;
        }
        try {
            int experience = Integer.parseInt(value);
            if (experience < 0 || experience > MAX_EXPERIENCE_YEARS) {
                throw new BusinessException(label + " phải từ 0 đến " + MAX_EXPERIENCE_YEARS + " năm.");
            }
            return experience;
        } catch (NumberFormatException exception) {
            throw new BusinessException(label + " không hợp lệ.");
        }
    }

    private static LocalDate optionalDate(HttpServletRequest request, String field, String label)
            throws BusinessException {
        String value = RequestUtil.text(request, field);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(label + " không hợp lệ.");
        }
    }
}
