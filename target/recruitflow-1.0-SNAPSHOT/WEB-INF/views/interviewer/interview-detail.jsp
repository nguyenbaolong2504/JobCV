<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Chi tiết phỏng vấn | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/interviewer-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><a class="small text-decoration-none" href="${pageContext.request.contextPath}/interviewer/interviews"><i class="bi bi-arrow-left me-1"></i>Lịch phỏng vấn</a><h4 class="mt-1 mb-0">Chi tiết phỏng vấn</h4></div><span class="badge text-bg-primary"><c:out value="${interview.status}" /></span></div>
            <div class="main-content">
                <c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="successMessage" scope="session" /></c:if>
                <c:if test="${not empty sessionScope.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${sessionScope.error}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="error" scope="session" /></c:if>
                <div class="row g-4">
                    <div class="col-xl-4"><section class="card h-100"><div class="card-body p-4"><div class="d-flex align-items-center gap-3 mb-4"><div class="rounded-circle bg-info-subtle text-info d-flex align-items-center justify-content-center fs-3" style="width:58px;height:58px;"><i class="bi bi-person"></i></div><div><h5 class="mb-1"><c:out value="${interview.candidateName}" /></h5><p class="text-muted mb-0"><c:out value="${application.candidateEmail}" /></p></div></div><dl class="row small mb-4"><dt class="col-5">Vị trí</dt><dd class="col-7"><c:out value="${interview.jobTitle}" /></dd><dt class="col-5">Ngày</dt><dd class="col-7"><c:out value="${interview.interviewDate}" /></dd><dt class="col-5">Thời gian</dt><dd class="col-7"><c:out value="${interview.startTime}" /> – <c:out value="${interview.endTime}" /></dd><dt class="col-5">Hình thức</dt><dd class="col-7"><c:out value="${interview.interviewType}" /></dd><dt class="col-5">Địa điểm</dt><dd class="col-7"><c:out value="${interview.location}" /></dd></dl><div class="border-top pt-3 mb-3"><h6 class="small text-uppercase text-muted">Hồ sơ ứng viên</h6><p class="small mb-1"><strong>Học vấn:</strong> <c:out value="${profile.university}" /> <c:if test="${not empty profile.major}">· <c:out value="${profile.major}" /></c:if></p><p class="small mb-1"><strong>Kinh nghiệm:</strong> <c:out value="${profile.experienceYears}" /> năm</p><c:if test="${not empty profile.skills}"><p class="small mb-1"><strong>Kỹ năng:</strong> <c:out value="${profile.skills}" /></p></c:if><c:if test="${not empty profile.summary}"><p class="small text-muted mb-0"><c:out value="${profile.summary}" /></p></c:if></div><a class="btn btn-outline-primary w-100" href="${pageContext.request.contextPath}/interviewer/resumes/download?interviewId=${interview.id}"><i class="bi bi-file-earmark-pdf me-1"></i> Xem CV ứng viên</a><c:if test="${not empty interview.note}"><hr><p class="small text-muted mb-1">Ghi chú từ HR</p><p class="mb-0"><c:out value="${interview.note}" /></p></c:if></div></section></div>
                    <div class="col-xl-8"><section class="card"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Đánh giá phỏng vấn</h5><small class="text-muted">Điểm tổng được tính tự động từ 4 tiêu chí. Hãy gửi feedback sau khi buổi phỏng vấn kết thúc.</small></div><div class="card-body p-4">
                        <c:choose>
                            <c:when test="${interview.status eq 'SCHEDULED' or interview.status eq 'RESCHEDULED'}">
                                <form id="feedbackForm" method="post" action="${pageContext.request.contextPath}/interviewer/interviews/feedback">
                                    <input type="hidden" name="interviewId" value="${interview.id}">
                                    <div class="row g-4">
                                        <div class="col-md-6"><label class="form-label fw-semibold" for="technicalScore">Kỹ thuật <span id="technicalScoreValue" class="badge text-bg-light border">5</span></label><input class="form-range" id="technicalScore" name="technicalScore" min="0" max="10" step="1" type="range" value="5"></div>
                                        <div class="col-md-6"><label class="form-label fw-semibold" for="communicationScore">Giao tiếp <span id="communicationScoreValue" class="badge text-bg-light border">5</span></label><input class="form-range" id="communicationScore" name="communicationScore" min="0" max="10" step="1" type="range" value="5"></div>
                                        <div class="col-md-6"><label class="form-label fw-semibold" for="experienceScore">Kinh nghiệm <span id="experienceScoreValue" class="badge text-bg-light border">5</span></label><input class="form-range" id="experienceScore" name="experienceScore" min="0" max="10" step="1" type="range" value="5"></div>
                                        <div class="col-md-6"><label class="form-label fw-semibold" for="attitudeScore">Thái độ <span id="attitudeScoreValue" class="badge text-bg-light border">5</span></label><input class="form-range" id="attitudeScore" name="attitudeScore" min="0" max="10" step="1" type="range" value="5"></div>
                                        <div class="col-md-4"><label class="form-label fw-semibold" for="overallScore">Điểm tổng</label><input class="form-control bg-light fw-bold" id="overallScore" name="overallScore" readonly value="5.00"></div>
                                        <div class="col-md-8"><label class="form-label fw-semibold" for="recommendation">Đề xuất <span class="text-danger">*</span></label><select class="form-select" id="recommendation" name="recommendation" required><option value="">-- Chọn đề xuất --</option><option value="STRONG_HIRE">STRONG_HIRE — Rất nên tuyển</option><option value="HIRE">HIRE — Nên tuyển</option><option value="CONSIDER">CONSIDER — Cần cân nhắc</option><option value="NO_HIRE">NO_HIRE — Không nên tuyển</option></select></div>
                                        <div class="col-12"><label class="form-label fw-semibold" for="comment">Nhận xét chi tiết</label><textarea class="form-control" id="comment" name="comment" rows="6" required placeholder="Nêu rõ điểm mạnh, điểm cần cải thiện và lý do cho đề xuất của bạn."></textarea></div>
                                        <div class="col-12 d-flex justify-content-end"><button class="btn btn-primary px-4" type="submit"><i class="bi bi-send-check me-1"></i> Gửi feedback</button></div>
                                    </div>
                                </form>
                            </c:when>
                            <c:otherwise><div class="text-center py-5"><i class="bi bi-check2-circle text-success fs-1 d-block mb-3"></i><h5>Buổi phỏng vấn đã được xử lý</h5><p class="text-muted mb-0">Feedback không thể thay đổi từ màn hình này.</p></div></c:otherwise>
                        </c:choose>
                    </div></section></div>
                </div>
            </div>
        </main>
    </div>
</div>
<script>
    (function () {
        ['technicalScore', 'communicationScore', 'experienceScore', 'attitudeScore'].forEach(function (id) {
            const input = document.getElementById(id);
            if (input) input.addEventListener('input', updateOverall);
        });
        function updateOverall() {
            const scores = ['technicalScore', 'communicationScore', 'experienceScore', 'attitudeScore']
                .map(function (id) { return Number(document.getElementById(id).value); });
            const average = scores.reduce(function (total, score) { return total + score; }, 0) / scores.length;
            scores.forEach(function (score, index) {
                const ids = ['technicalScoreValue', 'communicationScoreValue', 'experienceScoreValue', 'attitudeScoreValue'];
                document.getElementById(ids[index]).textContent = score;
            });
            document.getElementById('overallScore').value = average.toFixed(2);
        }
    }());
</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
