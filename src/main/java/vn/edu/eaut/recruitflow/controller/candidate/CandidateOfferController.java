package vn.edu.eaut.recruitflow.controller.candidate;

import vn.edu.eaut.recruitflow.service.OfferService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Candidate-owned offer list and accept/decline response endpoint. */
@WebServlet(name = "CandidateOfferController", urlPatterns = {
        "/candidate/offers",
        "/candidate/offers/respond"
})
public class CandidateOfferController extends CandidateBaseController {
    private OfferService offerService;

    @Override
    public void init() throws ServletException {
        offerService = new OfferService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        useUtf8(request, response);
        if (!"/candidate/offers".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("offers", List.of());
        try {
            request.setAttribute("offers", offerService.findForCandidate(currentCandidateId(request)));
        } catch (BusinessException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/candidate/offers.jsp", "Thư mời của tôi | RecruitFlow");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        useUtf8(request, response);
        if (!"/candidate/offers/respond".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            int candidateId = currentCandidateId(request);
            int offerId = RequestUtil.requiredPositiveInt(request, "offerId", "Thư mời");
            boolean accepted = parseResponse(RequestUtil.text(request, "response"));
            offerService.respond(candidateId, offerId, accepted);
            String message = accepted ? "Đã chấp nhận thư mời. Quy trình tiếp nhận đã được tạo."
                    : "Đã từ chối thư mời.";
            redirectWithSuccess(request, response, "/candidate/offers", message);
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/candidate/offers", ex.getMessage());
        }
    }

    private boolean parseResponse(String response) throws BusinessException {
        if ("ACCEPT".equals(response)) {
            return true;
        }
        if ("DECLINE".equals(response)) {
            return false;
        }
        throw new BusinessException("Phản hồi thư mời không hợp lệ.");
    }
}
