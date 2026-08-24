<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isEdit" value="${not empty job}" />
<c:set var="pageTitle" value="${isEdit ? 'Chỉnh sửa tin tuyển dụng' : 'Tạo tin tuyển dụng'} | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><p class="text-muted small mb-1">Tuyển dụng</p><h4 class="mb-0">${isEdit ? 'Chỉnh sửa tin tuyển dụng' : 'Tạo tin tuyển dụng'}</h4></div>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/hr/jobs"><i class="bi bi-arrow-left me-1"></i> Quay lại</a>
            </div>
            <div class="main-content">
                <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}" /></div></c:if>
                <section class="card">
                    <div class="card-body p-4 p-lg-5">
                        <form action="${pageContext.request.contextPath}${isEdit ? '/hr/jobs/update' : '/hr/jobs/create'}" method="post" class="row g-3">
                            <c:if test="${isEdit}"><input type="hidden" name="id" value="${job.id}"></c:if>
                            <div class="col-md-4"><label class="form-label fw-semibold" for="jobCode">Mã tin <span class="text-danger">*</span></label><input class="form-control" id="jobCode" name="jobCode" required maxlength="50" value="<c:out value='${job.jobCode}'/>"></div>
                            <div class="col-md-8"><label class="form-label fw-semibold" for="title">Tiêu đề công việc <span class="text-danger">*</span></label><input class="form-control" id="title" name="title" required maxlength="255" value="<c:out value='${job.title}'/>"></div>
                            <div class="col-md-4"><label class="form-label fw-semibold" for="departmentId">Phòng ban <span class="text-danger">*</span></label><select class="form-select" id="departmentId" name="departmentId" required><option value="">-- Chọn phòng ban --</option><c:forEach var="department" items="${departments}"><option value="${department.id}" <c:if test="${department.id eq job.departmentId}">selected</c:if>><c:out value="${department.displayName}" /></option></c:forEach></select></div>
                            <div class="col-md-4"><label class="form-label fw-semibold" for="location">Địa điểm <span class="text-danger">*</span></label><input class="form-control" id="location" name="location" required maxlength="255" value="<c:out value='${job.location}'/>"></div>
                            <div class="col-md-4"><label class="form-label fw-semibold" for="employmentType">Hình thức <span class="text-danger">*</span></label><select class="form-select" id="employmentType" name="employmentType" required><option value="FULL_TIME" <c:if test="${job.employmentType eq 'FULL_TIME'}">selected</c:if>>Toàn thời gian</option><option value="PART_TIME" <c:if test="${job.employmentType eq 'PART_TIME'}">selected</c:if>>Bán thời gian</option><option value="INTERNSHIP" <c:if test="${job.employmentType eq 'INTERNSHIP'}">selected</c:if>>Thực tập</option><option value="CONTRACT" <c:if test="${job.employmentType eq 'CONTRACT'}">selected</c:if>>Hợp đồng</option><option value="REMOTE" <c:if test="${job.employmentType eq 'REMOTE'}">selected</c:if>>Từ xa</option></select></div>
                            <div class="col-md-3"><label class="form-label fw-semibold" for="numberOfPositions">Số lượng <span class="text-danger">*</span></label><input class="form-control" id="numberOfPositions" name="numberOfPositions" type="number" min="1" required value="<c:out value='${job.numberOfPositions}'/>"></div>
                            <div class="col-md-3"><label class="form-label fw-semibold" for="experienceRequired">Kinh nghiệm (năm)</label><input class="form-control" id="experienceRequired" name="experienceRequired" type="number" min="0" value="<c:out value='${job.experienceRequired}'/>"></div>
                            <div class="col-md-3"><label class="form-label fw-semibold" for="salaryMin">Lương tối thiểu</label><input class="form-control" id="salaryMin" name="salaryMin" type="number" min="0" step="0.01" required value="<c:out value='${job.salaryMin}'/>"></div>
                            <div class="col-md-3"><label class="form-label fw-semibold" for="salaryMax">Lương tối đa</label><input class="form-control" id="salaryMax" name="salaryMax" type="number" min="0" step="0.01" required value="<c:out value='${job.salaryMax}'/>"></div>
                            <div class="col-md-6"><label class="form-label fw-semibold" for="deadline">Hạn nhận hồ sơ <span class="text-danger">*</span></label><input class="form-control" id="deadline" name="deadline" type="date" required value="<c:out value='${job.deadline}'/>"></div>
                            <div class="col-md-6"><label class="form-label fw-semibold" for="skills">Kỹ năng và trọng số</label><input class="form-control" id="skills" name="skills" value="<c:out value='${skillsText}'/>" placeholder="Java:5, JDBC:4, MySQL:4, Git:2"><div class="form-text">Nhập theo dạng Kỹ năng:trọng số, phân tách bằng dấu phẩy.</div></div>
                            <div class="col-12"><label class="form-label fw-semibold" for="description">Mô tả công việc <span class="text-danger">*</span></label><textarea class="form-control" id="description" name="description" rows="6" required><c:out value="${job.description}" /></textarea></div>
                            <div class="col-12"><label class="form-label fw-semibold" for="requirements">Yêu cầu ứng viên <span class="text-danger">*</span></label><textarea class="form-control" id="requirements" name="requirements" rows="6" required><c:out value="${job.requirements}" /></textarea></div>
                            <div class="col-12 d-flex justify-content-end gap-2 pt-3"><a class="btn btn-light" href="${pageContext.request.contextPath}/hr/jobs">Hủy</a><button class="btn btn-primary px-4" type="submit"><i class="bi bi-save me-1"></i> ${isEdit ? 'Lưu thay đổi' : 'Lưu bản nháp'}</button></div>
                        </form>
                    </div>
                </section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

