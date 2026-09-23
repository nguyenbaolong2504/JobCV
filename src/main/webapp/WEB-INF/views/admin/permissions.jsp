<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Phân quyền nâng cao | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><p class="text-muted small mb-1">Quản trị hệ thống</p><h4 class="mb-0">Phân quyền nâng cao</h4></div>
                <span class="badge text-bg-primary"><i class="bi bi-shield-check me-1"></i>RBAC theo hành động</span>
            </div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}" /></div></c:if>
                <section class="card mb-4">
                    <div class="card-body p-4">
                        <div class="d-flex flex-wrap justify-content-between gap-3 align-items-start">
                            <div><h5 class="mb-1">Ma trận quyền theo vai trò</h5><p class="text-muted mb-0">Quyền được kiểm tra ở từng request; thay đổi áp dụng ngay, không cần logout.</p></div>
                            <form method="get" action="${pageContext.request.contextPath}/admin/permissions" class="d-flex gap-2 align-items-center">
                                <label class="visually-hidden" for="roleId">Vai trò</label>
                                <select class="form-select" id="roleId" name="roleId" onchange="this.form.submit()">
                                    <c:forEach var="role" items="${roles}"><option value="${role.id}" ${role.id eq selectedRoleId ? 'selected' : ''}><c:out value="${role.roleName}" /> — <c:out value="${role.description}" /></option></c:forEach>
                                </select>
                            </form>
                        </div>
                    </div>
                </section>

                <form method="post" action="${pageContext.request.contextPath}/admin/permissions/update">
                    <input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>">
                    <input type="hidden" name="roleId" value="${selectedRoleId}">
                    <section class="card">
                        <div class="card-header bg-white border-0 px-4 pt-4 d-flex justify-content-between gap-3 align-items-center">
                            <div><h5 class="mb-1">Quyền được cấp</h5><small class="text-muted">Bỏ chọn một quyền sẽ trả HTTP 403 tại hành động tương ứng.</small></div>
                            <button class="btn btn-primary" type="submit"><i class="bi bi-save me-1"></i>Lưu quyền</button>
                        </div>
                        <div class="card-body px-4 pb-4">
                            <c:set var="currentModule" value="" />
                            <div class="row g-3">
                                <c:forEach var="permission" items="${permissions}">
                                    <div class="col-md-6 col-xl-4">
                                        <label class="border rounded-3 p-3 h-100 w-100 bg-white permission-option">
                                            <span class="d-flex gap-3 align-items-start">
                                                <input class="form-check-input mt-1" type="checkbox" name="permissionCode" value="<c:out value='${permission.permissionCode}'/>" ${assignedPermissionCodes.contains(permission.permissionCode) ? 'checked' : ''}>
                                                <span><small class="text-primary fw-semibold d-block mb-1"><c:out value="${permission.module}" /></small><strong class="d-block"><c:out value="${permission.displayName}" /></strong><small class="text-muted d-block mt-1"><c:out value="${permission.description}" /></small></span>
                                            </span>
                                        </label>
                                    </div>
                                </c:forEach>
                                <c:if test="${empty permissions}"><div class="col-12 text-center text-muted py-5">Chưa có quyền để cấu hình. Hãy chạy migration RBAC.</div></c:if>
                            </div>
                        </div>
                    </section>
                </form>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
