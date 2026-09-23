package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.model.CandidateProfile;
import vn.edu.eaut.recruitflow.service.CandidateProfileService;
import vn.edu.eaut.recruitflow.util.AvatarStorageUtil;
import vn.edu.eaut.recruitflow.util.BusinessException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/** Streams only the signed-in candidate's own avatar from server-owned storage. */
@WebServlet(name = "CandidateAvatarController", urlPatterns = "/candidate/avatar")
public class CandidateAvatarController extends CandidateBaseController {
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp"
    );

    private CandidateProfileService candidateProfileService;

    @Override
    public void init() throws ServletException {
        candidateProfileService = new CandidateProfileService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int candidateId = currentCandidateId(request);
            CandidateProfile profile = candidateProfileService.getProfile(candidateId);
            Path directory = AvatarStorageUtil.resolveDirectory(getServletContext());
            Path avatar = AvatarStorageUtil.resolveStored(directory, profile.getAvatarPath());
            String contentType = CONTENT_TYPES.get(extension(avatar));
            if (contentType == null) {
                throw new BusinessException("Định dạng ảnh đại diện không hợp lệ.");
            }

            response.reset();
            response.setContentType(contentType);
            response.setContentLengthLong(Files.size(avatar));
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "private, max-age=86400");
            Files.copy(avatar, response.getOutputStream());
        } catch (BusinessException | IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String extension(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
