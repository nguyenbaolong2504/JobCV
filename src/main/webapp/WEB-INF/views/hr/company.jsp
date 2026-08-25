<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Hồ sơ doanh nghiệp | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="app-layout">
    <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
    <main class="app-main bg-light">
        <div class="topbar"><div><p class="text-muted small mb-1">Thương hiệu tuyển dụng</p><h1 class="h4 mb-0">Hồ sơ doanh nghiệp</h1></div><c:if test="${not empty company and company.accountStatus eq 'ACTIVE' and not empty company.description}"><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/companies/detail?id=${company.id}" target="_blank" rel="noopener"><i class="bi bi-box-arrow-up-right me-1"></i>Xem trang công khai</a></c:if></div>
        <div class="main-content company-editor-page">
            <jsp:include page="/WEB-INF/views/common/flash.jsp" />
            <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}" /></div></c:if>
            <c:if test="${not empty profile}">
                <section class="company-editor-preview mb-4">
                    <div class="company-editor-cover"><c:if test="${not empty profile.coverPath}"><img src="${pageContext.request.contextPath}/companies/media?id=${company.id}&type=cover" alt="Ảnh bìa ${company.name}"></c:if></div>
                    <div class="company-editor-brand"><span><img src="${pageContext.request.contextPath}/companies/media?id=${company.id}&type=logo" alt="Logo ${company.name}"></span><div><small>TRANG TUYỂN DỤNG CÔNG KHAI</small><h2><c:out value="${profile.organizationName}" /></h2><p><c:out value="${empty profile.industry ? 'Hãy bổ sung lĩnh vực hoạt động' : profile.industry}" /></p></div><c:if test="${profile.verified}"><b><i class="bi bi-patch-check-fill"></i>Đã xác thực</b></c:if></div>
                </section>
                <form class="company-editor-form" action="${pageContext.request.contextPath}/hr/company" method="post" enctype="multipart/form-data" data-validate-form>
                    <div class="row g-4">
                        <div class="col-xl-8"><section class="content-card h-100"><div class="profile-section-heading"><span><i class="bi bi-buildings"></i></span><div><h2 class="h5 mb-1">Thông tin thương hiệu</h2><p class="small text-muted mb-0">Thông tin này xuất hiện trên trang công ty và trong tin tuyển dụng.</p></div></div><div class="row g-3 mt-1">
                            <div class="col-md-7"><label class="form-label" for="organizationName">Tên doanh nghiệp <span class="text-danger">*</span></label><input class="form-control" id="organizationName" name="organizationName" required minlength="2" maxlength="150" value="<c:out value='${profile.organizationName}'/>"></div>
                            <div class="col-md-5"><label class="form-label" for="industry">Lĩnh vực hoạt động</label><input class="form-control" id="industry" name="industry" maxlength="120" value="<c:out value='${profile.industry}'/>" placeholder="Ví dụ: IT - Phần mềm"></div>
                            <div class="col-md-5"><label class="form-label" for="companySize">Quy mô</label><select class="form-select" id="companySize" name="companySize"><option value="">Chọn quy mô</option><option value="Dưới 25 nhân sự" ${profile.companySize eq 'Dưới 25 nhân sự' ? 'selected' : ''}>Dưới 25 nhân sự</option><option value="25–99 nhân sự" ${profile.companySize eq '25–99 nhân sự' ? 'selected' : ''}>25–99 nhân sự</option><option value="100–500 nhân sự" ${profile.companySize eq '100–500 nhân sự' ? 'selected' : ''}>100–500 nhân sự</option><option value="Trên 500 nhân sự" ${profile.companySize eq 'Trên 500 nhân sự' ? 'selected' : ''}>Trên 500 nhân sự</option></select></div>
                            <div class="col-md-7"><label class="form-label" for="website">Website</label><input class="form-control" id="website" name="website" type="url" maxlength="255" value="<c:out value='${profile.website}'/>" placeholder="https://company.vn"></div>
                            <div class="col-12"><label class="form-label" for="address">Địa chỉ / khu vực làm việc</label><input class="form-control" id="address" name="address" maxlength="255" value="<c:out value='${profile.address}'/>" placeholder="Hà Nội, TP.HCM hoặc địa chỉ văn phòng"></div>
                            <div class="col-12"><label class="form-label" for="description">Giới thiệu doanh nghiệp</label><textarea class="form-control" id="description" name="description" rows="8" maxlength="5000" placeholder="Sứ mệnh, sản phẩm, môi trường và cơ hội phát triển..."><c:out value="${profile.description}" /></textarea><div class="form-text">Nội dung rõ ràng giúp ứng viên hiểu môi trường trước khi ứng tuyển.</div></div>
                        </div></section></div>
                        <div class="col-xl-4"><div class="vstack gap-4"><section class="content-card"><h2 class="h5 mb-3">Người liên hệ</h2><div class="mb-3"><label class="form-label" for="jobTitle">Chức danh <span class="text-danger">*</span></label><input class="form-control" id="jobTitle" name="jobTitle" required maxlength="100" value="<c:out value='${profile.jobTitle}'/>"></div><div><label class="form-label" for="workPhone">Số điện thoại công việc <span class="text-danger">*</span></label><input class="form-control" id="workPhone" name="workPhone" required maxlength="30" pattern="[0-9+() .-]{6,30}" value="<c:out value='${profile.workPhone}'/>"></div></section>
                            <section class="content-card"><h2 class="h5 mb-1">Nhận diện doanh nghiệp</h2><p class="small text-muted">JPG, PNG hoặc WEBP · tối đa 2 MB mỗi ảnh.</p><div class="mb-3"><label class="form-label" for="companyLogoFile">Logo vuông</label><input class="form-control" id="companyLogoFile" name="logoFile" type="file" accept="image/jpeg,image/png,image/webp"><c:if test="${not empty profile.logoPath}"><label class="form-check mt-2"><input class="form-check-input" type="checkbox" name="removeLogo" value="true"><span class="form-check-label small">Xóa logo hiện tại</span></label></c:if></div><div><label class="form-label" for="companyCoverFile">Ảnh bìa ngang</label><input class="form-control" id="companyCoverFile" name="coverFile" type="file" accept="image/jpeg,image/png,image/webp"><c:if test="${not empty profile.coverPath}"><label class="form-check mt-2"><input class="form-check-input" type="checkbox" name="removeCover" value="true"><span class="form-check-label small">Xóa ảnh bìa hiện tại</span></label></c:if></div></section>
                        </div></div>
                    </div>
                    <div class="company-editor-actions"><div><i class="bi bi-info-circle"></i>Thay đổi được hiển thị ngay trên trang công ty.</div><button class="btn btn-primary px-4" type="submit" data-loading-button><i class="bi bi-cloud-check me-1"></i>Lưu hồ sơ doanh nghiệp</button></div>
                </form>
            </c:if>
        </div>
    </main>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
