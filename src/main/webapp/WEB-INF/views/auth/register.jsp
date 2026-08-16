<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng ký | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-5">
            <div class="card p-4">
                <div class="text-center mb-4">
                    <h2 class="fw-bold text-primary">RecruitFlow</h2>
                    <p class="text-muted">Chọn loại tài khoản phù hợp với bạn.</p>
                </div>
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                
                <c:if test="${not empty error}">
                    <div class="alert alert-danger"><c:out value="${error}" /></div>
                </c:if>

                <form action="${pageContext.request.contextPath}/register" method="post">
                    <fieldset class="mb-4">
                        <legend class="form-label fw-semibold mb-2">Bạn đăng ký với tư cách</legend>
                        <div class="row g-2">
                            <div class="col-sm-6">
                                <input class="btn-check" type="radio" name="accountType" id="accountCandidate" value="CANDIDATE" checked required>
                                <label class="btn btn-outline-primary w-100 h-100 p-3 text-start" for="accountCandidate">
                                    <i class="bi bi-person-workspace fs-4 d-block mb-1"></i>
                                    <strong>Người tìm việc</strong>
                                    <small class="d-block mt-1">Tìm việc, tải CV và ứng tuyển</small>
                                </label>
                            </div>
                            <div class="col-sm-6">
                                <input class="btn-check" type="radio" name="accountType" id="accountRecruiter" value="HR" required>
                                <label class="btn btn-outline-primary w-100 h-100 p-3 text-start" for="accountRecruiter">
                                    <i class="bi bi-building fs-4 d-block mb-1"></i>
                                    <strong>Nhà tuyển dụng</strong>
                                    <small class="d-block mt-1">Đăng tin và quản lý ứng viên</small>
                                </label>
                            </div>
                        </div>
                    </fieldset>
                    <div class="mb-3">
                        <label for="fullName" class="form-label">Họ và tên</label>
                        <input type="text" class="form-control" id="fullName" name="fullName" required maxlength="100" placeholder="Nguyễn Văn A">
                    </div>
                    <div class="mb-3">
                        <label for="email" class="form-label">Email</label>
                        <input type="email" class="form-control" id="email" name="email" required maxlength="254" placeholder="email@example.com">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">Mật khẩu</label>
                        <input type="password" class="form-control" id="password" name="password" required minlength="6" maxlength="72" placeholder="********">
                    </div>
                    <div class="mb-3">
                        <label for="confirmPassword" class="form-label">Xác nhận mật khẩu</label>
                        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required minlength="6" maxlength="72" placeholder="********">
                    </div>
                    <button type="submit" class="btn btn-primary w-100 py-2 mt-2">Đăng ký</button>
                </form>
                
                <div class="text-center mt-3">
                    <p class="text-muted">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập ngay</a></p>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
