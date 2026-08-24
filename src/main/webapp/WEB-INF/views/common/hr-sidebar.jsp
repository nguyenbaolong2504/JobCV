<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <c:set var="currentPath" value="${pageContext.request.requestURI}" />
            <aside class="app-sidebar">
                <a class="sidebar-brand" href="${pageContext.request.contextPath}/hr/dashboard"><span
                        class="sidebar-brand-mark">R</span>RecruitFlow</a>
                <p class="sidebar-label">Nhân sự</p>
                <nav class="sidebar-nav">
                    <a class="${fn:endsWith(currentPath, '/hr/dashboard') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/hr/dashboard"><i class="bi bi-grid-1x2"></i> Tổng
                        quan</a>
                    <a class="${fn:contains(currentPath, '/hr/jobs') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/hr/jobs"><i class="bi bi-briefcase"></i> Tin tuyển
                        dụng</a>
                    <a class="${fn:contains(currentPath, '/hr/applications') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/hr/applications"><i class="bi bi-people"></i> Ứng
                        viên</a>
                    <a class="${fn:contains(currentPath, '/hr/interviews') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/hr/interviews"><i class="bi bi-calendar-event"></i>
                        Phỏng vấn</a>
                    <a class="${fn:contains(currentPath, '/hr/offers') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/hr/offers"><i class="bi bi-envelope-paper"></i>
                        Thư mời nhận việc</a>
                    <a class="${fn:contains(currentPath, '/hr/onboarding') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/hr/onboarding"><i class="bi bi-rocket-takeoff"></i>
                        Tiếp nhận nhân sự</a>
                    <a class="${fn:contains(currentPath, '/hr/reports') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/hr/reports"><i class="bi bi-bar-chart"></i> Báo cáo</a>
                </nav>
                <div class="sidebar-account">
                    <span class="avatar">
                        <c:out value="${fn:substring(sessionScope.fullName, 0, 1)}" />
                    </span>
                    <div><strong>
                            <c:out value="${sessionScope.fullName}" />
                        </strong><small>HR</small></div>
                    <form class="ms-auto" action="${pageContext.request.contextPath}/logout" method="post"><input
                            type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><button
                            class="btn btn-link p-0 text-reset" type="submit" aria-label="Đăng xuất"><i
                                class="bi bi-box-arrow-right"></i></button></form>
                </div>
            </aside>
