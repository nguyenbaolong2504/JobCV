<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Quên mật khẩu | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="auth-simple-page">
    <section class="auth-simple-card">
        <a class="auth-brand auth-brand-dark justify-content-center mb-4" href="${pageContext.request.contextPath}/home"><span class="rf-brand-mark">J</span><span>JobCV</span></a>
        <div class="auth-icon"><i class="bi bi-envelope-paper"></i></div>
        <h1>Quên mật khẩu?</h1>
        <p class="text-muted">Nhập email tài khoản. Nếu email hợp lệ, chúng tôi sẽ gửi mã OTP để bạn đặt lại mật khẩu.</p>
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <c:choose>
            <c:when test="${passwordResetAvailable}">
                <form action="${pageContext.request.contextPath}/forgot-password" method="post" class="mt-4">
                    <input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>">
                    <label for="email" class="form-label fw-semibold">Email</label>
                    <input class="form-control" id="email" name="email" type="email" maxlength="254" autocomplete="email" placeholder="name@example.com" required autofocus>
                    <button class="btn btn-primary w-100 py-2 mt-4" type="submit" data-loading-button>Gửi mã OTP</button>
                </form>
            </c:when>
            <c:otherwise>
                <div class="alert alert-warning mt-4 mb-0"><i class="bi bi-tools me-2"></i>Tính năng khôi phục mật khẩu hiện chưa được cấu hình. Vui lòng liên hệ quản trị viên.</div>
            </c:otherwise>
        </c:choose>
        <p class="text-center small text-muted mt-4 mb-0">Nhớ mật khẩu rồi? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
    </section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
