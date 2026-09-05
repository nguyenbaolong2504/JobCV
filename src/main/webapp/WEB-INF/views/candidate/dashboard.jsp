<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Bảng điều khiển ứng viên | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />
        <main class="candidate-main col-lg-9 col-xl-10">
            <jsp:include page="/WEB-INF/views/common/flash.jsp" />

            <section class="candidate-welcome mb-4">
                <div class="candidate-welcome-copy">
                    <span class="candidate-welcome-kicker"><i class="bi bi-stars"></i>Không gian sự nghiệp của bạn</span>
                    <h1>Chào <c:out value="${sessionScope.fullName}" />!</h1>
                    <p>Tiếp tục hoàn thiện hồ sơ và khám phá những cơ hội phù hợp nhất với năng lực của bạn.</p>
                    <div class="d-flex flex-wrap gap-2"><a class="btn btn-light" href="${pageContext.request.contextPath}/candidate/jobs"><i class="bi bi-search me-1"></i>Tìm việc phù hợp</a><a class="btn btn-outline-light" href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-file-earmark-person me-1"></i>Quản lý CV</a></div>
                </div>
                <div class="candidate-profile-meter">
                    <div class="candidate-profile-ring" style="--profile-value: ${stats.profileCompletion * 3.6}deg"><span><strong><c:out value="${stats.profileCompletion}" />%</strong><small>Hoàn thiện</small></span></div>
                    <a href="${pageContext.request.contextPath}/candidate/profile">Cập nhật hồ sơ<i class="bi bi-arrow-right"></i></a>
                </div>
            </section>

            <section class="row g-3 mb-4" aria-label="Tổng quan hành trình ứng tuyển">
                <div class="col-sm-6 col-xl-3"><a class="candidate-stat" href="${pageContext.request.contextPath}/candidate/applications"><span class="candidate-stat-icon stat-blue"><i class="bi bi-send-check"></i></span><div><small>Đơn ứng tuyển</small><strong><c:out value="${stats.totalApplications}" /></strong><em>Theo dõi toàn bộ hồ sơ</em></div><i class="bi bi-chevron-right"></i></a></div>
                <div class="col-sm-6 col-xl-3"><a class="candidate-stat" href="${pageContext.request.contextPath}/candidate/saved-jobs"><span class="candidate-stat-icon stat-green"><i class="bi bi-bookmark-heart"></i></span><div><small>Việc đã lưu</small><strong><c:out value="${savedJobCount}" /></strong><em>Cơ hội muốn xem lại</em></div><i class="bi bi-chevron-right"></i></a></div>
                <div class="col-sm-6 col-xl-3"><a class="candidate-stat" href="${pageContext.request.contextPath}/candidate/interviews"><span class="candidate-stat-icon stat-violet"><i class="bi bi-calendar-event"></i></span><div><small>Phỏng vấn sắp tới</small><strong><c:out value="${stats.upcomingInterviews}" /></strong><em>Chuẩn bị cho lịch hẹn</em></div><i class="bi bi-chevron-right"></i></a></div>
                <div class="col-sm-6 col-xl-3"><a class="candidate-stat" href="${pageContext.request.contextPath}/candidate/offers"><span class="candidate-stat-icon stat-green"><i class="bi bi-envelope-paper"></i></span><div><small>Thư mời nhận việc</small><strong><c:out value="${stats.offers}" /></strong><em>Xem và phản hồi thư mời</em></div><i class="bi bi-chevron-right"></i></a></div>
            </section>

            <c:if test="${stats.offers gt 0}"><div class="candidate-offer-alert mb-4"><span><i class="bi bi-envelope-heart"></i></span><div><strong>Bạn có thư mời đang chờ phản hồi</strong><p>Hãy xem kỹ điều khoản và phản hồi trước ngày hết hạn.</p></div><a class="btn btn-warning" href="${pageContext.request.contextPath}/candidate/offers">Xem thư mời<i class="bi bi-arrow-right ms-1"></i></a></div></c:if>

            <div class="row g-4 mb-4">
                <div class="col-xl-8">
                    <section class="candidate-panel h-100">
                        <header class="candidate-panel-header"><div><span>DÀNH RIÊNG CHO BẠN</span><h2>Việc làm phù hợp</h2><p>Xếp hạng dựa trên kỹ năng và CV hiện tại.</p></div><a href="${pageContext.request.contextPath}/candidate/jobs">Xem tất cả<i class="bi bi-arrow-right ms-1"></i></a></header>
                        <div class="row g-3">
                            <c:forEach var="recommendation" items="${recommendedJobs}">
                                <c:url var="recommendedJobUrl" value="/jobs/detail"><c:param name="id" value="${recommendation.job.id}" /></c:url>
                                <div class="col-md-6"><article class="candidate-job-card"><div class="candidate-job-top"><span><c:out value="${fn:toUpperCase(fn:substring(recommendation.job.title, 0, 1))}" /></span><div><small><c:out value="${recommendation.job.departmentName}" /></small><strong><c:out value="${recommendation.job.jobCode}" /></strong></div><b><c:out value="${recommendation.matchScore}" />%</b></div><a href="${recommendedJobUrl}"><c:out value="${recommendation.job.title}" /></a><div class="candidate-job-meta"><span><i class="bi bi-geo-alt"></i><c:out value="${recommendation.job.location}" /></span><span><i class="bi bi-briefcase"></i><c:choose><c:when test="${recommendation.job.employmentType eq 'FULL_TIME'}">Toàn thời gian</c:when><c:when test="${recommendation.job.employmentType eq 'PART_TIME'}">Bán thời gian</c:when><c:when test="${recommendation.job.employmentType eq 'INTERNSHIP'}">Thực tập</c:when><c:when test="${recommendation.job.employmentType eq 'REMOTE'}">Làm từ xa</c:when><c:otherwise>Hợp đồng</c:otherwise></c:choose></span></div><div class="candidate-job-skills"><c:forEach var="skill" items="${recommendation.matchedSkills}" end="2"><span><c:out value="${skill}" /></span></c:forEach><c:if test="${empty recommendation.matchedSkills}"><span>Đang phân tích kỹ năng</span></c:if></div><div class="candidate-match-track"><span style="width: ${recommendation.matchScore}%"></span></div></article></div>
                            </c:forEach>
                            <c:if test="${empty recommendedJobs}"><div class="col-12"><div class="empty-state py-5"><div class="empty-icon"><i class="bi bi-stars"></i></div><h5>Chưa có gợi ý cá nhân hóa</h5><p class="mb-3">Hãy cập nhật hồ sơ và tải CV để nhận các gợi ý phù hợp hơn.</p><a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/candidate/resumes">Tải CV lên</a></div></div></c:if>
                        </div>
                    </section>
                </div>
                <div class="col-xl-4">
                    <section class="candidate-panel h-100">
                        <header class="candidate-panel-header"><div><span>LỊCH SẮP TỚI</span><h2>Phỏng vấn</h2><p>Đừng bỏ lỡ cuộc hẹn quan trọng.</p></div><a href="${pageContext.request.contextPath}/candidate/interviews">Tất cả</a></header>
                        <div class="candidate-interview-list">
                            <c:forEach var="interview" items="${upcomingInterviews}"><article><span class="candidate-interview-date"><strong><fmt:formatDate value="${interview.interviewDate}" pattern="dd" /></strong><small><fmt:formatDate value="${interview.interviewDate}" pattern="'Th'M" /></small></span><div><strong><c:out value="${interview.jobTitle}" /></strong><small><i class="bi bi-clock"></i><c:out value="${interview.startTime}" /> – <c:out value="${interview.endTime}" /></small><em><c:choose><c:when test="${interview.type eq 'ONLINE'}">Phỏng vấn trực tuyến</c:when><c:when test="${interview.type eq 'PHONE'}">Phỏng vấn qua điện thoại</c:when><c:otherwise>Phỏng vấn trực tiếp</c:otherwise></c:choose></em></div></article></c:forEach>
                            <c:if test="${empty upcomingInterviews}"><div class="candidate-mini-empty"><i class="bi bi-calendar2-check"></i><strong>Chưa có lịch sắp tới</strong><small>Lịch phỏng vấn mới sẽ xuất hiện tại đây.</small></div></c:if>
                        </div>
                    </section>
                </div>
            </div>

            <div class="row g-4">
                <div class="col-xl-7">
                    <section class="candidate-panel h-100">
                        <header class="candidate-panel-header"><div><span>HOẠT ĐỘNG GẦN ĐÂY</span><h2>Đơn ứng tuyển</h2></div><a href="${pageContext.request.contextPath}/candidate/applications">Xem tất cả<i class="bi bi-arrow-right ms-1"></i></a></header>
                        <div class="candidate-application-list">
                            <c:forEach var="application" items="${recentApplications}"><c:url var="applicationUrl" value="/candidate/applications/detail"><c:param name="id" value="${application.id}" /></c:url><a href="${applicationUrl}"><span class="application-company-mark"><c:out value="${fn:toUpperCase(fn:substring(application.jobTitle, 0, 1))}" /></span><span><strong><c:out value="${application.jobTitle}" /></strong><small><fmt:formatDate value="${application.appliedAt}" pattern="dd/MM/yyyy" /> · Phù hợp <c:out value="${application.matchScore}" />%</small></span><em class="status-badge status-${application.status}"><c:choose><c:when test="${application.status eq 'SUBMITTED'}">Mới nộp</c:when><c:when test="${application.status eq 'SCREENING'}">Đang sàng lọc</c:when><c:when test="${application.status eq 'SHORTLISTED'}">Danh sách ngắn</c:when><c:when test="${application.status eq 'INTERVIEW_SCHEDULED'}">Đã hẹn phỏng vấn</c:when><c:when test="${application.status eq 'INTERVIEWED'}">Đã phỏng vấn</c:when><c:when test="${application.status eq 'OFFERED'}">Đã nhận thư mời</c:when><c:when test="${application.status eq 'HIRED'}">Đã tuyển</c:when><c:when test="${application.status eq 'WITHDRAWN'}">Đã rút đơn</c:when><c:otherwise>Đã từ chối</c:otherwise></c:choose></em></a></c:forEach>
                            <c:if test="${empty recentApplications}"><div class="candidate-mini-empty"><i class="bi bi-send"></i><strong>Bạn chưa ứng tuyển vị trí nào</strong><small>Khám phá công việc phù hợp và gửi hồ sơ đầu tiên.</small><a class="btn btn-sm btn-outline-primary mt-2" href="${pageContext.request.contextPath}/candidate/jobs">Tìm việc ngay</a></div></c:if>
                        </div>
                    </section>
                </div>
                <div class="col-xl-5">
                    <section class="candidate-panel h-100">
                        <header class="candidate-panel-header"><div><span>CẬP NHẬT MỚI</span><h2>Thông báo</h2></div><a href="${pageContext.request.contextPath}/candidate/notifications">Xem tất cả</a></header>
                        <div class="candidate-notification-list"><c:forEach var="notification" items="${notifications}"><article class="${notification.read ? '' : 'unread'}"><span><i class="bi ${notification.read ? 'bi-bell' : 'bi-bell-fill'}"></i></span><div><strong><c:out value="${notification.title}" /></strong><p><c:out value="${notification.message}" /></p><small><fmt:formatDate value="${notification.createdAt}" pattern="dd/MM/yyyy HH:mm" /></small></div></article></c:forEach><c:if test="${empty notifications}"><div class="candidate-mini-empty"><i class="bi bi-bell-slash"></i><strong>Chưa có thông báo</strong><small>Các cập nhật về hồ sơ sẽ xuất hiện tại đây.</small></div></c:if></div>
                    </section>
                </div>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
