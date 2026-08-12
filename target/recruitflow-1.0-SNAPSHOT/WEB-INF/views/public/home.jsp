<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="RecruitFlow | Tìm đúng công việc, xây đúng sự nghiệp" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main>
    <section class="public-hero">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-9 text-center">
                    <span class="badge rounded-pill text-bg-light text-primary px-3 py-2 mb-3">Cơ hội nghề nghiệp tại RecruitFlow</span>
                    <h1>Khởi đầu hành trình sự nghiệp phù hợp với bạn.</h1>
                    <p class="lead text-white-50 mt-3 mb-4">Khám phá các vị trí đang tuyển, nộp CV và theo dõi trọn vẹn quá trình ứng tuyển tại một nơi.</p>
                </div>
            </div>
            <form class="hero-search-card mx-auto" style="max-width: 980px;" action="${pageContext.request.contextPath}/jobs" method="get">
                <div class="row g-2 align-items-center">
                    <div class="col-lg-5">
                        <label class="visually-hidden" for="homeKeyword">Từ khóa công việc</label>
                        <div class="input-group">
                            <span class="input-group-text bg-white border-end-0"><i class="bi bi-search"></i></span>
                            <input class="form-control border-start-0" id="homeKeyword" type="search" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="Vị trí, kỹ năng hoặc từ khóa">
                        </div>
                    </div>
                    <div class="col-lg-3">
                        <label class="visually-hidden" for="homeDepartment">Phòng ban</label>
                        <select class="form-select" id="homeDepartment" name="departmentId">
                            <option value="">Tất cả phòng ban</option>
                            <c:forEach var="department" items="${departments}">
                                <option value="<c:out value='${department.id}'/>" ${param.departmentId eq department.id ? 'selected' : ''}><c:out value="${department.name}" /></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-lg-2">
                        <label class="visually-hidden" for="homeLocation">Địa điểm</label>
                        <input class="form-control" id="homeLocation" type="text" name="location" value="<c:out value='${param.location}'/>" placeholder="Địa điểm">
                    </div>
                    <div class="col-lg-2 d-grid">
                        <button class="btn btn-primary" type="submit"><i class="bi bi-search me-1"></i>Tìm việc</button>
                    </div>
                </div>
            </form>
        </div>
    </section>

    <section class="py-5">
        <div class="container">
            <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
                <div>
                    <p class="text-primary fw-semibold mb-1">VỊ TRÍ NỔI BẬT</p>
                    <h2 class="section-heading mb-0">Cơ hội mới nhất dành cho bạn</h2>
                </div>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/jobs">Xem tất cả việc làm <i class="bi bi-arrow-right ms-1"></i></a>
            </div>
            <div class="row g-4">
                <c:forEach var="job" items="${featuredJobs}">
                    <c:url var="jobDetailUrl" value="/jobs/detail">
                        <c:param name="id" value="${job.id}" />
                    </c:url>
                    <div class="col-md-6 col-xl-4">
                        <article class="job-card">
                            <div class="d-flex justify-content-between align-items-start gap-2 mb-3">
                                <span class="badge text-bg-primary-subtle text-primary"><c:out value="${job.departmentName}" /></span>
                                <span class="text-muted small"><i class="bi bi-clock me-1"></i>Hạn: <c:out value="${job.deadline}" /></span>
                            </div>
                            <a class="job-card-title d-block mb-3" href="${jobDetailUrl}"><c:out value="${job.title}" /></a>
                            <div class="d-flex flex-wrap gap-3 mb-3">
                                <span class="meta-item"><i class="bi bi-geo-alt"></i><c:out value="${job.location}" /></span>
                                <span class="meta-item"><i class="bi bi-briefcase"></i><c:out value="${job.employmentType}" /></span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center pt-2 border-top">
                                <span class="fw-semibold text-success"><fmt:formatNumber value="${job.salaryMin}" type="number" /> – <fmt:formatNumber value="${job.salaryMax}" type="number" /> VNĐ</span>
                                <a class="small fw-semibold text-decoration-none" href="${jobDetailUrl}">Xem chi tiết <i class="bi bi-arrow-right"></i></a>
                            </div>
                        </article>
                    </div>
                </c:forEach>
                <c:if test="${empty featuredJobs}">
                    <div class="col-12">
                        <div class="rf-card empty-state"><div class="empty-icon"><i class="bi bi-briefcase"></i></div><h5>Chưa có việc làm nổi bật</h5><p class="mb-0">Các cơ hội mới sẽ được cập nhật tại đây.</p></div>
                    </div>
                </c:if>
            </div>
        </div>
    </section>

    <section class="py-5 bg-white border-top border-bottom">
        <div class="container">
            <div class="text-center mb-4">
                <p class="text-primary fw-semibold mb-1">KHÁM PHÁ THEO PHÒNG BAN</p>
                <h2 class="section-heading">Tìm công việc phù hợp chuyên môn của bạn</h2>
            </div>
            <div class="row g-3 justify-content-center">
                <c:forEach var="department" items="${departments}">
                    <c:url var="departmentJobsUrl" value="/jobs"><c:param name="departmentId" value="${department.id}" /></c:url>
                    <div class="col-sm-6 col-lg-4 col-xl-3">
                        <a class="department-tile" href="${departmentJobsUrl}"><i class="bi bi-building fs-4"></i><span><c:out value="${department.name}" /></span><i class="bi bi-arrow-right ms-auto"></i></a>
                    </div>
                </c:forEach>
            </div>
        </div>
    </section>

    <section class="py-5">
        <div class="container">
            <div class="rf-card p-4 p-lg-5 text-center" style="background: linear-gradient(135deg, #eff6ff, #f5f3ff);">
                <h2 class="section-heading">Sẵn sàng cho cơ hội tiếp theo?</h2>
                <p class="text-muted mb-4">Tạo hồ sơ, tải CV và nhận thông báo về các vị trí phù hợp với năng lực của bạn.</p>
                <c:choose>
                    <c:when test="${sessionScope.role eq 'CANDIDATE'}"><a class="btn btn-primary px-4" href="${pageContext.request.contextPath}/candidate/jobs">Khám phá việc làm</a></c:when>
                    <c:otherwise><a class="btn btn-primary px-4" href="${pageContext.request.contextPath}/register">Tạo tài khoản ứng viên</a></c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
