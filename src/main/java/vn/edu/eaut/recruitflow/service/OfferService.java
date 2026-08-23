package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.OfferDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.enums.JobStatus;
import vn.edu.eaut.recruitflow.enums.OfferStatus;
import vn.edu.eaut.recruitflow.model.Application;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.Offer;
import vn.edu.eaut.recruitflow.model.User;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Offer drafting/sending and candidate acceptance transaction. */
public class OfferService {
    private final OfferDAO offerDAO;
    private final ApplicationDAO applicationDAO;
    private final JobDAO jobDAO;
    private final UserDAO userDAO;
    private final ApplicationService applicationService;
    private final OnboardingService onboardingService;
    private final NotificationService notificationService;

    public OfferService() {
        this(new OfferDAO(), new ApplicationDAO(), new JobDAO(), new UserDAO(), new ApplicationService(),
                new OnboardingService(), new NotificationService());
    }

    OfferService(OfferDAO offerDAO, ApplicationDAO applicationDAO, JobDAO jobDAO, UserDAO userDAO,
                 ApplicationService applicationService, OnboardingService onboardingService,
                 NotificationService notificationService) {
        this.offerDAO = offerDAO;
        this.applicationDAO = applicationDAO;
        this.jobDAO = jobDAO;
        this.userDAO = userDAO;
        this.applicationService = applicationService;
        this.onboardingService = onboardingService;
        this.notificationService = notificationService;
    }

