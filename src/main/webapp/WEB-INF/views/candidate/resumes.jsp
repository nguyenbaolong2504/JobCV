<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="CV của tôi | RecruitFlow" scope="request" />
<c:set var="defaultResumeId" value="" />
<c:set var="defaultResumeName" value="CV của bạn" />
<c:forEach var="availableResume" items="${resumes}">
    <c:if test="${availableResume.fileAvailable and (availableResume.defaultResume or empty defaultResumeId)}">
        <c:set var="defaultResumeId" value="${availableResume.id}" />
        <c:set var="defaultResumeName" value="${availableResume.fileName}" />
    </c:if>
</c:forEach>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />

        <main class="candidate-main col-lg-9 col-xl-10">
            <jsp:include page="/WEB-INF/views/common/flash.jsp" />

            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
                <div>
                    <div class="d-flex align-items-center gap-2 mb-1">
                        <h1 class="page-title mb-0">CV của tôi</h1>
                        <span class="badge rounded-pill text-bg-primary">AI CV Coach</span>
                    </div>
                    <p class="text-muted mb-0">Tải lên, chọn CV mặc định và nhận gợi ý cải thiện phù hợp với mục tiêu ứng tuyển.</p>
                </div>
                <div class="d-flex flex-wrap gap-2">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/cv-builder">
                        <i class="bi bi-magic me-1"></i>Tạo CV theo mẫu
                    </a>
                    <c:choose>
                        <c:when test="${not empty defaultResumeId}">
                            <button class="btn btn-outline-primary" type="button" data-bs-toggle="modal"
                                    data-bs-target="#aiReviewModal" data-ai-review-trigger
                                    data-resume-id="<c:out value='${defaultResumeId}'/>"
                                    data-resume-name="<c:out value='${defaultResumeName}'/>">
                                <i class="bi bi-stars me-1"></i>Nhờ AI đánh giá CV
                            </button>
                        </c:when>
                        <c:otherwise>
                            <a class="btn btn-outline-primary" href="#resumeFile"><i class="bi bi-cloud-arrow-up me-1"></i>Tải CV để dùng AI</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <section class="ai-review-result content-card mb-4 d-none" id="aiReviewResult"
                     aria-labelledby="aiReviewResultTitle" aria-live="polite" tabindex="-1">
                <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
                    <div class="d-flex gap-3">
                        <span class="ai-review-icon"><i class="bi bi-stars"></i></span>
                        <div>
                            <p class="text-primary text-uppercase small fw-bold mb-1">Kết quả từ AI CV Coach</p>
                            <h2 class="h4 fw-bold mb-1" id="aiReviewResultTitle">Đánh giá cho <span id="aiReviewResumeName">CV đã chọn</span></h2>
                            <p class="text-muted mb-0">Đây là gợi ý tham khảo. Chỉ giữ thông tin đúng với kinh nghiệm thực tế của bạn.</p>
                        </div>
                    </div>
                    <div class="ai-review-score text-center d-none" id="aiReviewScoreBox">
                        <strong id="aiReviewScore">0</strong><span>/100</span>
                        <small>Điểm sẵn sàng</small>
                    </div>
                </div>

                <div class="ai-review-summary mb-4 d-none" id="aiReviewSummary"></div>

                <div class="row g-3">
                    <div class="col-lg-6 d-none" id="aiReviewStrengthsColumn">
                        <div class="ai-review-list ai-review-list-success h-100">
                            <h3 class="h6 fw-bold"><i class="bi bi-check-circle-fill me-2"></i>Điểm mạnh</h3>
                            <ul class="mb-0" id="aiReviewStrengths"></ul>
                        </div>
                    </div>
                    <div class="col-lg-6 d-none" id="aiReviewImprovementsColumn">
                        <div class="ai-review-list ai-review-list-warning h-100">
                            <h3 class="h6 fw-bold"><i class="bi bi-arrow-up-circle-fill me-2"></i>Nên cải thiện</h3>
                            <ul class="mb-0" id="aiReviewImprovements"></ul>
                        </div>
                    </div>
                    <div class="col-lg-6 d-none" id="aiReviewMissingColumn">
                        <div class="ai-review-list ai-review-list-danger h-100">
                            <h3 class="h6 fw-bold"><i class="bi bi-exclamation-circle-fill me-2"></i>Phần còn thiếu</h3>
                            <ul class="mb-0" id="aiReviewMissingSections"></ul>
                        </div>
                    </div>
                    <div class="col-lg-6 d-none" id="aiReviewBulletsColumn">
                        <div class="ai-review-list ai-review-list-info h-100">
                            <h3 class="h6 fw-bold"><i class="bi bi-lightbulb-fill me-2"></i>Gợi ý diễn đạt</h3>
                            <ul class="mb-0" id="aiReviewSuggestedBullets"></ul>
                        </div>
                    </div>
                    <div class="col-12 d-none" id="aiReviewKeywordsColumn">
                        <div class="ai-keywords">
                            <h3 class="h6 fw-bold mb-2"><i class="bi bi-tags me-2"></i>Từ khóa nên cân nhắc bổ sung</h3>
                            <div class="d-flex flex-wrap gap-2" id="aiReviewKeywords"></div>
                        </div>
                    </div>
                    <div class="col-12 d-none" id="aiReviewRewriteColumn">
                        <div class="ai-rewrite-box">
                            <div class="d-flex align-items-center justify-content-between gap-2 mb-2">
                                <h3 class="h6 fw-bold mb-0"><i class="bi bi-pencil-square me-2"></i>Gợi ý viết lại phần giới thiệu</h3>
                                <span class="small text-muted">Hãy tự kiểm tra tính chính xác trước khi dùng</span>
                            </div>
                            <p class="mb-0" id="aiReviewRewrittenSummary"></p>
                        </div>
                    </div>
                </div>

                <p class="small text-muted mb-0 mt-3" id="aiReviewDisclaimer">
                    <i class="bi bi-shield-check me-1"></i>AI chỉ phân tích nội dung CV đã chọn; không tự thay đổi hoặc gửi CV thay bạn.
                </p>
            </section>

            <div class="row g-4">
                <div class="col-xl-4">
                    <section class="content-card mb-4">
                        <h2 class="h5 fw-bold mb-2">Tải CV mới</h2>
                        <p class="text-muted small">Hỗ trợ PDF, DOC, DOCX; tối đa 5 MB. Tên tệp sẽ được hệ thống đổi để bảo mật.</p>
                        <form class="resume-upload-dropzone" action="${pageContext.request.contextPath}/candidate/resumes/upload" method="post" enctype="multipart/form-data" data-resume-dropzone>
                            <div class="resume-drop-hint"><span><i class="bi bi-cloud-arrow-up"></i></span><strong>Kéo thả CV vào đây</strong><small>hoặc chọn tệp từ máy tính</small></div>
                            <label class="form-label" for="resumeFile">Chọn tệp CV <span class="text-danger">*</span></label>
                            <input class="form-control" id="resumeFile" name="resumeFile" type="file"
                                   accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                   required data-resume-upload data-feedback-target="resumeUploadFeedback">
                            <div id="resumeUploadFeedback" class="invalid-feedback d-block d-none"></div>
                            <button class="btn btn-primary w-100 mt-3" type="submit" data-loading-button>
                                <i class="bi bi-cloud-arrow-up me-1"></i>Tải CV lên
                            </button>
                        </form>
                    </section>

                    <aside class="ai-coach-card" aria-label="AI CV Coach">
                        <span class="ai-coach-card-icon"><i class="bi bi-stars"></i></span>
                        <h2 class="h5 fw-bold mb-2">AI CV Coach hỗ trợ gì?</h2>
                        <ul class="small mb-3 ps-3">
                            <li>Kiểm tra điểm mạnh và phần còn thiếu.</li>
                            <li>Gợi ý từ khóa theo vị trí mục tiêu.</li>
                            <li>Đề xuất cách viết lại phần giới thiệu.</li>
                        </ul>
                        <p class="small mb-0 text-muted">Có thể đánh giá cục bộ ngay cả khi chưa cấu hình khóa AI. AI không tự nộp đơn hoặc thay đổi CV.</p>
                    </aside>
                </div>

                <div class="col-xl-8">
                    <section class="content-card">
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                            <div>
                                <h2 class="h5 fw-bold mb-1">Danh sách CV</h2>
                                <span class="small text-muted">Chọn một CV làm mặc định để dùng khi ứng tuyển.</span>
                            </div>
                            <span class="small text-muted"><c:out value="${fn:length(resumes)}" /> CV</span>
                        </div>

                        <div class="vstack gap-3">
                            <c:forEach var="resume" items="${resumes}">
                                <article class="resume-item border rounded-3 p-3" id="resume-${resume.id}">
                                    <div class="d-flex flex-wrap align-items-start gap-3">
                                        <span class="resume-file-icon"><i class="bi ${resume.fileType eq 'pdf' ? 'bi-file-earmark-pdf' : 'bi-file-earmark-text'}"></i></span>
                                        <div class="flex-grow-1 min-w-0">
                                            <div class="d-flex flex-wrap align-items-center gap-2">
                                                <h3 class="h6 mb-0 text-break"><c:out value="${resume.fileName}" /></h3>
                                                <c:if test="${resume.defaultResume}"><span class="badge text-bg-success">CV mặc định</span></c:if>
                                                <c:if test="${not resume.fileAvailable}"><span class="badge text-bg-warning">Tệp cần tải lại</span></c:if>
                                            </div>
                                            <p class="text-muted small mb-0 mt-1">
                                                <c:out value="${fn:toUpperCase(resume.fileType)}" /> · <fmt:formatNumber value="${resume.fileSize / 1024}" maxFractionDigits="1" /> KB · Tải lên <fmt:formatDate value="${resume.uploadedAt}" pattern="dd/MM/yyyy HH:mm" />
                                            </p>
                                        </div>
                                        <div class="resume-actions d-flex flex-wrap gap-2">
                                            <c:choose>
                                                <c:when test="${resume.fileAvailable}">
                                                    <c:url var="downloadResumeUrl" value="/candidate/resumes/download"><c:param name="id" value="${resume.id}" /></c:url>
                                                    <a class="btn btn-sm btn-outline-primary" href="${downloadResumeUrl}" target="_blank" rel="noopener" title="Xem CV">
                                                        <i class="bi bi-eye"></i><span class="visually-hidden">Xem CV</span>
                                                    </a>
                                                    <button class="btn btn-sm btn-outline-primary" type="button" data-bs-toggle="modal"
                                                            data-bs-target="#aiReviewModal" data-ai-review-trigger
                                                            data-resume-id="<c:out value='${resume.id}'/>"
                                                            data-resume-name="<c:out value='${resume.fileName}'/>" title="Nhờ AI đánh giá CV">
                                                        <i class="bi bi-stars me-1"></i><span class="d-none d-sm-inline">AI đánh giá</span>
                                                    </button>
                                                </c:when>
                                                <c:otherwise><span class="small text-warning-emphasis align-self-center">Tải CV mới để dùng lại.</span></c:otherwise>
                                            </c:choose>
                                            <c:if test="${not resume.defaultResume and resume.fileAvailable}">
                                                <form action="${pageContext.request.contextPath}/candidate/resumes/default" method="post">
                                                    <input type="hidden" name="resumeId" value="<c:out value='${resume.id}'/>">
                                                    <button class="btn btn-sm btn-outline-secondary" type="submit" title="Đặt làm CV mặc định">
                                                        <i class="bi bi-star"></i><span class="visually-hidden">Đặt làm CV mặc định</span>
                                                    </button>
                                                </form>
                                            </c:if>
                                            <form action="${pageContext.request.contextPath}/candidate/resumes/delete" method="post" data-confirm="Bạn có chắc muốn xóa CV này?">
                                                <input type="hidden" name="resumeId" value="<c:out value='${resume.id}'/>">
                                                <button class="btn btn-sm btn-outline-danger" type="submit" title="Xóa CV">
                                                    <i class="bi bi-trash"></i><span class="visually-hidden">Xóa CV</span>
                                                </button>
                                            </form>
                                        </div>
                                    </div>
                                </article>
                            </c:forEach>

                            <c:if test="${empty resumes}">
                                <div class="empty-state">
                                    <div class="empty-icon"><i class="bi bi-file-earmark-arrow-up"></i></div>
                                    <h3 class="h5">Bạn chưa tải CV nào</h3>
                                    <p class="mb-3">Hãy tải CV để có thể ứng tuyển và nhận góp ý cá nhân hóa từ AI CV Coach.</p>
                                    <a class="btn btn-outline-primary" href="#resumeFile"><i class="bi bi-cloud-arrow-up me-1"></i>Tải CV ngay</a>
                                </div>
                            </c:if>
                        </div>
                    </section>
                </div>
            </div>
        </main>
    </div>
