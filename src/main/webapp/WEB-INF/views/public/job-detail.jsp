<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Chi tiết việc làm | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="py-4 py-lg-5">
    <div class="container">
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <c:choose>
            <c:when test="${not empty job}">
                <nav aria-label="breadcrumb"><ol class="breadcrumb"><li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Trang chủ</a></li><li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/jobs">Việc làm</a></li><li class="breadcrumb-item active" aria-current="page"><c:out value="${job.title}" /></li></ol></nav>
                <div class="row g-4">
                    <div class="col-lg-8">
                        <section class="content-card job-detail-hero mb-4">
                            <div class="d-flex gap-3 align-items-start">
                                <a class="company-mark" href="${pageContext.request.contextPath}/companies/detail?id=${company.id}"><img src="${pageContext.request.contextPath}/company-logo?id=${company.id}" alt="Logo ${company.name}"></a>
                                <div class="flex-grow-1">
                            <span class="badge text-bg-primary-subtle text-primary mb-2">Đang tuyển · <c:out value="${job.departmentName}" /></span>
                            <h1 class="page-title mb-2"><c:out value="${job.title}" /></h1>
                            <p class="job-company-line mb-3"><a href="${pageContext.request.contextPath}/companies/detail?id=${company.id}"><c:out value="${company.name}" /></a> <span class="verified-company"><i class="bi bi-patch-check-fill"></i> Đã xác thực</span></p>
                            <div class="d-flex flex-wrap gap-3 text-muted">
                                <span class="meta-item"><i class="bi bi-geo-alt"></i><c:out value="${job.location}" /></span>
                                <span class="meta-item"><i class="bi bi-briefcase"></i><span data-enum-label="${job.employmentType}"><c:out value="${job.employmentType}" /></span></span>
                                <span class="meta-item"><i class="bi bi-people"></i>Còn <c:out value="${job.remainingPositions}" />/<c:out value="${job.numberOfPositions}" /> vị trí</span>
                                <span class="meta-item"><i class="bi bi-calendar3"></i>Hạn nộp: <c:out value="${job.deadline}" /></span>
                            </div>
                                </div>
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
                        <section class="content-card mb-4">
                            <h2 class="h4 fw-bold mb-3">Quyền lợi</h2>
                            <div class="text-secondary" style="white-space: pre-line;"><c:out value="${job.benefits}" /></div>
                        </section>
                        <section class="content-card">
                            <h2 class="h4 fw-bold mb-3">Kỹ năng mong muốn</h2>
                            <div class="d-flex flex-wrap gap-2">
                                <c:forEach var="skill" items="${skills}"><span class="badge rounded-pill text-bg-light border text-dark"><i class="bi bi-check2-circle text-primary me-1"></i><c:out value="${skill.skillName}" /><c:if test="${skill.required}"><span class="text-danger ms-1">*</span></c:if></span></c:forEach>
                                <c:if test="${empty skills}"><span class="text-muted">Kỹ năng chi tiết sẽ được trao đổi trong quá trình tuyển dụng.</span></c:if>
                            </div>
                        </section>
                        <section class="content-card mt-4">
                            <h2 class="h4 fw-bold mb-3">Quy trình ứng tuyển</h2>
                            <div class="application-steps">
                                <div><span>1</span><strong>Chọn CV</strong><small>Kiểm tra hồ sơ phù hợp nhất</small></div>
                                <div><span>2</span><strong>Gửi ứng tuyển</strong><small>Thêm lời giới thiệu với nhà tuyển dụng</small></div>
                                <div><span>3</span><strong>Theo dõi tiến độ</strong><small>Nhận cập nhật tại trang Đơn ứng tuyển</small></div>
                            </div>
                        </section>
                        <c:if test="${not empty relatedJobs}"><section class="content-card mt-4"><div class="d-flex justify-content-between align-items-center mb-3"><h2 class="h4 fw-bold mb-0">Việc làm tương tự</h2><a class="small fw-semibold" href="${pageContext.request.contextPath}/jobs?departmentId=${job.departmentId}">Xem tất cả</a></div><div class="related-job-list"><c:forEach var="related" items="${relatedJobs}"><a href="${pageContext.request.contextPath}/jobs/detail?id=${related.id}"><span><strong><c:out value="${related.title}" /></strong><small><i class="bi bi-geo-alt"></i><c:out value="${related.location}" /> · Hạn <c:out value="${related.deadline}" /></small></span><i class="bi bi-arrow-right"></i></a></c:forEach></div></section></c:if>
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
                                    <div class="d-grid gap-2">
                                        <c:choose>
                                            <c:when test="${alreadyApplied}"><a class="btn btn-success disabled" aria-disabled="true"><i class="bi bi-check-circle-fill me-1"></i>Đã ứng tuyển</a><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/candidate/applications">Theo dõi đơn ứng tuyển</a></c:when>
                                            <c:when test="${empty candidateResumes}"><a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-file-earmark-arrow-up me-1"></i>Tải CV để ứng tuyển</a><small class="text-muted text-center">Bạn cần có ít nhất một CV trong hồ sơ.</small></c:when>
                                            <c:otherwise><button id="apply-now" class="btn btn-primary w-100" type="button" data-bs-toggle="modal" data-bs-target="#applyJobModal"><i class="bi bi-send me-1"></i>Ứng tuyển ngay</button></c:otherwise>
                                        </c:choose>
                                        <form action="${pageContext.request.contextPath}/candidate/saved-jobs/${jobSaved ? 'remove' : 'save'}" method="post">
                                            <input type="hidden" name="jobId" value="<c:out value='${job.id}'/>">
                                            <input type="hidden" name="returnTo" value="/jobs/detail?id=<c:out value='${job.id}'/>">
                                            <button class="btn ${jobSaved ? 'btn-primary' : 'btn-outline-primary'} w-100" type="submit"><i class="bi ${jobSaved ? 'bi-bookmark-heart-fill' : 'bi-bookmark-heart'} me-1"></i>${jobSaved ? 'Đã lưu việc làm' : 'Lưu việc làm'}</button>
                                        </form>
                                    </div>
                                </c:when>
                                <c:when test="${empty sessionScope.userId}"><a class="btn btn-primary w-100" href="${pageContext.request.contextPath}/login"><i class="bi bi-box-arrow-in-right me-1"></i>Đăng nhập để ứng tuyển</a></c:when>
                                <c:otherwise><a class="btn btn-outline-primary w-100" href="${pageContext.request.contextPath}/candidate/jobs">Xem trong cổng ứng viên</a></c:otherwise>
                            </c:choose>
                            <a class="btn btn-link w-100 mt-2" href="${pageContext.request.contextPath}/jobs"><i class="bi bi-arrow-left me-1"></i>Quay lại danh sách</a>
                            <div class="job-safety-note"><i class="bi bi-shield-check"></i><span><strong>Ứng tuyển an toàn</strong>JobCV không yêu cầu ứng viên chuyển khoản hoặc cung cấp mật khẩu.</span></div>
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

