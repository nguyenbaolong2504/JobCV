<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đơn ứng tuyển | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell"><div class="row g-0">
    <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />
    <main class="candidate-main col-lg-9 col-xl-10">
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <div class="d-flex flex-wrap justify-content-between gap-3 mb-4"><div><h1 class="page-title mb-1">Đơn ứng tuyển</h1><p class="text-muted mb-0">Theo dõi tiến trình tuyển dụng của từng vị trí bạn đã nộp.</p></div><a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/jobs"><i class="bi bi-plus-lg me-1"></i>Tìm việc làm</a></div>
        <form class="filter-card mb-4" action="${pageContext.request.contextPath}/candidate/applications" method="get"><div class="row g-3 align-items-end"><div class="col-md-7"><label class="form-label fw-semibold" for="applicationKeyword">Tìm kiếm</label><input class="form-control" id="applicationKeyword" name="keyword" type="search" value="<c:out value='${param.keyword}'/>" placeholder="Tên vị trí hoặc mã công việc"></div><div class="col-md-3"><label class="form-label fw-semibold" for="applicationStatus">Trạng thái</label><select class="form-select" id="applicationStatus" name="status"><option value="">Tất cả trạng thái</option><c:forEach var="status" items="${statuses}"><option value="<c:out value='${status}'/>" ${param.status eq status ? 'selected' : ''}><c:out value="${status}" /></option></c:forEach></select></div><div class="col-md-2 d-grid"><button class="btn btn-outline-primary" type="submit">Lọc</button></div></div></form>
        <section class="content-card p-0 overflow-hidden"><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th>Vị trí ứng tuyển</th><th>Ngày nộp</th><th>Độ phù hợp</th><th>Trạng thái</th><th class="text-end">Thao tác</th></tr></thead><tbody>
            <c:forEach var="application" items="${page.items}"><c:url var="applicationDetailUrl" value="/candidate/applications/detail"><c:param name="id" value="${application.id}" /></c:url><tr><td><a class="fw-semibold text-decoration-none" href="${applicationDetailUrl}"><c:out value="${application.jobTitle}" /></a><div class="small text-muted"><c:out value="${application.jobCode}" /></div></td><td><c:out value="${application.appliedAt}" /></td><td><span class="match-badge"><c:out value="${application.matchScore}" />%</span></td><td><span class="status-badge status-${application.status}"><c:out value="${application.status}" /></span></td><td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${applicationDetailUrl}">Chi tiết <i class="bi bi-arrow-right ms-1"></i></a></td></tr></c:forEach>
            <c:if test="${empty page.items}"><tr><td colspan="5"><div class="empty-state"><div class="empty-icon"><i class="bi bi-send"></i></div><h2 class="h5">Chưa có đơn ứng tuyển</h2><p class="mb-3">Hãy khám phá vị trí đang mở và ứng tuyển bằng CV của bạn.</p><a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/jobs">Tìm việc làm</a></div></td></tr></c:if>
        </tbody></table></div></section>
        <c:set var="paginationPath" value="/candidate/applications" scope="request" /><jsp:include page="/WEB-INF/views/common/pagination.jsp" />
    </main>
</div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
