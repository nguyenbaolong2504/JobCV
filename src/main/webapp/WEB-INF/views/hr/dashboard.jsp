<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Bảng điều khiển tuyển dụng | JobCV" />
<jsp:useBean id="now" class="java.util.Date" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><p class="text-muted mb-1 small">Trung tâm điều hành</p><h4 class="mb-0">Tổng quan tuyển dụng</h4></div>
                <div class="d-flex align-items-center gap-2">
                    <a class="btn btn-light border" href="${pageContext.request.contextPath}/hr/reports"><i class="bi bi-bar-chart me-1"></i>Báo cáo</a>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/hr/jobs/create"><i class="bi bi-plus-lg me-1"></i>Tạo tin tuyển dụng</a>
                </div>
            </div>

            <div class="main-content hr-dashboard">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />

                <section class="dashboard-welcome mb-4">
                    <div>
                        <span class="dashboard-date"><i class="bi bi-calendar3 me-1"></i><fmt:formatDate value="${now}" pattern="dd/MM/yyyy" /></span>
                        <h2>Chào <c:out value="${sessionScope.fullName}" />!</h2>
                        <p>
                            <c:choose>
                                <c:when test="${hrStats.newApplications gt 0}">Có <strong><c:out value="${hrStats.newApplications}" /> hồ sơ mới</strong> đang chờ bạn xem xét trong pipeline.</c:when>
                                <c:otherwise>Mọi thứ đang trong tầm kiểm soát. Hãy kiểm tra các đầu việc tuyển dụng tiếp theo.</c:otherwise>
                            </c:choose>
                        </p>
                        <div class="d-flex flex-wrap gap-2">
                            <a class="btn btn-light" href="${pageContext.request.contextPath}/hr/applications"><i class="bi bi-kanban me-1"></i>Mở pipeline</a>
                            <a class="btn btn-outline-light" href="${pageContext.request.contextPath}/hr/interviews"><i class="bi bi-calendar-event me-1"></i>Xem lịch phỏng vấn</a>
                        </div>
                    </div>
                    <div class="dashboard-health" aria-label="Sức khỏe tuyển dụng">
                        <div class="dashboard-health-ring" style="--health-value: ${hrStats.hiringRate * 3.6}deg"><span><strong><c:out value="${hrStats.hiringRate}" />%</strong><small>Tỷ lệ tuyển</small></span></div>
                        <p><i class="bi bi-activity me-1"></i>Sức khỏe tuyển dụng</p>
                    </div>
                </section>

                <div class="row g-3 mb-4 dashboard-kpis">
                    <div class="col-sm-6 col-xxl-3">
                        <a class="dashboard-kpi-card" href="${pageContext.request.contextPath}/hr/jobs">
                            <span class="dashboard-kpi-icon metric-blue"><i class="bi bi-briefcase"></i></span>
                            <div><small>Tin đang tuyển</small><strong><c:out value="${hrStats.activeJobs}" /></strong><span>Vị trí đang công khai</span></div><i class="bi bi-arrow-up-right dashboard-kpi-arrow"></i>
                        </a>
                    </div>
                    <div class="col-sm-6 col-xxl-3">
                        <a class="dashboard-kpi-card" href="${pageContext.request.contextPath}/hr/applications">
                            <span class="dashboard-kpi-icon metric-violet"><i class="bi bi-people"></i></span>
                            <div><small>Tổng ứng viên</small><strong><c:out value="${hrStats.totalApplications}" /></strong><span><c:out value="${hrStats.newApplications}" /> hồ sơ mới cần xem</span></div><i class="bi bi-arrow-up-right dashboard-kpi-arrow"></i>
                        </a>
                    </div>
                    <div class="col-sm-6 col-xxl-3">
                        <a class="dashboard-kpi-card" href="${pageContext.request.contextPath}/hr/interviews">
                            <span class="dashboard-kpi-icon metric-amber"><i class="bi bi-calendar2-check"></i></span>
                            <div><small>Phỏng vấn sắp tới</small><strong><c:out value="${hrStats.upcomingInterviews}" /></strong><span>Lịch đang chờ thực hiện</span></div><i class="bi bi-arrow-up-right dashboard-kpi-arrow"></i>
                        </a>
                    </div>
                    <div class="col-sm-6 col-xxl-3">
                        <a class="dashboard-kpi-card" href="${pageContext.request.contextPath}/hr/offers">
                            <span class="dashboard-kpi-icon metric-green"><i class="bi bi-envelope-check"></i></span>
                            <div><small>Tỷ lệ nhận thư mời</small><strong><c:out value="${hrStats.offerAcceptanceRate}" />%</strong><span><c:out value="${hrStats.acceptedOffers}" /> thư mời đã chấp nhận</span></div><i class="bi bi-arrow-up-right dashboard-kpi-arrow"></i>
                        </a>
                    </div>
                </div>

                <div class="row g-4 mb-4">
                    <div class="col-xl-8">
                        <section class="dashboard-panel h-100">
                            <header class="dashboard-panel-header"><div><span class="dashboard-eyebrow">Hiệu suất 6 tháng</span><h5>Lượng hồ sơ ứng tuyển</h5></div><a href="${pageContext.request.contextPath}/hr/reports">Xem báo cáo<i class="bi bi-arrow-right ms-1"></i></a></header>
                            <c:set var="monthlyMax" value="0" />
                            <c:forEach var="monthlyEntry" items="${hrStats.applicationsByMonth}"><c:if test="${monthlyEntry.value gt monthlyMax}"><c:set var="monthlyMax" value="${monthlyEntry.value}" /></c:if></c:forEach>
                            <div class="dashboard-chart" role="img" aria-label="Biểu đồ hồ sơ ứng tuyển trong 6 tháng">
                                <div class="dashboard-chart-grid"><span></span><span></span><span></span><span></span></div>
                                <c:forEach var="monthlyEntry" items="${hrStats.applicationsByMonth}">
                                    <div class="dashboard-chart-column">
                                        <div class="dashboard-chart-value"><span><c:out value="${monthlyEntry.value}" /></span><div style="height: ${monthlyMax gt 0 ? monthlyEntry.value * 100 / monthlyMax : 4}%"></div></div>
                                        <small>Tháng ${fn:substring(monthlyEntry.key, 5, 7)}</small>
                                    </div>
                                </c:forEach>
                            </div>
                        </section>
                    </div>
                    <div class="col-xl-4">
                        <section class="dashboard-panel h-100">
                            <header class="dashboard-panel-header"><div><span class="dashboard-eyebrow">Ưu tiên</span><h5>Việc cần xử lý</h5></div><span class="dashboard-live"><i></i>Trực tiếp</span></header>
                            <div class="dashboard-action-list">
                                <a href="${pageContext.request.contextPath}/hr/applications?status=SUBMITTED"><span class="action-dot action-blue"><i class="bi bi-inbox"></i></span><span><strong>Hồ sơ mới</strong><small>Cần xem và bắt đầu sàng lọc</small></span><b><c:out value="${hrStats.newApplications}" /></b></a>
                                <a href="${pageContext.request.contextPath}/hr/applications?status=SHORTLISTED"><span class="action-dot action-violet"><i class="bi bi-calendar-plus"></i></span><span><strong>Chờ xếp lịch</strong><small>Ứng viên trong danh sách ngắn</small></span><b><c:out value="${hrStats.shortlistedCandidates}" /></b></a>
                                <a href="${pageContext.request.contextPath}/hr/applications?status=INTERVIEWED"><span class="action-dot action-amber"><i class="bi bi-clipboard-check"></i></span><span><strong>Chờ quyết định</strong><small>Đã hoàn tất phỏng vấn</small></span><b><c:out value="${hrStats.interviewedCandidates}" /></b></a>
                                <a href="${pageContext.request.contextPath}/hr/offers?status=DRAFT"><span class="action-dot action-green"><i class="bi bi-send"></i></span><span><strong>Thư mời bản nháp</strong><small>Cần kiểm tra và gửi ứng viên</small></span><b><c:out value="${hrStats.draftOffers}" /></b></a>
                            </div>
                        </section>
                    </div>
                </div>

                <div class="row g-4">
                    <div class="col-xl-7">
                        <section class="dashboard-panel h-100">
                            <header class="dashboard-panel-header"><div><span class="dashboard-eyebrow">Chuyển đổi</span><h5>Quy trình tuyển dụng</h5></div><a href="${pageContext.request.contextPath}/hr/applications">Chi tiết<i class="bi bi-arrow-right ms-1"></i></a></header>
                            <div class="dashboard-funnel">
                                <div class="dashboard-funnel-row"><div><span>Ứng tuyển</span><strong><c:out value="${funnel['APPLIED']}" /></strong></div><div class="funnel-track"><span class="funnel-applied" style="width: ${funnel['APPLIED'] gt 0 ? 100 : 0}%"></span></div><small>${funnel['APPLIED'] gt 0 ? 100 : 0}%</small></div>
                                <div class="dashboard-funnel-row"><div><span>Sàng lọc</span><strong><c:out value="${funnel['SCREENING']}" /></strong></div><div class="funnel-track"><span class="funnel-screening" style="width: ${funnel['APPLIED'] gt 0 ? funnel['SCREENING'] * 100 / funnel['APPLIED'] : 0}%"></span></div><small><fmt:formatNumber value="${funnel['APPLIED'] gt 0 ? funnel['SCREENING'] * 100 / funnel['APPLIED'] : 0}" maxFractionDigits="0" />%</small></div>
                                <div class="dashboard-funnel-row"><div><span>Phỏng vấn</span><strong><c:out value="${funnel['INTERVIEW']}" /></strong></div><div class="funnel-track"><span class="funnel-interview" style="width: ${funnel['APPLIED'] gt 0 ? funnel['INTERVIEW'] * 100 / funnel['APPLIED'] : 0}%"></span></div><small><fmt:formatNumber value="${funnel['APPLIED'] gt 0 ? funnel['INTERVIEW'] * 100 / funnel['APPLIED'] : 0}" maxFractionDigits="0" />%</small></div>
                                <div class="dashboard-funnel-row"><div><span>Gửi thư mời</span><strong><c:out value="${funnel['OFFERED']}" /></strong></div><div class="funnel-track"><span class="funnel-offered" style="width: ${funnel['APPLIED'] gt 0 ? funnel['OFFERED'] * 100 / funnel['APPLIED'] : 0}%"></span></div><small><fmt:formatNumber value="${funnel['APPLIED'] gt 0 ? funnel['OFFERED'] * 100 / funnel['APPLIED'] : 0}" maxFractionDigits="0" />%</small></div>
                                <div class="dashboard-funnel-row"><div><span>Đã tuyển</span><strong><c:out value="${funnel['HIRED']}" /></strong></div><div class="funnel-track"><span class="funnel-hired" style="width: ${funnel['APPLIED'] gt 0 ? funnel['HIRED'] * 100 / funnel['APPLIED'] : 0}%"></span></div><small><c:out value="${hrStats.hiringRate}" />%</small></div>
                            </div>
                        </section>
                    </div>
                    <div class="col-xl-5">
                        <section class="dashboard-panel h-100">
                            <header class="dashboard-panel-header"><div><span class="dashboard-eyebrow">Lịch làm việc</span><h5>Phỏng vấn sắp tới</h5></div><a href="${pageContext.request.contextPath}/hr/interviews">Tất cả<i class="bi bi-arrow-right ms-1"></i></a></header>
                            <div class="dashboard-interviews">
                                <c:forEach var="interview" items="${interviews}" end="3">
                                    <a href="${pageContext.request.contextPath}/hr/applications/detail?id=${interview.applicationId}">
                                        <span class="interview-date"><strong><fmt:formatDate value="${interview.interviewDate}" pattern="dd" /></strong><small><fmt:formatDate value="${interview.interviewDate}" pattern="'Th'M" /></small></span>
                                        <span class="interview-info"><strong><c:out value="${interview.candidateName}" /></strong><small><c:out value="${interview.jobTitle}" /></small><em><i class="bi bi-clock"></i><c:out value="${interview.startTime}" /> · <c:choose><c:when test="${interview.interviewType eq 'ONLINE'}">Trực tuyến</c:when><c:when test="${interview.interviewType eq 'PHONE'}">Điện thoại</c:when><c:otherwise>Trực tiếp</c:otherwise></c:choose></em></span>
                                        <i class="bi bi-chevron-right"></i>
                                    </a>
                                </c:forEach>
                                <c:if test="${empty interviews}"><div class="dashboard-interviews-empty"><span><i class="bi bi-calendar2-check"></i></span><strong>Chưa có lịch sắp tới</strong><small>Các buổi phỏng vấn mới sẽ xuất hiện tại đây.</small><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/hr/interviews/create">Tạo lịch phỏng vấn</a></div></c:if>
                            </div>
                        </section>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