<div class="mobile-job-action">
    <div><small>Mức lương</small><strong><fmt:formatNumber value="${job.salaryMin}" type="number" />–<fmt:formatNumber value="${job.salaryMax}" type="number" /> VNĐ</strong></div>
    <c:choose><c:when test="${sessionScope.role eq 'CANDIDATE' and alreadyApplied}"><a class="btn btn-success" href="${pageContext.request.contextPath}/candidate/applications"><i class="bi bi-check-circle me-1"></i>Đã ứng tuyển</a></c:when><c:when test="${sessionScope.role eq 'CANDIDATE' and empty candidateResumes}"><a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/resumes">Tải CV ứng tuyển</a></c:when><c:when test="${sessionScope.role eq 'CANDIDATE'}"><button class="btn btn-primary" type="button" data-bs-toggle="modal" data-bs-target="#applyJobModal">Ứng tuyển ngay</button></c:when><c:when test="${empty sessionScope.userId}"><a class="btn btn-primary" href="${pageContext.request.contextPath}/login">Đăng nhập ứng tuyển</a></c:when></c:choose>
</div>

<c:if test="${sessionScope.role eq 'CANDIDATE' and not alreadyApplied and not empty candidateResumes}">
<div class="modal fade" id="applyJobModal" tabindex="-1" aria-labelledby="applyJobModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg"><div class="modal-content apply-modal">
        <div class="modal-header"><div><span class="modal-kicker">Hoàn tất ứng tuyển</span><h2 class="modal-title fs-4" id="applyJobModalLabel"><c:out value="${job.title}" /></h2></div><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button></div>
        <form action="${pageContext.request.contextPath}/candidate/applications/apply" method="post" data-validate-form>
            <div class="modal-body">
                <input type="hidden" name="jobId" value="<c:out value='${job.id}'/>">
                <div class="apply-section-heading"><span>1</span><div><strong>Chọn CV ứng tuyển</strong><small>Nhà tuyển dụng sẽ nhận đúng hồ sơ bạn chọn.</small></div></div>
                <div class="resume-choice-list">
                    <c:forEach var="resume" items="${candidateResumes}" varStatus="loop">
                        <label class="resume-choice">
                            <input class="form-check-input" type="radio" name="resumeId" value="<c:out value='${resume.id}'/>" ${resume.defaultResume or loop.first ? 'checked' : ''} required>
                            <span class="resume-choice-icon"><i class="bi bi-file-earmark-pdf"></i></span>
                            <span class="flex-grow-1"><strong><c:out value="${resume.fileName}" /></strong><small>Tải lên <fmt:formatDate value="${resume.uploadedAt}" pattern="dd/MM/yyyy" /></small></span>
                            <c:if test="${resume.defaultResume}"><span class="badge rounded-pill text-bg-success-subtle text-success">CV mặc định</span></c:if>
                        </label>
                    </c:forEach>
                </div>
                <a class="small fw-semibold d-inline-block mt-2" href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-plus-circle me-1"></i>Quản lý hoặc tải CV khác</a>
                <div class="apply-section-heading mt-4"><span>2</span><div><strong>Lời giới thiệu</strong><small>Nêu ngắn gọn lý do bạn phù hợp với vị trí.</small></div></div>
                <textarea class="form-control" name="coverLetter" rows="5" minlength="20" maxlength="2000" required data-character-counter="applyCoverCounter" placeholder="Ví dụ: Tôi có kinh nghiệm phù hợp với các yêu cầu chính của vị trí và mong muốn được trao đổi sâu hơn..."></textarea>
                <div class="d-flex justify-content-between mt-1"><small class="text-muted">Tối thiểu 20 ký tự</small><small class="text-muted" id="applyCoverCounter">0/2000</small></div>
                <div class="apply-consent"><i class="bi bi-info-circle"></i> Khi gửi hồ sơ, bạn đồng ý chia sẻ CV và lời giới thiệu này với nhà tuyển dụng của tin đăng.</div>
            </div>
            <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Xem lại sau</button><button class="btn btn-primary px-4" type="submit" data-loading-button><i class="bi bi-send-check me-1"></i>Gửi hồ sơ ứng tuyển</button></div>
        </form>
    </div></div>
</div>
</c:if>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
