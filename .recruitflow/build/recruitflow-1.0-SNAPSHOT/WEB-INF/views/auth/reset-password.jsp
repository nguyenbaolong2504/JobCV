<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Tạo mật khẩu mới | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="auth-simple-page">
    <section class="auth-simple-card">
        <a class="auth-brand auth-brand-dark justify-content-center mb-4" href="${pageContext.request.contextPath}/home"><span class="rf-brand-mark">J</span><span>JobCV</span></a>
        <div class="auth-icon"><i class="bi bi-key-fill"></i></div>
        <h1>Tạo mật khẩu mới</h1>
        <p class="text-muted">Chọn mật khẩu mạnh mà bạn chưa dùng ở nơi khác.</p>
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <form action="${pageContext.request.contextPath}/forgot-password/reset" method="post" class="mt-4">
            <input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>">
            <div class="mb-3">
                <label for="password" class="form-label fw-semibold">Mật khẩu mới</label>
                <div class="input-group">
                    <input class="form-control" id="password" name="password" type="password" minlength="8" maxlength="72" autocomplete="new-password" required autofocus>
                    <button class="btn btn-outline-secondary" type="button" data-password-toggle aria-controls="password" aria-label="Hiển thị mật khẩu" aria-pressed="false" title="Hiển thị mật khẩu">
                        <i class="bi bi-eye" aria-hidden="true"></i>
                    </button>
                </div>
                <div class="form-text">Ít nhất 8 ký tự, có tối thiểu một chữ cái và một chữ số.</div>
            </div>
            <div class="mb-3">
                <label for="confirmPassword" class="form-label fw-semibold">Xác nhận mật khẩu mới</label>
                <div class="input-group">
                    <input class="form-control" id="confirmPassword" name="confirmPassword" type="password" minlength="8" maxlength="72" autocomplete="new-password" required>
                    <button class="btn btn-outline-secondary" type="button" data-password-toggle aria-controls="confirmPassword" aria-label="Hiển thị mật khẩu" aria-pressed="false" title="Hiển thị mật khẩu">
                        <i class="bi bi-eye" aria-hidden="true"></i>
                    </button>
                </div>
            </div>
            <button class="btn btn-primary w-100 py-2 mt-3" type="submit" data-loading-button>Đặt lại mật khẩu</button>
        </form>
    </section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
