package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.Application;
import vn.edu.eaut.recruitflow.model.Offer;
import vn.edu.eaut.recruitflow.service.ApplicationService;
import vn.edu.eaut.recruitflow.service.OfferService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/hr/offers/create")
public class OfferCreateController extends BaseController {
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
            String applicationId = RequestUtil.text(request, "applicationId");
            List<Application> applications = new ArrayList<>(applicationService.findInterviewed());
            if (!applicationId.isEmpty()) {
                Application application = applicationService.getForHr(
                        RequestUtil.requiredPositiveInt(request, "applicationId", "Đơn ứng tuyển"));
                request.setAttribute("application", application);
                // A replacement offer is allowed only after a previous one expired. The service
                // remains the authority for that rule; this keeps the selected row visible here.
                boolean alreadyPresent = applications.stream().anyMatch(item -> item.getId() == application.getId());
                if (!alreadyPresent) {
                    applications.add(application);
                }
            }
            request.setAttribute("applications", applications);
            view(request, response, "/WEB-INF/views/hr/offer-form.jsp", "Tạo offer | RecruitFlow");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/offers", ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            offerService.create(bindOffer(request), RequestUtil.currentUserId(request));
            redirectWithSuccess(request, response, "/hr/offers", "Đã tạo offer ở trạng thái bản nháp.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/offers/create", ex.getMessage());
        }
    }

    static Offer bindOffer(HttpServletRequest request) throws BusinessException {
        Offer offer = new Offer();
        offer.setApplicationId(RequestUtil.requiredPositiveInt(request, "applicationId", "Đơn ứng tuyển"));
        offer.setSalary(RequestUtil.decimal(request, "salary", "Mức lương"));
        offer.setStartDate(Date.valueOf(RequestUtil.date(request, "startDate", "Ngày bắt đầu")));
        offer.setProbationMonths(RequestUtil.text(request, "probationMonths").isEmpty()
                ? 2 : RequestUtil.nonNegativeInt(request, "probationMonths", "Thời gian thử việc"));
        offer.setLocation(RequestUtil.text(request, "location"));
        offer.setExpiryDate(Date.valueOf(RequestUtil.date(request, "expiryDate", "Hạn phản hồi")));
        offer.setNote(RequestUtil.text(request, "note"));
        return offer;
    }
}
