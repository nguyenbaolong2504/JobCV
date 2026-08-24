package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.enums.Gender;
import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.service.CandidateProfileService;
import vn.edu.eaut.recruitflow.service.UserService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.AvatarStorageUtil;
import vn.edu.eaut.recruitflow.util.RequestUtil;
import vn.edu.eaut.recruitflow.util.UploadUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;

/** Candidate profile retrieval and update. The profile service owns the update transaction. */
@WebServlet(name = "CandidateProfileController", urlPatterns = {
        "/candidate/profile",
        "/candidate/profile/avatar"
})
@MultipartConfig(
        fileSizeThreshold = 512 * 1024,
        maxFileSize = UploadUtil.MAX_AVATAR_SIZE,
        maxRequestSize = 3L * 1024L * 1024L
)
public class CandidateProfileController extends CandidateBaseController {
    private CandidateProfileService candidateProfileService;
    private UserService userService;

    @Override
    public void init() throws ServletException {
        candidateProfileService = new CandidateProfileService();
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        if (!"/candidate/profile".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("user", new User());
        request.setAttribute("profile", new CandidateProfile());

        try {
            int candidateId = currentCandidateId(request);
            request.setAttribute("user", userService.getById(candidateId));
            request.setAttribute("profile", candidateProfileService.getProfile(candidateId));
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }

        view(request, response, "/WEB-INF/views/candidate/profile.jsp", "Hồ sơ ứng viên | RecruitFlow");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        useUtf8(request, response);
        if ("/candidate/profile/avatar".equals(request.getServletPath())) {
            updateAvatar(request, response);
            return;
        }
        if (!"/candidate/profile".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            int candidateId = currentCandidateId(request);
            String fullName = requiredName(request);
            CandidateProfile profile = bindProfile(request);

            // This service operation must atomically update both the account name and profile data.
            candidateProfileService.updateProfile(candidateId, fullName, profile);
            request.getSession(false).setAttribute("fullName", fullName);
            redirectWithSuccess(request, response, "/candidate/profile", "Đã cập nhật hồ sơ cá nhân.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/candidate/profile", ex.getMessage());
        }
    }

    private void updateAvatar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path uploadDirectory = null;
        Path storedFile = null;
        try {
            int candidateId = currentCandidateId(request);
            CandidateProfile currentProfile = candidateProfileService.getProfile(candidateId);
            uploadDirectory = AvatarStorageUtil.resolveDirectory(getServletContext());

            if ("true".equals(request.getParameter("removeAvatar"))) {
                candidateProfileService.updateAvatar(candidateId, null);
                AvatarStorageUtil.deleteStored(uploadDirectory, currentProfile.getAvatarPath());
                redirectWithSuccess(request, response, "/candidate/profile", "Đã xóa ảnh đại diện.");
                return;
            }

            Part avatarPart = request.getPart("avatarFile");
            storedFile = UploadUtil.storeAvatar(avatarPart, candidateId, uploadDirectory);
            candidateProfileService.updateAvatar(candidateId, storedFile.getFileName().toString());
            AvatarStorageUtil.deleteStored(uploadDirectory, currentProfile.getAvatarPath());
            redirectWithSuccess(request, response, "/candidate/profile", "Đã cập nhật ảnh đại diện.");
        } catch (BusinessException exception) {
            if (uploadDirectory != null && storedFile != null) {
                AvatarStorageUtil.deleteStored(uploadDirectory, storedFile.getFileName().toString());
            }
            redirectWithError(request, response, "/candidate/profile", exception.getMessage());
        } catch (IllegalStateException | ServletException exception) {
            redirectWithError(request, response, "/candidate/profile",
                    "Ảnh không hợp lệ hoặc vượt quá giới hạn 2 MB.");
        } catch (IOException exception) {
            if (uploadDirectory != null && storedFile != null) {
                AvatarStorageUtil.deleteStored(uploadDirectory, storedFile.getFileName().toString());
            }
            redirectWithError(request, response, "/candidate/profile",
                    "Không thể lưu ảnh đại diện. Vui lòng thử lại.");
        }
    }

    private CandidateProfile bindProfile(HttpServletRequest request) throws BusinessException {
        CandidateProfile profile = new CandidateProfile();
        String dateOfBirth = RequestUtil.text(request, "dateOfBirth");
        if (!dateOfBirth.isEmpty()) {
            LocalDate parsedDate = RequestUtil.date(request, "dateOfBirth", "Ngày sinh");
            if (parsedDate.isAfter(LocalDate.now())) {
                throw new BusinessException("Ngày sinh không được ở tương lai.");
            }
            profile.setDateOfBirth(Date.valueOf(parsedDate));
        }

        String gender = RequestUtil.text(request, "gender");
        if (!gender.isEmpty()) {
            profile.setGender(Gender.fromValue(gender).name());
        }
        profile.setAddress(boundedText(request, "address", "Địa chỉ", 255));
        profile.setUniversity(boundedText(request, "university", "Trường đại học", 150));
        profile.setMajor(boundedText(request, "major", "Chuyên ngành", 100));
        String experienceYears = RequestUtil.text(request, "experienceYears");
        profile.setExperienceYears(experienceYears.isEmpty()
                ? 0
                : RequestUtil.nonNegativeInt(request, "experienceYears", "Số năm kinh nghiệm"));
        profile.setSkills(boundedText(request, "skills", "Kỹ năng", 4000));
        profile.setSummary(boundedText(request, "summary", "Giới thiệu", 8000));
        profile.setPhone(boundedText(request, "phone", "Số điện thoại", 20).replaceAll("[ .-]", ""));
        profile.setTargetPosition(boundedText(request, "targetPosition", "Vị trí mong muốn", 150));
        profile.setTargetLocation(boundedText(request, "targetLocation", "Địa điểm mong muốn", 100));
        String expectedSalary = RequestUtil.text(request, "expectedSalary");
        if (!expectedSalary.isEmpty()) {
            try {
                profile.setExpectedSalary(new java.math.BigDecimal(expectedSalary));
            } catch (NumberFormatException exception) {
                throw new BusinessException("Mức lương mong muốn phải là một số hợp lệ.");
            }
        }
        profile.setCareerGoal(boundedText(request, "careerGoal", "Mục tiêu nghề nghiệp", 3000));
        profile.setCertificates(boundedText(request, "certificates", "Chứng chỉ", 2000));
        return profile;
    }

    private String requiredName(HttpServletRequest request) throws BusinessException {
        String name = RequestUtil.text(request, "fullName").replaceAll("\\s+", " ");
        if (name.length() < 2 || name.length() > 100) {
            throw new BusinessException("Họ và tên phải có từ 2 đến 100 ký tự.");
        }
        return name;
    }
}
