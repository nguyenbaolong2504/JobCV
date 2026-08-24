<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng ký | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="auth-page"><div class="auth-shell">
    <section class="auth-brand-panel auth-register-brand">
        <a class="auth-brand" href="${pageContext.request.contextPath}/"><span>R</span><strong>RecruitFlow</strong></a>
        <div class="auth-brand-copy"><span class="auth-eyebrow"><i class="bi bi-rocket-takeoff"></i> BẮT ĐẦU CÙNG RECRUITFLOW</span><h1>Xây dựng hành trình tuyển dụng chuyên nghiệp.</h1><p>Một tài khoản, hai trải nghiệm được thiết kế riêng cho người tìm việc và nhà tuyển dụng.</p></div>
        <div class="auth-role-preview"><div><span><i class="bi bi-person-workspace"></i></span><strong>Dành cho ứng viên</strong><small>Tìm việc, quản lý CV, lưu cơ hội và theo dõi đơn.</small></div><div><span><i class="bi bi-building-check"></i></span><strong>Dành cho nhà tuyển dụng</strong><small>Đăng tin, sàng lọc, phỏng vấn, offer và onboarding.</small></div></div>
    </section>
    <section class="auth-form-panel"><div class="auth-form-wrap auth-register-wrap">
        <a class="auth-back" href="${pageContext.request.contextPath}/"><i class="bi bi-arrow-left"></i>Về trang chủ</a>
        <div class="auth-form-heading"><span>TẠO TÀI KHOẢN MỚI</span><h2>Bạn muốn sử dụng RecruitFlow thế nào?</h2><p>Chọn đúng vai trò để nhận không gian làm việc phù hợp.</p></div>
        <jsp:include page="/WEB-INF/views/common/flash.jsp" />
        <c:if test="${not empty error}"><div class="alert alert-danger d-flex gap-2"><i class="bi bi-exclamation-circle-fill"></i><c:out value="${error}" /></div></c:if>
        <form action="${pageContext.request.contextPath}/register" method="post" data-validate-form novalidate>
            <fieldset class="mb-4"><legend class="visually-hidden">Loại tài khoản</legend><div class="auth-role-selector">
                <input class="btn-check" type="radio" name="accountType" id="accountCandidate" value="CANDIDATE" checked required><label for="accountCandidate"><i class="bi bi-person-workspace"></i><span><strong>Người tìm việc</strong><small>Tìm việc và quản lý CV</small></span><i class="bi bi-check-circle-fill role-check"></i></label>
                <input class="btn-check" type="radio" name="accountType" id="accountRecruiter" value="HR" required><label for="accountRecruiter"><i class="bi bi-building"></i><span><strong>Nhà tuyển dụng</strong><small>Tuyển và quản lý ứng viên</small></span><i class="bi bi-check-circle-fill role-check"></i></label>
            </div></fieldset>
            <div class="mb-3"><label class="form-label" for="fullName">Họ và tên</label><div class="auth-input"><i class="bi bi-person"></i><input class="form-control" id="fullName" name="fullName" required minlength="2" maxlength="100" autocomplete="name" placeholder="Nguyễn Văn A"></div><div class="invalid-feedback">Họ tên cần có từ 2 ký tự.</div></div>
            <div class="mb-3"><label class="form-label" for="email">Địa chỉ email</label><div class="auth-input"><i class="bi bi-envelope"></i><input class="form-control" id="email" name="email" type="email" required maxlength="254" autocomplete="email" placeholder="email@example.com"></div><div class="invalid-feedback">Vui lòng nhập email hợp lệ.</div></div>
            <div class="row g-3"><div class="col-sm-6"><label class="form-label" for="password">Mật khẩu</label><div class="auth-input"><i class="bi bi-lock"></i><input class="form-control" id="password" name="password" type="password" required minlength="6" maxlength="72" autocomplete="new-password" placeholder="Tối thiểu 6 ký tự"><button type="button" data-password-toggle="password" aria-label="Hiện mật khẩu"><i class="bi bi-eye"></i></button></div><div class="invalid-feedback">Cần ít nhất 6 ký tự.</div></div><div class="col-sm-6"><label class="form-label" for="confirmPassword">Nhập lại mật khẩu</label><div class="auth-input"><i class="bi bi-shield-lock"></i><input class="form-control" id="confirmPassword" name="confirmPassword" type="password" required minlength="6" maxlength="72" autocomplete="new-password" placeholder="Xác nhận mật khẩu"><button type="button" data-password-toggle="confirmPassword" aria-label="Hiện mật khẩu"><i class="bi bi-eye"></i></button></div><div class="invalid-feedback">Mật khẩu xác nhận chưa hợp lệ.</div></div></div>
            <div class="password-strength" data-password-strength><span></span><span></span><span></span><span></span><small>Độ mạnh mật khẩu</small></div>
            <label class="auth-consent"><input class="form-check-input" type="checkbox" required> <span>Tôi đồng ý với điều khoản sử dụng và chính sách bảo mật của RecruitFlow.</span></label>
            <button class="btn btn-primary auth-submit" type="submit" data-loading-button><span>Tạo tài khoản</span><i class="bi bi-arrow-right"></i></button>
        </form>
        <div class="auth-divider"><span>Đã có tài khoản?</span></div><a class="btn btn-outline-primary w-100" href="${pageContext.request.contextPath}/login">Đăng nhập ngay</a>
    </div></section>
</div></main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
