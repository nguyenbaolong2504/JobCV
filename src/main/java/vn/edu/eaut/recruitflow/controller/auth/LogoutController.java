package vn.edu.eaut.recruitflow.controller.auth;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.util.FlashMessage;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** Ends the authenticated session through a CSRF-protected POST request. */
@WebServlet(name = "LogoutController", urlPatterns = "/logout")
public class LogoutController extends BaseController {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logout(request, response);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // A fresh anonymous session is deliberately created only to carry the PRG flash message.
        FlashMessage.success(request.getSession(true), "Bạn đã đăng xuất thành công.");
        redirect(request, response, "/login");
    }
}