</div>

<div class="modal fade" id="aiReviewModal" tabindex="-1" aria-labelledby="aiReviewModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <form id="aiReviewForm" action="${pageContext.request.contextPath}/candidate/resumes/ai-review" method="post" data-ai-review-form>
                <div class="modal-header border-0 pb-0">
                    <div>
                        <p class="text-primary small text-uppercase fw-bold mb-1">AI CV Coach</p>
                        <h2 class="modal-title h5 fw-bold" id="aiReviewModalTitle">Nhờ AI đánh giá CV</h2>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
                </div>
                <div class="modal-body pt-3">
                    <input type="hidden" id="aiReviewResumeId" name="resumeId">
                    <div class="ai-review-selected mb-3">
                        <i class="bi bi-file-earmark-text me-2"></i>
                        <span>CV được chọn: <strong id="aiReviewSelectedName">CV của bạn</strong></span>
                    </div>
                    <p class="small text-muted">Chọn mục tiêu để AI đưa ra gợi ý sát với nhu cầu của bạn. Trình duyệt không tải lại tệp CV; chỉ gửi mã CV và lựa chọn đánh giá đến hệ thống.</p>
                    <div class="mb-3">
                        <label class="form-label" for="reviewGoal">Bạn muốn AI tập trung vào</label>
                        <select class="form-select" id="reviewGoal" name="reviewGoal">
                            <option value="GENERAL">Tổng quan: bố cục, nội dung và tính rõ ràng</option>
                            <option value="JOB_MATCH">Độ phù hợp với vị trí ứng tuyển</option>
                            <option value="GRAMMAR">Cách diễn đạt và phần giới thiệu</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="targetRole">Vị trí mục tiêu <span class="text-muted fw-normal">(không bắt buộc)</span></label>
                        <input class="form-control" id="targetRole" name="targetRole" maxlength="120" placeholder="Ví dụ: Java Backend Developer">
                    </div>
                    <div class="form-check ai-consent-box">
                        <input class="form-check-input" type="checkbox" value="true" id="aiReviewConsent" name="aiReviewConsent" required>
                        <label class="form-check-label small" for="aiReviewConsent">
                            Tôi đồng ý để hệ thống phân tích nội dung CV đã chọn nhằm đưa ra gợi ý. Tôi không gửi thông tin nhạy cảm không cần thiết.
                        </label>
                    </div>
                    <div class="form-check ai-external-consent-box mt-2">
                        <input class="form-check-input" type="checkbox" value="true" id="externalConsent" name="externalConsent">
                        <label class="form-check-label small" for="externalConsent">
                            Tôi đồng ý rõ ràng rằng nội dung CV đã trích xuất có thể được gửi đến nhà cung cấp AI bên ngoài đã cấu hình để nhận góp ý nâng cao.
                        </label>
                    </div>
                    <div class="alert alert-light border small mb-0 mt-3">
                        <i class="bi bi-shield-check me-1"></i> Nếu không tick ô thứ hai, hệ thống chỉ dùng AI Coach cục bộ và không cần khóa API. Tệp CV không được tải lên lại từ trình duyệt.
                    </div>
                    <div class="alert alert-danger small d-none mt-3 mb-0" id="aiReviewFeedback" role="alert"></div>
                </div>
                <div class="modal-footer border-0 pt-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                    <button class="btn btn-primary" id="aiReviewSubmit" type="submit">
                        <i class="bi bi-stars me-1"></i>Nhận gợi ý từ AI
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
