<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Hồ sơ ứng viên | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell"><div class="row g-0">
    <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />
    <main class="candidate-main col-lg-9 col-xl-10">
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <div class="d-flex flex-wrap justify-content-between gap-3 mb-4"><div><h1 class="page-title mb-1">Hồ sơ cá nhân</h1><p class="text-muted mb-0">Cập nhật thông tin để nhà tuyển dụng hiểu rõ hơn về bạn.</p></div><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-file-earmark-person me-1"></i>Quản lý CV</a></div>
        <div class="row g-4">
            <div class="col-xl-4"><aside class="content-card text-center"><span class="profile-avatar mb-3">C</span><h2 class="h5 fw-bold mb-1"><c:out value="${user.fullName}" /></h2><p class="text-muted small mb-3"><c:out value="${user.email}" /></p><div class="border-top pt-3 text-start"><p class="small fw-semibold mb-1">Mẹo hoàn thiện hồ sơ</p><p class="small text-muted mb-0">Bổ sung học vấn, kỹ năng, kinh nghiệm và tải CV để tăng chất lượng gợi ý công việc.</p></div></aside></div>
            <div class="col-xl-8"><section class="content-card"><form action="${pageContext.request.contextPath}/candidate/profile" method="post"><h2 class="h5 fw-bold mb-3">Thông tin cơ bản</h2><div class="row g-3">
                <div class="col-md-6"><label class="form-label" for="fullName">Họ và tên <span class="text-danger">*</span></label><input class="form-control" id="fullName" name="fullName" required maxlength="100" value="<c:out value='${user.fullName}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="email">Email</label><input class="form-control" id="email" type="email" value="<c:out value='${user.email}'/>" readonly></div>
                <div class="col-md-6"><label class="form-label" for="dateOfBirth">Ngày sinh</label><input class="form-control" id="dateOfBirth" name="dateOfBirth" type="date" value="<c:out value='${profile.dateOfBirth}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="gender">Giới tính</label><select class="form-select" id="gender" name="gender"><option value="">Chọn giới tính</option><option value="MALE" ${profile.gender eq 'MALE' ? 'selected' : ''}>Nam</option><option value="FEMALE" ${profile.gender eq 'FEMALE' ? 'selected' : ''}>Nữ</option><option value="OTHER" ${profile.gender eq 'OTHER' ? 'selected' : ''}>Khác</option></select></div>
                <div class="col-12"><label class="form-label" for="address">Địa chỉ</label><input class="form-control" id="address" name="address" maxlength="255" value="<c:out value='${profile.address}'/>" placeholder="Quận/Huyện, Tỉnh/Thành phố"></div>
            </div><hr class="my-4"><h2 class="h5 fw-bold mb-3">Học vấn và kinh nghiệm</h2><div class="row g-3">
                <div class="col-md-6"><label class="form-label" for="university">Trường đại học</label><input class="form-control" id="university" name="university" maxlength="150" value="<c:out value='${profile.university}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="major">Chuyên ngành</label><input class="form-control" id="major" name="major" maxlength="100" value="<c:out value='${profile.major}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="experienceYears">Số năm kinh nghiệm</label><input class="form-control" id="experienceYears" name="experienceYears" type="number" min="0" max="80" step="1" value="<c:out value='${profile.experienceYears}'/>"></div>
                <div class="col-12"><label class="form-label" for="skills">Kỹ năng</label><input class="form-control" id="skills" name="skills" maxlength="1000" value="<c:out value='${profile.skills}'/>" placeholder="Ví dụ: Java, JDBC, MySQL, Git"></div>
                <div class="col-12"><label class="form-label" for="summary">Giới thiệu ngắn</label><textarea class="form-control" id="summary" name="summary" rows="5" maxlength="3000" placeholder="Tóm tắt kinh nghiệm, mục tiêu nghề nghiệp và điểm mạnh của bạn."><c:out value="${profile.summary}" /></textarea></div>
            </div><div class="d-flex justify-content-end mt-4"><button class="btn btn-primary" type="submit" data-loading-button><i class="bi bi-check2 me-1"></i>Lưu hồ sơ</button></div></form></section></div>
        </div>
    </main>
</div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
