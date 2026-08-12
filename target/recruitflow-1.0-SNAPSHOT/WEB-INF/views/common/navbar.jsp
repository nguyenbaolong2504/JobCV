<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="navbar navbar-expand-lg rf-navbar sticky-top">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${pageContext.request.contextPath}/">
            <span class="rf-brand-mark">R</span>
            <span class="rf-brand-name">RecruitFlow</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#publicNavbar" aria-controls="publicNavbar" aria-expanded="false" aria-label="Mở điều hướng">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="publicNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item"><a class="nav-link rf-nav-link" href="${pageContext.request.contextPath}/">Trang chủ</a></li>
                <li class="nav-item"><a class="nav-link rf-nav-link" href="${pageContext.request.contextPath}/jobs">Việc làm</a></li>
            </ul>
            <c:choose>
                <c:when test="${not empty sessionScope.userId}">
                    <div class="dropdown">
                        <button class="btn btn-light border dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="bi bi-person-circle me-1"></i>
                            <c:out value="${sessionScope.fullName}" />
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                            <c:choose>
                                <c:when test="${sessionScope.role eq 'CANDIDATE'}">
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/candidate/dashboard">Bảng điều khiển</a></li>
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/candidate/profile">Hồ sơ cá nhân</a></li>
                                </c:when>
                                <c:when test="${sessionScope.role eq 'HR'}">
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/hr/dashboard">HR Dashboard</a></li>
                                </c:when>
                                <c:when test="${sessionScope.role eq 'INTERVIEWER'}">
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/interviewer/dashboard">Interviewer Dashboard</a></li>
                                </c:when>
                                <c:when test="${sessionScope.role eq 'ADMIN'}">
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/dashboard">Admin Dashboard</a></li>
                                </c:when>
                            </c:choose>
                            <li><hr class="dropdown-divider"></li>
                            <li><form action="${pageContext.request.contextPath}/logout" method="post"><input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><button class="dropdown-item text-danger" type="submit"><i class="bi bi-box-arrow-right me-2"></i>Đăng xuất</button></form></li>
                        </ul>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="d-flex gap-2">
                        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                        <a class="btn btn-primary" href="${pageContext.request.contextPath}/register">Đăng ký</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</nav>
