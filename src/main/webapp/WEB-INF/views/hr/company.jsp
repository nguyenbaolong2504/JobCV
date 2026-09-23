<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Thông tin công ty | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="container-fluid p-0"><div class="row g-0"><jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
<main class="col-md-10 offset-md-2 bg-light min-vh-100"><div class="topbar"><div><p class="text-muted small mb-1">Doanh nghiệp</p><h4 class="mb-0">Thông tin công ty</h4></div><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/companies/detail?id=${company.id}" target="_blank"><i class="bi bi-box-arrow-up-right me-1"></i>Xem trang công khai</a></div>
<div class="main-content"><jsp:include page="/WEB-INF/views/common/flash.jsp" /><section class="card"><div class="card-body p-4 p-lg-5">
<div class="d-flex align-items-center gap-3 mb-4"><span class="company-profile-logo"><img src="${pageContext.request.contextPath}/company-logo?id=${company.id}" alt="Logo công ty"></span><div><h2 class="h4 mb-1"><c:out value="${company.name}" /></h2><p class="text-muted mb-0">Mọi HR thuộc công ty đều dùng chung hồ sơ này.</p></div></div>
<form action="${pageContext.request.contextPath}/hr/company" method="post" enctype="multipart/form-data" class="row g-3">
<div class="col-12"><label class="form-label fw-semibold" for="logoFile">Logo công ty</label><input class="form-control" id="logoFile" name="logoFile" type="file" accept="image/jpeg,image/png,image/webp"><div class="form-text">JPG, PNG hoặc WEBP, tối đa 2 MB.</div></div>
<div class="col-md-6"><label class="form-label fw-semibold" for="name">Tên công ty *</label><input class="form-control" id="name" name="name" maxlength="150" required value="<c:out value='${company.name}'/>"></div>
<div class="col-md-6"><label class="form-label fw-semibold" for="industry">Lĩnh vực</label><input class="form-control" id="industry" name="industry" maxlength="150" value="<c:out value='${company.industry}'/>"></div>
<div class="col-md-6"><label class="form-label fw-semibold" for="size">Quy mô</label><input class="form-control" id="size" name="size" maxlength="100" placeholder="Ví dụ: 50 - 200 nhân sự" value="<c:out value='${company.size}'/>"></div>
<div class="col-md-6"><label class="form-label fw-semibold" for="website">Website</label><input class="form-control" id="website" name="website" maxlength="255" type="url" value="<c:out value='${company.website}'/>"></div>
<div class="col-12"><label class="form-label fw-semibold" for="location">Địa chỉ</label><input class="form-control" id="location" name="location" maxlength="255" value="<c:out value='${company.location}'/>"></div>
<div class="col-12"><label class="form-label fw-semibold" for="description">Giới thiệu công ty</label><textarea class="form-control" id="description" name="description" rows="7" maxlength="5000"><c:out value="${company.description}" /></textarea></div>
<div class="col-12 d-flex justify-content-end"><button class="btn btn-primary px-4" type="submit"><i class="bi bi-save me-1"></i>Lưu thông tin công ty</button></div>
</form></div></section></div></main></div></div><jsp:include page="/WEB-INF/views/common/footer.jsp" />
