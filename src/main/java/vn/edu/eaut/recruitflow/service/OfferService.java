package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.ApplicationDAO;
import vn.edu.eaut.recruitflow.dao.JobDAO;
import vn.edu.eaut.recruitflow.dao.OfferDAO;
import vn.edu.eaut.recruitflow.dao.UserDAO;
import vn.edu.eaut.recruitflow.dao.CompanyDAO;
import vn.edu.eaut.recruitflow.enums.ApplicationStatus;
import vn.edu.eaut.recruitflow.enums.OfferStatus;
import vn.edu.eaut.recruitflow.model.Application;
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
    private final CompanyDAO companyDAO = new CompanyDAO();

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
        applicationService.getForHr(offer.getApplicationId(), actorId);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Application application = applicationDAO.findById(connection, offer.getApplicationId());
                if (application == null || !ApplicationStatus.INTERVIEWED.name().equals(application.getStatus())) {
                    throw new BusinessException("Chỉ ứng viên đã hoàn thành phỏng vấn mới có thể tạo thư mời.");
                }
                if (offerDAO.findByApplicationId(connection, offer.getApplicationId()) != null) {
                    throw new BusinessException("Đơn ứng tuyển này đã có thư mời.");
                }
                offer.setStatus(OfferStatus.DRAFT.name());
                offerDAO.insert(connection, offer);
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể tạo thư mời.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để tạo thư mời.", exception);
        }
    }

    public void update(Offer submitted, int actorId) throws BusinessException {
        validateHrActor(actorId);
        validateOffer(submitted);
        try {
            Offer existing = offerDAO.findById(submitted.getId());
            if (existing == null) {
                throw new BusinessException("Không tìm thấy thư mời.");
            }
            applicationService.getForHr(existing.getApplicationId(), actorId);
            if (!OfferStatus.DRAFT.name().equals(existing.getStatus())) {
                throw new BusinessException("Chỉ có thể chỉnh sửa thư mời ở trạng thái bản nháp.");
            }
            submitted.setApplicationId(existing.getApplicationId());
            if (!offerDAO.update(submitted)) {
                throw new BusinessException("Không thể cập nhật thư mời.");
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể cập nhật thư mời.", exception);
        }
    }

    public void send(int offerId, int actorId) throws BusinessException {
        validateHrActor(actorId);
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Offer offer = offerDAO.findByIdForUpdate(connection, offerId);
                if (offer == null || !OfferStatus.DRAFT.name().equals(offer.getStatus())) {
                    throw new BusinessException("Chỉ có thể gửi thư mời ở trạng thái bản nháp.");
                }
                if (offer.getExpiryDate() == null || offer.getExpiryDate().toLocalDate().isBefore(LocalDate.now())) {
                    throw new BusinessException("Hạn phản hồi thư mời phải từ hôm nay trở đi.");
                }
                Application application = applicationDAO.findByIdForUpdate(connection, offer.getApplicationId());
                if (application == null || !ApplicationStatus.INTERVIEWED.name().equals(application.getStatus())) {
                    throw new BusinessException("Đơn ứng tuyển không còn đủ điều kiện gửi thư mời.");
                }
                applicationService.getForHr(application.getId(), actorId);
                offerDAO.updateStatus(connection, offerId, OfferStatus.SENT.name());
                applicationService.transition(connection, application, ApplicationStatus.OFFERED, actorId, "Đã gửi thư mời.");
                notificationService.create(connection, application.getCandidateId(), "Bạn nhận được thư mời mới",
                        "Thư mời cho vị trí " + application.getJobTitle() + " đang chờ phản hồi trước ngày " + offer.getExpiryDate() + ".");
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể gửi thư mời.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để gửi thư mời.", exception);
        }
    }

    public void respond(int candidateId, int offerId, boolean accepted) throws BusinessException {
        try (Connection connection = DBUtil.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Offer offer = offerDAO.findByIdForUpdate(connection, offerId);
                if (offer == null || offer.getCandidateId() != candidateId) {
                    throw new BusinessException("Bạn chỉ có thể phản hồi thư mời của chính mình.");
                }
                if (!OfferStatus.SENT.name().equals(offer.getStatus())) {
                    throw new BusinessException("Thư mời này không còn chờ phản hồi.");
                }
                Application application = applicationDAO.findByIdForUpdate(connection, offer.getApplicationId());
                if (application == null || !ApplicationStatus.OFFERED.name().equals(application.getStatus())) {
                    throw new BusinessException("Đơn ứng tuyển không còn phù hợp để phản hồi thư mời.");
                }
                if (offer.getExpiryDate() == null || offer.getExpiryDate().toLocalDate().isBefore(LocalDate.now())) {
                    offerDAO.updateStatus(connection, offerId, OfferStatus.EXPIRED.name());
                    connection.commit();
                    throw new BusinessException("Thư mời đã hết hạn phản hồi.");
                }
                if (accepted) {
                    offerDAO.updateStatus(connection, offerId, OfferStatus.ACCEPTED.name());
                    applicationService.transition(connection, application, ApplicationStatus.HIRED, candidateId, "Ứng viên chấp nhận thư mời.");
                    onboardingService.createForHired(connection, application);
                    notificationService.create(connection, candidateId, "Chào mừng bạn gia nhập JobCV",
                            "Quy trình tiếp nhận cho vị trí " + application.getJobTitle() + " đã được khởi tạo.");
                    int hrId = jobDAO.findById(connection, application.getJobId()).getCreatedBy();
                    notificationService.create(connection, hrId, "Ứng viên đã chấp nhận thư mời",
                            application.getCandidateName() + " đã chấp nhận thư mời cho vị trí " + application.getJobTitle() + ".");
                } else {
                    offerDAO.updateStatus(connection, offerId, OfferStatus.DECLINED.name());
                    applicationService.transition(connection, application, ApplicationStatus.REJECTED, candidateId, "Ứng viên từ chối thư mời.");
                    int hrId = jobDAO.findById(connection, application.getJobId()).getCreatedBy();
                    notificationService.create(connection, hrId, "Ứng viên từ chối thư mời",
                            application.getCandidateName() + " đã từ chối thư mời cho vị trí " + application.getJobTitle() + ".");
                }
                connection.commit();
            } catch (BusinessException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw new BusinessException("Không thể phản hồi thư mời.", exception);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new BusinessException("Không thể kết nối cơ sở dữ liệu để phản hồi thư mời.", exception);
        }
    }

    public List<Offer> findForCandidate(int candidateId) throws BusinessException {
        try {
            return offerDAO.findByCandidateId(candidateId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách thư mời.", exception);
        }
    }

    public Offer getForCandidate(int offerId, int candidateId) throws BusinessException {
        try {
            Offer offer = offerDAO.findById(offerId);
            if (offer == null || offer.getCandidateId() != candidateId) {
                throw new BusinessException("Bạn không có quyền xem thư mời này.");
            }
            return offer;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thư mời.", exception);
        }
    }

    public Offer getForHr(int offerId, int actorId) throws BusinessException {
        try {
            Offer offer = offerDAO.findById(offerId);
            if (offer == null) throw new BusinessException("Không tìm thấy thư mời.");
            applicationService.getForHr(offer.getApplicationId(), actorId);
            return offer;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải thư mời.", exception);
        }
    }

    public List<Offer> searchForHr(String keyword, String status, Date expiryDate) throws BusinessException {
        try {
            return offerDAO.search(keyword, status, expiryDate);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách thư mời.", exception);
        }
    }

    public List<Offer> searchForHr(String keyword, String status, Date expiryDate, int actorId) throws BusinessException {
        User actor = requireHrActor(actorId);
        Integer ownerId = "ADMIN".equals(actor.getRoleName()) ? null : companyIdFor(actorId);
        try {
            return offerDAO.search(keyword, status, expiryDate, ownerId);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể tải danh sách thư mời.", exception);
        }
    }

    private void validateOffer(Offer offer) throws BusinessException {
        if (offer == null || offer.getApplicationId() <= 0 || offer.getSalary() == null || offer.getSalary().compareTo(BigDecimal.ZERO) <= 0
                || offer.getStartDate() == null || offer.getExpiryDate() == null || offer.getProbationMonths() < 0 || offer.getProbationMonths() > 36
                || offer.getLocation() == null || offer.getLocation().isBlank()) {
            throw new BusinessException("Thông tin thư mời không hợp lệ.");
        }
        if (offer.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Ngày bắt đầu làm việc không được ở quá khứ.");
        }
        if (offer.getExpiryDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Hạn phản hồi thư mời không được ở quá khứ.");
        }
        if (offer.getLocation() != null && offer.getLocation().length() > 255) {
            throw new BusinessException("Địa điểm làm việc không được quá 255 ký tự.");
        }
    }

    private void validateHrActor(int actorId) throws BusinessException {
        requireHrActor(actorId);
    }

    private User requireHrActor(int actorId) throws BusinessException {
        try {
            User user = userDAO.findById(actorId);
            if (user == null || !("HR".equals(user.getRoleName()) || "ADMIN".equals(user.getRoleName()))) {
                throw new BusinessException("Chỉ Nhân sự hoặc Quản trị viên được phép quản lý thư mời.");
            }
            return user;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực quyền người dùng.", exception);
        }
    }

    private int companyIdFor(int userId) throws BusinessException {
        try {
            Integer companyId = companyDAO.findCompanyIdByUserId(userId);
            if (companyId == null) throw new BusinessException("Tài khoản HR chưa được liên kết với công ty.");
            return companyId;
        } catch (SQLException exception) {
            throw new BusinessException("Không thể xác thực công ty của HR.", exception);
        }
    }
}
