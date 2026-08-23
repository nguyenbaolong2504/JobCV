<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Thông báo | RecruitFlow" scope="request" />
<c:set var="notificationBase" value="${notificationPortal eq 'INTERVIEWER' ? '/interviewer/notifications' : '/hr/notifications'}" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <c:choose>
            <c:when test="${notificationPortal eq 'INTERVIEWER'}"><jsp:include page="/WEB-INF/views/common/interviewer-sidebar.jsp" /></c:when>
            <c:otherwise><jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" /></c:otherwise>
        </c:choose>
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><p class="text-muted small mb-1">Cập nhật công việc</p><h4 class="mb-0">Thông báo</h4></div>
                <form action="${pageContext.request.contextPath}${notificationBase}/read-all" method="post">
                    <input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>">
                    <button class="btn btn-outline-primary" type="submit"><i class="bi bi-check2-all me-1"></i> Đánh dấu tất cả đã đọc</button>
                </form>
            </div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}" /></div></c:if>
                <section class="card">
                    <div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Hộp thư thông báo</h5><small class="text-muted">Các cập nhật về lịch phỏng vấn, offer và hoạt động tuyển dụng được lưu tại đây.</small></div>
                    <div class="list-group list-group-flush">
                        <c:forEach var="notification" items="${notifications}">
                            <article class="list-group-item px-4 py-3 ${notification.read ? '' : 'list-group-item-light'}">
                                <div class="d-flex align-items-start gap-3">
                                    <span class="icon-shape flex-shrink-0 ${notification.read ? 'bg-light text-secondary' : 'bg-primary-subtle text-primary'}"><i class="bi bi-bell"></i></span>
                                    <div class="flex-grow-1">
                                        <div class="d-flex flex-wrap justify-content-between gap-2"><h2 class="h6 fw-semibold mb-0"><c:out value="${notification.title}" /></h2><small class="text-muted"><c:out value="${notification.createdAt}" /></small></div>
                                        <p class="mb-2 mt-1 text-secondary"><c:out value="${notification.message}" /></p>
                                        <div class="d-flex align-items-center gap-3">
                                            <c:if test="${not notification.read}"><form action="${pageContext.request.contextPath}${notificationBase}/read" method="post"><input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><input type="hidden" name="notificationId" value="<c:out value='${notification.id}'/>"><button class="btn btn-link btn-sm p-0" type="submit">Đánh dấu đã đọc</button></form></c:if>
                                            <c:if test="${not empty notification.linkUrl}"><a class="btn btn-link btn-sm p-0" href="<c:out value='${notification.linkUrl}'/>">Xem chi tiết <i class="bi bi-arrow-right"></i></a></c:if>
                                        </div>
                                    </div>
                                </div>
                            </article>
                        </c:forEach>
                        <c:if test="${empty notifications}"><div class="py-5 text-center text-muted"><i class="bi bi-bell-slash d-block fs-3 mb-2"></i>Chưa có thông báo nào.</div></c:if>
                    </div>
                    <c:if test="${not empty page and page.totalPages gt 1}"><nav class="p-3 border-top" aria-label="Phân trang thông báo"><ul class="pagination pagination-sm mb-0 justify-content-end"><c:forEach begin="1" end="${page.totalPages}" var="p"><li class="page-item ${p eq page.currentPage ? 'active' : ''}"><a class="page-link" href="${pageContext.request.contextPath}${notificationBase}?page=${p}">${p}</a></li></c:forEach></ul></nav></c:if>
                </section>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
