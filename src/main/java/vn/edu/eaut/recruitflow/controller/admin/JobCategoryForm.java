package vn.edu.eaut.recruitflow.controller.admin;

import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.http.HttpServletRequest;

/** Shared parsing for the Admin job-category form. */
final class JobCategoryForm {
    private JobCategoryForm() {
    }

    static Integer parentId(HttpServletRequest request) throws BusinessException {
        String value = RequestUtil.text(request, "parentId");
        return value.isEmpty() ? null : RequestUtil.requiredPositiveInt(request, "parentId", "Danh mục cha");
    }

    static int displayOrder(HttpServletRequest request) throws BusinessException {
        String value = RequestUtil.text(request, "displayOrder");
        if (value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new BusinessException("Thứ tự hiển thị không hợp lệ.");
        }
    }

    static boolean active(HttpServletRequest request) {
        return request.getParameter("active") != null;
    }
}
