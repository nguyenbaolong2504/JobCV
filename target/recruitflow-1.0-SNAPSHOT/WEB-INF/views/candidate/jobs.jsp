<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Tìm việc làm | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />
        <main class="candidate-main col-lg-9 col-xl-10">
            <jsp:include page="/WEB-INF/views/common/flash.jsp" />
            <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}" /></div></c:if>
            <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
                <div>
                    <p class="eyebrow mb-2"><i class="bi bi-stars me-1"></i>Khám phá cơ hội</p>
                    <h1 class="page-title mb-1">Tìm việc làm phù hợp</h1>
                    <p class="text-muted mb-0">Lọc việc làm đang mở, sau đó xem mức độ khớp với CV mặc định của bạn.</p>
                </div>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-file-earmark-person me-1"></i>Cập nhật CV</a>
            </div>

            <div class="row g-4 job-discovery-layout">
                <aside class="col-xl-4 col-xxl-3" aria-label="Bộ lọc việc làm">
                    <form class="filter-card job-filter-sidebar" action="${pageContext.request.contextPath}/candidate/jobs" method="get">
                        <div class="d-flex align-items-start justify-content-between gap-2 mb-3">
                            <div>
                                <h2 class="h5 fw-bold mb-1"><i class="bi bi-sliders2-vertical me-2 text-primary"></i>Bộ lọc tìm việc</h2>
                                <p class="small text-muted mb-0">Lọc nhiều điều kiện cùng lúc.</p>
                            </div>
                            <a class="btn btn-sm btn-link p-0" href="${pageContext.request.contextPath}/candidate/jobs">Xóa lọc</a>
                        </div>

                        <div class="job-filter-section pt-0">
                            <label class="form-label fw-semibold" for="candidateKeyword">Từ khóa / kỹ năng</label>
                            <div class="input-group">
                                <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
                                <input class="form-control" id="candidateKeyword" name="keyword" type="search" maxlength="150" value="<c:out value='${param.keyword}'/>" placeholder="Java, SQL, mã tin...">
                            </div>
                            <div class="form-text">Tìm trong kỹ năng, mô tả, yêu cầu và mã tin.</div>
                        </div>

                        <div class="job-filter-section">
                            <label class="form-label fw-semibold" for="candidateTitle">Vị trí / chức danh</label>
                            <input class="form-control" id="candidateTitle" name="title" type="search" maxlength="150" value="<c:out value='${param.title}'/>" placeholder="Ví dụ: Tester">
                        </div>

                        <div class="job-filter-section">
                            <label class="form-label fw-semibold" for="candidateDepartment">Phòng ban</label>
                            <select class="form-select" id="candidateDepartment" name="departmentId">
                                <option value="">Tất cả phòng ban</option>
                                <c:forEach var="department" items="${departments}">
                                    <option value="<c:out value='${department.id}'/>" ${param.departmentId eq department.id ? 'selected' : ''}><c:out value="${department.name}" /></option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="job-filter-section">
                            <label class="form-label fw-semibold" for="candidateCategory">Danh mục nghề nghiệp</label>
                            <select class="form-select" id="candidateCategory" name="categoryId">
                                <option value="">Tất cả danh mục</option>
                                <c:forEach var="category" items="${jobCategories}">
                                    <option value="<c:out value='${category.id}'/>" ${param.categoryId eq category.id ? 'selected' : ''}>
                                        <c:out value="${empty category.parentName ? category.name : category.parentName}" /><c:if test="${not empty category.parentName}"> › <c:out value="${category.name}" /></c:if>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="job-filter-section">
                            <label class="form-label fw-semibold" for="candidateLocation">Địa điểm</label>
                            <input class="form-control" id="candidateLocation" name="location" maxlength="100" value="<c:out value='${param.location}'/>" placeholder="Hà Nội, Đà Nẵng, Remote...">
                        </div>

                        <div class="job-filter-section">
                            <label class="form-label fw-semibold" for="candidateEmploymentType">Hình thức làm việc</label>
                            <select class="form-select" id="candidateEmploymentType" name="employmentType">
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
                                <div class="col-6"><label class="visually-hidden" for="candidateSalaryMin">Lương từ</label><input class="form-control" id="candidateSalaryMin" name="salaryMin" type="number" min="0" step="500000" inputmode="decimal" value="<c:out value='${param.salaryMin}'/>" placeholder="Từ"></div>
                                <div class="col-6"><label class="visually-hidden" for="candidateSalaryMax">Lương đến</label><input class="form-control" id="candidateSalaryMax" name="salaryMax" type="number" min="0" step="500000" inputmode="decimal" value="<c:out value='${param.salaryMax}'/>" placeholder="Đến"></div>
                            </div>
                            <div class="form-text">Tin có dải lương giao với mức đã chọn.</div>
                        </fieldset>

                        <fieldset class="job-filter-section">
                            <legend class="form-label fw-semibold mb-2">Kinh nghiệm yêu cầu (năm)</legend>
                            <div class="row g-2">
                                <div class="col-6"><label class="visually-hidden" for="candidateExperienceMin">Kinh nghiệm từ</label><input class="form-control" id="candidateExperienceMin" name="experienceMin" type="number" min="0" max="100" step="1" inputmode="numeric" value="<c:out value='${param.experienceMin}'/>" placeholder="Từ"></div>
                                <div class="col-6"><label class="visually-hidden" for="candidateExperienceMax">Kinh nghiệm đến</label><input class="form-control" id="candidateExperienceMax" name="experienceMax" type="number" min="0" max="100" step="1" inputmode="numeric" value="<c:out value='${param.experienceMax}'/>" placeholder="Đến"></div>
                            </div>
                        </fieldset>

                        <fieldset class="job-filter-section">
                            <legend class="form-label fw-semibold mb-2">Hạn nộp hồ sơ</legend>
                            <div class="row g-2">
                                <div class="col-6"><label class="visually-hidden" for="candidateDeadlineFrom">Hạn nộp từ</label><input class="form-control" id="candidateDeadlineFrom" name="deadlineFrom" type="date" value="<c:out value='${param.deadlineFrom}'/>"></div>
                                <div class="col-6"><label class="visually-hidden" for="candidateDeadlineTo">Hạn nộp đến</label><input class="form-control" id="candidateDeadlineTo" name="deadlineTo" type="date" value="<c:out value='${param.deadlineTo}'/>"></div>
                            </div>
                        </fieldset>

                        <div class="filter-form-actions d-grid gap-2 pt-2">
                            <button class="btn btn-primary" type="submit"><i class="bi bi-search me-1"></i>Tìm việc ngay</button>
                            <a class="btn btn-light" href="${pageContext.request.contextPath}/candidate/jobs">Đặt lại bộ lọc</a>
                        </div>
                    </form>
                </aside>

                <section class="col-xl-8 col-xxl-9" aria-labelledby="candidate-job-results-title">
                    <div class="job-results-toolbar mb-3">
                        <div>
                            <h2 class="h5 mb-1" id="candidate-job-results-title">Các việc làm đang mở</h2>
                            <p class="job-result-count mb-0"><strong><fmt:formatNumber value="${page.totalItems}" type="number" /></strong> cơ hội còn hạn nhận hồ sơ</p>
                        </div>
                        <form action="${pageContext.request.contextPath}/candidate/jobs" method="get" class="d-flex align-items-center gap-2">
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
                            <label class="small text-muted text-nowrap" for="candidateSort">Sắp xếp</label>
                            <select class="form-select form-select-sm" id="candidateSort" name="sort" onchange="this.form.submit()">
                                <option value="newest" ${empty param.sort or param.sort eq 'newest' ? 'selected' : ''}>Mới đăng</option>
                                <option value="deadline" ${param.sort eq 'deadline' ? 'selected' : ''}>Hạn nộp gần nhất</option>
                                <option value="salary" ${param.sort eq 'salary' ? 'selected' : ''}>Lương cao nhất</option>
                                <option value="experience" ${param.sort eq 'experience' ? 'selected' : ''}>Ít kinh nghiệm trước</option>
                            </select>
                        </form>
                    </div>

                    <div class="row g-3 g-xxl-4">
                        <c:forEach var="jobMatch" items="${page.items}">
                            <c:url var="candidateJobDetailUrl" value="/jobs/detail"><c:param name="id" value="${jobMatch.job.id}" /></c:url>
                            <div class="col-md-6">
                                <article class="job-card job-result-card">
                                    <div class="d-flex justify-content-between align-items-start gap-2">
                                        <div class="d-flex flex-wrap gap-1"><span class="badge text-bg-primary-subtle text-primary"><c:out value="${jobMatch.job.departmentName}" /></span><c:if test="${not empty jobMatch.job.categoryName}"><span class="badge text-bg-light border text-secondary"><c:out value="${jobMatch.job.categoryName}" /></span></c:if></div>
                                        <span class="match-badge">Match <fmt:formatNumber value="${jobMatch.matchScore}" maxFractionDigits="0" />%</span>
                                    </div>
                                    <a class="job-card-title d-block mt-3 mb-2" href="${candidateJobDetailUrl}"><c:out value="${jobMatch.job.title}" /></a>
                                    <div class="d-flex flex-column gap-2">
                                        <span class="meta-item"><i class="bi bi-geo-alt"></i><c:out value="${jobMatch.job.location}" /></span>
                                        <span class="meta-item"><i class="bi bi-briefcase"></i><c:out value="${jobMatch.job.employmentTypeLabel}" /></span>
                                        <span class="meta-item"><i class="bi bi-person-workspace"></i>Từ <c:out value="${jobMatch.job.experienceRequired}" /> năm kinh nghiệm</span>
                                        <span class="job-deadline"><i class="bi bi-calendar3 me-1"></i>Hạn <fmt:formatDate value="${jobMatch.job.deadline}" pattern="dd/MM/yyyy" /></span>
                                    </div>
                                    <div class="small text-muted mt-3">Kỹ năng khớp: <c:forEach var="skill" items="${jobMatch.matchedSkills}" varStatus="loop"><c:out value="${skill}" /><c:if test="${not loop.last}">, </c:if></c:forEach><c:if test="${empty jobMatch.matchedSkills}">Chưa có kỹ năng khớp</c:if></div>
                                    <div class="job-card-footer pt-3 mt-3 border-top">
                                        <span class="fw-semibold text-success small"><fmt:formatNumber value="${jobMatch.job.salaryMin}" type="number" /> – <fmt:formatNumber value="${jobMatch.job.salaryMax}" type="number" /> VNĐ</span>
                                        <div class="d-flex gap-2">
                                            <a class="btn btn-sm btn-outline-primary" href="${candidateJobDetailUrl}">Chi tiết</a>
                                            <form action="${pageContext.request.contextPath}/candidate/applications/apply" method="post" data-confirm="Bạn muốn ứng tuyển vị trí này bằng CV mặc định?">
                                                <input type="hidden" name="jobId" value="<c:out value='${jobMatch.job.id}'/>">
                                                <button class="btn btn-sm btn-primary" type="submit" data-loading-button>Ứng tuyển</button>
                                            </form>
                                        </div>
                                    </div>
                                </article>
                            </div>
                        </c:forEach>
                        <c:if test="${empty page.items}">
                            <div class="col-12">
                                <div class="rf-card empty-state">
                                    <div class="empty-icon"><i class="bi bi-search"></i></div>
                                    <h2 class="h5">Chưa có việc làm phù hợp</h2>
                                    <p class="mb-3">Bạn có thể nới rộng bộ lọc hoặc cập nhật CV để nhận gợi ý tốt hơn.</p>
                                    <div class="d-flex flex-wrap justify-content-center gap-2"><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/candidate/jobs">Xóa bộ lọc</a><a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/resumes">Cập nhật CV</a></div>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    <jsp:include page="/WEB-INF/views/common/pagination.jsp" />
                </section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
