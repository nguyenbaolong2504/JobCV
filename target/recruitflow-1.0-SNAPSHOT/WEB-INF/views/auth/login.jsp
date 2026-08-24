<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng nhập | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="auth-page"><div class="auth-shell">
    <section class="auth-brand-panel">
        <a class="auth-brand" href="${pageContext.request.contextPath}/"><span>R</span><strong>RecruitFlow</strong></a>
        <div class="auth-brand-copy"><span class="auth-eyebrow"><i class="bi bi-stars"></i> NỀN TẢNG TUYỂN DỤNG TOÀN TRÌNH</span><h1>Mỗi cơ hội tốt bắt đầu từ một kết nối đúng.</h1><p>Tìm việc phù hợp, quản lý CV và theo dõi toàn bộ hành trình ứng tuyển trong một không gian thống nhất.</p></div>
        <div class="auth-feature-list"><div><i class="bi bi-search-heart"></i><span><strong>Gợi ý việc làm phù hợp</strong><small>Đối chiếu kỹ năng trong CV với yêu cầu vị trí.</small></span></div><div><i class="bi bi-signpost-split"></i><span><strong>Theo dõi tiến trình rõ ràng</strong><small>Từ nộp đơn, phỏng vấn đến nhận việc.</small></span></div><div><i class="bi bi-shield-check"></i><span><strong>Hồ sơ được bảo vệ</strong><small>Chỉ chia sẻ CV khi bạn chủ động ứng tuyển.</small></span></div></div>
        <div class="auth-proof"><div><strong>36+</strong><span>việc đang mở</span></div><div><strong>9</strong><span>lĩnh vực</span></div><div><strong>24/7</strong><span>quản lý hồ sơ</span></div></div>
    </section>
    <section class="auth-form-panel"><div class="auth-form-wrap">
        <a class="auth-back" href="${pageContext.request.contextPath}/"><i class="bi bi-arrow-left"></i>Về trang chủ</a>
        <div class="auth-form-heading"><span>CHÀO MỪNG TRỞ LẠI</span><h2>Đăng nhập tài khoản</h2><p>Tiếp tục hành trình nghề nghiệp hoặc quản lý quy trình tuyển dụng của bạn.</p></div>
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <c:if test="${not empty error}"><div class="alert alert-danger d-flex gap-2"><i class="bi bi-exclamation-circle-fill"></i><c:out value="${error}" /></div></c:if>
        <form action="${pageContext.request.contextPath}/login" method="post" data-validate-form novalidate>
            <div class="mb-3"><label class="form-label" for="email">Địa chỉ email</label><div class="auth-input"><i class="bi bi-envelope"></i><input class="form-control" type="email" id="email" name="email" required maxlength="254" autocomplete="email" placeholder="email@example.com"></div><div class="invalid-feedback">Vui lòng nhập email hợp lệ.</div></div>
            <div class="mb-2"><div class="d-flex justify-content-between"><label class="form-label" for="password">Mật khẩu</label><span class="small text-muted">Tối đa 72 ký tự</span></div><div class="auth-input"><i class="bi bi-lock"></i><input class="form-control" type="password" id="password" name="password" required maxlength="72" autocomplete="current-password" placeholder="Nhập mật khẩu"><button type="button" data-password-toggle="password" aria-label="Hiện mật khẩu"><i class="bi bi-eye"></i></button></div><div class="invalid-feedback">Vui lòng nhập mật khẩu.</div></div>
            <div class="auth-helper-row"><label><input class="form-check-input" type="checkbox" name="rememberEmail" value="true"> Ghi nhớ email</label><span>Gặp vấn đề? Liên hệ quản trị viên</span></div>
            <button class="btn btn-primary auth-submit" type="submit" data-loading-button><span>Đăng nhập</span><i class="bi bi-arrow-right"></i></button>
        </form>
        <div class="auth-divider"><span>Chưa có tài khoản?</span></div><a class="btn btn-outline-primary w-100" href="${pageContext.request.contextPath}/register">Tạo tài khoản miễn phí</a>
        <p class="auth-terms">Bằng việc tiếp tục, bạn đồng ý tuân thủ điều khoản sử dụng và chính sách bảo mật của RecruitFlow.</p>
    </div></section>
</div></main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
