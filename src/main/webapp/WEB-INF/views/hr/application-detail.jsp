<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Hồ sơ ứng viên | JobCV" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><a class="small text-decoration-none" href="${pageContext.request.contextPath}/hr/applications"><i class="bi bi-arrow-left me-1"></i>Đơn ứng tuyển</a><h4 class="mt-1 mb-0">Hồ sơ ứng viên</h4></div>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/hr/resumes/download?applicationId=${application.id}"><i class="bi bi-file-earmark-arrow-down me-1"></i> Tải CV</a>
            </div>
            <div class="main-content">
                <c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="successMessage" scope="session" /></c:if>
                <c:if test="${not empty sessionScope.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${sessionScope.error}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="error" scope="session" /></c:if>

                <section class="card mb-4 overflow-hidden">
                    <div class="card-body p-4">
                        <div class="row align-items-center g-3">
                            <div class="col-auto"><div class="rounded-circle bg-primary-subtle text-primary fw-bold d-flex align-items-center justify-content-center fs-4" style="width:64px;height:64px;"><i class="bi bi-person"></i></div></div>
                            <div class="col"><h3 class="mb-1"><c:out value="${application.candidateName}" /></h3><p class="text-muted mb-1"><c:out value="${application.candidateEmail}" /></p><span class="me-2">Ứng tuyển: <strong><c:out value="${application.jobTitle}" /></strong></span></div>
                            <div class="col-md-auto text-md-end"><div class="mb-2"><span class="badge text-bg-primary fs-6"><c:out value="${application.status}" /></span></div><span class="badge rounded-pill text-bg-light border">Điểm phù hợp: <c:out value="${application.matchScore}" />%</span></div>
                        </div>
                    </div>
                </section>

                <div class="row g-4">
                    <div class="col-xl-8">
                        <section class="card">
                            <div class="card-header bg-white border-0 pt-4 px-4">
                                <ul class="nav nav-tabs card-header-tabs" role="tablist">
                                    <li class="nav-item"><button class="nav-link active" data-bs-toggle="tab" data-bs-target="#overview" type="button">Tổng quan</button></li>
                                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#resume" type="button">CV</button></li>
                                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#timeline" type="button">Lịch sử xử lý</button></li>
                                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#interviews" type="button">Phỏng vấn</button></li>
                                    <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#offers" type="button">Thư mời</button></li>
                                </ul>
                            </div>
                            <div class="card-body p-4 tab-content">
                                <div class="tab-pane fade show active" id="overview">
                                    <div class="row g-4">
                                        <div class="col-md-6"><h6 class="text-uppercase text-muted small">Thông tin liên hệ</h6><dl class="row mb-0"><dt class="col-5">Họ tên</dt><dd class="col-7"><c:out value="${application.candidateName}" /></dd><dt class="col-5">Email</dt><dd class="col-7 text-break"><c:out value="${application.candidateEmail}" /></dd><dt class="col-5">Ngày nộp</dt><dd class="col-7"><c:out value="${application.appliedAt}" /></dd></dl></div>
                                        <div class="col-md-6"><h6 class="text-uppercase text-muted small">Thông tin ứng tuyển</h6><dl class="row mb-0"><dt class="col-5">Vị trí</dt><dd class="col-7"><c:out value="${application.jobTitle}" /></dd><dt class="col-5">Điểm phù hợp</dt><dd class="col-7"><strong class="text-success"><c:out value="${application.matchScore}" />%</strong></dd><dt class="col-5">CV</dt><dd class="col-7"><a href="${pageContext.request.contextPath}/hr/resumes/download?applicationId=${application.id}">Xem CV</a></dd></dl></div>
                                    </div>
                                    <div class="application-cover-letter mt-4"><span><i class="bi bi-chat-quote"></i></span><div><h6>Lời giới thiệu của ứng viên</h6><c:choose><c:when test="${not empty application.coverLetter}"><p><c:out value="${application.coverLetter}" /></p></c:when><c:otherwise><p class="text-muted fst-italic">Ứng viên không gửi kèm lời giới thiệu cho đơn này.</p></c:otherwise></c:choose></div></div>
                                </div>
                                <div class="tab-pane fade" id="resume">
                                    <div class="border rounded-3 p-4 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
                                        <div><div class="d-flex align-items-center gap-2"><i class="bi bi-file-earmark-pdf text-danger fs-3"></i><div><h6 class="mb-1">CV đã nộp cùng đơn ứng tuyển</h6><p class="small text-muted mb-0">Tải về để xem hồ sơ gốc của ứng viên.</p></div></div></div>
                                        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/hr/resumes/download?applicationId=${application.id}"><i class="bi bi-download me-1"></i> Tải CV</a>
                                    </div>
                                </div>
                                <div class="tab-pane fade" id="timeline">
                                    <div class="timeline">
                                        <c:forEach var="entry" items="${history}">
                                            <div class="border-start border-2 border-primary ps-3 pb-4 ms-2 position-relative">
                                                <span class="position-absolute top-0 start-0 translate-middle rounded-circle bg-primary" style="width:10px;height:10px;"></span>
                                                <div class="d-flex justify-content-between gap-3"><strong><c:out value="${entry.newStatus}" /></strong><small class="text-muted text-nowrap"><c:out value="${entry.changedAt}" /></small></div>
                                                <small class="text-muted">Thay đổi bởi <c:out value="${entry.changedByName}" /></small>
                                                <c:if test="${not empty entry.remarks}"><p class="mb-0 mt-1"><c:out value="${entry.remarks}" /></p></c:if>
                                            </div>
                                        </c:forEach>
                                        <c:if test="${empty history}"><p class="text-muted mb-0">Chưa có lịch sử thay đổi.</p></c:if>
                                    </div>
                                </div>
                                <div class="tab-pane fade" id="interviews">
                                    <c:forEach var="interview" items="${interviews}">
                                        <div class="border rounded-3 p-3 mb-3"><div class="d-flex justify-content-between"><div><strong><c:out value="${interview.interviewType}" /></strong><p class="mb-0 text-muted"><c:out value="${interview.interviewDate}" /> · <c:out value="${interview.startTime}" /> – <c:out value="${interview.endTime}" /></p></div><span class="badge text-bg-light border"><c:out value="${interview.status}" /></span></div><c:if test="${not empty interview.note}"><p class="mb-0 mt-2"><c:out value="${interview.note}" /></p></c:if><c:set var="feedback" value="${feedbackByInterview[interview.id]}" /><c:if test="${not empty feedback}"><div class="bg-light rounded-3 p-3 mt-3 small"><div class="d-flex justify-content-between"><strong>Đánh giá phỏng vấn</strong><span class="badge text-bg-info"><c:out value="${feedback.recommendation}" /></span></div><div class="row mt-2"><div class="col-6">Kỹ thuật: <strong><c:out value="${feedback.technicalScore}" /></strong></div><div class="col-6">Giao tiếp: <strong><c:out value="${feedback.communicationScore}" /></strong></div><div class="col-6">Kinh nghiệm: <strong><c:out value="${feedback.experienceScore}" /></strong></div><div class="col-6">Thái độ: <strong><c:out value="${feedback.attitudeScore}" /></strong></div></div><div class="mt-2">Điểm tổng: <strong><c:out value="${feedback.overallScore}" /></strong></div><c:if test="${not empty feedback.comment}"><p class="mb-0 mt-2 text-muted"><c:out value="${feedback.comment}" /></p></c:if></div></c:if></div>
                                    </c:forEach>
                                    <c:if test="${empty interviews}"><p class="text-muted mb-0">Chưa có lịch phỏng vấn.</p></c:if>
                                </div>
                                <div class="tab-pane fade" id="offers">
                                    <c:forEach var="offer" items="${offers}">
                                        <div class="border rounded-3 p-3 mb-3"><div class="d-flex justify-content-between gap-3"><div><strong><c:out value="${offer.status}" /></strong><p class="mb-0 text-muted">Lương: <c:out value="${offer.salary}" /> ₫ · Bắt đầu: <c:out value="${offer.startDate}" /></p></div><small class="text-muted">Hết hạn: <c:out value="${offer.expiryDate}" /></small></div><c:if test="${not empty offer.note}"><p class="mb-0 mt-2"><c:out value="${offer.note}" /></p></c:if></div>
                                    </c:forEach>
                                    <c:if test="${empty offers}"><p class="text-muted mb-0">Chưa có thư mời cho ứng viên này.</p></c:if>
                                </div>
                            </div>
                        </section>
                    </div>
                    <div class="col-xl-4">
                        <section class="card">
                            <div class="card-body p-4"><h5 class="mb-3">Xử lý hồ sơ</h5><p class="text-muted small">Chỉ các chuyển đổi hợp lệ mới được hệ thống thực hiện.</p>
                                <c:if test="${application.status eq 'SUBMITTED'}"><form action="${pageContext.request.contextPath}/hr/applications/status" method="post"><input type="hidden" name="applicationId" value="${application.id}"><input type="hidden" name="status" value="SCREENING"><button class="btn btn-warning w-100" type="submit"><i class="bi bi-funnel me-1"></i> Bắt đầu sàng lọc</button></form></c:if>
                                <c:if test="${application.status eq 'SCREENING'}"><div class="d-grid gap-2"><form action="${pageContext.request.contextPath}/hr/applications/status" method="post"><input type="hidden" name="applicationId" value="${application.id}"><input type="hidden" name="status" value="SHORTLISTED"><button class="btn btn-primary w-100" type="submit"><i class="bi bi-person-check me-1"></i> Đưa vào danh sách ngắn</button></form><button class="btn btn-outline-danger" type="button" data-bs-toggle="collapse" data-bs-target="#rejectForm">Từ chối hồ sơ</button></div></c:if>
                                <c:if test="${application.status eq 'SHORTLISTED'}"><div class="d-grid gap-2"><a class="btn btn-primary" href="${pageContext.request.contextPath}/hr/interviews/create?applicationId=${application.id}"><i class="bi bi-calendar-plus me-1"></i> Lên lịch phỏng vấn</a><button class="btn btn-outline-danger" type="button" data-bs-toggle="collapse" data-bs-target="#rejectForm">Từ chối hồ sơ</button></div></c:if>
                                <c:if test="${application.status eq 'INTERVIEWED'}"><div class="d-grid gap-2"><a class="btn btn-success" href="${pageContext.request.contextPath}/hr/offers/create?applicationId=${application.id}"><i class="bi bi-envelope-plus me-1"></i> Tạo thư mời</a><button class="btn btn-outline-danger" type="button" data-bs-toggle="collapse" data-bs-target="#rejectForm">Từ chối hồ sơ</button></div></c:if>
                                <div class="collapse mt-3" id="rejectForm"><form action="${pageContext.request.contextPath}/hr/applications/status" method="post"><input type="hidden" name="applicationId" value="${application.id}"><input type="hidden" name="status" value="REJECTED"><label class="form-label small" for="remarks">Lý do / ghi chú</label><textarea class="form-control mb-2" id="remarks" name="remarks" rows="3"></textarea><button class="btn btn-danger w-100" type="submit">Xác nhận từ chối</button></form></div>
                            </div>
                        </section>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
