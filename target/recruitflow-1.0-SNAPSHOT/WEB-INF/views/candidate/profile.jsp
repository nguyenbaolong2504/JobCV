<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Hồ sơ ứng viên | RecruitFlow" scope="request" />
<c:set var="avatarInitial" value="${fn:toUpperCase(fn:substring(user.fullName, 0, 1))}" />
<c:url var="profileAvatarUrl" value="/candidate/avatar"><c:param name="v" value="${profile.updatedAt.time}" /></c:url>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell"><div class="row g-0">
    <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />
    <main class="candidate-main profile-page col-lg-9 col-xl-10">
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <div class="profile-page-heading d-flex flex-wrap justify-content-between gap-3 mb-4"><div><span class="profile-heading-kicker"><i class="bi bi-patch-check-fill"></i>Hồ sơ nghề nghiệp</span><h1 class="page-title mb-1">Hồ sơ cá nhân</h1><p class="text-muted mb-0">Cập nhật thông tin để nhà tuyển dụng hiểu rõ hơn về bạn.</p></div><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-file-earmark-person me-1"></i>Quản lý CV</a></div>
        <div class="row g-4">
            <div class="col-xl-4"><aside class="content-card text-center">
                <div class="profile-avatar-editor mb-3">
                    <div class="profile-avatar-preview">
                        <img id="candidateAvatarPreview" class="profile-avatar-image ${empty profile.avatarPath ? 'd-none' : ''}" src="${empty profile.avatarPath ? 'data:image/gif;base64,R0lGODlhAQABAAAAACw=' : profileAvatarUrl}" alt="Ảnh đại diện của ${fn:escapeXml(user.fullName)}" data-avatar-image data-fallback-target="candidateAvatarFallback">
                        <span id="candidateAvatarFallback" class="profile-avatar ${empty profile.avatarPath ? '' : 'd-none'}" data-avatar-fallback><c:out value="${empty avatarInitial ? 'U' : avatarInitial}" /></span>
                        <label class="profile-avatar-edit" for="avatarFile" title="Chọn ảnh đại diện"><i class="bi bi-camera-fill" aria-hidden="true"></i><span class="visually-hidden">Chọn ảnh đại diện</span></label>
                    </div>
                </div>
                <h2 class="h5 fw-bold mb-1"><c:out value="${user.fullName}" /></h2><p class="text-muted small mb-3"><c:out value="${user.email}" /></p>
                <form id="avatarUploadForm" action="${pageContext.request.contextPath}/candidate/profile/avatar" method="post" enctype="multipart/form-data" class="avatar-upload-form">
                    <input class="visually-hidden" id="avatarFile" name="avatarFile" type="file" accept="image/jpeg,image/png,image/webp" data-avatar-upload data-preview-target="candidateAvatarPreview" data-fallback-target="candidateAvatarFallback" data-feedback-target="avatarFeedback" data-name-target="avatarFileName" data-save-target="avatarSaveButton">
                    <label class="btn btn-outline-primary btn-sm" for="avatarFile"><i class="bi bi-image me-1"></i>Chọn ảnh</label>
                    <button class="btn btn-primary btn-sm" id="avatarSaveButton" type="submit" disabled data-loading-button><i class="bi bi-cloud-arrow-up me-1"></i>Lưu ảnh</button>
                    <span class="small text-muted text-truncate w-100" id="avatarFileName">JPG, PNG hoặc WEBP · tối đa 2 MB</span>
                    <span class="small text-danger d-none w-100" id="avatarFeedback" role="alert"></span>
                </form>
                <c:if test="${not empty profile.avatarPath}"><form class="mt-2" action="${pageContext.request.contextPath}/candidate/profile/avatar" method="post" data-confirm="Bạn có chắc muốn xóa ảnh đại diện?"><input type="hidden" name="removeAvatar" value="true"><button class="btn btn-link btn-sm text-danger text-decoration-none" type="submit"><i class="bi bi-trash3 me-1"></i>Xóa ảnh hiện tại</button></form></c:if>
                <div class="border-top pt-3 mt-3 text-start"><p class="small fw-semibold mb-1">Mẹo hoàn thiện hồ sơ</p><p class="small text-muted mb-0">Bổ sung học vấn, kỹ năng, kinh nghiệm và tải CV để tăng chất lượng gợi ý công việc.</p></div>
            </aside></div>
            <div class="col-xl-8"><section class="content-card"><form action="${pageContext.request.contextPath}/candidate/profile" method="post"><h2 class="h5 fw-bold mb-3">Thông tin cơ bản</h2><div class="row g-3">
                <div class="col-md-6"><label class="form-label" for="fullName">Họ và tên <span class="text-danger">*</span></label><input class="form-control" id="fullName" name="fullName" required maxlength="100" value="<c:out value='${user.fullName}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="email">Email</label><input class="form-control" id="email" type="email" value="<c:out value='${user.email}'/>" readonly></div>
                <div class="col-md-6"><label class="form-label" for="phone">Số điện thoại</label><input class="form-control" id="phone" name="phone" type="tel" maxlength="20" pattern="(?:\\+84|0)[0-9]{9,10}" value="<c:out value='${profile.phone}'/>" placeholder="0912345678"><div class="invalid-feedback">Nhập số điện thoại Việt Nam hợp lệ.</div></div>
                <div class="col-md-6"><label class="form-label" for="dateOfBirth">Ngày sinh</label><input class="form-control" id="dateOfBirth" name="dateOfBirth" type="date" value="<c:out value='${profile.dateOfBirth}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="gender">Giới tính</label><select class="form-select" id="gender" name="gender"><option value="">Chọn giới tính</option><option value="MALE" ${profile.gender eq 'MALE' ? 'selected' : ''}>Nam</option><option value="FEMALE" ${profile.gender eq 'FEMALE' ? 'selected' : ''}>Nữ</option><option value="OTHER" ${profile.gender eq 'OTHER' ? 'selected' : ''}>Khác</option></select></div>
                <div class="col-12"><label class="form-label" for="address">Địa chỉ</label><input class="form-control" id="address" name="address" maxlength="255" value="<c:out value='${profile.address}'/>" placeholder="Quận/Huyện, Tỉnh/Thành phố"></div>
            </div><hr class="my-4"><div class="profile-section-heading"><span><i class="bi bi-bullseye"></i></span><div><h2 class="h5 fw-bold mb-1">Mục tiêu tìm việc</h2><p class="small text-muted mb-0">Thông tin này giúp hệ thống ưu tiên các cơ hội phù hợp hơn.</p></div></div><div class="row g-3">
                <div class="col-md-6"><label class="form-label" for="targetPosition">Vị trí mong muốn</label><input class="form-control" id="targetPosition" name="targetPosition" maxlength="150" value="<c:out value='${profile.targetPosition}'/>" placeholder="Ví dụ: Java Developer"></div>
                <div class="col-md-6"><label class="form-label" for="targetLocation">Địa điểm mong muốn</label><input class="form-control" id="targetLocation" name="targetLocation" maxlength="100" value="<c:out value='${profile.targetLocation}'/>" placeholder="Hà Nội, TP.HCM hoặc Remote"></div>
                <div class="col-md-6"><label class="form-label" for="expectedSalary">Mức lương mong muốn (VNĐ/tháng)</label><div class="input-group"><span class="input-group-text"><i class="bi bi-cash-stack"></i></span><input class="form-control" id="expectedSalary" name="expectedSalary" type="number" min="0" max="1000000000" step="500000" value="<c:out value='${profile.expectedSalary}'/>" placeholder="15000000"></div></div>
                <div class="col-12"><label class="form-label" for="careerGoal">Mục tiêu nghề nghiệp</label><textarea class="form-control" id="careerGoal" name="careerGoal" rows="3" maxlength="3000" placeholder="Mục tiêu trong 1–3 năm tới và giá trị bạn muốn tạo ra..."><c:out value="${profile.careerGoal}" /></textarea></div>
            </div><hr class="my-4"><div class="profile-section-heading"><span><i class="bi bi-mortarboard"></i></span><div><h2 class="h5 fw-bold mb-1">Học vấn và kinh nghiệm</h2><p class="small text-muted mb-0">Trình bày ngắn gọn, ưu tiên thông tin liên quan đến công việc.</p></div></div><div class="row g-3">
                <div class="col-md-6"><label class="form-label" for="university">Trường đại học</label><input class="form-control" id="university" name="university" maxlength="150" value="<c:out value='${profile.university}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="major">Chuyên ngành</label><input class="form-control" id="major" name="major" maxlength="100" value="<c:out value='${profile.major}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="experienceYears">Số năm kinh nghiệm</label><input class="form-control" id="experienceYears" name="experienceYears" type="number" min="0" max="80" step="1" value="<c:out value='${profile.experienceYears}'/>"></div>
                <div class="col-12"><label class="form-label" for="skills">Kỹ năng</label><input class="form-control" id="skills" name="skills" maxlength="1000" value="<c:out value='${profile.skills}'/>" placeholder="Ví dụ: Java, JDBC, MySQL, Git"></div>
                <div class="col-12"><label class="form-label" for="certificates">Chứng chỉ và thành tích</label><textarea class="form-control" id="certificates" name="certificates" rows="3" maxlength="2000" placeholder="Mỗi chứng chỉ hoặc thành tích trên một dòng"><c:out value="${profile.certificates}" /></textarea></div>
                <div class="col-12"><label class="form-label" for="summary">Giới thiệu ngắn</label><textarea class="form-control" id="summary" name="summary" rows="5" maxlength="3000" placeholder="Tóm tắt kinh nghiệm, mục tiêu nghề nghiệp và điểm mạnh của bạn."><c:out value="${profile.summary}" /></textarea></div>
            </div><div class="d-flex justify-content-end mt-4"><button class="btn btn-primary" type="submit" data-loading-button><i class="bi bi-check2 me-1"></i>Lưu hồ sơ</button></div></form></section></div>
        </div>
    </main>
</div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
