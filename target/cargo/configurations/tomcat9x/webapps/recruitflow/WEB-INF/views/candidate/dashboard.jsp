<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Bảng điều khiển ứng viên | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />
        <main class="candidate-main col-lg-9 col-xl-10">
            <jsp:include page="/WEB-INF/views/common/flash.jsp" />
            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
                <div><p class="text-muted mb-1">Chào mừng trở lại,</p><h1 class="page-title mb-0"><c:out value="${sessionScope.fullName}" /></h1></div>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/jobs"><i class="bi bi-search me-1"></i>Tìm việc làm</a>
            </div>

            <section class="row g-3 mb-4" aria-label="Tổng quan hồ sơ">
                <div class="col-sm-6 col-xl-3"><div class="summary-card"><div class="d-flex justify-content-between"><div><p class="text-muted small mb-1">Đơn ứng tuyển</p><div class="summary-value"><c:out value="${stats.totalApplications}" /></div></div><span class="summary-icon"><i class="bi bi-send-check fs-5"></i></span></div></div></div>
                <div class="col-sm-6 col-xl-3"><div class="summary-card"><div class="d-flex justify-content-between"><div><p class="text-muted small mb-1">Phỏng vấn sắp tới</p><div class="summary-value"><c:out value="${stats.upcomingInterviews}" /></div></div><span class="summary-icon"><i class="bi bi-calendar-event fs-5"></i></span></div></div></div>
                <div class="col-sm-6 col-xl-3"><div class="summary-card"><div class="d-flex justify-content-between"><div><p class="text-muted small mb-1">Offer đang chờ</p><div class="summary-value"><c:out value="${stats.offers}" /></div></div><span class="summary-icon"><i class="bi bi-envelope-paper fs-5"></i></span></div></div></div>
                <div class="col-sm-6 col-xl-3"><div class="summary-card"><div class="d-flex justify-content-between"><div><p class="text-muted small mb-1">Hoàn thiện hồ sơ</p><div class="summary-value"><c:out value="${stats.profileCompletion}" />%</div></div><span class="summary-icon"><i class="bi bi-person-check fs-5"></i></span></div><div class="progress mt-3" style="height: .45rem;"><div class="progress-bar" role="progressbar" style="width: <c:out value='${stats.profileCompletion}'/>%;" aria-valuenow="<c:out value='${stats.profileCompletion}'/>" aria-valuemin="0" aria-valuemax="100"></div></div></div></div>
            </section>
            <c:if test="${stats.offers gt 0}">
                <div class="alert alert-warning d-flex align-items-center justify-content-between gap-3 mb-4" role="alert"><div><i class="bi bi-envelope-exclamation me-2"></i><strong>Bạn có offer đang chờ phản hồi.</strong> Hãy xem điều khoản và phản hồi trước hạn.</div><a class="btn btn-sm btn-warning border" href="${pageContext.request.contextPath}/candidate/offers">Xem offer</a></div>
            </c:if>

            <div class="row g-4">
                <div class="col-xl-8">
                    <section class="content-card h-100">
                        <div class="d-flex justify-content-between align-items-center gap-2 mb-3"><div><h2 class="h4 fw-bold mb-1">Việc làm phù hợp với bạn</h2><p class="text-muted small mb-0">Được xếp hạng theo CV và kỹ năng của bạn.</p></div><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/candidate/jobs">Xem tất cả</a></div>
                        <div class="row g-3">
                            <c:forEach var="recommendation" items="${recommendedJobs}">
                                <c:url var="recommendedJobUrl" value="/jobs/detail"><c:param name="id" value="${recommendation.job.id}" /></c:url>
                                <div class="col-md-6"><article class="border rounded-3 p-3 h-100"><div class="d-flex justify-content-between gap-2"><span class="badge text-bg-primary-subtle text-primary"><c:out value="${recommendation.job.departmentName}" /></span><span class="match-badge">Match <c:out value="${recommendation.matchScore}" />%</span></div><a class="job-card-title d-block mt-3 mb-2" href="${recommendedJobUrl}"><c:out value="${recommendation.job.title}" /></a><div class="meta-item"><i class="bi bi-geo-alt"></i><c:out value="${recommendation.job.location}" /></div><div class="small text-muted mt-3">Khớp: <c:forEach var="skill" items="${recommendation.matchedSkills}" varStatus="loop"><c:out value="${skill}" /><c:if test="${not loop.last}">, </c:if></c:forEach><c:if test="${empty recommendation.matchedSkills}">Chưa có kỹ năng khớp rõ ràng</c:if></div></article></div>
                            </c:forEach>
                            <c:if test="${empty recommendedJobs}"><div class="col-12"><div class="empty-state py-4"><div class="empty-icon"><i class="bi bi-stars"></i></div><h5>Chưa có gợi ý cá nhân hóa</h5><p class="mb-3">Hãy cập nhật hồ sơ và tải CV để nhận các gợi ý phù hợp hơn.</p><a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/candidate/resumes">Tải CV lên</a></div></div></c:if>
                        </div>
                    </section>
                </div>
                <div class="col-xl-4">
                    <section class="content-card h-100">
                        <div class="d-flex justify-content-between align-items-center mb-3"><h2 class="h5 fw-bold mb-0">Phỏng vấn sắp tới</h2><a class="small" href="${pageContext.request.contextPath}/candidate/interviews">Lịch đầy đủ</a></div>
                        <c:forEach var="interview" items="${upcomingInterviews}">
                            <div class="border-start border-3 border-primary ps-3 py-2 mb-3"><div class="fw-semibold"><c:out value="${interview.jobTitle}" /></div><div class="small text-muted mt-1"><i class="bi bi-calendar3 me-1"></i><c:out value="${interview.interviewDate}" /> · <c:out value="${interview.startTime}" /></div><div class="small text-muted"><i class="bi bi-camera-video me-1"></i><c:out value="${interview.type}" /></div></div>
                        </c:forEach>
                        <c:if test="${empty upcomingInterviews}"><p class="text-muted small mb-0">Bạn chưa có lịch phỏng vấn sắp tới.</p></c:if>
                    </section>
                </div>
                <div class="col-xl-7">
                    <section class="content-card h-100">
                        <div class="d-flex justify-content-between align-items-center mb-3"><h2 class="h5 fw-bold mb-0">Đơn ứng tuyển gần đây</h2><a class="small" href="${pageContext.request.contextPath}/candidate/applications">Xem tất cả</a></div>
                        <div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead><tr><th>Vị trí</th><th>Ngày nộp</th><th>Match</th><th>Trạng thái</th></tr></thead><tbody>
                            <c:forEach var="application" items="${recentApplications}"><c:url var="applicationUrl" value="/candidate/applications/detail"><c:param name="id" value="${application.id}" /></c:url><tr><td><a class="fw-semibold text-decoration-none" href="${applicationUrl}"><c:out value="${application.jobTitle}" /></a></td><td><c:out value="${application.appliedAt}" /></td><td><span class="match-badge"><c:out value="${application.matchScore}" />%</span></td><td><span class="status-badge status-${application.status}"><c:out value="${application.status}" /></span></td></tr></c:forEach>
                            <c:if test="${empty recentApplications}"><tr><td colspan="4" class="text-center text-muted py-4">Bạn chưa nộp đơn ứng tuyển nào.</td></tr></c:if>
                        </tbody></table></div>
                    </section>
                </div>
                <div class="col-xl-5">
                    <section class="content-card h-100">
                        <div class="d-flex justify-content-between align-items-center mb-2"><h2 class="h5 fw-bold mb-0">Thông báo mới</h2><a class="small" href="${pageContext.request.contextPath}/candidate/notifications">Xem tất cả</a></div>
                        <c:forEach var="notification" items="${notifications}"><div class="notification-item ${notification.read ? '' : 'unread'}"><div class="fw-semibold small"><c:out value="${notification.title}" /></div><p class="small text-muted mb-1"><c:out value="${notification.message}" /></p><small class="text-muted"><c:out value="${notification.createdAt}" /></small></div></c:forEach>
                        <c:if test="${empty notifications}"><p class="text-muted small mb-0">Bạn chưa có thông báo nào.</p></c:if>
                    </section>
                </div>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
