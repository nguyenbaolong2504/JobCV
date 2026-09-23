<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Thông báo | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell"><div class="row g-0">
    <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />
    <main class="candidate-main col-lg-9 col-xl-10">
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4"><div><h1 class="page-title mb-1">Thông báo</h1><p class="text-muted mb-0">Cập nhật mới nhất về đơn ứng tuyển, lịch phỏng vấn và thư mời của bạn.</p></div><form action="${pageContext.request.contextPath}/candidate/notifications/read-all" method="post"><button class="btn btn-outline-primary" type="submit"><i class="bi bi-check2-all me-1"></i>Đánh dấu đã đọc</button></form></div>
        <section class="content-card"><c:forEach var="notification" items="${notifications}"><article class="notification-item ${notification.read ? '' : 'unread'}"><div class="d-flex gap-3"><span class="summary-icon flex-shrink-0" style="width: 2.35rem; height: 2.35rem;"><i class="bi bi-bell"></i></span><div class="flex-grow-1"><div class="d-flex flex-wrap justify-content-between gap-2"><h2 class="h6 fw-bold mb-0"><c:out value="${notification.title}" /></h2><small class="text-muted"><c:out value="${notification.createdAt}" /></small></div><p class="mb-2 mt-1 text-secondary"><c:out value="${notification.message}" /></p><div class="d-flex align-items-center gap-3"><c:if test="${not notification.read}"><form action="${pageContext.request.contextPath}/candidate/notifications/read" method="post"><input type="hidden" name="notificationId" value="<c:out value='${notification.id}'/>"><button class="btn btn-link btn-sm p-0" type="submit">Đánh dấu đã đọc</button></form></c:if><c:if test="${not empty notification.linkUrl}"><a class="btn btn-link btn-sm p-0" href="<c:out value='${notification.linkUrl}'/>">Xem chi tiết <i class="bi bi-arrow-right"></i></a></c:if></div></div></div></article></c:forEach><c:if test="${empty notifications}"><div class="empty-state"><div class="empty-icon"><i class="bi bi-bell-slash"></i></div><h2 class="h5">Bạn chưa có thông báo</h2><p class="mb-0">Khi có cập nhật về hồ sơ hoặc đơn ứng tuyển, chúng tôi sẽ thông báo tại đây.</p></div></c:if></section>
    </main>
</div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
