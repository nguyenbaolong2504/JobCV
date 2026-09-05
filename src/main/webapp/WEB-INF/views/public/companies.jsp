<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Danh sách công ty | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="companies-page">
    <section class="companies-hero"><div class="container"><span class="section-kicker"><i class="bi bi-buildings"></i> NHÀ TUYỂN DỤNG UY TÍN</span><h1>Khám phá môi trường làm việc phù hợp</h1><p>Tìm hiểu văn hóa, lĩnh vực hoạt động và các vị trí đang tuyển trước khi bạn ứng tuyển.</p><form action="${pageContext.request.contextPath}/companies" method="get"><i class="bi bi-search"></i><input name="keyword" type="search" maxlength="100" value="<c:out value='${keyword}'/>" placeholder="Tên công ty, lĩnh vực hoặc địa điểm"><button class="btn btn-primary" type="submit">Tìm công ty</button></form></div></section>
    <section class="public-section"><div class="container">
        <div class="section-heading d-flex justify-content-between align-items-end gap-3"><div><span class="section-kicker">DANH SÁCH DOANH NGHIỆP</span><h2>Công ty nổi bật</h2><p class="mb-0">Thông tin tuyển dụng được chuẩn hóa trên JobCV.</p></div><span class="result-count"><strong><c:out value="${companies.size()}" /></strong> doanh nghiệp</span></div>
        <div class="company-directory-grid mt-4">
            <c:forEach var="company" items="${companies}"><c:url var="companyUrl" value="/companies/detail"><c:param name="id" value="${company.id}" /></c:url>
                <article class="company-directory-card">
                    <div class="company-card-cover"><span class="company-logo-large"><img src="${pageContext.request.contextPath}${company.uploadedLogo ? '/company-logo?id=' : '/assets/images/employers/'}${company.uploadedLogo ? company.id : company.logoFile}" alt="Logo ${company.name}" loading="lazy"></span><c:if test="${company.verified}"><span class="company-verified"><i class="bi bi-patch-check-fill"></i>Đã xác thực</span></c:if></div>
                    <div class="company-card-body"><h3><a href="${companyUrl}"><c:out value="${company.name}" /></a></h3><p class="company-industry"><c:out value="${company.industry}" /></p><p class="company-description"><c:out value="${company.description}" /></p><div class="company-card-meta"><span><i class="bi bi-geo-alt"></i><c:out value="${company.location}" /></span><span><i class="bi bi-people"></i><c:out value="${company.size}" /></span></div><a class="company-card-jobs" href="${companyUrl}"><span><i class="bi bi-briefcase"></i><strong><c:out value="${company.openJobs}" /></strong> vị trí đang tuyển</span><i class="bi bi-arrow-right"></i></a></div>
                </article>
            </c:forEach>
        </div>
        <c:if test="${empty companies}"><div class="rf-card empty-state"><div class="empty-icon"><i class="bi bi-building-x"></i></div><h2 class="h5">Không tìm thấy công ty</h2><p class="mb-3">Thử tìm bằng tên lĩnh vực hoặc bỏ bớt từ khóa.</p><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/companies">Xem tất cả công ty</a></div></c:if>
    </div></section>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