    public void create(Offer offer, int actorId) throws BusinessException {
        validateHrActor(actorId);
        validateOffer(offer);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Application application = applicationDAO.findById(connection, offer.getApplicationId());
                if (application == null) {
                    throw new BusinessException("Không tìm thấy đơn ứng tuyển.");
                }
                // Normalize within this transaction so an offer that expired between page load and
                // submit can be safely reissued without breaking the one-offer-per-application rule.
                offerDAO.expirePastDueSentOffers(connection);
                Offer existing = offerDAO.findByApplicationId(connection, offer.getApplicationId());
                if (existing == null) {
                    if (!ApplicationStatus.INTERVIEWED.name().equals(application.getStatus())) {
                        throw new BusinessException("Chỉ ứng viên đã INTERVIEWED mới có thể tạo offer.");
                    }
                    offer.setStatus(OfferStatus.DRAFT.name());
                    offerDAO.insert(connection, offer);
                } else if (OfferStatus.EXPIRED.name().equals(existing.getStatus())
                        && ApplicationStatus.OFFERED.name().equals(application.getStatus())) {
                    offer.setId(existing.getId());
                    offer.setStatus(OfferStatus.DRAFT.name());
                    if (!offerDAO.replaceExpiredWithDraft(connection, offer)) {
                        throw new BusinessException("Offer đã thay đổi, vui lòng tải lại trang và thử lại.");
                    }
                } else {
                    throw new BusinessException("Đơn ứng tuyển này đã có offer đang xử lý hoặc đã được phản hồi.");
                }
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể tạo offer.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để tạo offer.", exception);
        }
    }

    public void update(Offer submitted, int actorId) throws BusinessException {
        validateHrActor(actorId);
        validateOffer(submitted);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                offerDAO.expirePastDueSentOffers(connection);
                Offer existing = offerDAO.findById(connection, submitted.getId());
                if (existing == null) {
                    throw new BusinessException("Không tìm thấy offer.");
                }
                if (!OfferStatus.DRAFT.name().equals(existing.getStatus())) {
                    throw new BusinessException("Chỉ có thể chỉnh sửa offer ở trạng thái DRAFT.");
                }
                submitted.setApplicationId(existing.getApplicationId());
                if (!offerDAO.updateDraft(connection, submitted)) {
                    throw new BusinessException("Offer đã được gửi hoặc thay đổi bởi thao tác khác. Vui lòng tải lại trang.");
                }
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể cập nhật offer.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật offer.", exception);
        }
    }

    public void send(int offerId, int actorId) throws BusinessException {
        validateHrActor(actorId);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                offerDAO.expirePastDueSentOffers(connection);
                Offer offer = offerDAO.findById(connection, offerId);
                if (offer == null || !OfferStatus.DRAFT.name().equals(offer.getStatus())) {
                    throw new BusinessException("Chỉ có thể gửi offer ở trạng thái DRAFT.");
                }
                if (offer.getExpiryDate() == null || offer.getExpiryDate().toLocalDate().isBefore(LocalDate.now())) {
                    throw new BusinessException("Hạn phản hồi offer phải từ hôm nay trở đi.");
                }
                Application application = applicationDAO.findById(connection, offer.getApplicationId());
                if (application == null || !(ApplicationStatus.INTERVIEWED.name().equals(application.getStatus())
                        || ApplicationStatus.OFFERED.name().equals(application.getStatus()))) {
                    throw new BusinessException("Đơn ứng tuyển không còn đủ điều kiện gửi offer.");
                }
                if (!offerDAO.updateStatusIfCurrent(connection, offerId, OfferStatus.SENT.name(), OfferStatus.DRAFT.name())) {
                    throw new BusinessException("Offer đã được gửi hoặc thay đổi bởi thao tác khác. Vui lòng tải lại trang.");
                }
                if (ApplicationStatus.INTERVIEWED.name().equals(application.getStatus())) {
                    applicationService.transition(connection, application, ApplicationStatus.OFFERED, actorId, "Đã gửi offer.");
                }
                notificationService.create(connection, application.getCandidateId(), "Bạn nhận được offer mới",
                        "Offer cho vị trí " + application.getJobTitle() + " đang chờ phản hồi trước ngày " + offer.getExpiryDate() + ".");
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể gửi offer.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để gửi offer.", exception);
        }
    }

    public void respond(int candidateId, int offerId, boolean accepted) throws BusinessException {
        boolean expired = false;
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                // All acceptances for the same candidate serialize here, preventing two offer
                // responses from creating two onboardings in this single-employer application.
                if (!userDAO.lockById(connection, candidateId)) {
                    throw new BusinessException("Không tìm thấy tài khoản ứng viên.");
                }
                Offer offer = offerDAO.findById(connection, offerId);
                if (offer == null || offer.getCandidateId() != candidateId) {
                    throw new BusinessException("Bạn chỉ có thể phản hồi offer của chính mình.");
                }
                if (!OfferStatus.SENT.name().equals(offer.getStatus())) {
                    throw new BusinessException("Offer này không còn chờ phản hồi.");
                }
                Application application = applicationDAO.findById(connection, offer.getApplicationId());
                if (application == null || !ApplicationStatus.OFFERED.name().equals(application.getStatus())) {
                    throw new BusinessException("Đơn ứng tuyển không còn phù hợp để phản hồi offer.");
                }
                if (offer.getExpiryDate() == null || offer.getExpiryDate().toLocalDate().isBefore(LocalDate.now())) {
                    if (!offerDAO.updateStatusIfCurrent(connection, offerId, OfferStatus.EXPIRED.name(), OfferStatus.SENT.name())) {
                        throw new BusinessException("Offer đã được phản hồi hoặc thay đổi bởi thao tác khác.");
                    }
                    expired = true;
                } else if (accepted) {
                    if (offerDAO.hasAcceptedOfferForCandidate(connection, candidateId, offerId)
                            || applicationDAO.hasHiredApplicationForCandidate(connection, candidateId, application.getId())) {
                        throw new BusinessException("Bạn đã chấp nhận một offer khác. Hệ thống chỉ cho phép một quy trình onboarding đang hiệu lực.");
                    }

                    if (!jobDAO.lockById(connection, application.getJobId())) {
                        throw new BusinessException("Không tìm thấy vị trí tuyển dụng của offer này.");
                    }
                    Job job = jobDAO.findById(connection, application.getJobId());
                    if (job == null) {
                        throw new BusinessException("Không tìm thấy vị trí tuyển dụng của offer này.");
                    }
                    long hiredCount = applicationDAO.countByJobAndStatus(connection, job.getId(), ApplicationStatus.HIRED.name());
                    if (hiredCount >= job.getNumberOfPositions()) {
                        throw new BusinessException("Vị trí này đã tuyển đủ số lượng. Vui lòng liên hệ HR để được hỗ trợ.");
                    }
                    if (!offerDAO.updateStatusIfCurrent(connection, offerId, OfferStatus.ACCEPTED.name(), OfferStatus.SENT.name())) {
                        throw new BusinessException("Offer đã được phản hồi hoặc thay đổi bởi thao tác khác.");
                    }
                    applicationService.transition(connection, application, ApplicationStatus.HIRED, candidateId, "Ứng viên chấp nhận offer.");
                    onboardingService.createForHired(connection, application);
                    if (hiredCount + 1 >= job.getNumberOfPositions()
                            && JobStatus.PUBLISHED.name().equals(job.getStatus())) {
                        jobDAO.updateStatus(connection, job.getId(), JobStatus.CLOSED.name());
                    }
                    notificationService.create(connection, candidateId, "Chào mừng bạn gia nhập RecruitFlow",
                            "Onboarding cho vị trí " + application.getJobTitle() + " đã được khởi tạo.");
                    int hrId = job.getCreatedBy();
                    notificationService.create(connection, hrId, "Ứng viên đã chấp nhận offer",
                            application.getCandidateName() + " đã chấp nhận offer cho vị trí " + application.getJobTitle() + ".");
                } else {
                    if (!offerDAO.updateStatusIfCurrent(connection, offerId, OfferStatus.DECLINED.name(), OfferStatus.SENT.name())) {
                        throw new BusinessException("Offer đã được phản hồi hoặc thay đổi bởi thao tác khác.");
                    }
                    applicationService.transition(connection, application, ApplicationStatus.REJECTED, candidateId, "Ứng viên từ chối offer.");
                    Job job = jobDAO.findById(connection, application.getJobId());
                    if (job == null) {
                        throw new BusinessException("Không tìm thấy vị trí tuyển dụng của offer này.");
                    }
                    int hrId = job.getCreatedBy();
                    notificationService.create(connection, hrId, "Ứng viên từ chối offer",
                            application.getCandidateName() + " đã từ chối offer cho vị trí " + application.getJobTitle() + ".");
                }
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể phản hồi offer.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để phản hồi offer.", exception);
        }
        if (expired) {
            throw new BusinessException("Offer đã hết hạn phản hồi.");
        }
    }

    public List<Offer> findForCandidate(int candidateId) throws BusinessException {
        try {
            offerDAO.expirePastDueSentOffers();
            return offerDAO.findByCandidateId(candidateId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách offer.", exception);
        }
    }

    public Offer getForCandidate(int offerId, int candidateId) throws BusinessException {
        try {
            offerDAO.expirePastDueSentOffers();
            Offer offer = offerDAO.findById(offerId);
            if (offer == null || offer.getCandidateId() != candidateId || OfferStatus.DRAFT.name().equals(offer.getStatus())) {
                throw new BusinessException("Bạn không có quyền xem offer này.");
            }
            return offer;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải offer.", exception);
        }
    }

    public Offer getForHr(int offerId) throws BusinessException {
        try {
            offerDAO.expirePastDueSentOffers();
            Offer offer = offerDAO.findById(offerId);
            if (offer == null) throw new BusinessException("Không tìm thấy offer.");
            return offer;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải offer.", exception);
        }
    }

    public List<Offer> searchForHr(String keyword, String status, Date expiryDate) throws BusinessException {
        try {
            offerDAO.expirePastDueSentOffers();
            return offerDAO.search(keyword, status, expiryDate);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách offer.", exception);
        }
    }

    private void validateOffer(Offer offer) throws BusinessException {
        if (offer == null || offer.getApplicationId() <= 0 || offer.getSalary() == null || offer.getSalary().compareTo(BigDecimal.ZERO) <= 0
                || offer.getStartDate() == null || offer.getExpiryDate() == null || offer.getProbationMonths() < 0 || offer.getProbationMonths() > 36
                || offer.getLocation() == null || offer.getLocation().isBlank()) {
            throw new BusinessException("Thông tin offer không hợp lệ.");
        }
        if (offer.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Ngày bắt đầu làm việc không được ở quá khứ.");
        }
        if (offer.getExpiryDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Hạn phản hồi offer không được ở quá khứ.");
        }
        if (offer.getStartDate().toLocalDate().isBefore(offer.getExpiryDate().toLocalDate())) {
            throw new BusinessException("Ngày bắt đầu làm việc phải vào hoặc sau hạn phản hồi offer.");
        }
        if (offer.getLocation() != null && offer.getLocation().length() > 255) {
            throw new BusinessException("Địa điểm làm việc không được quá 255 ký tự.");
        }
    }

    private void validateHrActor(int actorId) throws BusinessException {
        try {
            User user = userDAO.findById(actorId);
            if (user == null || !"ACTIVE".equals(user.getStatus())
                    || !("HR".equals(user.getRoleName()) || "ADMIN".equals(user.getRoleName()))) {
                throw new BusinessException("Chỉ HR hoặc Admin được phép quản lý offer.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền người dùng.", exception);
        }
    }
}
