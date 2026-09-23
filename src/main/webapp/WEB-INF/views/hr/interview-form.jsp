<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isEdit" value="${not empty interview}" />
<c:set var="pageTitle" value="${isEdit ? 'Đổi lịch phỏng vấn' : 'Lên lịch phỏng vấn'} | JobCV" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Tuyển dụng</p><h4 class="mb-0">${isEdit ? 'Đổi lịch phỏng vấn' : 'Lên lịch phỏng vấn'}</h4></div><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/hr/interviews"><i class="bi bi-arrow-left me-1"></i> Quay lại</a></div>
            <div class="main-content">
                <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}" /></div></c:if>
                <section class="card"><div class="card-body p-4 p-lg-5">
                    <form class="row g-3" method="post" action="${pageContext.request.contextPath}${isEdit ? '/hr/interviews/update' : '/hr/interviews/create'}">
                        <c:if test="${isEdit}"><input type="hidden" name="id" value="${interview.id}"></c:if>
                        <div class="col-md-6">
                            <label class="form-label fw-semibold" for="applicationId">Đơn ứng tuyển <span class="text-danger">*</span></label>
                            <c:choose>
                                <c:when test="${isEdit}">
                                    <input type="hidden" name="applicationId" value="${interview.applicationId}">
                                    <div class="form-control bg-light"><c:out value="${interview.candidateName}" /> — <c:out value="${interview.jobTitle}" /></div>
                                    <div class="form-text">Không thể đổi ứng viên khi cập nhật lịch phỏng vấn.</div>
                                </c:when>
                                <c:otherwise>
                                    <select class="form-select" id="applicationId" name="applicationId" required>
                                        <option value="">-- Chọn ứng viên --</option>
                                        <c:forEach var="item" items="${applications}">
                                            <option value="${item.id}" <c:if test="${item.id eq application.id}">selected</c:if>><c:out value="${item.candidateName}" /> — <c:out value="${item.jobTitle}" /></option>
                                        </c:forEach>
                                    </select>
                                    <div class="form-text">Chỉ hiển thị hồ sơ ở trạng thái SHORTLISTED.</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="col-md-6"><label class="form-label fw-semibold" for="interviewerId">Người phỏng vấn <span class="text-danger">*</span></label><select class="form-select" id="interviewerId" name="interviewerId" required><option value="">-- Chọn người phỏng vấn --</option><c:forEach var="interviewer" items="${interviewers}"><option value="${interviewer.id}" <c:if test="${interviewer.id eq interview.interviewerId}">selected</c:if>><c:out value="${interviewer.fullName}" /> · <c:out value="${interviewer.email}" /></option></c:forEach></select></div>
                        <div class="col-md-4"><label class="form-label fw-semibold" for="interviewType">Hình thức <span class="text-danger">*</span></label><select class="form-select" id="interviewType" name="interviewType" required><option value="ONLINE" <c:if test="${interview.interviewType eq 'ONLINE'}">selected</c:if>>Trực tuyến</option><option value="OFFLINE" <c:if test="${interview.interviewType eq 'OFFLINE'}">selected</c:if>>Trực tiếp</option></select></div>
                        <div class="col-md-4"><label class="form-label fw-semibold" for="interviewDate">Ngày phỏng vấn <span class="text-danger">*</span></label><input class="form-control" id="interviewDate" type="date" name="interviewDate" required value="<c:out value='${interview.interviewDate}'/>"></div>
                        <div class="col-md-2"><label class="form-label fw-semibold" for="startTime">Bắt đầu <span class="text-danger">*</span></label><input class="form-control" id="startTime" type="time" name="startTime" required value="<c:out value='${interview.startTime}'/>"></div>
                        <div class="col-md-2"><label class="form-label fw-semibold" for="endTime">Kết thúc <span class="text-danger">*</span></label><input class="form-control" id="endTime" type="time" name="endTime" required value="<c:out value='${interview.endTime}'/>"></div>
                        <div class="col-md-6"><label class="form-label fw-semibold" for="location">Địa điểm</label><input class="form-control" id="location" name="location" maxlength="255" value="<c:out value='${interview.location}'/>" placeholder="Phòng họp hoặc địa chỉ văn phòng"></div>
                        <div class="col-md-6"><label class="form-label fw-semibold" for="meetingUrl">Liên kết cuộc họp</label><input class="form-control" id="meetingUrl" name="meetingUrl" type="url" maxlength="255" value="<c:out value='${interview.meetingUrl}'/>" placeholder="https://..."></div>
                        <div class="col-12"><label class="form-label fw-semibold" for="note">Ghi chú cho buổi phỏng vấn</label><textarea class="form-control" id="note" name="note" rows="4"><c:out value="${interview.note}" /></textarea></div>
                        <div class="col-12"><div class="alert alert-info mb-0"><i class="bi bi-info-circle me-1"></i> Hệ thống sẽ kiểm tra lịch trùng của người phỏng vấn và chỉ cho phép thời gian bắt đầu trước thời gian kết thúc.</div></div>
                        <div class="col-12 d-flex justify-content-end gap-2 pt-2"><a class="btn btn-light" href="${pageContext.request.contextPath}/hr/interviews">Hủy</a><button class="btn btn-primary px-4" type="submit"><i class="bi bi-calendar-check me-1"></i> ${isEdit ? 'Lưu lịch mới' : 'Tạo lịch phỏng vấn'}</button></div>
                    </form>
                </div></section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

