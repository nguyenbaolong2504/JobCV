<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <c:set var="candidatePath" value="${pageContext.request.requestURI}" />
            <c:set var="dashboardActive"
                value="${fn:endsWith(candidatePath, '/candidate/dashboard') ? 'active' : ''}" />
            <c:set var="profileActive" value="${fn:endsWith(candidatePath, '/candidate/profile') ? 'active' : ''}" />
            <c:set var="resumesActive" value="${fn:endsWith(candidatePath, '/candidate/resumes') ? 'active' : ''}" />
            <c:set var="cvBuilderActive"
                value="${fn:contains(candidatePath, '/candidate/cv-builder') ? 'active' : ''}" />
            <c:set var="jobsActive" value="${fn:endsWith(candidatePath, '/candidate/jobs') ? 'active' : ''}" />
            <c:set var="savedJobsActive" value="${fn:endsWith(candidatePath, '/candidate/saved-jobs') ? 'active' : ''}" />
            <c:set var="jobAlertsActive" value="${fn:endsWith(candidatePath, '/candidate/job-alerts') ? 'active' : ''}" />
            <c:set var="applicationsActive"
                value="${fn:contains(candidatePath, '/candidate/applications') ? 'active' : ''}" />
            <c:set var="interviewsActive"
                value="${fn:endsWith(candidatePath, '/candidate/interviews') ? 'active' : ''}" />
            <c:set var="offersActive" value="${fn:endsWith(candidatePath, '/candidate/offers') ? 'active' : ''}" />
            <c:set var="onboardingActive"
                value="${fn:contains(candidatePath, '/candidate/onboarding') ? 'active' : ''}" />
            <c:set var="notificationsActive"
                value="${fn:endsWith(candidatePath, '/candidate/notifications') ? 'active' : ''}" />
            <c:url var="sidebarAvatarUrl" value="/candidate/avatar"><c:param name="v" value="${layoutCandidateProfile.updatedAt.time}" /></c:url>

            <aside class="candidate-sidebar col-lg-3 col-xl-2">
                <a class="brand" href="${pageContext.request.contextPath}/home" title="Về trang chủ JobCV">
                    <img class="jobcv-brand-logo jobcv-brand-logo-sidebar" src="${pageContext.request.contextPath}/assets/images/jobcv-logo.png" alt="JobCV">
                </a>
                <a class="candidate-home-return" href="${pageContext.request.contextPath}/home">
                    <span class="candidate-home-return-icon"><i class="bi bi-house-door-fill"></i></span>
                    <span><strong>Về trang chủ</strong><small>Khám phá việc làm mới</small></span>
                    <i class="bi bi-arrow-up-right"></i>
                </a>
                <nav aria-label="Điều hướng ứng viên">
                    <a class="sidebar-link ${dashboardActive}"
                        href="${pageContext.request.contextPath}/candidate/dashboard"><i class="bi bi-grid-1x2"></i>
                        Tổng quan</a>
                    <a class="sidebar-link ${profileActive}"
                        href="${pageContext.request.contextPath}/candidate/profile"><i class="bi bi-person-vcard"></i>
                        Hồ sơ</a>
                    <a class="sidebar-link ${resumesActive}"
                        href="${pageContext.request.contextPath}/candidate/resumes"><i
                            class="bi bi-file-earmark-person"></i> CV của tôi</a>
                    <a class="sidebar-link ${cvBuilderActive}"
                        href="${pageContext.request.contextPath}/candidate/cv-builder"><i
                            class="bi bi-magic"></i> Tạo CV theo mẫu</a>
                    <a class="sidebar-link ${jobsActive}" href="${pageContext.request.contextPath}/candidate/jobs"><i
                            class="bi bi-search"></i> Tìm việc làm</a>
                    <a class="sidebar-link ${savedJobsActive}"
                        href="${pageContext.request.contextPath}/candidate/saved-jobs"><i class="bi bi-bookmark-heart"></i>
                        Việc đã lưu<c:if test="${savedJobCount gt 0}"><span class="sidebar-badge"><c:out value="${savedJobCount gt 99 ? '99+' : savedJobCount}" /></span></c:if></a>
                    <a class="sidebar-link ${jobAlertsActive}"
                        href="${pageContext.request.contextPath}/candidate/job-alerts"><i class="bi bi-bell-fill"></i>
                        Thông báo việc làm<c:if test="${jobAlertCount gt 0}"><span class="sidebar-badge"><c:out value="${jobAlertCount gt 99 ? '99+' : jobAlertCount}" /></span></c:if></a>
                    <a class="sidebar-link ${applicationsActive}"
                        href="${pageContext.request.contextPath}/candidate/applications"><i
                            class="bi bi-send-check"></i> Đơn ứng tuyển</a>
                    <a class="sidebar-link ${interviewsActive}"
                        href="${pageContext.request.contextPath}/candidate/interviews"><i
                            class="bi bi-calendar-event"></i> Lịch phỏng vấn</a>
                    <a class="sidebar-link ${offersActive}"
                        href="${pageContext.request.contextPath}/candidate/offers"><i class="bi bi-envelope-paper"></i>
                        Thư mời nhận việc</a>
                    <a class="sidebar-link ${onboardingActive}"
                        href="${pageContext.request.contextPath}/candidate/onboarding"><i
                            class="bi bi-rocket-takeoff"></i> Tiếp nhận</a>
                    <a class="sidebar-link ${notificationsActive}"
                        href="${pageContext.request.contextPath}/candidate/notifications"><i class="bi bi-bell"></i>
                        Thông báo<c:if test="${unreadNotificationCount gt 0}"><span class="sidebar-badge"><c:out value="${unreadNotificationCount gt 99 ? '99+' : unreadNotificationCount}" /></span></c:if></a>
                </nav>
                <div class="sidebar-footer">
                    <div class="d-flex align-items-center gap-2">
                        <span class="sidebar-avatar-wrap">
                            <img class="sidebar-avatar ${empty layoutCandidateProfile.avatarPath ? 'd-none' : ''}" src="${empty layoutCandidateProfile.avatarPath ? 'data:image/gif;base64,R0lGODlhAQABAAAAACw=' : sidebarAvatarUrl}" alt="" data-avatar-image data-fallback-target="sidebarAvatarFallback">
                            <span id="sidebarAvatarFallback" class="sidebar-avatar sidebar-avatar-fallback ${empty layoutCandidateProfile.avatarPath ? '' : 'd-none'}" data-avatar-fallback><c:out value="${fn:toUpperCase(fn:substring(sessionScope.fullName, 0, 1))}" /></span>
                        </span>
                        <div class="overflow-hidden">
                            <div class="candidate-name text-truncate fw-semibold">
                                <c:out value="${sessionScope.fullName}" />
                            </div>
                            <small class="text-white-50">Ứng viên</small>
                        </div>
                        <form class="ms-auto" action="${pageContext.request.contextPath}/logout" method="post"><input
                                type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><button
                                class="btn btn-link p-0 text-white-50" type="submit" title="Đăng xuất"
                                aria-label="Đăng xuất"><i class="bi bi-box-arrow-right"></i></button></form>
                    </div>
                </div>
            </aside>
