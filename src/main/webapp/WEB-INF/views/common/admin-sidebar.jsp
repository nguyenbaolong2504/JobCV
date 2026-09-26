<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <c:set var="currentPath" value="${pageContext.request.requestURI}" />
            <aside class="app-sidebar">
                <a class="sidebar-brand" href="${pageContext.request.contextPath}/admin/dashboard"><img class="jobcv-brand-logo jobcv-brand-logo-sidebar" src="${pageContext.request.contextPath}/assets/images/jobcv-logo.png" alt="JobCV"></a>
                <p class="sidebar-label">Quản trị hệ thống</p>
                <nav class="sidebar-nav">
                    <a class="${fn:endsWith(currentPath, '/admin/dashboard') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/admin/dashboard"><i class="bi bi-grid-1x2"></i> Tổng
                        quan</a>
                    <a class="${fn:contains(currentPath, '/admin/users') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/admin/users"><i class="bi bi-people"></i> Người
                        dùng</a>
                    <a class="${fn:contains(currentPath, '/admin/roles') or fn:contains(currentPath, '/admin/permissions') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/admin/permissions"><i class="bi bi-shield-lock"></i> Phân
                        quyền</a>
                    <a class="${fn:contains(currentPath, '/admin/job-categories') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/admin/job-categories"><i class="bi bi-diagram-3"></i>
                        Danh mục nghề nghiệp</a>
                    <a class="${fn:contains(currentPath, '/admin/home-banners') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/admin/home-banners"><i class="bi bi-images"></i>
                        Banner trang chủ</a>
                    <a class="${fn:contains(currentPath, '/admin/audit-logs') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/admin/audit-logs"><i class="bi bi-journal-text"></i>
                        Nhật ký hệ thống</a>
                </nav>
                <div class="sidebar-account">
                    <span class="avatar">
                        <c:out value="${fn:substring(sessionScope.fullName, 0, 1)}" />
                    </span>
                    <div><strong>
                            <c:out value="${sessionScope.fullName}" />
                        </strong><small>ADMIN</small></div>
                    <form class="ms-auto" action="${pageContext.request.contextPath}/logout" method="post"><input
                            type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><button
                            class="btn btn-link p-0 text-reset" type="submit" aria-label="Đăng xuất"><i
                                class="bi bi-box-arrow-right"></i></button></form>
                </div>
            </aside>
