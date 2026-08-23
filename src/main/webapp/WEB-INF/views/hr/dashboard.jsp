<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="HR Dashboard | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div>
                    <p class="text-muted mb-1 small">Tổng quan tuyển dụng</p>
                    <h4 class="mb-0">HR Dashboard</h4>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/hr/jobs/create">
                        <i class="bi bi-plus-lg me-1"></i> Tạo tin tuyển dụng
                    </a>
                    <span class="fw-semibold"><c:out value="${sessionScope.fullName}" /></span>
                </div>
            </div>

            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <div class="row g-3 mb-4">
                    <div class="col-sm-6 col-xl-3">
                        <div class="card h-100 p-3">
                            <div class="d-flex justify-content-between">
                                <div><p class="text-muted mb-1">Tin đang tuyển</p><h3 class="mb-0"><c:out value="${hrStats.activeJobs}" /></h3></div>
                                <span class="icon-shape bg-primary-subtle text-primary"><i class="bi bi-briefcase"></i></span>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card h-100 p-3">
                            <div class="d-flex justify-content-between">
                                <div><p class="text-muted mb-1">Tổng đơn ứng tuyển</p><h3 class="mb-0"><c:out value="${hrStats.totalApplications}" /></h3></div>
                                <span class="icon-shape bg-info-subtle text-info"><i class="bi bi-file-earmark-person"></i></span>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card h-100 p-3">
                            <div class="d-flex justify-content-between">
                                <div><p class="text-muted mb-1">Đang sàng lọc</p><h3 class="mb-0"><c:out value="${hrStats.screeningCandidates}" /></h3></div>
                                <span class="icon-shape bg-warning-subtle text-warning"><i class="bi bi-funnel"></i></span>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card h-100 p-3">
                            <div class="d-flex justify-content-between">
                                <div><p class="text-muted mb-1">Sắp phỏng vấn</p><h3 class="mb-0"><c:out value="${hrStats.upcomingInterviews}" /></h3></div>
                                <span class="icon-shape bg-success-subtle text-success"><i class="bi bi-calendar-check"></i></span>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card h-100 p-3">
                            <div class="d-flex justify-content-between">
                                <div><p class="text-muted mb-1">Offer đã gửi</p><h3 class="mb-0"><c:out value="${hrStats.offersSent}" /></h3></div>
                                <span class="icon-shape bg-secondary-subtle text-secondary"><i class="bi bi-envelope-check"></i></span>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card h-100 p-3">
                            <div class="d-flex justify-content-between">
                                <div><p class="text-muted mb-1">Đã tuyển</p><h3 class="mb-0"><c:out value="${hrStats.hiredCandidates}" /></h3></div>
                                <span class="icon-shape bg-success-subtle text-success"><i class="bi bi-person-check"></i></span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row g-4">
                    <div class="col-xl-7">
                        <section class="card h-100">
                            <div class="card-header bg-white border-0 d-flex justify-content-between align-items-center pt-4 px-4">
                                <div><h5 class="mb-1">Tuyển dụng theo pipeline</h5><p class="text-muted small mb-0">Tình trạng đơn ứng tuyển hiện tại</p></div>
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/hr/reports">Xem báo cáo</a>
                            </div>
                            <div class="card-body px-4 pb-4">
                                <div class="row g-2 text-center">
                                    <div class="col"><div class="border rounded-3 p-3"><small class="text-muted d-block">Đã nộp</small><strong class="fs-4"><c:out value="${funnel['SUBMITTED']}" /></strong></div></div>
                                    <div class="col"><div class="border rounded-3 p-3"><small class="text-muted d-block">Sàng lọc</small><strong class="fs-4"><c:out value="${funnel['SCREENING']}" /></strong></div></div>
                                    <div class="col"><div class="border rounded-3 p-3"><small class="text-muted d-block">Phỏng vấn</small><strong class="fs-4"><c:out value="${funnel['INTERVIEW']}" /></strong></div></div>
                                    <div class="col"><div class="border rounded-3 p-3"><small class="text-muted d-block">Offer</small><strong class="fs-4"><c:out value="${funnel['OFFERED']}" /></strong></div></div>
                                    <div class="col"><div class="border rounded-3 p-3"><small class="text-muted d-block">Hired</small><strong class="fs-4 text-success"><c:out value="${funnel['HIRED']}" /></strong></div></div>
                                </div>
                            </div>
                        </section>
                    </div>
                    <div class="col-xl-5">
                        <section class="card h-100">
                            <div class="card-header bg-white border-0 d-flex justify-content-between align-items-center pt-4 px-4">
                                <div><h5 class="mb-1">Phỏng vấn sắp tới</h5><p class="text-muted small mb-0">Lịch cần chuẩn bị</p></div>
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/hr/interviews">Tất cả</a>
                            </div>
                            <div class="list-group list-group-flush">
                                <c:forEach var="interview" items="${interviews}" end="4">
                                    <a class="list-group-item list-group-item-action px-4 py-3"
                                       href="${pageContext.request.contextPath}/hr/applications/detail?id=${interview.applicationId}">
                                        <div class="d-flex justify-content-between gap-3">
                                            <div><div class="fw-semibold"><c:out value="${interview.candidateName}" /></div><small class="text-muted"><c:out value="${interview.jobTitle}" /></small></div>
                                            <div class="text-end"><small class="d-block"><c:out value="${interview.interviewDate}" /></small><small class="text-muted"><c:out value="${interview.startTime}" /></small></div>
                                        </div>
                                    </a>
                                </c:forEach>
                                <c:if test="${empty interviews}">
                                    <div class="px-4 py-4 text-center text-muted"><i class="bi bi-calendar-x d-block fs-4 mb-2"></i>Chưa có lịch phỏng vấn sắp tới.</div>
                                </c:if>
                            </div>
                        </section>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
