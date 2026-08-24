<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Interviewer Dashboard | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/interviewer-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Khu vực interviewer</p><h4 class="mb-0">Dashboard</h4></div><span class="fw-semibold"><c:out value="${sessionScope.fullName}" /></span></div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <div class="row g-3 mb-4">
                    <div class="col-md-6"><div class="card p-4 h-100"><div class="d-flex justify-content-between align-items-center"><div><p class="text-muted mb-1">Lịch phỏng vấn sắp tới</p><h2 class="mb-0"><c:out value="${fn:length(interviews)}" /></h2></div><span class="icon-shape bg-primary-subtle text-primary"><i class="bi bi-calendar-week fs-4"></i></span></div></div></div>
                    <div class="col-md-6"><div class="card p-4 h-100"><div class="d-flex justify-content-between align-items-center"><div><p class="text-muted mb-1">Việc cần thực hiện</p><h2 class="mb-0"><c:out value="${fn:length(interviews)}" /></h2><small class="text-muted">Xem lịch và hoàn thiện feedback đúng hạn.</small></div><span class="icon-shape bg-warning-subtle text-warning"><i class="bi bi-clipboard-check fs-4"></i></span></div></div></div>
                </div>
                <section class="card">
                    <div class="card-header bg-white border-0 px-4 pt-4 d-flex justify-content-between align-items-center"><div><h5 class="mb-1">Lịch phỏng vấn sắp tới</h5><small class="text-muted">Chuẩn bị hồ sơ ứng viên trước thời gian hẹn.</small></div><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/interviewer/interviews">Xem tất cả</a></div>
                    <div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th class="ps-4">Ứng viên</th><th>Vị trí</th><th>Thời gian</th><th>Hình thức</th><th class="text-end pe-4">Thao tác</th></tr></thead><tbody>
                        <c:forEach var="item" items="${interviews}" end="5"><tr><td class="ps-4"><div class="fw-semibold"><c:out value="${item.candidateName}" /></div><small class="text-muted">#<c:out value="${item.applicationId}" /></small></td><td><c:out value="${item.jobTitle}" /></td><td><div><c:out value="${item.interviewDate}" /></div><small class="text-muted"><c:out value="${item.startTime}" /> – <c:out value="${item.endTime}" /></small></td><td><c:out value="${item.interviewType}" /></td><td class="text-end pe-4"><a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/interviewer/interviews/detail?id=${item.id}">Mở hồ sơ</a></td></tr></c:forEach>
                        <c:if test="${empty interviews}"><tr><td colspan="5" class="py-5 text-center text-muted"><i class="bi bi-calendar-x d-block fs-3 mb-2"></i>Bạn chưa có lịch phỏng vấn sắp tới.</td></tr></c:if>
                    </tbody></table></div>
                </section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

