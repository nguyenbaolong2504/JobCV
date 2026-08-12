<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Lịch phỏng vấn | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><p class="text-muted small mb-1">Tuyển dụng</p><h4 class="mb-0">Lịch phỏng vấn</h4></div>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/hr/interviews/create"><i class="bi bi-calendar-plus me-1"></i> Lên lịch mới</a>
            </div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="successMessage" scope="session" /></c:if>
                <c:if test="${not empty sessionScope.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${sessionScope.error}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="error" scope="session" /></c:if>

                <section class="card mb-4"><div class="card-body"><form class="row gy-2 gx-3 align-items-end" method="get" action="${pageContext.request.contextPath}/hr/interviews"><div class="col-md-4"><label class="form-label small text-muted">Từ khóa</label><input class="form-control" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="Ứng viên, vị trí, người phỏng vấn"></div><div class="col-md-3"><label class="form-label small text-muted">Ngày phỏng vấn</label><input class="form-control" type="date" name="date" value="<c:out value='${param.date}'/>"></div><div class="col-md-3"><label class="form-label small text-muted">Trạng thái</label><select class="form-select" name="status"><option value="">Tất cả trạng thái</option><option value="SCHEDULED">Đã lên lịch</option><option value="COMPLETED">Hoàn thành</option><option value="RESCHEDULED">Đã đổi lịch</option><option value="CANCELLED">Đã hủy</option></select></div><div class="col-md-2 d-flex gap-2"><button class="btn btn-outline-primary flex-grow-1" type="submit"><i class="bi bi-search"></i> Lọc</button><a class="btn btn-light" href="${pageContext.request.contextPath}/hr/interviews"><i class="bi bi-arrow-counterclockwise"></i></a></div></form></div></section>

                <section class="card">
                    <div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Danh sách lịch phỏng vấn</h5><small class="text-muted">Kiểm tra lịch trống của interviewer trước khi tạo hoặc thay đổi lịch.</small></div>
                    <div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th class="ps-4">Ứng viên</th><th>Vị trí</th><th>Interviewer</th><th>Thời gian</th><th>Hình thức</th><th>Trạng thái</th><th class="text-end pe-4">Thao tác</th></tr></thead><tbody>
                        <c:forEach var="item" items="${interviews}">
                            <tr><td class="ps-4"><div class="fw-semibold"><c:out value="${item.candidateName}" /></div><small class="text-muted">#<c:out value="${item.applicationId}" /></small></td><td><c:out value="${item.jobTitle}" /></td><td><c:out value="${item.interviewerName}" /></td><td><div><c:out value="${item.interviewDate}" /></div><small class="text-muted"><c:out value="${item.startTime}" /> – <c:out value="${item.endTime}" /></small></td><td><c:out value="${item.interviewType}" /></td><td><c:choose><c:when test="${item.status eq 'SCHEDULED'}"><span class="badge text-bg-primary">Đã lên lịch</span></c:when><c:when test="${item.status eq 'COMPLETED'}"><span class="badge text-bg-success">Hoàn thành</span></c:when><c:when test="${item.status eq 'RESCHEDULED'}"><span class="badge text-bg-warning">Đổi lịch</span></c:when><c:otherwise><span class="badge text-bg-secondary">Đã hủy</span></c:otherwise></c:choose></td>
                            <td class="text-end pe-4"><div class="dropdown"><button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">Thao tác</button><ul class="dropdown-menu dropdown-menu-end"><li><a class="dropdown-item" href="${pageContext.request.contextPath}/hr/applications/detail?id=${item.applicationId}"><i class="bi bi-eye me-2"></i>Hồ sơ ứng viên</a></li><c:if test="${item.status eq 'SCHEDULED' or item.status eq 'RESCHEDULED'}"><li><a class="dropdown-item" href="${pageContext.request.contextPath}/hr/interviews/edit?id=${item.id}"><i class="bi bi-calendar-event me-2"></i>Đổi lịch</a></li><li><form action="${pageContext.request.contextPath}/hr/interviews/cancel" method="post"><input type="hidden" name="id" value="${item.id}"><button class="dropdown-item text-danger" type="submit"><i class="bi bi-x-circle me-2"></i>Hủy lịch</button></form></li></c:if></ul></div></td></tr>
                        </c:forEach>
                        <c:if test="${empty interviews}"><tr><td colspan="7" class="py-5 text-center text-muted"><i class="bi bi-calendar-x d-block fs-3 mb-2"></i>Chưa có lịch phỏng vấn.</td></tr></c:if>
                    </tbody></table></div>
                </section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
