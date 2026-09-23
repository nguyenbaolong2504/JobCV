<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Thông báo việc làm | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell"><div class="row g-0">
    <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />
    <main class="candidate-main col-lg-9 col-xl-10 job-alerts-page">
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <section class="job-alert-hero mb-4">
            <div><span class="profile-heading-kicker"><i class="bi bi-bell-fill"></i>Cơ hội không bị bỏ lỡ</span><h1 class="page-title mb-1">Thông báo việc làm</h1><p class="mb-0">Lưu bộ lọc yêu thích và quay lại xem ngay khi có vị trí phù hợp.</p></div>
            <span class="job-alert-hero-icon"><i class="bi bi-radar"></i></span>
        </section>

        <div class="row g-4">
            <div class="col-xl-5">
                <section class="candidate-panel job-alert-builder">
                    <header class="candidate-panel-header"><div><span>TẠO BỘ LỌC MỚI</span><h2>Nhận đúng việc bạn cần</h2><p>Tạo tối đa 10 thông báo, mỗi thông báo cần ít nhất một tiêu chí.</p></div></header>
                    <form action="${pageContext.request.contextPath}/candidate/job-alerts/create" method="post">
                        <div class="mb-3"><label class="form-label fw-semibold" for="alertName">Tên thông báo *</label><input class="form-control" id="alertName" name="name" maxlength="80" required value="<c:out value='${param.name}'/>" placeholder="Ví dụ: Java tại Hà Nội"></div>
                        <div class="mb-3"><label class="form-label fw-semibold" for="alertKeyword">Từ khóa</label><input class="form-control" id="alertKeyword" name="keyword" maxlength="150" value="<c:out value='${param.keyword}'/>" placeholder="Java, Marketing, Kế toán..."></div>
                        <div class="row g-3 mb-3"><div class="col-md-6"><label class="form-label fw-semibold" for="alertDepartment">Phòng ban</label><select class="form-select" id="alertDepartment" name="departmentId"><option value="">Tất cả</option><c:forEach var="department" items="${departments}"><option value="<c:out value='${department.id}'/>" ${param.departmentId eq department.id ? 'selected' : ''}><c:out value="${department.displayName}" /></option></c:forEach></select></div><div class="col-md-6"><label class="form-label fw-semibold" for="alertType">Loại hình</label><select class="form-select" id="alertType" name="employmentType"><option value="">Tất cả</option><option value="FULL_TIME" ${param.employmentType eq 'FULL_TIME' ? 'selected' : ''}>Toàn thời gian</option><option value="PART_TIME" ${param.employmentType eq 'PART_TIME' ? 'selected' : ''}>Bán thời gian</option><option value="INTERNSHIP" ${param.employmentType eq 'INTERNSHIP' ? 'selected' : ''}>Thực tập</option><option value="CONTRACT" ${param.employmentType eq 'CONTRACT' ? 'selected' : ''}>Hợp đồng</option><option value="REMOTE" ${param.employmentType eq 'REMOTE' ? 'selected' : ''}>Làm từ xa</option></select></div></div>
                        <div class="row g-3 mb-4"><div class="col-md-7"><label class="form-label fw-semibold" for="alertLocation">Địa điểm</label><input class="form-control" id="alertLocation" name="location" maxlength="100" value="<c:out value='${param.location}'/>" placeholder="Hà Nội, Đà Nẵng..."></div><div class="col-md-5"><label class="form-label fw-semibold" for="alertFrequency">Tần suất</label><select class="form-select" id="alertFrequency" name="frequency"><option value="DAILY">Hàng ngày</option><option value="WEEKLY">Hàng tuần</option></select></div></div>
                        <button class="btn btn-primary w-100" type="submit" data-loading-button><i class="bi bi-bell-plus me-1"></i>Tạo thông báo việc làm</button>
                    </form>
                </section>
            </div>
            <div class="col-xl-7">
                <section class="candidate-panel h-100">
                    <header class="candidate-panel-header"><div><span>DANH SÁCH CỦA BẠN</span><h2><c:out value="${alerts.size()}" /> thông báo việc làm</h2><p>Bật, tạm dừng hoặc xóa bất cứ lúc nào.</p></div></header>
                    <div class="job-alert-list">
                        <c:forEach var="alert" items="${alerts}">
                            <c:url var="alertJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="${alert.keyword}"/><c:param name="departmentId" value="${alert.departmentId}"/><c:param name="location" value="${alert.location}"/><c:param name="employmentType" value="${alert.employmentType}"/></c:url>
                            <article class="job-alert-card ${alert.active ? '' : 'is-paused'}">
                                <div class="job-alert-card-icon"><i class="bi ${alert.active ? 'bi-bell-fill' : 'bi-bell-slash'}"></i></div>
                                <div class="job-alert-card-body"><div class="d-flex flex-wrap align-items-center gap-2"><h3><c:out value="${alert.name}" /></h3><span class="job-alert-state">${alert.active ? 'Đang bật' : 'Tạm dừng'}</span></div><div class="job-alert-criteria"><c:if test="${not empty alert.keyword}"><span><i class="bi bi-search"></i><c:out value="${alert.keyword}" /></span></c:if><c:if test="${not empty alert.departmentName}"><span><i class="bi bi-building"></i><c:out value="${alert.departmentName}" /></span></c:if><c:if test="${not empty alert.location}"><span><i class="bi bi-geo-alt"></i><c:out value="${alert.location}" /></span></c:if><c:if test="${not empty alert.employmentType}"><span><i class="bi bi-briefcase"></i><span data-enum-label="${alert.employmentType}"><c:out value="${alert.employmentType}" /></span></span></c:if></div><small>${alert.frequency eq 'DAILY' ? 'Cập nhật hàng ngày' : 'Cập nhật hàng tuần'} · Tạo ngày <fmt:formatDate value="${alert.createdAt}" pattern="dd/MM/yyyy" /></small></div>
                                <div class="job-alert-card-actions"><a class="job-alert-match" href="${alertJobsUrl}"><strong><c:out value="${alert.matchingJobs}" /></strong><span>việc phù hợp</span></a><div class="d-flex gap-2"><form action="${pageContext.request.contextPath}/candidate/job-alerts/toggle" method="post"><input type="hidden" name="alertId" value="<c:out value='${alert.id}'/>"><input type="hidden" name="active" value="${!alert.active}"><button class="btn btn-sm btn-outline-primary" type="submit" title="${alert.active ? 'Tạm dừng' : 'Bật thông báo'}" aria-label="${alert.active ? 'Tạm dừng thông báo' : 'Bật thông báo'}"><i class="bi ${alert.active ? 'bi-pause-fill' : 'bi-play-fill'}"></i></button></form><form action="${pageContext.request.contextPath}/candidate/job-alerts/delete" method="post" data-confirm="Bạn chắc chắn muốn xóa thông báo việc làm này?"><input type="hidden" name="alertId" value="<c:out value='${alert.id}'/>"><button class="btn btn-sm btn-outline-danger" type="submit" title="Xóa thông báo" aria-label="Xóa thông báo"><i class="bi bi-trash"></i></button></form></div></div>
                            </article>
                        </c:forEach>
                        <c:if test="${empty alerts}"><div class="candidate-mini-empty job-alert-empty"><i class="bi bi-bell-plus"></i><strong>Chưa có thông báo việc làm</strong><small>Tạo bộ lọc đầu tiên để JobCV ghi nhớ công việc bạn đang tìm.</small></div></c:if>
                    </div>
                </section>
            </div>
        </div>
    </main>
</div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
