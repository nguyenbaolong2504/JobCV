<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Bảng điều khiển quản trị | JobCV" />
<c:set var="activeRate" value="${stats.totalUsers gt 0 ? stats.activeUsers * 100 / stats.totalUsers : 0}" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="app-layout">
    <jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />
    <main class="app-main bg-light">
        <div class="topbar"><div><p class="text-muted small mb-1">Trung tâm quản trị</p><h4 class="mb-0">Tổng quan hệ thống</h4></div><a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/users"><i class="bi bi-person-plus me-1"></i>Quản lý người dùng</a></div>
        <div class="main-content admin-dashboard">
            <jsp:include page="/WEB-INF/views/common/flash.jsp" />

            <section class="admin-welcome mb-4">
                <div><span><i class="bi bi-shield-check"></i>Hệ thống đang hoạt động ổn định</span><h1>Xin chào, <c:out value="${sessionScope.fullName}" />!</h1><p>Kiểm soát người dùng, phân quyền và theo dõi toàn bộ hoạt động JobCV từ một nơi.</p><div class="d-flex flex-wrap gap-2"><a class="btn btn-light" href="${pageContext.request.contextPath}/admin/audit-logs"><i class="bi bi-journal-text me-1"></i>Xem nhật ký</a><a class="btn btn-outline-light" href="${pageContext.request.contextPath}/admin/roles"><i class="bi bi-shield-lock me-1"></i>Kiểm tra phân quyền</a></div></div>
                <div class="admin-system-mark"><i class="bi bi-hdd-stack"></i><strong>JobCV</strong><small>Trung tâm quản trị</small></div>
            </section>

            <div class="row g-3 mb-4">
                <div class="col-sm-6 col-xl-3"><a class="admin-kpi" href="${pageContext.request.contextPath}/admin/users"><span class="admin-kpi-icon admin-blue"><i class="bi bi-people"></i></span><div><small>Tổng người dùng</small><strong><c:out value="${stats.totalUsers}" /></strong><em>Tất cả tài khoản</em></div><i class="bi bi-arrow-up-right"></i></a></div>
                <div class="col-sm-6 col-xl-3"><a class="admin-kpi" href="${pageContext.request.contextPath}/admin/users?status=ACTIVE"><span class="admin-kpi-icon admin-green"><i class="bi bi-person-check"></i></span><div><small>Đang hoạt động</small><strong><c:out value="${stats.activeUsers}" /></strong><em><fmt:formatNumber value="${activeRate}" maxFractionDigits="0" />% tổng tài khoản</em></div><i class="bi bi-arrow-up-right"></i></a></div>
                <div class="col-sm-6 col-xl-3"><a class="admin-kpi" href="${pageContext.request.contextPath}/admin/users?status=LOCKED"><span class="admin-kpi-icon admin-red"><i class="bi bi-person-lock"></i></span><div><small>Đang bị khóa</small><strong><c:out value="${stats.lockedUsers}" /></strong><em>Cần kiểm tra</em></div><i class="bi bi-arrow-up-right"></i></a></div>
                <div class="col-sm-6 col-xl-3"><a class="admin-kpi" href="${pageContext.request.contextPath}/admin/departments"><span class="admin-kpi-icon admin-violet"><i class="bi bi-buildings"></i></span><div><small>Phòng ban</small><strong><c:out value="${stats.totalDepartments}" /></strong><em>Cơ cấu tổ chức</em></div><i class="bi bi-arrow-up-right"></i></a></div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-xl-8">
                    <section class="admin-panel h-100">
                        <header class="admin-panel-header"><div><span>TÁC VỤ QUẢN TRỊ</span><h2>Điều hành nhanh</h2><p>Truy cập các chức năng hệ thống thường sử dụng.</p></div></header>
                        <div class="row g-3 admin-actions">
                            <div class="col-md-6"><a href="${pageContext.request.contextPath}/admin/users"><span class="admin-blue"><i class="bi bi-people"></i></span><div><strong>Quản lý người dùng</strong><small>Tìm kiếm, khóa hoặc kích hoạt tài khoản</small></div><i class="bi bi-chevron-right"></i></a></div>
                            <div class="col-md-6"><a href="${pageContext.request.contextPath}/admin/roles"><span class="admin-violet"><i class="bi bi-shield-lock"></i></span><div><strong>Vai trò và phân quyền</strong><small>Kiểm soát quyền truy cập theo vai trò</small></div><i class="bi bi-chevron-right"></i></a></div>
                            <div class="col-md-6"><a href="${pageContext.request.contextPath}/admin/departments"><span class="admin-green"><i class="bi bi-building-add"></i></span><div><strong>Cơ cấu phòng ban</strong><small>Cập nhật đơn vị trong tổ chức</small></div><i class="bi bi-chevron-right"></i></a></div>
                            <div class="col-md-6"><a href="${pageContext.request.contextPath}/admin/audit-logs"><span class="admin-amber"><i class="bi bi-journal-code"></i></span><div><strong>Nhật ký hệ thống</strong><small>Truy vết các thay đổi quan trọng</small></div><i class="bi bi-chevron-right"></i></a></div>
                        </div>
                    </section>
                </div>
                <div class="col-xl-4">
                    <section class="admin-panel h-100">
                        <header class="admin-panel-header"><div><span>SỨC KHỎE HỆ THỐNG</span><h2>Tình trạng tài khoản</h2></div><b class="admin-online"><i></i>Trực tuyến</b></header>
                        <div class="admin-health"><div class="admin-health-ring" style="--admin-health: ${activeRate * 3.6}deg"><span><strong><fmt:formatNumber value="${activeRate}" maxFractionDigits="0" />%</strong><small>Hoạt động</small></span></div><div class="admin-health-legend"><span><i class="legend-green"></i>Hoạt động<b><c:out value="${stats.activeUsers}" /></b></span><span><i class="legend-red"></i>Bị khóa<b><c:out value="${stats.lockedUsers}" /></b></span><span><i class="legend-blue"></i>Tổng tài khoản<b><c:out value="${stats.totalUsers}" /></b></span></div></div>
                        <div class="admin-volume"><span><small>Tin tuyển dụng</small><strong><c:out value="${stats.totalJobs}" /></strong></span><span><small>Đơn ứng tuyển</small><strong><c:out value="${stats.totalApplications}" /></strong></span></div>
                    </section>
                </div>
            </div>

            <section class="admin-panel">
                <header class="admin-panel-header"><div><span>GIÁM SÁT BẢO MẬT</span><h2>Hoạt động gần đây</h2><p>Các thay đổi mới nhất đã được ghi lại trong nhật ký.</p></div><a href="${pageContext.request.contextPath}/admin/audit-logs">Xem toàn bộ<i class="bi bi-arrow-right ms-1"></i></a></header>
                <div class="admin-audit-list">
                    <c:forEach var="log" items="${recentAuditLogs}"><article><span class="admin-audit-icon"><i class="bi bi-activity"></i></span><div><strong><c:out value="${empty log.userName ? 'Hệ thống' : log.userName}" /></strong><p><c:out value="${log.details}" /></p><small><c:out value="${log.entityName}" /><c:if test="${not empty log.entityId}"> #<c:out value="${log.entityId}" /></c:if></small></div><em><fmt:formatDate value="${log.createdAt}" pattern="dd/MM/yyyy HH:mm" /></em><span class="admin-action-code"><c:out value="${log.action}" /></span></article></c:forEach>
                    <c:if test="${empty recentAuditLogs}"><div class="admin-audit-empty"><i class="bi bi-shield-check"></i><strong>Chưa có hoạt động mới</strong><small>Những thay đổi quản trị sẽ được hiển thị tại đây.</small></div></c:if>
                </div>
            </section>
        </div>
    </main>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
