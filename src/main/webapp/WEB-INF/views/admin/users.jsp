<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Quản lý người dùng | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><p class="text-muted small mb-1">Quản trị hệ thống</p><h4 class="mb-0">Quản lý người dùng</h4></div>
                <span class="text-muted small">Kiểm tra thông tin tổ chức trước khi kích hoạt recruiter INACTIVE.</span>
            </div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />

                <section class="card mb-4">
                    <div class="card-body">
                        <form class="row gy-2 gx-3 align-items-end" method="get" action="${pageContext.request.contextPath}/admin/users">
                            <div class="col-lg-5"><label class="form-label small text-muted">Tìm kiếm</label><input class="form-control" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="Họ tên hoặc email"></div>
                            <div class="col-lg-2"><label class="form-label small text-muted">Vai trò</label><select class="form-select" name="role"><option value="">Tất cả vai trò</option><option value="ADMIN" ${param.role eq 'ADMIN' ? 'selected' : ''}>ADMIN</option><option value="HR" ${param.role eq 'HR' ? 'selected' : ''}>HR</option><option value="INTERVIEWER" ${param.role eq 'INTERVIEWER' ? 'selected' : ''}>INTERVIEWER</option><option value="CANDIDATE" ${param.role eq 'CANDIDATE' ? 'selected' : ''}>CANDIDATE</option></select></div>
                            <div class="col-lg-3"><label class="form-label small text-muted">Trạng thái</label><select class="form-select" name="status"><option value="">Tất cả trạng thái</option><option value="ACTIVE" ${param.status eq 'ACTIVE' ? 'selected' : ''}>ACTIVE</option><option value="LOCKED" ${param.status eq 'LOCKED' ? 'selected' : ''}>LOCKED</option><option value="INACTIVE" ${param.status eq 'INACTIVE' ? 'selected' : ''}>INACTIVE</option></select></div>
                            <div class="col-lg-2 d-flex gap-2"><button class="btn btn-outline-primary flex-grow-1" type="submit"><i class="bi bi-search"></i> Lọc</button><a class="btn btn-light" href="${pageContext.request.contextPath}/admin/users"><i class="bi bi-arrow-counterclockwise"></i></a></div>
                        </form>
                    </div>
                </section>

                <section class="card">
                    <div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Danh sách tài khoản</h5><small class="text-muted">Kích hoạt, khóa hoặc thay đổi vai trò đều được ghi vào audit log. Tài khoản bị đổi trạng thái bị kiểm tra lại ở request tiếp theo.</small></div>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light"><tr><th class="ps-4">Người dùng / xác minh</th><th>Vai trò</th><th>Trạng thái</th><th>Ngày tạo</th><th class="text-end pe-4">Thao tác</th></tr></thead>
                            <tbody>
                                <c:forEach var="user" items="${users}">
                                    <c:set var="recruiterProfile" value="${recruiterProfiles[user.id]}" />
                                    <tr>
                                        <td class="ps-4">
                                            <div class="fw-semibold"><c:out value="${user.fullName}" /></div>
                                            <small class="text-muted d-block"><c:out value="${user.email}" /></small>
                                            <c:if test="${not empty recruiterProfile}">
                                                <div class="small mt-2 p-2 rounded bg-light border">
                                                    <div class="fw-semibold text-primary"><i class="bi bi-building me-1"></i><c:out value="${recruiterProfile.organizationName}" /></div>
                                                    <div class="text-muted"><c:out value="${recruiterProfile.jobTitle}" /> · <c:out value="${recruiterProfile.workPhone}" /></div>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${user.id eq sessionScope.userId}"><span class="badge text-bg-light border"><c:out value="${user.roleName}" /></span></c:when>
                                                <c:otherwise>
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/users/role" class="d-flex gap-1 align-items-center">
                                                        <input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><input type="hidden" name="userId" value="${user.id}">
                                                        <select class="form-select form-select-sm" name="roleId" aria-label="Vai trò"><c:forEach var="role" items="${roles}"><option value="${role.id}" ${role.roleName eq user.roleName ? 'selected' : ''}><c:out value="${role.roleName}" /></option></c:forEach></select>
                                                        <button class="btn btn-sm btn-outline-primary" type="submit" title="Lưu vai trò"><i class="bi bi-check2"></i></button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><c:choose><c:when test="${user.status eq 'ACTIVE'}"><span class="badge text-bg-success">ACTIVE</span></c:when><c:when test="${user.status eq 'LOCKED'}"><span class="badge text-bg-danger">LOCKED</span></c:when><c:otherwise><span class="badge text-bg-secondary">INACTIVE</span></c:otherwise></c:choose></td>
                                        <td><c:out value="${user.createdAt}" /></td>
                                        <td class="text-end pe-4">
                                            <c:choose>
                                                <c:when test="${user.id eq sessionScope.userId}"><span class="small text-muted">Tài khoản hiện tại</span></c:when>
                                                <c:when test="${user.status eq 'LOCKED'}">
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/users/status" class="d-inline"><input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><input type="hidden" name="userId" value="${user.id}"><input type="hidden" name="status" value="ACTIVE"><button class="btn btn-sm btn-success" type="submit"><i class="bi bi-unlock me-1"></i>Mở khóa</button></form>
                                                </c:when>
                                                <c:when test="${user.status eq 'INACTIVE'}">
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/users/status" class="d-inline" data-confirm="Xác nhận đã kiểm tra thông tin và kích hoạt tài khoản này?"><input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><input type="hidden" name="userId" value="${user.id}"><input type="hidden" name="status" value="ACTIVE"><button class="btn btn-sm btn-primary" type="submit"><i class="bi bi-person-check me-1"></i>Kích hoạt</button></form>
                                                </c:when>
                                                <c:otherwise>
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/users/status" class="d-inline" data-confirm="Khóa tài khoản này?"><input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><input type="hidden" name="userId" value="${user.id}"><input type="hidden" name="status" value="LOCKED"><button class="btn btn-sm btn-outline-danger" type="submit"><i class="bi bi-lock me-1"></i>Khóa</button></form>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty users}"><tr><td colspan="5" class="py-5 text-center text-muted"><i class="bi bi-people d-block fs-3 mb-2"></i>Không tìm thấy người dùng phù hợp.</td></tr></c:if>
                            </tbody>
                        </table>
                    </div>
                    <c:if test="${not empty page and page.totalPages gt 1}">
                        <nav class="p-3 border-top" aria-label="Phân trang người dùng"><ul class="pagination pagination-sm mb-0 justify-content-end"><c:forEach begin="1" end="${page.totalPages}" var="p"><li class="page-item ${p eq page.currentPage ? 'active' : ''}"><a class="page-link" href="${pageContext.request.contextPath}/admin/users?page=${p}&amp;keyword=${param.keyword}&amp;role=${param.role}&amp;status=${param.status}">${p}</a></li></c:forEach></ul></nav>
                    </c:if>
                </section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
