<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng ký | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="auth-page">
    <div class="container py-4 py-lg-5">
        <div class="row justify-content-center align-items-stretch g-0 auth-frame auth-frame-wide">
            <section class="col-lg-4 auth-intro p-4 p-lg-5">
                <a class="auth-brand" href="${pageContext.request.contextPath}/home"><span class="rf-brand-mark">J</span><span>JobCV</span></a>
                <div class="mt-auto mb-auto">
                    <span class="auth-kicker"><i class="bi bi-person-plus-fill me-2"></i>Tạo tài khoản</span>
                    <h1>Bắt đầu đúng vai trò.</h1>
                    <p>Chọn loại tài khoản để hệ thống thiết lập đúng hành trình và quyền truy cập cho bạn.</p>
                    <div class="auth-role-note"><i class="bi bi-person-check"></i><span><strong>Người tìm việc</strong> dùng ngay sau khi đăng ký để tìm việc, tạo CV và ứng tuyển.</span></div>
                    <div class="auth-role-note"><i class="bi bi-building-check"></i><span><strong>Nhà tuyển dụng</strong> cung cấp thông tin tổ chức và chờ Admin kích hoạt để bảo vệ hệ thống.</span></div>
                </div>
                <a class="auth-back-link" href="${pageContext.request.contextPath}/home"><i class="bi bi-arrow-left me-1"></i>Quay về trang tìm việc</a>
            </section>
            <section class="col-lg-8 bg-white auth-panel p-4 p-md-5">
                <div class="auth-panel-heading">
                    <p class="text-primary fw-semibold text-uppercase small mb-2">Đăng ký</p>
                    <h2 class="mb-2">Tạo tài khoản RecruitFlow</h2>
                    <p class="text-muted mb-0">Thông tin được xử lý theo đúng loại tài khoản bạn chọn.</p>
                </div>
                <div class="mt-4"><jsp:include page="/WEB-INF/views/common/flash.jsp" /></div>

                <form action="${pageContext.request.contextPath}/register" method="post" class="mt-4" data-register-form>
                    <input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>">
                    <fieldset class="mb-4">
                        <legend class="form-label fw-semibold mb-2">Bạn muốn đăng ký với tư cách</legend>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <input class="btn-check" type="radio" name="accountType" id="accountCandidate" value="CANDIDATE" checked required>
                                <label class="auth-account-type h-100" for="accountCandidate">
                                    <i class="bi bi-person-workspace fs-3"></i>
                                    <span><strong>Người tìm việc</strong><small>Tìm việc, tạo CV, nộp hồ sơ và theo dõi ứng tuyển.</small></span>
                                    <em>Hoạt động ngay</em>
                                </label>
                            </div>
                            <div class="col-md-6">
                                <input class="btn-check" type="radio" name="accountType" id="accountRecruiter" value="HR" required>
                                <label class="auth-account-type h-100" for="accountRecruiter">
                                    <i class="bi bi-building fs-3"></i>
                                    <span><strong>Nhà tuyển dụng</strong><small>Đăng tin, sàng lọc ứng viên và quản lý quy trình tuyển dụng.</small></span>
                                    <em>Cần Admin duyệt</em>
                                </label>
                            </div>
                        </div>
                    </fieldset>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label for="fullName" class="form-label fw-semibold">Họ và tên</label>
                            <input type="text" class="form-control" id="fullName" name="fullName" required maxlength="100" autocomplete="name" placeholder="Nguyễn Văn A">
                        </div>
                        <div class="col-md-6">
                            <label for="email" class="form-label fw-semibold">Email</label>
                            <input type="email" class="form-control" id="email" name="email" required maxlength="254" autocomplete="email" placeholder="name@example.com">
                        </div>
                        <div class="col-md-6">
                            <label for="password" class="form-label fw-semibold">Mật khẩu</label>
                            <div class="input-group">
                                <input type="password" class="form-control" id="password" name="password" required minlength="8" maxlength="72" autocomplete="new-password" aria-describedby="passwordHelp" placeholder="Ít nhất 8 ký tự">
                                <button class="btn btn-outline-secondary" type="button" data-password-toggle aria-controls="password" aria-label="Hiển thị mật khẩu" aria-pressed="false" title="Hiển thị mật khẩu">
                                    <i class="bi bi-eye" aria-hidden="true"></i>
                                </button>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <label for="confirmPassword" class="form-label fw-semibold">Xác nhận mật khẩu</label>
                            <div class="input-group">
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required minlength="8" maxlength="72" autocomplete="new-password" placeholder="Nhập lại mật khẩu">
                                <button class="btn btn-outline-secondary" type="button" data-password-toggle aria-controls="confirmPassword" aria-label="Hiển thị mật khẩu" aria-pressed="false" title="Hiển thị mật khẩu">
                                    <i class="bi bi-eye" aria-hidden="true"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                    <p class="form-text" id="passwordHelp">Dùng ít nhất 8 ký tự, gồm tối thiểu một chữ cái và một chữ số.</p>

                    <fieldset class="auth-recruiter-fields mt-4" data-recruiter-fields hidden>
                        <legend class="h6 mb-1"><i class="bi bi-patch-check text-primary me-1"></i>Thông tin xác minh nhà tuyển dụng</legend>
                        <p class="small text-muted mb-3">Admin dùng thông tin này để duyệt yêu cầu trước khi kích hoạt tài khoản HR.</p>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="organizationName" class="form-label fw-semibold">Tên công ty/tổ chức</label>
                                <input type="text" class="form-control" id="organizationName" name="organizationName" maxlength="150" autocomplete="organization" placeholder="Công ty ABC">
                            </div>
                            <div class="col-md-6">
                                <label for="jobTitle" class="form-label fw-semibold">Chức danh của bạn</label>
                                <input type="text" class="form-control" id="jobTitle" name="jobTitle" maxlength="100" autocomplete="organization-title" placeholder="Chuyên viên tuyển dụng">
                            </div>
                            <div class="col-md-6">
                                <label for="workPhone" class="form-label fw-semibold">Số điện thoại công việc</label>
                                <input type="tel" class="form-control" id="workPhone" name="workPhone" maxlength="30" autocomplete="tel" placeholder="0901 234 567">
                            </div>
                        </div>
                    </fieldset>

                    <div class="form-check mt-4">
                        <input class="form-check-input" type="checkbox" value="on" id="termsAccepted" name="termsAccepted" required>
                        <label class="form-check-label small" for="termsAccepted">Tôi xác nhận thông tin là chính xác và đồng ý với điều khoản sử dụng RecruitFlow.</label>
                    </div>
                    <button type="submit" class="btn btn-primary w-100 py-2 mt-4" data-loading-button><i class="bi bi-person-plus me-2"></i>Tạo tài khoản</button>
                </form>

                <c:if test="${googleOAuthEnabled}">
                    <div class="auth-divider"><span>hoặc</span></div>
                    <a class="btn btn-outline-secondary w-100 py-2" href="${pageContext.request.contextPath}/oauth/google"><i class="bi bi-google me-2"></i>Đăng ký Người tìm việc bằng Google</a>
                </c:if>
                <p class="text-center text-muted mt-4 mb-0">Đã có tài khoản? <a class="fw-semibold" href="${pageContext.request.contextPath}/login">Đăng nhập ngay</a></p>
            </section>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
