<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đơn ứng tuyển | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><p class="text-muted small mb-1">Tuyển dụng</p><h4 class="mb-0">Đơn ứng tuyển</h4></div>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/hr/interviews/create"><i class="bi bi-calendar-plus me-1"></i> Lên lịch phỏng vấn</a>
            </div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="successMessage" scope="session" /></c:if>
                <c:if test="${not empty sessionScope.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${sessionScope.error}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="error" scope="session" /></c:if>

                <section class="card mb-4">
                    <div class="card-body">
                        <form class="row gy-2 gx-3 align-items-end" method="get" action="${pageContext.request.contextPath}/hr/applications">
                            <div class="col-lg-4"><label class="form-label small text-muted">Tìm kiếm</label><input class="form-control" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="Tên ứng viên hoặc vị trí"></div>
                            <div class="col-lg-3"><label class="form-label small text-muted">Tin tuyển dụng</label><select class="form-select" name="jobId"><option value="">Tất cả vị trí</option><c:forEach var="job" items="${jobs}"><option value="${job.id}" <c:if test="${param.jobId eq job.id}">selected</c:if>><c:out value="${job.title}" /></option></c:forEach></select></div>
                            <div class="col-lg-2"><label class="form-label small text-muted">Trạng thái</label><select class="form-select" name="status"><option value="">Tất cả trạng thái</option><option value="SUBMITTED">Đã nộp</option><option value="SCREENING">Sàng lọc</option><option value="SHORTLISTED">Danh sách ngắn</option><option value="INTERVIEW_SCHEDULED">Đã hẹn phỏng vấn</option><option value="INTERVIEWED">Đã phỏng vấn</option><option value="OFFERED">Đã gửi offer</option><option value="HIRED">Đã tuyển</option><option value="REJECTED">Từ chối</option></select></div>
                            <div class="col-lg-2"><label class="form-label small text-muted">Match score từ</label><input class="form-control" type="number" min="0" max="100" name="minMatchScore" value="<c:out value='${param.minMatchScore}'/>" placeholder="0–100"></div>
                            <div class="col-lg-1 d-flex gap-2"><button class="btn btn-outline-primary flex-grow-1" type="submit" aria-label="Lọc"><i class="bi bi-search"></i></button><a class="btn btn-light" href="${pageContext.request.contextPath}/hr/applications" aria-label="Xóa bộ lọc"><i class="bi bi-arrow-counterclockwise"></i></a></div>
                        </form>
                    </div>
                </section>

                <section class="card">
                    <div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Ứng viên</h5><small class="text-muted">Theo dõi và xử lý từng bước của quy trình tuyển dụng.</small></div>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light"><tr><th class="ps-4">Ứng viên</th><th>Vị trí ứng tuyển</th><th>Ngày nộp</th><th>Match score</th><th>Trạng thái</th><th class="text-end pe-4">Thao tác</th></tr></thead>
                            <tbody>
                                <c:forEach var="item" items="${applicationPage.items}">
                                    <tr>
                                        <td class="ps-4"><div class="fw-semibold"><c:out value="${item.candidateName}" /></div><small class="text-muted"><c:out value="${item.candidateEmail}" /></small></td>
                                        <td><c:out value="${item.jobTitle}" /></td>
                                        <td><c:out value="${item.appliedAt}" /></td>
                                        <td><span class="fw-semibold ${item.matchScore ge 70 ? 'text-success' : item.matchScore ge 45 ? 'text-warning' : 'text-danger'}"><c:out value="${item.matchScore}" />%</span></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.status eq 'SUBMITTED'}"><span class="badge text-bg-secondary">Đã nộp</span></c:when>
                                                <c:when test="${item.status eq 'SCREENING'}"><span class="badge text-bg-warning">Sàng lọc</span></c:when>
                                                <c:when test="${item.status eq 'SHORTLISTED'}"><span class="badge text-bg-info">Danh sách ngắn</span></c:when>
                                                <c:when test="${item.status eq 'INTERVIEW_SCHEDULED'}"><span class="badge text-bg-primary">Đã hẹn PV</span></c:when>
                                                <c:when test="${item.status eq 'INTERVIEWED'}"><span class="badge text-bg-primary">Đã phỏng vấn</span></c:when>
                                                <c:when test="${item.status eq 'OFFERED'}"><span class="badge text-bg-info">Đã gửi offer</span></c:when>
                                                <c:when test="${item.status eq 'HIRED'}"><span class="badge text-bg-success">Đã tuyển</span></c:when>
                                                <c:when test="${item.status eq 'WITHDRAWN'}"><span class="badge text-bg-dark">Đã rút đơn</span></c:when>
                                                <c:otherwise><span class="badge text-bg-danger">Từ chối</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end pe-4"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/hr/applications/detail?id=${item.id}">Xem hồ sơ <i class="bi bi-arrow-right ms-1"></i></a></td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty applicationPage.items}"><tr><td colspan="6" class="text-center text-muted py-5"><i class="bi bi-people d-block fs-3 mb-2"></i>Không có đơn ứng tuyển phù hợp.</td></tr></c:if>
                            </tbody>
                        </table>
                    </div>
                    <c:if test="${not empty applicationPage and applicationPage.totalPages gt 1}">
                        <nav class="p-3 border-top" aria-label="Phân trang đơn ứng tuyển"><ul class="pagination pagination-sm mb-0 justify-content-end"><c:forEach begin="1" end="${applicationPage.totalPages}" var="p"><li class="page-item ${p eq applicationPage.currentPage ? 'active' : ''}"><a class="page-link" href="${pageContext.request.contextPath}/hr/applications?page=${p}&keyword=${param.keyword}&jobId=${param.jobId}&status=${param.status}&minMatchScore=${param.minMatchScore}">${p}</a></li></c:forEach></ul></nav>
                    </c:if>
                </section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

