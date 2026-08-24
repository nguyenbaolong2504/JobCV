<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Việc làm đang tuyển | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="py-4 py-lg-5">
    <div class="container">
        <div class="mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-2">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Trang chủ</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Việc làm</li>
                </ol>
            </nav>
            <div class="d-flex flex-wrap align-items-end justify-content-between gap-3">
                <div>
                    <p class="eyebrow mb-2"><i class="bi bi-briefcase-fill me-1"></i>Cơ hội đang mở</p>
                    <h1 class="page-title mb-1">Tìm công việc phù hợp với bạn</h1>
                    <p class="text-muted mb-0">Tìm theo kỹ năng, vị trí, lương, kinh nghiệm và thời hạn ứng tuyển.</p>
                </div>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/register">
                    <i class="bi bi-person-plus me-1"></i>Tạo hồ sơ ứng tuyển
                </a>
            </div>
        </div>

        <div class="row g-4 job-discovery-layout">
            <aside class="col-lg-4 col-xl-3" aria-label="Bộ lọc việc làm">
                <form class="filter-card job-filter-sidebar" action="${pageContext.request.contextPath}/jobs" method="get">
                    <div class="d-flex align-items-start justify-content-between gap-2 mb-3">
                        <div>
                            <h2 class="h5 fw-bold mb-1"><i class="bi bi-sliders2-vertical me-2 text-primary"></i>Bộ lọc tìm việc</h2>
                            <p class="small text-muted mb-0">Chọn một hoặc nhiều điều kiện.</p>
                        </div>
                        <a class="btn btn-sm btn-link p-0" href="${pageContext.request.contextPath}/jobs">Xóa lọc</a>
                    </div>

                    <div class="job-filter-section pt-0">
                        <label class="form-label fw-semibold" for="keyword">Từ khóa / kỹ năng</label>
                        <div class="input-group">
                            <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
                            <input class="form-control" id="keyword" name="keyword" type="search" maxlength="150" value="<c:out value='${param.keyword}'/>" placeholder="Java, SQL, mã tin...">
                        </div>
                        <div class="form-text">Tìm trong kỹ năng, mô tả, yêu cầu và mã tin.</div>
                    </div>

                    <div class="job-filter-section">
                        <label class="form-label fw-semibold" for="title">Vị trí / chức danh</label>
                        <input class="form-control" id="title" name="title" type="search" maxlength="150" value="<c:out value='${param.title}'/>" placeholder="Ví dụ: Backend Developer">
                    </div>

                    <div class="job-filter-section">
                        <label class="form-label fw-semibold" for="departmentId">Phòng ban</label>
                        <select class="form-select" id="departmentId" name="departmentId">
                            <option value="">Tất cả phòng ban</option>
                            <c:forEach var="department" items="${departments}">
                                <option value="<c:out value='${department.id}'/>" ${param.departmentId eq department.id ? 'selected' : ''}><c:out value="${department.name}" /></option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="job-filter-section">
                        <label class="form-label fw-semibold" for="categoryId">Danh mục nghề nghiệp</label>
                        <select class="form-select" id="categoryId" name="categoryId">
                            <option value="">Tất cả danh mục</option>
                            <c:forEach var="category" items="${jobCategories}">
                                <option value="<c:out value='${category.id}'/>" ${param.categoryId eq category.id ? 'selected' : ''}>
                                    <c:out value="${empty category.parentName ? category.name : category.parentName}" /><c:if test="${not empty category.parentName}"> › <c:out value="${category.name}" /></c:if>
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="job-filter-section">
                        <label class="form-label fw-semibold" for="location">Địa điểm</label>
                        <input class="form-control" id="location" name="location" maxlength="100" value="<c:out value='${param.location}'/>" placeholder="Hà Nội, Đà Nẵng, Remote...">
                    </div>

                    <div class="job-filter-section">
                        <label class="form-label fw-semibold" for="employmentType">Hình thức làm việc</label>
                        <select class="form-select" id="employmentType" name="employmentType">
                            <option value="">Tất cả hình thức</option>
                            <option value="FULL_TIME" ${param.employmentType eq 'FULL_TIME' ? 'selected' : ''}>Toàn thời gian</option>
                            <option value="PART_TIME" ${param.employmentType eq 'PART_TIME' ? 'selected' : ''}>Bán thời gian</option>
                            <option value="INTERNSHIP" ${param.employmentType eq 'INTERNSHIP' ? 'selected' : ''}>Thực tập</option>
                            <option value="CONTRACT" ${param.employmentType eq 'CONTRACT' ? 'selected' : ''}>Hợp đồng</option>
                            <option value="REMOTE" ${param.employmentType eq 'REMOTE' ? 'selected' : ''}>Từ xa / Remote</option>
                        </select>
                    </div>

                    <fieldset class="job-filter-section">
                        <legend class="form-label fw-semibold mb-2">Khoảng lương mong muốn (VNĐ)</legend>
                        <div class="row g-2">
                            <div class="col-6"><label class="visually-hidden" for="salaryMin">Lương từ</label><input class="form-control" id="salaryMin" name="salaryMin" type="number" min="0" step="500000" inputmode="decimal" value="<c:out value='${param.salaryMin}'/>" placeholder="Từ"></div>
                            <div class="col-6"><label class="visually-hidden" for="salaryMax">Lương đến</label><input class="form-control" id="salaryMax" name="salaryMax" type="number" min="0" step="500000" inputmode="decimal" value="<c:out value='${param.salaryMax}'/>" placeholder="Đến"></div>
                        </div>
                        <div class="form-text">Hiển thị các tin có khoảng lương giao với mức bạn chọn.</div>
                    </fieldset>

                    <fieldset class="job-filter-section">
                        <legend class="form-label fw-semibold mb-2">Kinh nghiệm yêu cầu (năm)</legend>
                        <div class="row g-2">
                            <div class="col-6"><label class="visually-hidden" for="experienceMin">Kinh nghiệm từ</label><input class="form-control" id="experienceMin" name="experienceMin" type="number" min="0" max="100" step="1" inputmode="numeric" value="<c:out value='${param.experienceMin}'/>" placeholder="Từ"></div>
                            <div class="col-6"><label class="visually-hidden" for="experienceMax">Kinh nghiệm đến</label><input class="form-control" id="experienceMax" name="experienceMax" type="number" min="0" max="100" step="1" inputmode="numeric" value="<c:out value='${param.experienceMax}'/>" placeholder="Đến"></div>
                        </div>
                    </fieldset>

                    <fieldset class="job-filter-section">
                        <legend class="form-label fw-semibold mb-2">Hạn nộp hồ sơ</legend>
                        <div class="row g-2">
                            <div class="col-6"><label class="visually-hidden" for="deadlineFrom">Hạn nộp từ</label><input class="form-control" id="deadlineFrom" name="deadlineFrom" type="date" value="<c:out value='${param.deadlineFrom}'/>"></div>
                            <div class="col-6"><label class="visually-hidden" for="deadlineTo">Hạn nộp đến</label><input class="form-control" id="deadlineTo" name="deadlineTo" type="date" value="<c:out value='${param.deadlineTo}'/>"></div>
                        </div>
                    </fieldset>

                    <div class="filter-form-actions d-grid gap-2 pt-2">
                        <button class="btn btn-primary" type="submit"><i class="bi bi-search me-1"></i>Tìm việc ngay</button>
                        <a class="btn btn-light" href="${pageContext.request.contextPath}/jobs">Đặt lại bộ lọc</a>
                    </div>
                </form>
            </aside>

            <section class="col-lg-8 col-xl-9" aria-labelledby="job-results-title">
                <div class="job-results-toolbar mb-3">
                    <div>
                        <h2 class="h5 mb-1" id="job-results-title">Việc làm đang tuyển</h2>
                        <p class="job-result-count mb-0"><strong><fmt:formatNumber value="${page.totalItems}" type="number" /></strong> cơ hội còn hạn nhận hồ sơ</p>
                    </div>
                    <form action="${pageContext.request.contextPath}/jobs" method="get" class="d-flex align-items-center gap-2">
                        <input type="hidden" name="keyword" value="<c:out value='${param.keyword}'/>">
                        <input type="hidden" name="title" value="<c:out value='${param.title}'/>">
                        <input type="hidden" name="departmentId" value="<c:out value='${param.departmentId}'/>">
                        <input type="hidden" name="categoryId" value="<c:out value='${param.categoryId}'/>">
                        <input type="hidden" name="location" value="<c:out value='${param.location}'/>">
                        <input type="hidden" name="employmentType" value="<c:out value='${param.employmentType}'/>">
                        <input type="hidden" name="salaryMin" value="<c:out value='${param.salaryMin}'/>">
                        <input type="hidden" name="salaryMax" value="<c:out value='${param.salaryMax}'/>">
                        <input type="hidden" name="experienceMin" value="<c:out value='${param.experienceMin}'/>">
                        <input type="hidden" name="experienceMax" value="<c:out value='${param.experienceMax}'/>">
                        <input type="hidden" name="deadlineFrom" value="<c:out value='${param.deadlineFrom}'/>">
                        <input type="hidden" name="deadlineTo" value="<c:out value='${param.deadlineTo}'/>">
                        <input type="hidden" name="pageSize" value="<c:out value='${empty param.pageSize ? page.pageSize : param.pageSize}'/>">
                        <label class="small text-muted text-nowrap" for="sort">Sắp xếp</label>
                        <select class="form-select form-select-sm" id="sort" name="sort" onchange="this.form.submit()">
                            <option value="newest" ${empty param.sort or param.sort eq 'newest' ? 'selected' : ''}>Mới đăng</option>
                            <option value="deadline" ${param.sort eq 'deadline' ? 'selected' : ''}>Hạn nộp gần nhất</option>
                            <option value="salary" ${param.sort eq 'salary' ? 'selected' : ''}>Lương cao nhất</option>
                            <option value="experience" ${param.sort eq 'experience' ? 'selected' : ''}>Ít kinh nghiệm trước</option>
                        </select>
                    </form>
                </div>

                <div class="row g-3 g-xl-4">
                    <c:forEach var="job" items="${page.items}">
                        <c:url var="jobDetailUrl" value="/jobs/detail"><c:param name="id" value="${job.id}" /></c:url>
                        <div class="col-md-6">
                            <article class="job-card job-result-card">
                                <div class="d-flex justify-content-between align-items-start gap-2 mb-3">
                                    <div class="d-flex flex-wrap gap-1"><span class="badge text-bg-primary-subtle text-primary"><c:out value="${job.departmentName}" /></span><c:if test="${not empty job.categoryName}"><span class="badge text-bg-light border text-secondary"><c:out value="${job.categoryName}" /></span></c:if></div>
                                    <span class="job-deadline"><i class="bi bi-calendar3 me-1"></i>Hạn <fmt:formatDate value="${job.deadline}" pattern="dd/MM/yyyy" /></span>
                                </div>
                                <a class="job-card-title d-block mb-3" href="${jobDetailUrl}"><c:out value="${job.title}" /></a>
                                <div class="d-flex flex-column gap-2 mb-3">
                                    <span class="meta-item"><i class="bi bi-geo-alt"></i><c:out value="${job.location}" /></span>
                                    <span class="meta-item"><i class="bi bi-briefcase"></i><c:out value="${job.employmentTypeLabel}" /></span>
                                    <span class="meta-item"><i class="bi bi-person-workspace"></i>Từ <c:out value="${job.experienceRequired}" /> năm kinh nghiệm</span>
                                </div>
                                <div class="job-card-footer pt-3 border-top">
                                    <span class="fw-semibold text-success"><fmt:formatNumber value="${job.salaryMin}" type="number" /> – <fmt:formatNumber value="${job.salaryMax}" type="number" /> VNĐ</span>
                                    <a href="${jobDetailUrl}" class="btn btn-sm btn-outline-primary">Xem chi tiết</a>
                                </div>
                            </article>
                        </div>
                    </c:forEach>
                    <c:if test="${empty page.items}">
                        <div class="col-12">
                            <div class="rf-card empty-state">
                                <div class="empty-icon"><i class="bi bi-search"></i></div>
                                <h3 class="h5">Chưa tìm thấy công việc phù hợp</h3>
                                <p class="mb-3">Thử bỏ bớt điều kiện hoặc mở rộng khoảng lương, kinh nghiệm và hạn nộp.</p>
                                <a href="${pageContext.request.contextPath}/jobs" class="btn btn-outline-primary">Xóa toàn bộ bộ lọc</a>
                            </div>
                        </div>
                    </c:if>
                </div>
                <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
            </section>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
