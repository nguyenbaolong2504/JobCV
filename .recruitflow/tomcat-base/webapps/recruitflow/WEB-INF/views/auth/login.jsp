<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng nhập | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="auth-page">
    <div class="container py-4 py-lg-5">
        <div class="row justify-content-center align-items-stretch g-0 auth-frame">
            <section class="col-lg-5 auth-intro p-4 p-lg-5">
                <a class="auth-brand" href="${pageContext.request.contextPath}/home"><span class="rf-brand-mark">J</span><span>JobCV</span></a>
                <div class="mt-auto mb-auto">
                    <span class="auth-kicker"><i class="bi bi-briefcase-fill me-2"></i>Nền tảng tuyển dụng</span>
                    <h1>Chào mừng bạn trở lại.</h1>
                    <p>Khám phá việc làm, quản lý CV và theo dõi từng bước trong hành trình ứng tuyển tại một nơi.</p>
                    <ul class="auth-benefits list-unstyled mb-0">
                        <li><i class="bi bi-check2-circle"></i>Việc làm và hồ sơ luôn trong tầm tay</li>
                        <li><i class="bi bi-check2-circle"></i>Mật khẩu được bảo vệ bằng BCrypt</li>
                        <li><i class="bi bi-check2-circle"></i>OTP email có thể được bật cho đăng nhập</li>
                    </ul>
                </div>
                <a class="auth-back-link" href="${pageContext.request.contextPath}/home"><i class="bi bi-arrow-left me-1"></i>Quay về trang tìm việc</a>
            </section>
            <section class="col-lg-7 bg-white auth-panel p-4 p-md-5">
                <div class="auth-panel-heading">
                    <p class="text-primary fw-semibold text-uppercase small mb-2">Đăng nhập</p>
                    <h2 class="mb-2">Tiếp tục hành trình của bạn</h2>
                    <p class="text-muted mb-0">Nhập email và mật khẩu đã đăng ký.</p>
                </div>
                <div class="mt-4"><jsp:include page="/WEB-INF/views/common/flash.jsp" /></div>

                <c:if test="${loginOtpRequired}">
                    <div class="alert alert-primary small d-flex gap-2 align-items-start" role="status"><i class="bi bi-shield-lock mt-1"></i><span>Sau khi xác thực mật khẩu, chúng tôi sẽ gửi mã OTP đến email của bạn để hoàn tất đăng nhập.</span></div>
                </c:if>

                <form action="${pageContext.request.contextPath}/login" method="post" class="mt-4" novalidate>
                    <input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>">
                    <div class="mb-3">
                        <label for="email" class="form-label fw-semibold">Email</label>
                        <div class="input-group">
                            <span class="input-group-text bg-white"><i class="bi bi-envelope"></i></span>
                            <input type="email" class="form-control" id="email" name="email" required maxlength="254" autocomplete="email" placeholder="name@example.com">
                        </div>
                    </div>
                    <div class="mb-2">
                        <div class="d-flex justify-content-between gap-3">
                            <label for="password" class="form-label fw-semibold">Mật khẩu</label>
                            <a class="small fw-semibold" href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
                        </div>
                        <div class="input-group">
                            <span class="input-group-text bg-white"><i class="bi bi-key"></i></span>
                            <input type="password" class="form-control" id="password" name="password" required maxlength="72" autocomplete="current-password" placeholder="Nhập mật khẩu">
                            <button class="btn btn-outline-secondary" type="button" data-password-toggle aria-controls="password" aria-label="Hiển thị mật khẩu" aria-pressed="false" title="Hiển thị mật khẩu">
                                <i class="bi bi-eye" aria-hidden="true"></i>
                            </button>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary w-100 py-2 mt-4" data-loading-button><i class="bi bi-box-arrow-in-right me-2"></i>Đăng nhập</button>
                </form>

                <c:if test="${googleOAuthEnabled}">
                    <div class="auth-divider"><span>hoặc</span></div>
                    <a class="btn btn-outline-secondary w-100 py-2" href="${pageContext.request.contextPath}/oauth/google"><i class="bi bi-google me-2"></i>Tiếp tục bằng Google</a>
                    <p class="small text-muted text-center mt-2 mb-0">Google chỉ tạo hoặc liên kết tài khoản Người tìm việc có email đã xác minh.</p>
                </c:if>

                <p class="text-center text-muted mt-4 mb-0">Chưa có tài khoản? <a class="fw-semibold" href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
            </section>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
