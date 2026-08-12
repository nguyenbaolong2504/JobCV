package vn.edu.eaut.recruitflow.controller.hr;

import vn.edu.eaut.recruitflow.controller.BaseController;
import vn.edu.eaut.recruitflow.model.Offer;
import vn.edu.eaut.recruitflow.service.OfferService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/hr/offers/update")
public class OfferUpdateController extends BaseController {
    private OfferService offerService;

    @Override
    public void init() throws ServletException {
        offerService = new OfferService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Offer offer = OfferCreateController.bindOffer(request);
            offer.setId(RequestUtil.requiredPositiveInt(request, "id", "Offer"));
            offerService.update(offer, RequestUtil.currentUserId(request));
            redirectWithSuccess(request, response, "/hr/offers", "Đã cập nhật offer.");
        } catch (BusinessException | IllegalArgumentException ex) {
            redirectWithError(request, response, "/hr/offers", ex.getMessage());
        }
    }
}
