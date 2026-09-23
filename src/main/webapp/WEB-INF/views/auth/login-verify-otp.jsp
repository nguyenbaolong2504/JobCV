<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Xác minh OTP | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="auth-simple-page">
    <section class="auth-simple-card">
        <a class="auth-brand auth-brand-dark justify-content-center mb-4" href="${pageContext.request.contextPath}/home"><img class="jobcv-brand-logo jobcv-brand-logo-auth-dark" src="${pageContext.request.contextPath}/assets/images/jobcv-logo.png" alt="JobCV"></a>
        <div class="auth-icon"><i class="bi bi-shield-lock"></i></div>
        <h1>Xác minh đăng nhập</h1>
        <p class="text-muted">Chúng tôi đã gửi mã gồm 6 chữ số đến <strong><c:out value="${maskedEmail}" /></strong>. Mã có hiệu lực trong 10 phút.</p>
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <form action="${pageContext.request.contextPath}/login/verify-otp" method="post" class="mt-4">
            <input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>">
            <label for="otp" class="form-label fw-semibold">Mã OTP</label>
            <input class="form-control auth-otp-input" id="otp" name="otp" type="text" inputmode="numeric" autocomplete="one-time-code" pattern="[0-9]{6}" minlength="6" maxlength="6" required placeholder="000000" autofocus>
            <button class="btn btn-primary w-100 py-2 mt-4" type="submit" data-loading-button>Xác minh và đăng nhập</button>
        </form>
        <form action="${pageContext.request.contextPath}/login/verify-otp/resend" method="post" class="text-center mt-3">
            <input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>">
            <button type="submit" class="btn btn-link btn-sm">Chưa nhận được mã? Gửi lại OTP</button>
        </form>
        <p class="text-center small text-muted mt-3 mb-0"><a href="${pageContext.request.contextPath}/login">Quay lại trang đăng nhập</a></p>
    </section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
