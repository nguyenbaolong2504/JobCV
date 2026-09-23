package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.model.CvBuilderData;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.service.CandidateProfileService;
import vn.edu.eaut.recruitflow.service.CvBuilderService;
import vn.edu.eaut.recruitflow.service.UserService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;
import vn.edu.eaut.recruitflow.util.ResumeStorageUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Candidate-facing CV builder that produces a normal DOCX resume usable in the apply flow. */
@WebServlet(name = "CandidateCvBuilderController", urlPatterns = {
        "/candidate/cv-builder",
        "/candidate/cv-builder/create"
})
public class CandidateCvBuilderController extends CandidateBaseController {
    private CvBuilderService cvBuilderService;
    private UserService userService;
    private CandidateProfileService candidateProfileService;

    @Override
    public void init() throws ServletException {
        cvBuilderService = new CvBuilderService();
        userService = new UserService();
        candidateProfileService = new CandidateProfileService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        if (!"/candidate/cv-builder".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            int candidateId = currentCandidateId(request);
            User user = userService.getById(candidateId);
            CandidateProfile profile = candidateProfileService.getProfile(candidateId);
            request.setAttribute("user", user);
            request.setAttribute("profile", profile);
        } catch (BusinessException exception) {
            redirectWithError(request, response, "/candidate/resumes", exception.getMessage());
            return;
        }
        view(request, response, "/WEB-INF/views/candidate/cv-builder.jsp", "Tạo CV theo mẫu | JobCV");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        useUtf8(request, response);
        if (!"/candidate/cv-builder/create".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            int candidateId = currentCandidateId(request);
            CvBuilderData data = bind(request);
            cvBuilderService.generate(candidateId, data,
                    ResumeStorageUtil.resolveUploadDirectory(getServletContext()));
            redirectWithSuccess(request, response, "/candidate/resumes",
                    "Đã tạo CV DOCX theo mẫu. Bạn có thể tải xuống hoặc dùng CV này để ứng tuyển.");
        } catch (BusinessException exception) {
            redirectWithError(request, response, "/candidate/cv-builder", exception.getMessage());
        }
    }

    private CvBuilderData bind(HttpServletRequest request) {
        CvBuilderData data = new CvBuilderData();
        data.setTemplate(RequestUtil.text(request, "template"));
        data.setFullName(RequestUtil.text(request, "fullName"));
        data.setEmail(RequestUtil.text(request, "email"));
        data.setPhone(RequestUtil.text(request, "phone"));
        data.setLocation(RequestUtil.text(request, "location"));
        data.setTargetRole(RequestUtil.text(request, "targetRole"));
        data.setSummary(RequestUtil.text(request, "summary"));
        data.setSkills(RequestUtil.text(request, "skills"));
        data.setExperience(RequestUtil.text(request, "experience"));
        data.setEducation(RequestUtil.text(request, "education"));
        data.setProjects(RequestUtil.text(request, "projects"));
        data.setCertifications(RequestUtil.text(request, "certifications"));
        data.setMakeDefault("true".equalsIgnoreCase(RequestUtil.text(request, "makeDefault")));
        return data;
    }

}
