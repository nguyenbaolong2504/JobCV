<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Việc làm đã lưu | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell"><div class="row g-0">
    <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />
    <main class="candidate-main col-lg-9 col-xl-10 saved-jobs-page">
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <section class="saved-jobs-heading mb-4">
            <div>
                <span class="profile-heading-kicker"><i class="bi bi-bookmark-heart-fill"></i>Bộ sưu tập cơ hội</span>
                <h1 class="page-title mb-1">Việc làm đã lưu</h1>
                <p class="text-muted mb-0">Giữ lại những vị trí bạn quan tâm để so sánh và ứng tuyển khi sẵn sàng.</p>
            </div>
            <div class="saved-jobs-total"><strong><c:out value="${page.totalItems}" /></strong><span>vị trí đang mở</span></div>
        </section>

        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
            <p class="mb-0 text-muted">Danh sách được sắp xếp theo thời gian lưu gần nhất.</p>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/jobs"><i class="bi bi-search me-1"></i>Khám phá thêm việc làm</a>
        </div>

        <div class="row g-4">
            <c:forEach var="job" items="${page.items}">
                <c:url var="savedJobDetailUrl" value="/jobs/detail"><c:param name="id" value="${job.id}" /></c:url>
                <div class="col-md-6 col-xxl-4">
                    <article class="job-card saved-job-card h-100">
                        <div class="d-flex justify-content-between align-items-start gap-2">
                            <div class="saved-job-company"><span class="job-company-logo"><img src="${pageContext.request.contextPath}/company-logo?id=${job.companyId}" alt="Logo ${job.companyName}" loading="lazy"></span><div><strong><c:out value="${job.companyName}" /></strong><small><c:out value="${job.departmentName}" /></small></div></div>
                            <form action="${pageContext.request.contextPath}/candidate/saved-jobs/remove" method="post">
                                <input type="hidden" name="jobId" value="<c:out value='${job.id}'/>">
                                <input type="hidden" name="returnTo" value="/candidate/saved-jobs">
                                <button class="btn btn-sm btn-primary saved-job-button" type="submit" title="Bỏ lưu việc làm" aria-label="Bỏ lưu việc làm"><i class="bi bi-bookmark-heart-fill"></i></button>
                            </form>
                        </div>
                        <a class="job-card-title d-block mt-3 mb-2" href="${savedJobDetailUrl}"><c:out value="${job.title}" /></a>
                        <span class="small text-muted fw-semibold"><c:out value="${job.jobCode}" /></span>
                        <div class="d-flex flex-column gap-2 mt-3">
                            <span class="meta-item"><i class="bi bi-geo-alt"></i><c:out value="${job.location}" /></span>
                            <span class="meta-item"><i class="bi bi-briefcase"></i><span data-enum-label="${job.employmentType}"><c:out value="${job.employmentType}" /></span></span>
                            <span class="meta-item"><i class="bi bi-calendar3"></i>Hạn: <c:out value="${job.deadline}" /></span>
                        </div>
                        <div class="saved-job-card-footer pt-3 mt-3 border-top">
                            <span class="fw-semibold text-success"><fmt:formatNumber value="${job.salaryMin}" type="number" /> – <fmt:formatNumber value="${job.salaryMax}" type="number" /> VNĐ</span>
                            <div class="d-flex gap-2 mt-3">
                                <a class="btn btn-sm btn-outline-primary flex-grow-1" href="${savedJobDetailUrl}">Xem chi tiết</a>
                                <form class="flex-grow-1" action="${pageContext.request.contextPath}/candidate/applications/apply" method="post" data-confirm="Bạn muốn ứng tuyển vị trí này bằng CV mặc định?">
                                    <input type="hidden" name="jobId" value="<c:out value='${job.id}'/>">
                                    <button class="btn btn-sm btn-primary w-100" type="submit" data-loading-button>Ứng tuyển</button>
                                </form>
                            </div>
                        </div>
                    </article>
                </div>
            </c:forEach>
            <c:if test="${empty page.items}">
                <div class="col-12"><div class="candidate-panel empty-state saved-jobs-empty"><div class="empty-icon"><i class="bi bi-bookmark-heart"></i></div><h2 class="h4">Chưa có việc làm nào được lưu</h2><p class="mb-3">Khi gặp một cơ hội phù hợp, hãy bấm biểu tượng lưu để quay lại xem sau.</p><a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/jobs">Tìm việc ngay</a></div></div>
            </c:if>
        </div>
        <c:set var="paginationPath" value="/candidate/saved-jobs" scope="request" /><jsp:include page="/WEB-INF/views/common/pagination.jsp" />
    </main>
</div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
