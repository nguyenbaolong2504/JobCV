package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.enums.OfferStatus;
import vn.edu.eaut.recruitflow.service.OfferService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/hr/offers")
public class HROfferController extends BaseController {
    private OfferService offerService;

    @Override
    public void init() throws ServletException {
        offerService = new OfferService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String status = RequestUtil.text(request, "status").isEmpty()
                    ? null : OfferStatus.fromValue(RequestUtil.text(request, "status")).name();
            Date expiryDate = RequestUtil.text(request, "expiryDate").isEmpty()
                    ? null : Date.valueOf(RequestUtil.date(request, "expiryDate", "Ngày hết hạn"));
            request.setAttribute("offers", offerService.searchForHr(
                    RequestUtil.text(request, "keyword"), status, expiryDate));
        } catch (BusinessException | IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
        }
        view(request, response, "/WEB-INF/views/hr/offers.jsp", "Offer Management | RecruitFlow");
    }
}
