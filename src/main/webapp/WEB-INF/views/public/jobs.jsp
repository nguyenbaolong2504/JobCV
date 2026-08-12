<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Việc làm đang tuyển | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="py-4 py-lg-5">
    <div class="container">
        <div class="mb-4">
            <nav aria-label="breadcrumb"><ol class="breadcrumb mb-2"><li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Trang chủ</a></li><li class="breadcrumb-item active" aria-current="page">Việc làm</li></ol></nav>
            <h1 class="page-title mb-1">Tìm cơ hội phù hợp với bạn</h1>
            <p class="text-muted mb-0">Lọc theo nhu cầu và bắt đầu hành trình ứng tuyển ngay hôm nay.</p>
        </div>

        <form class="filter-card mb-4" action="${pageContext.request.contextPath}/jobs" method="get">
            <div class="row g-3 align-items-end">
                <div class="col-lg-4">
                    <label class="form-label fw-semibold" for="keyword">Từ khóa</label>
                    <div class="input-group"><span class="input-group-text bg-white"><i class="bi bi-search"></i></span><input class="form-control" id="keyword" name="keyword" type="search" value="<c:out value='${param.keyword}'/>" placeholder="Tên vị trí hoặc kỹ năng"></div>
                </div>
                <div class="col-sm-6 col-lg-2">
                    <label class="form-label fw-semibold" for="departmentId">Phòng ban</label>
                    <select class="form-select" id="departmentId" name="departmentId">
                        <option value="">Tất cả</option>
                        <c:forEach var="department" items="${departments}"><option value="<c:out value='${department.id}'/>" ${param.departmentId eq department.id ? 'selected' : ''}><c:out value="${department.name}" /></option></c:forEach>
                    </select>
                </div>
                <div class="col-sm-6 col-lg-2">
                    <label class="form-label fw-semibold" for="location">Địa điểm</label>
                    <input class="form-control" id="location" name="location" type="text" value="<c:out value='${param.location}'/>" placeholder="Hà Nội, Remote...">
                </div>
                <div class="col-sm-6 col-lg-2">
                    <label class="form-label fw-semibold" for="employmentType">Loại hình</label>
                    <select class="form-select" id="employmentType" name="employmentType">
                        <option value="">Tất cả</option>
                        <option value="FULL_TIME" ${param.employmentType eq 'FULL_TIME' ? 'selected' : ''}>Toàn thời gian</option>
                        <option value="PART_TIME" ${param.employmentType eq 'PART_TIME' ? 'selected' : ''}>Bán thời gian</option>
                        <option value="INTERNSHIP" ${param.employmentType eq 'INTERNSHIP' ? 'selected' : ''}>Thực tập</option>
                        <option value="CONTRACT" ${param.employmentType eq 'CONTRACT' ? 'selected' : ''}>Hợp đồng</option>
                        <option value="REMOTE" ${param.employmentType eq 'REMOTE' ? 'selected' : ''}>Remote</option>
                    </select>
                </div>
                <div class="col-sm-6 col-lg-2 d-grid">
                    <button class="btn btn-primary" type="submit">Áp dụng bộ lọc</button>
                </div>
            </div>
        </form>

        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h5 mb-0">Danh sách việc làm</h2>
            <form action="${pageContext.request.contextPath}/jobs" method="get" class="d-flex gap-2 align-items-center">
                <input type="hidden" name="keyword" value="<c:out value='${param.keyword}'/>">
                <input type="hidden" name="departmentId" value="<c:out value='${param.departmentId}'/>">
                <input type="hidden" name="location" value="<c:out value='${param.location}'/>">
                <input type="hidden" name="employmentType" value="<c:out value='${param.employmentType}'/>">
                <label class="small text-muted" for="sort">Sắp xếp</label>
                <select class="form-select form-select-sm" id="sort" name="sort" onchange="this.form.submit()">
                    <option value="newest" ${empty param.sort or param.sort eq 'newest' ? 'selected' : ''}>Mới nhất</option>
                    <option value="deadline" ${param.sort eq 'deadline' ? 'selected' : ''}>Hạn nộp gần nhất</option>
                    <option value="salary" ${param.sort eq 'salary' ? 'selected' : ''}>Mức lương</option>
                </select>
            </form>
        </div>

        <div class="row g-4">
            <c:forEach var="job" items="${page.items}">
                <c:url var="jobDetailUrl" value="/jobs/detail"><c:param name="id" value="${job.id}" /></c:url>
                <div class="col-md-6 col-xl-4">
                    <article class="job-card">
                        <div class="d-flex justify-content-between gap-2 mb-3"><span class="badge text-bg-primary-subtle text-primary"><c:out value="${job.departmentName}" /></span><span class="small text-muted"><i class="bi bi-calendar3 me-1"></i><c:out value="${job.deadline}" /></span></div>
                        <a class="job-card-title d-block mb-3" href="${jobDetailUrl}"><c:out value="${job.title}" /></a>
                        <div class="d-flex flex-column gap-2 mb-3"><span class="meta-item"><i class="bi bi-geo-alt"></i><c:out value="${job.location}" /></span><span class="meta-item"><i class="bi bi-briefcase"></i><c:out value="${job.employmentType}" /></span><span class="meta-item"><i class="bi bi-person-workspace"></i>Tối thiểu <c:out value="${job.experienceRequired}" /> năm kinh nghiệm</span></div>
                        <div class="pt-3 border-top d-flex justify-content-between align-items-center"><span class="fw-semibold text-success"><fmt:formatNumber value="${job.salaryMin}" type="number" /> – <fmt:formatNumber value="${job.salaryMax}" type="number" /> VNĐ</span><a href="${jobDetailUrl}" class="btn btn-sm btn-outline-primary">Chi tiết</a></div>
                    </article>
                </div>
            </c:forEach>
            <c:if test="${empty page.items}">
                <div class="col-12"><div class="rf-card empty-state"><div class="empty-icon"><i class="bi bi-search"></i></div><h5>Không tìm thấy công việc phù hợp</h5><p class="mb-3">Hãy thử đổi từ khóa hoặc nới rộng bộ lọc tìm kiếm.</p><a href="${pageContext.request.contextPath}/jobs" class="btn btn-outline-primary">Xóa bộ lọc</a></div></div>
            </c:if>
        </div>
        <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
