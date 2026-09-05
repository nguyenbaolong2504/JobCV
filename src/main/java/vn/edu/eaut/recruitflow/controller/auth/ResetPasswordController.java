package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.PasswordResetService;
import vn.edu.eaut.recruitflow.util.AuthValidation;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.util.PasswordResetSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** Changes the BCrypt password only after a matching reset OTP was consumed in this browser session. */
@WebServlet(name = "ResetPasswordController", urlPatterns = "/forgot-password/reset")
public class ResetPasswordController extends BaseController {
    private PasswordResetService passwordResetService;

    @Override
    public void init() throws ServletException {
        passwordResetService = new PasswordResetService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (PasswordResetSession.verifiedUserId(request.getSession(false)) == null) {
            redirectWithError(request, response, "/forgot-password", "Vui lòng xác minh OTP trước khi đặt lại mật khẩu.");
            return;
        }
        view(request, response, "/WEB-INF/views/auth/reset-password.jsp", "Tạo mật khẩu mới | JobCV");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Integer userId = PasswordResetSession.verifiedUserId(session);
        if (userId == null) {
            redirectWithError(request, response, "/forgot-password", "Phiên đặt lại mật khẩu đã hết hạn. Vui lòng yêu cầu mã mới.");
            return;
        }
        try {
            String password = AuthValidation.newPassword(request.getParameter("password"));
            if (!password.equals(request.getParameter("confirmPassword"))) {
                throw new BusinessException("Xác nhận mật khẩu không khớp.");
            }
            passwordResetService.resetPassword(userId, password);
            PasswordResetSession.clearAll(session);
            session.invalidate();
            FlashMessage.success(request.getSession(true), "Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.");
            redirect(request, response, "/login");
        } catch (BusinessException exception) {
            redirectWithError(request, response, "/forgot-password/reset", exception.getMessage());
        }
    }
}
