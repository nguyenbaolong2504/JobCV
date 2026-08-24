<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <nav class="navbar navbar-expand-lg rf-navbar sticky-top">
            <div class="container">
                <a class="navbar-brand d-flex align-items-center gap-2" href="${pageContext.request.contextPath}/">
                    <span class="rf-brand-mark">J</span>
                    <span class="rf-brand-name">JobCV</span>
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#publicNavbar"
                    aria-controls="publicNavbar" aria-expanded="false" aria-label="Mở điều hướng">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="publicNavbar">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                        <li class="nav-item"><a class="nav-link rf-nav-link"
                                href="${pageContext.request.contextPath}/">Trang chủ</a></li>
                        <li class="nav-item dropdown">
                            <c:choose>
                                <c:when test="${not empty requestScope.categoryRoots}">
                                    <a class="nav-link rf-nav-link dropdown-toggle" href="${pageContext.request.contextPath}/jobs"
                                        id="careerCategoryMenu" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                        Việc làm
                                    </a>
                                <ul class="dropdown-menu rf-category-mega-menu shadow border-0" aria-labelledby="careerCategoryMenu">
                                    <li>
                                        <div class="rf-category-mega-grid">
                                            <div class="rf-category-menu-intro">
                                                <span class="rf-category-menu-icon"><i class="bi bi-search-heart"></i></span>
                                                <strong>Khám phá theo ngành nghề</strong>
                                                <small>Tìm nhanh vị trí phù hợp với chuyên môn của bạn.</small>
                                                <a href="${pageContext.request.contextPath}/jobs">Xem toàn bộ việc làm <i class="bi bi-arrow-right"></i></a>
                                            </div>
                                            <c:forEach var="categoryRoot" items="${requestScope.categoryRoots}">
                                                <section class="rf-category-menu-group">
                                                    <c:url var="rootCategoryUrl" value="/jobs"><c:param name="categoryId" value="${categoryRoot.id}" /></c:url>
                                                    <a class="rf-category-menu-root" href="${rootCategoryUrl}"><c:out value="${categoryRoot.name}" /><i class="bi bi-arrow-up-right"></i></a>
                                                    <c:forEach var="childCategory" items="${categoryRoot.children}">
                                                        <c:url var="childCategoryUrl" value="/jobs"><c:param name="categoryId" value="${childCategory.id}" /></c:url>
                                                        <a class="rf-category-menu-child" href="${childCategoryUrl}"><c:out value="${childCategory.name}" /></a>
                                                    </c:forEach>
                                                    <c:if test="${empty categoryRoot.children}"><span class="rf-category-menu-child text-muted">Chưa có chuyên mục con</span></c:if>
                                                </section>
                                            </c:forEach>
                                        </div>
                                    </li>
                                </ul>
                                </c:when>
                                <c:otherwise><a class="nav-link rf-nav-link" href="${pageContext.request.contextPath}/jobs">Việc làm</a></c:otherwise>
                            </c:choose>
                        </li>
                    </ul>
                    <c:choose>
                        <c:when test="${not empty requestScope.currentUserId}">
                            <div class="dropdown">
                                <button class="btn btn-light border dropdown-toggle" type="button"
                                    data-bs-toggle="dropdown" aria-expanded="false">
                                    <i class="bi bi-person-circle me-1"></i>
                                    <c:out value="${requestScope.currentFullName}" />
                                </button>
                                <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                                    <c:choose>
                                        <c:when test="${requestScope.currentRole eq 'CANDIDATE'}">
                                            <li><a class="dropdown-item"
                                                    href="${pageContext.request.contextPath}/candidate/dashboard">Bảng
                                                    điều khiển</a></li>
                                            <li><a class="dropdown-item"
                                                    href="${pageContext.request.contextPath}/candidate/cv-builder"><i class="bi bi-magic me-2"></i>Tạo CV theo mẫu</a></li>
                                            <li><a class="dropdown-item"
                                                    href="${pageContext.request.contextPath}/candidate/applications"><i class="bi bi-send-check me-2"></i>Đơn ứng tuyển</a></li>
                                            <li><a class="dropdown-item"
                                                    href="${pageContext.request.contextPath}/candidate/profile">Hồ sơ cá
                                                    nhân</a></li>
                                        </c:when>
                                        <c:when test="${requestScope.currentRole eq 'HR'}">
                                            <li><a class="dropdown-item"
                                                    href="${pageContext.request.contextPath}/hr/dashboard">HR
                                                    Dashboard</a></li>
                                        </c:when>
                                        <c:when test="${requestScope.currentRole eq 'INTERVIEWER'}">
                                            <li><a class="dropdown-item"
                                                    href="${pageContext.request.contextPath}/interviewer/dashboard">Interviewer
                                                    Dashboard</a></li>
                                        </c:when>
                                        <c:when test="${requestScope.currentRole eq 'ADMIN'}">
                                            <li><a class="dropdown-item"
                                                    href="${pageContext.request.contextPath}/admin/dashboard">Admin
                                                    Dashboard</a></li>
                                        </c:when>
                                    </c:choose>
                                    <li>
                                        <hr class="dropdown-divider">
                                    </li>
                                    <li>
                                        <form action="${pageContext.request.contextPath}/logout" method="post"><input
                                                type="hidden" name="_csrf"
                                                value="<c:out value='${requestScope.csrfToken}'/>"><button
                                                class="dropdown-item text-danger" type="submit"><i
                                                    class="bi bi-box-arrow-right me-2"></i>Đăng xuất</button></form>
                                    </li>
                                </ul>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="d-flex gap-2">
                                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/login">Đăng
                                    nhập</a>
                                <a class="btn btn-primary" href="${pageContext.request.contextPath}/register">Đăng
                                    ký</a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </nav>
