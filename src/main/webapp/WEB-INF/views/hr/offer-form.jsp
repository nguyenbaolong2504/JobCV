<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isEdit" value="${not empty offer}" />
<c:set var="pageTitle" value="${isEdit ? 'Chỉnh sửa thư mời' : 'Tạo thư mời'} | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Tuyển dụng</p><h4 class="mb-0">${isEdit ? 'Chỉnh sửa thư mời' : 'Tạo thư mời'}</h4></div><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/hr/offers"><i class="bi bi-arrow-left me-1"></i> Quay lại</a></div>
            <div class="main-content">
                <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}" /></div></c:if>
                <section class="card"><div class="card-body p-4 p-lg-5"><form class="row g-3" method="post" action="${pageContext.request.contextPath}${isEdit ? '/hr/offers/update' : '/hr/offers/create'}">
                    <c:if test="${isEdit}"><input type="hidden" name="id" value="${offer.id}"></c:if>
                    <div class="col-12">
                        <label class="form-label fw-semibold" for="applicationId">Ứng viên <span class="text-danger">*</span></label>
                        <c:choose>
                            <c:when test="${isEdit}">
                                <input type="hidden" name="applicationId" value="${offer.applicationId}">
                                <div class="form-control bg-light"><c:out value="${offer.candidateName}" /> — <c:out value="${offer.jobTitle}" /></div>
                                <div class="form-text">Không thể đổi ứng viên của một thư mời đã tạo.</div>
                            </c:when>
                            <c:otherwise>
                                <select class="form-select" id="applicationId" name="applicationId" required>
                                    <option value="">-- Chọn ứng viên đã phỏng vấn --</option>
                                    <c:forEach var="item" items="${applications}">
                                        <option value="${item.id}" <c:if test="${item.id eq application.id}">selected</c:if>><c:out value="${item.candidateName}" /> — <c:out value="${item.jobTitle}" /></option>
                                    </c:forEach>
                                </select>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="col-md-6"><label class="form-label fw-semibold" for="salary">Mức lương (VND) <span class="text-danger">*</span></label><input class="form-control" id="salary" name="salary" min="0" step="0.01" required type="number" value="<c:out value='${offer.salary}'/>"></div>
                    <div class="col-md-3"><label class="form-label fw-semibold" for="startDate">Ngày bắt đầu <span class="text-danger">*</span></label><input class="form-control" id="startDate" name="startDate" required type="date" value="<c:out value='${offer.startDate}'/>"></div>
                    <div class="col-md-3"><label class="form-label fw-semibold" for="probationMonths">Thử việc (tháng)</label><input class="form-control" id="probationMonths" name="probationMonths" min="0" max="24" type="number" value="<c:out value='${offer.probationMonths}'/>"></div>
                    <div class="col-md-6"><label class="form-label fw-semibold" for="location">Địa điểm làm việc</label><input class="form-control" id="location" name="location" maxlength="255" value="<c:out value='${offer.location}'/>"></div>
                    <div class="col-md-6"><label class="form-label fw-semibold" for="expiryDate">Hạn phản hồi <span class="text-danger">*</span></label><input class="form-control" id="expiryDate" name="expiryDate" required type="date" value="<c:out value='${offer.expiryDate}'/>"></div>
                    <div class="col-12"><label class="form-label fw-semibold" for="note">Ghi chú / điều khoản bổ sung</label><textarea class="form-control" id="note" name="note" rows="5"><c:out value="${offer.note}" /></textarea></div>
                    <div class="col-12"><div class="alert alert-info mb-0"><i class="bi bi-info-circle me-1"></i> Lưu để tạo bản nháp. Chỉ khi chọn <strong>Gửi thư mời</strong>, ứng viên mới nhận được thông báo và đơn ứng tuyển chuyển sang trạng thái đã gửi thư mời.</div></div>
                    <div class="col-12 d-flex justify-content-end gap-2 pt-2"><a class="btn btn-light" href="${pageContext.request.contextPath}/hr/offers">Hủy</a><button class="btn btn-primary px-4" type="submit"><i class="bi bi-save me-1"></i> Lưu thư mời</button></div>
                </form></div></section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

