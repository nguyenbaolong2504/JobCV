<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Quản lý thư mời nhận việc | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Tuyển dụng</p><h4 class="mb-0">Quản lý thư mời nhận việc</h4></div><a class="btn btn-primary" href="${pageContext.request.contextPath}/hr/offers/create"><i class="bi bi-envelope-plus me-1"></i> Tạo thư mời</a></div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="successMessage" scope="session" /></c:if>
                <c:if test="${not empty sessionScope.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${sessionScope.error}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="error" scope="session" /></c:if>

                <section class="card mb-4"><div class="card-body"><form class="row gy-2 gx-3 align-items-end" method="get" action="${pageContext.request.contextPath}/hr/offers"><div class="col-md-5"><label class="form-label small text-muted">Từ khóa</label><input class="form-control" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="Ứng viên hoặc vị trí"></div><div class="col-md-3"><label class="form-label small text-muted">Trạng thái</label><select class="form-select" name="status"><option value="">Tất cả trạng thái</option><option value="DRAFT">Bản nháp</option><option value="SENT">Đã gửi</option><option value="ACCEPTED">Đã chấp nhận</option><option value="DECLINED">Đã từ chối</option><option value="EXPIRED">Hết hạn</option></select></div><div class="col-md-2"><label class="form-label small text-muted">Hết hạn trước</label><input class="form-control" name="expiryDate" type="date" value="<c:out value='${param.expiryDate}'/>"></div><div class="col-md-2 d-flex gap-2"><button class="btn btn-outline-primary flex-grow-1" type="submit"><i class="bi bi-search"></i> Lọc</button><a class="btn btn-light" href="${pageContext.request.contextPath}/hr/offers"><i class="bi bi-arrow-counterclockwise"></i></a></div></form></div></section>

                <section class="card"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Danh sách thư mời</h5><small class="text-muted">Thư mời chỉ được gửi sau khi ứng viên đã hoàn thành phỏng vấn.</small></div><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th class="ps-4">Ứng viên</th><th>Vị trí</th><th>Mức lương</th><th>Ngày bắt đầu</th><th>Hết hạn</th><th>Trạng thái</th><th class="text-end pe-4">Thao tác</th></tr></thead><tbody>
                    <c:forEach var="item" items="${offers}">
                        <tr><td class="ps-4"><div class="fw-semibold"><c:out value="${item.candidateName}" /></div><small class="text-muted">Đơn ứng tuyển #<c:out value="${item.applicationId}" /></small></td><td><c:out value="${item.jobTitle}" /></td><td><strong><fmt:formatNumber value="${item.salary}" type="number" maxFractionDigits="0" /> ₫</strong></td><td><c:out value="${item.startDate}" /></td><td><c:out value="${item.expiryDate}" /></td><td><c:choose><c:when test="${item.status eq 'DRAFT'}"><span class="badge text-bg-secondary">Bản nháp</span></c:when><c:when test="${item.status eq 'SENT'}"><span class="badge text-bg-primary">Đã gửi</span></c:when><c:when test="${item.status eq 'ACCEPTED'}"><span class="badge text-bg-success">Đã chấp nhận</span></c:when><c:when test="${item.status eq 'DECLINED'}"><span class="badge text-bg-danger">Đã từ chối</span></c:when><c:otherwise><span class="badge text-bg-warning">Hết hạn</span></c:otherwise></c:choose></td>
                        <td class="text-end pe-4"><div class="dropdown"><button class="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" type="button">Thao tác</button><ul class="dropdown-menu dropdown-menu-end"><li><a class="dropdown-item" href="${pageContext.request.contextPath}/hr/offers/edit?id=${item.id}"><i class="bi bi-pencil me-2"></i>Chỉnh sửa</a></li><c:if test="${item.status eq 'DRAFT'}"><li><form action="${pageContext.request.contextPath}/hr/offers/send" method="post"><input type="hidden" name="id" value="${item.id}"><button class="dropdown-item text-primary" type="submit"><i class="bi bi-send me-2"></i>Gửi thư mời</button></form></li></c:if><li><a class="dropdown-item" href="${pageContext.request.contextPath}/hr/applications/detail?id=${item.applicationId}"><i class="bi bi-person me-2"></i>Xem ứng viên</a></li></ul></div></td></tr>
                    </c:forEach>
                    <c:if test="${empty offers}"><tr><td colspan="7" class="py-5 text-center text-muted"><i class="bi bi-envelope d-block fs-3 mb-2"></i>Chưa có thư mời nào.</td></tr></c:if>
                </tbody></table></div></section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

