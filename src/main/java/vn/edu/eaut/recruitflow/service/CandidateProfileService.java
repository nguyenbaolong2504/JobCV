package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.CandidateProfileDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.DBUtil;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;
import java.sql.Connection;

public class CandidateProfileService {
    private final CandidateProfileDAO profileDAO;
    private final UserDAO userDAO;

    public CandidateProfileService() {
        this(new CandidateProfileDAO(), new UserDAO());
    }

    CandidateProfileService(CandidateProfileDAO profileDAO, UserDAO userDAO) {
        this.profileDAO = profileDAO;
        this.userDAO = userDAO;
    }

    public CandidateProfile getProfile(int candidateId) throws BusinessException {
        try {
            CandidateProfile profile = profileDAO.findByUserId(candidateId);
            if (profile == null) {
                throw new BusinessException("Không tìm thấy hồ sơ ứng viên.");
            }
            return profile;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải hồ sơ ứng viên.", exception);
        }
    }

    public void updateProfile(int candidateId, CandidateProfile submitted) throws BusinessException {
        if (submitted == null) {
            throw new BusinessException("Thông tin hồ sơ không hợp lệ.");
        }
        if (submitted.getExperienceYears() < 0 || submitted.getExperienceYears() > 80) {
            throw new BusinessException("Số năm kinh nghiệm không hợp lệ.");
        }
        validateLength(submitted.getAddress(), 255, "Địa chỉ");
        validateLength(submitted.getUniversity(), 150, "Trường đại học");
        validateLength(submitted.getMajor(), 100, "Chuyên ngành");
        validateLength(submitted.getSkills(), 4000, "Kỹ năng");
        validateLength(submitted.getSummary(), 8000, "Giới thiệu");
        validateCareerFields(submitted);
        submitted.setUserId(candidateId);
        try {
            if (!profileDAO.update(submitted)) {
                throw new BusinessException("Không tìm thấy hồ sơ ứng viên.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật hồ sơ ứng viên.", exception);
        }
    }

    public void updateAvatar(int candidateId, String avatarPath) throws BusinessException {
        if (avatarPath != null && !avatarPath.matches("[A-Za-z0-9._-]{1,255}")) {
            throw new BusinessException("Tên tệp ảnh đại diện không hợp lệ.");
        }
        try {
            if (!profileDAO.updateAvatar(candidateId, avatarPath)) {
                throw new BusinessException("Không tìm thấy hồ sơ ứng viên.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật ảnh đại diện.", exception);
        }
    }

    /** Updates account display name and candidate data together for the inline profile form. */
    public void updateProfile(int candidateId, String fullName, CandidateProfile submitted) throws BusinessException {
        String normalizedName = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        if (normalizedName.length() < 2 || normalizedName.length() > 100) {
            throw new BusinessException("Họ và tên phải có từ 2 đến 100 ký tự.");
        }
        if (submitted == null) {
            throw new BusinessException("Thông tin hồ sơ không hợp lệ.");
        }
        if (submitted.getExperienceYears() < 0 || submitted.getExperienceYears() > 80) {
            throw new BusinessException("Số năm kinh nghiệm không hợp lệ.");
        }
        validateLength(submitted.getAddress(), 255, "Địa chỉ");
        validateLength(submitted.getUniversity(), 150, "Trường đại học");
        validateLength(submitted.getMajor(), 100, "Chuyên ngành");
        validateLength(submitted.getSkills(), 4000, "Kỹ năng");
        validateLength(submitted.getSummary(), 8000, "Giới thiệu");
        validateCareerFields(submitted);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                User user = userDAO.findById(connection, candidateId);
                if (user == null) {
                    throw new BusinessException("Không tìm thấy tài khoản ứng viên.");
                }
                user.setFullName(normalizedName);
                if (!userDAO.update(connection, user)) {
                    throw new BusinessException("Không thể cập nhật thông tin tài khoản.");
                }
                submitted.setUserId(candidateId);
                if (!profileDAO.update(connection, submitted)) {
                    throw new BusinessException("Không tìm thấy hồ sơ ứng viên.");
                }
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể cập nhật hồ sơ ứng viên.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để cập nhật hồ sơ.", exception);
        }
    }

    public int completion(CandidateProfile profile) {
        if (profile == null) {
            return 0;
        }
        int completed = 0;
        int total = 14;
        if (profile.getDateOfBirth() != null) completed++;
        if (notBlank(profile.getAddress())) completed++;
        if (notBlank(profile.getUniversity())) completed++;
        if (notBlank(profile.getMajor())) completed++;
        if (notBlank(profile.getSkills())) completed++;
        if (notBlank(profile.getSummary())) completed++;
        if (profile.getExperienceYears() > 0) completed++;
        if (notBlank(profile.getAvatarPath())) completed++;
        if (notBlank(profile.getPhone())) completed++;
        if (notBlank(profile.getTargetPosition())) completed++;
        if (notBlank(profile.getTargetLocation())) completed++;
        if (profile.getExpectedSalary() != null && profile.getExpectedSalary().signum() > 0) completed++;
        if (notBlank(profile.getCareerGoal())) completed++;
        if (notBlank(profile.getCertificates())) completed++;
        return Math.round(completed * 100f / total);
    }

    private void validateLength(String value, int max, String label) throws BusinessException {
        if (value != null && value.trim().length() > max) {
            throw new BusinessException(label + " không được quá " + max + " ký tự.");
        }
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private void validateCareerFields(CandidateProfile profile) throws BusinessException {
        validateLength(profile.getPhone(), 20, "Số điện thoại");
        validateLength(profile.getTargetPosition(), 150, "Vị trí mong muốn");
        validateLength(profile.getTargetLocation(), 100, "Địa điểm mong muốn");
        validateLength(profile.getCareerGoal(), 3000, "Mục tiêu nghề nghiệp");
        validateLength(profile.getCertificates(), 2000, "Chứng chỉ");
        if (profile.getPhone() != null && !profile.getPhone().isBlank()
                && !profile.getPhone().matches("^(?:\\+84|0)[0-9]{9,10}$")) {
            throw new BusinessException("Số điện thoại chưa đúng định dạng Việt Nam.");
        }
        if (profile.getExpectedSalary() != null
                && (profile.getExpectedSalary().signum() < 0
                || profile.getExpectedSalary().compareTo(new java.math.BigDecimal("1000000000")) > 0)) {
            throw new BusinessException("Mức lương mong muốn không hợp lệ.");
        }
    }
}
