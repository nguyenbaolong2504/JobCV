package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.OfferService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hr/offers/edit")
public class OfferEditController extends BaseController {
    private OfferService offerService;
    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        offerService = new OfferService();
        applicationService = new ApplicationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int offerId = RequestUtil.requiredPositiveInt(request, "id", "Thư mời");
            int actorId = RequestUtil.currentUserId(request);
            request.setAttribute("offer", offerService.getForHr(offerId, actorId));
            request.setAttribute("applications", applicationService.findInterviewed(actorId));
            view(request, response, "/WEB-INF/views/hr/offer-form.jsp", "Chỉnh sửa thư mời | RecruitFlow");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/offers", ex.getMessage());
        }
    }
}
