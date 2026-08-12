<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Chi tiết việc làm | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="py-4 py-lg-5">
    <div class="container">
        <c:choose>
            <c:when test="${not empty job}">
                <nav aria-label="breadcrumb"><ol class="breadcrumb"><li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Trang chủ</a></li><li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/jobs">Việc làm</a></li><li class="breadcrumb-item active" aria-current="page"><c:out value="${job.title}" /></li></ol></nav>
                <div class="row g-4">
                    <div class="col-lg-8">
                        <section class="content-card mb-4">
                            <span class="badge text-bg-primary-subtle text-primary mb-3"><c:out value="${job.departmentName}" /></span>
                            <h1 class="page-title mb-3"><c:out value="${job.title}" /></h1>
                            <div class="d-flex flex-wrap gap-3 text-muted">
                                <span class="meta-item"><i class="bi bi-geo-alt"></i><c:out value="${job.location}" /></span>
                                <span class="meta-item"><i class="bi bi-briefcase"></i><c:out value="${job.employmentType}" /></span>
                                <span class="meta-item"><i class="bi bi-people"></i><c:out value="${job.numberOfPositions}" /> vị trí</span>
                                <span class="meta-item"><i class="bi bi-calendar3"></i>Hạn nộp: <c:out value="${job.deadline}" /></span>
                            </div>
                        </section>

                        <section class="content-card mb-4">
                            <h2 class="h4 fw-bold mb-3">Mô tả công việc</h2>
                            <div class="text-secondary" style="white-space: pre-line;"><c:out value="${job.description}" /></div>
                        </section>
                        <section class="content-card mb-4">
                            <h2 class="h4 fw-bold mb-3">Yêu cầu ứng viên</h2>
                            <div class="text-secondary" style="white-space: pre-line;"><c:out value="${job.requirements}" /></div>
                        </section>
                        <section class="content-card">
                            <h2 class="h4 fw-bold mb-3">Kỹ năng mong muốn</h2>
                            <div class="d-flex flex-wrap gap-2">
                                <c:forEach var="skill" items="${skills}"><span class="badge rounded-pill text-bg-light border text-dark"><i class="bi bi-check2-circle text-primary me-1"></i><c:out value="${skill.skillName}" /><c:if test="${skill.required}"><span class="text-danger ms-1">*</span></c:if></span></c:forEach>
                                <c:if test="${empty skills}"><span class="text-muted">Kỹ năng chi tiết sẽ được trao đổi trong quá trình tuyển dụng.</span></c:if>
                            </div>
                        </section>
                    </div>
                    <aside class="col-lg-4">
                        <div class="content-card position-sticky" style="top: 5.5rem;">
                            <h2 class="h5 fw-bold">Thông tin công việc</h2>
                            <dl class="row small mb-4 mt-3">
                                <dt class="col-5 text-muted fw-normal">Mã công việc</dt><dd class="col-7"><c:out value="${job.jobCode}" /></dd>
                                <dt class="col-5 text-muted fw-normal">Mức lương</dt><dd class="col-7 text-success fw-semibold"><fmt:formatNumber value="${job.salaryMin}" type="number" /> – <fmt:formatNumber value="${job.salaryMax}" type="number" /> VNĐ</dd>
                                <dt class="col-5 text-muted fw-normal">Kinh nghiệm</dt><dd class="col-7"><c:out value="${job.experienceRequired}" /> năm</dd>
                                <dt class="col-5 text-muted fw-normal">Hạn nộp</dt><dd class="col-7"><c:out value="${job.deadline}" /></dd>
                            </dl>
                            <c:choose>
                                <c:when test="${sessionScope.role eq 'CANDIDATE'}">
                                    <form action="${pageContext.request.contextPath}/candidate/applications/apply" method="post" data-confirm="Bạn muốn ứng tuyển vị trí này bằng CV mặc định?">
                                        <input type="hidden" name="jobId" value="<c:out value='${job.id}'/>">
                                        <button class="btn btn-primary w-100" type="submit" data-loading-button><i class="bi bi-send me-1"></i>Ứng tuyển ngay</button>
                                    </form>
                                </c:when>
                                <c:when test="${empty sessionScope.userId}"><a class="btn btn-primary w-100" href="${pageContext.request.contextPath}/login"><i class="bi bi-box-arrow-in-right me-1"></i>Đăng nhập để ứng tuyển</a></c:when>
                                <c:otherwise><a class="btn btn-outline-primary w-100" href="${pageContext.request.contextPath}/candidate/jobs">Xem trong cổng ứng viên</a></c:otherwise>
                            </c:choose>
                            <a class="btn btn-link w-100 mt-2" href="${pageContext.request.contextPath}/jobs"><i class="bi bi-arrow-left me-1"></i>Quay lại danh sách</a>
                        </div>
                    </aside>
                </div>
            </c:when>
            <c:otherwise>
                <div class="rf-card empty-state"><div class="empty-icon"><i class="bi bi-briefcase"></i></div><h1 class="h4">Không tìm thấy tin tuyển dụng</h1><p class="mb-3">Tin tuyển dụng có thể đã được đóng hoặc không còn tồn tại.</p><a class="btn btn-primary" href="${pageContext.request.contextPath}/jobs">Xem các vị trí khác</a></div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
