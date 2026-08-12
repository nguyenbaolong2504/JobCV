<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
            <c:set var="requestPath" value="${pageContext.request.requestURI}" />

            <aside class="sidebar col-md-2 p-0 position-fixed h-100 overflow-auto" aria-label="Điều hướng phỏng vấn">
                <a class="sidebar-brand text-decoration-none"
                    href="${pageContext.request.contextPath}/interviewer/dashboard">
                    <span class="sidebar-brand-icon bg-info">R</span>
                    <span>RecruitFlow</span>
                </a>

                <nav class="mt-3 pb-5">
                    <a class="${fn:contains(requestPath, '/interviewer/dashboard') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/interviewer/dashboard">
                        <i class="bi bi-speedometer2 me-2"></i> Dashboard
                    </a>
                    <a class="${fn:contains(requestPath, '/interviewer/interviews') ? 'active' : ''}"
                        href="${pageContext.request.contextPath}/interviewer/interviews">
                        <i class="bi bi-calendar2-week me-2"></i> Lịch phỏng vấn
                    </a>
                </nav>

                <div class="p-3 position-absolute bottom-0 w-100 border-top" style="border-color:#333 !important;">
                    <div class="d-flex align-items-center">
                        <div class="rounded-circle bg-info text-dark d-flex align-items-center justify-content-center fw-bold"
                            style="width:34px;height:34px;">
                            <i class="bi bi-person-workspace"></i>
                        </div>
                        <div class="ms-2 lh-sm overflow-hidden">
                            <div class="fw-semibold text-white text-truncate">
                                <c:out value="${sessionScope.fullName}" />
                            </div>
                            <small class="text-muted">INTERVIEWER</small>
                        </div>
                        <form class="ms-auto" action="${pageContext.request.contextPath}/logout" method="post"><input
                                type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><button
                                class="btn btn-link p-0 text-muted" type="submit" aria-label="Đăng xuất"><i
                                    class="bi bi-box-arrow-right"></i></button></form>
                    </div>
                </div>
            </aside>