package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.util.FlashMessage;
import vn.edu.eaut.recruitflow.service.CandidateProfileService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Shared small controller helpers; business work stays in services. */
public abstract class BaseController extends HttpServlet {
    private final CandidateProfileService layoutCandidateProfileService = new CandidateProfileService();

    protected void view(HttpServletRequest request, HttpServletResponse response, String view, String pageTitle)
            throws ServletException, IOException {
        if (request.getAttribute("layoutCandidateProfile") == null
                && request.getSession(false) != null
                && "CANDIDATE".equals(request.getSession(false).getAttribute("role"))) {
            try {
                request.setAttribute("layoutCandidateProfile",
                        layoutCandidateProfileService.getProfile(RequestUtil.currentUserId(request)));
            } catch (BusinessException ignored) {
                // A missing avatar must never block the requested business page.
            }
        }
        request.setAttribute("pageTitle", pageTitle);
        request.getRequestDispatcher(view).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void redirectWithError(HttpServletRequest request, HttpServletResponse response, String path, String message)
            throws IOException {
        FlashMessage.error(request.getSession(), message);
        redirect(request, response, path);
    }

    protected void redirectWithSuccess(HttpServletRequest request, HttpServletResponse response, String path, String message)
            throws IOException {
        FlashMessage.success(request.getSession(), message);
        redirect(request, response, path);
    }
}
