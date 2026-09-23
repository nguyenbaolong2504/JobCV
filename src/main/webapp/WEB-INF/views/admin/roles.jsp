<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Phân quyền | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Quản trị hệ thống</p><h4 class="mb-0">Phân quyền</h4></div><a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/users"><i class="bi bi-people me-1"></i> Gán vai trò cho người dùng</a></div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <div class="alert alert-info border-0"><i class="bi bi-info-circle me-2"></i>Các vai trò hệ thống là cố định để bảo đảm các bộ lọc phân quyền và luồng tuyển dụng luôn nhất quán. Quản trị viên quản lý việc gán vai trò tại màn hình Người dùng.</div>
                <section class="card"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Vai trò hệ thống</h5><small class="text-muted">Mỗi vai trò có tập quyền riêng trên các URL bảo vệ.</small></div><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th class="ps-4">Vai trò</th><th>Mô tả</th><th class="pe-4">Phạm vi truy cập</th></tr></thead><tbody><c:forEach var="role" items="${roles}"><tr><td class="ps-4"><span class="badge text-bg-primary"><c:out value="${role.roleName}" /></span></td><td><c:out value="${role.description}" /></td><td class="pe-4"><c:choose><c:when test="${role.roleName eq 'ADMIN'}">/admin/*</c:when><c:when test="${role.roleName eq 'HR'}">/hr/*</c:when><c:when test="${role.roleName eq 'INTERVIEWER'}">/interviewer/*</c:when><c:otherwise>/candidate/*</c:otherwise></c:choose></td></tr></c:forEach><c:if test="${empty roles}"><tr><td colspan="3" class="py-5 text-center text-muted">Chưa tải được cấu hình vai trò.</td></tr></c:if></tbody></table></div></section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
