<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Audit Logs | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Quản trị hệ thống</p><h4 class="mb-0">Audit logs</h4></div><span class="badge text-bg-light border">Chỉ đọc</span></div>
            <div class="main-content">
                <section class="card mb-4"><div class="card-body"><form class="row gy-2 gx-3 align-items-end" method="get" action="${pageContext.request.contextPath}/admin/audit-logs"><input type="hidden" name="pageSize" value="<c:out value='${empty param.pageSize ? 10 : param.pageSize}'/>"><div class="col-lg-4"><label class="form-label small text-muted">Người dùng / hành động</label><input class="form-control" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="Tên, email, hành động hoặc chi tiết"></div><div class="col-lg-3"><label class="form-label small text-muted">Entity</label><input class="form-control" name="entityName" value="<c:out value='${param.entityName}'/>" placeholder="users, jobs, applications..."></div><div class="col-lg-3"><label class="form-label small text-muted">Từ ngày</label><input class="form-control" name="fromDate" type="date" value="<c:out value='${param.fromDate}'/>"></div><div class="col-lg-2 d-flex gap-2"><button class="btn btn-outline-primary flex-grow-1" type="submit"><i class="bi bi-search"></i> Lọc</button><a class="btn btn-light" href="${pageContext.request.contextPath}/admin/audit-logs"><i class="bi bi-arrow-counterclockwise"></i></a></div></form></div></section>
                <section class="card"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Nhật ký hoạt động</h5><small class="text-muted">Bản ghi được tạo cho các hành động quản trị và thao tác quan trọng.</small></div><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th class="ps-4">Thời điểm</th><th>Người thực hiện</th><th>Hành động</th><th>Đối tượng</th><th>Chi tiết</th><th class="pe-4">IP</th></tr></thead><tbody>
                    <c:forEach var="log" items="${auditLogs}"><tr><td class="ps-4 text-nowrap"><c:out value="${log.createdAt}" /></td><td><c:out value="${log.userName}" /></td><td><span class="badge text-bg-light border"><c:out value="${log.action}" /></span></td><td><c:out value="${log.entityName}" /><c:if test="${not empty log.entityId}"> #<c:out value="${log.entityId}" /></c:if></td><td class="text-break" style="min-width:240px;"><c:out value="${log.details}" /></td><td class="pe-4 text-nowrap"><c:out value="${log.ipAddress}" /></td></tr></c:forEach>
                    <c:if test="${empty auditLogs}"><tr><td colspan="6" class="py-5 text-center text-muted"><i class="bi bi-journal-x d-block fs-3 mb-2"></i>Không có bản ghi phù hợp.</td></tr></c:if>
                </tbody></table></div><c:if test="${not empty page and page.totalPages gt 1}"><nav class="p-3 border-top" aria-label="Phân trang audit log"><ul class="pagination pagination-sm mb-0 justify-content-end"><c:forEach begin="1" end="${page.totalPages}" var="p"><c:url var="auditPageUrl" value="/admin/audit-logs"><c:param name="page" value="${p}" /><c:param name="pageSize" value="${page.pageSize}" /><c:param name="keyword" value="${param.keyword}" /><c:param name="entityName" value="${param.entityName}" /><c:param name="fromDate" value="${param.fromDate}" /></c:url><li class="page-item ${p eq page.currentPage ? 'active' : ''}"><a class="page-link" href="${auditPageUrl}">${p}</a></li></c:forEach></ul></nav></c:if></section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
