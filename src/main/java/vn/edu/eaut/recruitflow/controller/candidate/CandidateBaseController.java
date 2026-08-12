package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/** Common request/session safeguards for the candidate-only controller area. */
abstract class CandidateBaseController extends BaseController {
    protected int currentCandidateId(HttpServletRequest request) throws BusinessException {
        HttpSession session = request.getSession(false);
        if (session == null || !"CANDIDATE".equals(session.getAttribute("role"))) {
            throw new BusinessException("Phiên đăng nhập ứng viên không hợp lệ.");
        }
        return RequestUtil.currentUserId(request);
    }

    protected void useUtf8(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }

    protected Integer optionalPositiveInt(HttpServletRequest request, String field, String label)
            throws BusinessException {
        return RequestUtil.text(request, field).isEmpty()
                ? null
                : RequestUtil.requiredPositiveInt(request, field, label);
    }

    protected String boundedText(HttpServletRequest request, String field, String label, int maximumLength)
            throws BusinessException {
        String value = RequestUtil.text(request, field);
        if (value.length() > maximumLength) {
            throw new BusinessException(label + " không được vượt quá " + maximumLength + " ký tự.");
        }
        return value;
    }

    protected String safeSort(HttpServletRequest request, Set<String> allowedValues, String defaultValue) {
        String requested = RequestUtil.text(request, "sort").toLowerCase(Locale.ROOT);
        return allowedValues.contains(requested) ? requested : defaultValue;
    }
}
