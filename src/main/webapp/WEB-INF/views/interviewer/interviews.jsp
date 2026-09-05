<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Lịch phỏng vấn của tôi | JobCV" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/interviewer-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Khu vực người phỏng vấn</p><h4 class="mb-0">Lịch phỏng vấn của tôi</h4></div><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/interviewer/dashboard"><i class="bi bi-grid me-1"></i> Bảng điều khiển</a></div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="successMessage" scope="session" /></c:if>
                <section class="card"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Các buổi phỏng vấn được phân công</h5><small class="text-muted">Bạn chỉ có thể gửi đánh giá cho các buổi phỏng vấn thuộc lịch của mình.</small></div><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th class="ps-4">Ứng viên</th><th>Vị trí</th><th>Ngày &amp; giờ</th><th>Địa điểm / cuộc họp</th><th>Trạng thái</th><th class="text-end pe-4">Thao tác</th></tr></thead><tbody>
                    <c:forEach var="item" items="${interviews}"><tr><td class="ps-4"><div class="fw-semibold"><c:out value="${item.candidateName}" /></div><small class="text-muted">Đơn ứng tuyển #<c:out value="${item.applicationId}" /></small></td><td><c:out value="${item.jobTitle}" /></td><td><div><c:out value="${item.interviewDate}" /></div><small class="text-muted"><c:out value="${item.startTime}" /> – <c:out value="${item.endTime}" /></small></td><td><c:choose><c:when test="${not empty item.meetingUrl}"><span class="text-truncate d-inline-block" style="max-width:190px;"><c:out value="${item.meetingUrl}" /></span></c:when><c:otherwise><c:out value="${item.location}" /></c:otherwise></c:choose></td><td><c:choose><c:when test="${item.status eq 'SCHEDULED'}"><span class="badge text-bg-primary">Đã lên lịch</span></c:when><c:when test="${item.status eq 'COMPLETED'}"><span class="badge text-bg-success">Đã hoàn thành</span></c:when><c:when test="${item.status eq 'RESCHEDULED'}"><span class="badge text-bg-warning">Đổi lịch</span></c:when><c:otherwise><span class="badge text-bg-secondary">Đã hủy</span></c:otherwise></c:choose></td><td class="text-end pe-4"><a class="btn btn-sm ${item.status eq 'SCHEDULED' ? 'btn-primary' : 'btn-outline-primary'}" href="${pageContext.request.contextPath}/interviewer/interviews/detail?id=${item.id}">${item.status eq 'SCHEDULED' ? 'Đánh giá' : 'Chi tiết'}</a></td></tr></c:forEach>
                    <c:if test="${empty interviews}"><tr><td colspan="6" class="py-5 text-center text-muted"><i class="bi bi-calendar-x d-block fs-3 mb-2"></i>Chưa có buổi phỏng vấn được phân công.</td></tr></c:if>
                </tbody></table></div></section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
