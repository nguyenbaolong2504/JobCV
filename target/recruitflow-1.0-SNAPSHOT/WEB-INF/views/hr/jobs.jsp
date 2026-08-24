<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Quản lý tin tuyển dụng | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><p class="text-muted small mb-1">Tuyển dụng</p><h4 class="mb-0">Tin tuyển dụng</h4></div>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/hr/jobs/create"><i class="bi bi-plus-lg me-1"></i> Thêm tin mới</a>
            </div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty sessionScope.successMessage}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" data-bs-dismiss="alert" type="button"></button></div>
                    <c:remove var="successMessage" scope="session" />
                </c:if>
                <c:if test="${not empty sessionScope.error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${sessionScope.error}" /><button class="btn-close" data-bs-dismiss="alert" type="button"></button></div>
                    <c:remove var="error" scope="session" />
                </c:if>

                <section class="card mb-4">
                    <div class="card-body">
                        <form class="row gy-2 gx-3 align-items-end" method="get" action="${pageContext.request.contextPath}/hr/jobs">
                            <div class="col-lg-4"><label class="form-label small text-muted">Từ khóa</label><input class="form-control" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="Mã hoặc tiêu đề công việc"></div>
                            <div class="col-lg-3"><label class="form-label small text-muted">Phòng ban</label><select class="form-select" name="department"><option value="">Tất cả phòng ban</option><c:forEach var="department" items="${departments}"><option value="${department.id}" <c:if test="${param.department eq department.id}">selected</c:if>><c:out value="${department.displayName}" /></option></c:forEach></select></div>
                            <div class="col-lg-3"><label class="form-label small text-muted">Trạng thái</label><select class="form-select" name="status"><option value="">Tất cả trạng thái</option><option value="DRAFT" <c:if test="${param.status eq 'DRAFT'}">selected</c:if>>Bản nháp</option><option value="PUBLISHED" <c:if test="${param.status eq 'PUBLISHED'}">selected</c:if>>Đã đăng</option><option value="CLOSED" <c:if test="${param.status eq 'CLOSED'}">selected</c:if>>Đã đóng</option><option value="ARCHIVED" <c:if test="${param.status eq 'ARCHIVED'}">selected</c:if>>Lưu trữ</option></select></div>
                            <div class="col-lg-2 d-flex gap-2"><button class="btn btn-outline-primary flex-grow-1" type="submit"><i class="bi bi-search"></i> Lọc</button><a class="btn btn-light" href="${pageContext.request.contextPath}/hr/jobs" aria-label="Xóa bộ lọc"><i class="bi bi-arrow-counterclockwise"></i></a></div>
                        </form>
                    </div>
                </section>

                <section class="card">
                    <div class="card-header bg-white border-0 px-4 pt-4 d-flex justify-content-between"><div><h5 class="mb-1">Danh sách vị trí</h5><small class="text-muted">Quản lý vòng đời của từng tin tuyển dụng.</small></div></div>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light"><tr><th class="ps-4">Mã tin</th><th>Vị trí</th><th>Phòng ban</th><th>Hạn nộp</th><th>Trạng thái</th><th class="text-end pe-4">Thao tác</th></tr></thead>
                            <tbody>
                                <c:forEach var="job" items="${jobs}">
                                    <tr>
                                        <td class="ps-4"><span class="badge text-bg-light border"><c:out value="${job.jobCode}" /></span></td>
                                        <td><div class="fw-semibold"><c:out value="${job.title}" /></div><small class="text-muted"><c:out value="${job.location}" /> · <span data-enum-label="${job.employmentType}"><c:out value="${job.employmentType}" /></span></small></td>
                                        <td><c:out value="${job.departmentName}" /></td>
                                        <td><c:out value="${job.deadline}" /></td>
                                        <td><c:choose><c:when test="${job.status eq 'PUBLISHED'}"><span class="badge text-bg-success">Đã đăng</span></c:when><c:when test="${job.status eq 'DRAFT'}"><span class="badge text-bg-warning">Bản nháp</span></c:when><c:when test="${job.status eq 'CLOSED'}"><span class="badge text-bg-danger">Đã đóng</span></c:when><c:otherwise><span class="badge text-bg-secondary">Lưu trữ</span></c:otherwise></c:choose></td>
                                        <td class="text-end pe-4">
                                            <div class="dropdown">
                                                <button class="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" type="button">Thao tác</button>
                                                <ul class="dropdown-menu dropdown-menu-end">
                                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/hr/jobs/edit?id=${job.id}"><i class="bi bi-pencil me-2"></i>Chỉnh sửa</a></li>
                                                    <c:if test="${job.status eq 'DRAFT' or job.status eq 'CLOSED'}"><li><form action="${pageContext.request.contextPath}/hr/jobs/status" method="post"><input type="hidden" name="jobId" value="${job.id}"><input type="hidden" name="status" value="PUBLISHED"><button class="dropdown-item text-success" type="submit"><i class="bi bi-globe2 me-2"></i>Đăng tin</button></form></li></c:if>
                                                    <c:if test="${job.status eq 'PUBLISHED'}"><li><form action="${pageContext.request.contextPath}/hr/jobs/status" method="post"><input type="hidden" name="jobId" value="${job.id}"><input type="hidden" name="status" value="CLOSED"><button class="dropdown-item text-warning" type="submit"><i class="bi bi-pause-circle me-2"></i>Đóng tin</button></form></li></c:if>
                                                    <li><hr class="dropdown-divider"></li>
                                                    <li><form action="${pageContext.request.contextPath}/hr/jobs/status" method="post"><input type="hidden" name="jobId" value="${job.id}"><input type="hidden" name="status" value="ARCHIVED"><button class="dropdown-item text-danger" type="submit"><i class="bi bi-archive me-2"></i>Lưu trữ</button></form></li>
                                                </ul>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty jobs}"><tr><td class="py-5 text-center text-muted" colspan="6"><i class="bi bi-briefcase d-block fs-3 mb-2"></i>Không tìm thấy tin tuyển dụng phù hợp.</td></tr></c:if>
                            </tbody>
                        </table>
                    </div>
                    <c:if test="${not empty page and page.totalPages gt 1}">
                        <nav class="p-3 border-top" aria-label="Phân trang tin tuyển dụng"><ul class="pagination pagination-sm mb-0 justify-content-end"><c:forEach begin="1" end="${page.totalPages}" var="p"><li class="page-item ${p eq page.currentPage ? 'active' : ''}"><a class="page-link" href="${pageContext.request.contextPath}/hr/jobs?page=${p}&keyword=${param.keyword}&department=${param.department}&status=${param.status}">${p}</a></li></c:forEach></ul></nav>
                    </c:if>
                </section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

