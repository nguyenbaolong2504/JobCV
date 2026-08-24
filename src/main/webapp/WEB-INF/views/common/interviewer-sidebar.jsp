<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="requestPath" value="${pageContext.request.requestURI}" />

<aside class="app-sidebar" aria-label="Điều hướng phỏng vấn">
    <a class="sidebar-brand" href="${pageContext.request.contextPath}/interviewer/dashboard"><span class="sidebar-brand-mark">R</span>RecruitFlow</a>
    <p class="sidebar-label">Hội đồng phỏng vấn</p>
    <nav class="sidebar-nav">
        <a class="${fn:contains(requestPath, '/interviewer/dashboard') ? 'active' : ''}" href="${pageContext.request.contextPath}/interviewer/dashboard"><i class="bi bi-grid-1x2"></i>Tổng quan</a>
        <a class="${fn:contains(requestPath, '/interviewer/interviews') ? 'active' : ''}" href="${pageContext.request.contextPath}/interviewer/interviews"><i class="bi bi-calendar2-week"></i>Lịch phỏng vấn</a>
    </nav>
    <div class="sidebar-account">
        <span class="avatar"><c:out value="${fn:toUpperCase(fn:substring(sessionScope.fullName, 0, 1))}" /></span>
        <div><strong><c:out value="${sessionScope.fullName}" /></strong><small>Người phỏng vấn</small></div>
        <form class="ms-auto" action="${pageContext.request.contextPath}/logout" method="post"><input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><button class="btn btn-link p-0 text-reset" type="submit" aria-label="Đăng xuất"><i class="bi bi-box-arrow-right"></i></button></form>
    </div>
</aside>
