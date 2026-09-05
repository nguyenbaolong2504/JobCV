package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.service.CareerAgentService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.SimpleJson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Read-only JSON endpoint used by the shared JobCV career assistant. */
@WebServlet(name = "CareerAgentController", urlPatterns = "/assistant")
public class CareerAgentController extends HttpServlet {
    private CareerAgentService agentService;

    @Override
    public void init() throws ServletException {
        agentService = new CareerAgentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String question = request.getParameter("q");
        if (question == null || question.isBlank() || question.length() > 500) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(SimpleJson.stringify(error("Câu hỏi phải có từ 1 đến 500 ký tự.")));
            return;
        }

        HttpSession session = request.getSession(false);
        Integer userId = session != null && session.getAttribute("userId") instanceof Integer id ? id : null;
        String role = session != null && session.getAttribute("role") instanceof String value ? value : null;
        try {
            response.getWriter().write(SimpleJson.stringify(agentService.answer(question, userId, role)));
        } catch (BusinessException exception) {
            response.setStatus(422);
            response.getWriter().write(SimpleJson.stringify(error(exception.getMessage())));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("error", message);
        result.put("text", message);
        result.put("actions", java.util.List.of());
        return result;
    }
}
