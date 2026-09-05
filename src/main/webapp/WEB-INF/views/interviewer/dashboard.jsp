<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Bảng điều khiển phỏng vấn | JobCV" />
<c:set var="onlineInterviewCount" value="0" /><c:forEach var="countInterview" items="${interviews}"><c:if test="${countInterview.interviewType eq 'ONLINE'}"><c:set var="onlineInterviewCount" value="${onlineInterviewCount + 1}" /></c:if></c:forEach>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/interviewer-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Không gian hội đồng</p><h4 class="mb-0">Tổng quan phỏng vấn</h4></div><a class="btn btn-primary" href="${pageContext.request.contextPath}/interviewer/interviews"><i class="bi bi-calendar2-week me-1"></i>Mở lịch làm việc</a></div>
            <div class="main-content interviewer-dashboard">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />

                <section class="interviewer-welcome mb-4">
                    <div><span><i class="bi bi-person-workspace"></i>Không gian phỏng vấn JobCV</span><h1>Sẵn sàng cho buổi phỏng vấn tiếp theo?</h1><p>Chào <strong><c:out value="${sessionScope.fullName}" /></strong>. Xem trước hồ sơ, ghi chú trọng tâm và hoàn thiện đánh giá ngay sau cuộc trao đổi.</p><a class="btn btn-light" href="${pageContext.request.contextPath}/interviewer/interviews">Xem toàn bộ lịch<i class="bi bi-arrow-right ms-1"></i></a></div>
                    <div class="interviewer-hero-icon"><i class="bi bi-chat-square-quote"></i><small>Đánh giá công bằng</small></div>
                </section>

                <div class="row g-3 mb-4">
                    <div class="col-sm-6 col-xl-4"><a class="interviewer-stat" href="${pageContext.request.contextPath}/interviewer/interviews"><span class="interview-stat-blue"><i class="bi bi-calendar-event"></i></span><div><small>Lịch sắp tới</small><strong><c:out value="${fn:length(interviews)}" /></strong><em>Cuộc phỏng vấn</em></div></a></div>
                    <div class="col-sm-6 col-xl-4"><div class="interviewer-stat"><span class="interview-stat-violet"><i class="bi bi-camera-video"></i></span><div><small>Trực tuyến</small><strong><c:out value="${onlineInterviewCount}" /></strong><em>Lịch họp trực tuyến</em></div></div></div>
                    <div class="col-sm-6 col-xl-4"><div class="interviewer-stat"><span class="interview-stat-green"><i class="bi bi-clipboard-check"></i></span><div><small>Cần chuẩn bị</small><strong><c:out value="${fn:length(interviews)}" /></strong><em>Hồ sơ cần xem trước</em></div></div></div>
                </div>

                <div class="row g-4">
                    <div class="col-xl-8">
                        <section class="interviewer-panel h-100">
                            <header class="interviewer-panel-header"><div><span>LỊCH LÀM VIỆC</span><h2>Phỏng vấn sắp tới</h2><p>Chuẩn bị hồ sơ trước thời gian hẹn.</p></div><a href="${pageContext.request.contextPath}/interviewer/interviews">Xem tất cả<i class="bi bi-arrow-right ms-1"></i></a></header>
                            <div class="interviewer-schedule">
                                <c:forEach var="item" items="${interviews}" end="5">
                                    <a href="${pageContext.request.contextPath}/interviewer/interviews/detail?id=${item.id}">
                                        <span class="interviewer-date"><strong><fmt:formatDate value="${item.interviewDate}" pattern="dd" /></strong><small><fmt:formatDate value="${item.interviewDate}" pattern="'Th'M" /></small></span>
                                        <span class="interviewer-person"><span><c:out value="${fn:toUpperCase(fn:substring(item.candidateName, 0, 1))}" /></span><span><strong><c:out value="${item.candidateName}" /></strong><small>Hồ sơ #<c:out value="${item.applicationId}" /></small></span></span>
                                        <span class="interviewer-position"><strong><c:out value="${item.jobTitle}" /></strong><small><i class="bi bi-clock"></i><c:out value="${item.startTime}" /> – <c:out value="${item.endTime}" /></small></span>
                                        <span class="interviewer-type"><i class="bi ${item.interviewType eq 'ONLINE' ? 'bi-camera-video' : item.interviewType eq 'PHONE' ? 'bi-telephone' : 'bi-building'}"></i><span data-enum-label="${item.interviewType}"><c:out value="${item.interviewType}" /></span></span>
                                        <i class="bi bi-chevron-right"></i>
                                    </a>
                                </c:forEach>
                                <c:if test="${empty interviews}"><div class="interviewer-empty"><i class="bi bi-calendar2-check"></i><strong>Lịch của bạn đang trống</strong><p>Buổi phỏng vấn mới do HR phân công sẽ xuất hiện tại đây.</p></div></c:if>
                            </div>
                        </section>
                    </div>
                    <div class="col-xl-4">
                        <section class="interviewer-panel mb-4">
                            <header class="interviewer-panel-header"><div><span>CHUẨN BỊ TỐT</span><h2>Danh sách kiểm tra phỏng vấn</h2></div></header>
                            <div class="interviewer-checklist"><span><i class="bi bi-check2"></i><span><strong>Đọc CV và mô tả công việc</strong><small>Ghi lại các điểm cần làm rõ</small></span></span><span><i class="bi bi-check2"></i><span><strong>Chuẩn bị bộ câu hỏi</strong><small>Bám sát tiêu chí của vị trí</small></span></span><span><i class="bi bi-check2"></i><span><strong>Kiểm tra phòng họp</strong><small>Sẵn sàng trước lịch hẹn 5 phút</small></span></span><span><i class="bi bi-check2"></i><span><strong>Gửi đánh giá đúng hạn</strong><small>Đánh giá dựa trên bằng chứng</small></span></span></div>
                        </section>
                        <aside class="interviewer-tip"><span><i class="bi bi-lightbulb"></i></span><div><strong>Mẹo phỏng vấn</strong><p>Đặt cùng một nhóm câu hỏi cốt lõi cho mọi ứng viên để đảm bảo đánh giá công bằng.</p></div></aside>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
