<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty sessionScope.flashSuccess}">
    <div class="alert alert-success alert-dismissible fade show" role="alert"><i class="bi bi-check-circle me-2"></i><c:out value="${sessionScope.flashSuccess}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
    <c:remove var="flashSuccess" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.flashError}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert"><i class="bi bi-exclamation-octagon me-2"></i><c:out value="${sessionScope.flashError}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
    <c:remove var="flashError" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.flashWarning}">
    <div class="alert alert-warning alert-dismissible fade show" role="alert"><i class="bi bi-exclamation-triangle me-2"></i><c:out value="${sessionScope.flashWarning}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
    <c:remove var="flashWarning" scope="session" />
</c:if>

<c:if test="${not empty requestScope.success}">
    <div class="alert alert-success alert-dismissible fade show" role="alert"><i class="bi bi-check-circle me-2"></i><c:out value="${requestScope.success}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
</c:if>
<c:if test="${not empty requestScope.error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert"><i class="bi bi-exclamation-octagon me-2"></i><c:out value="${requestScope.error}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
</c:if>
<c:if test="${not empty requestScope.warning}">
    <div class="alert alert-warning alert-dismissible fade show" role="alert"><i class="bi bi-exclamation-triangle me-2"></i><c:out value="${requestScope.warning}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
</c:if>

<c:if test="${not empty sessionScope.success}">
    <div class="alert alert-success alert-dismissible fade show" role="alert"><i class="bi bi-check-circle me-2"></i><c:out value="${sessionScope.success}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
    <c:remove var="success" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.successMessage}">
    <div class="alert alert-success alert-dismissible fade show" role="alert"><i class="bi bi-check-circle me-2"></i><c:out value="${sessionScope.successMessage}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
    <c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert"><i class="bi bi-exclamation-octagon me-2"></i><c:out value="${sessionScope.error}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
    <c:remove var="error" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.warning}">
    <div class="alert alert-warning alert-dismissible fade show" role="alert"><i class="bi bi-exclamation-triangle me-2"></i><c:out value="${sessionScope.warning}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button></div>
    <c:remove var="warning" scope="session" />
</c:if>
