<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng nhập | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-5">
            <div class="card p-4">
                <div class="text-center mb-4">
                    <h2 class="fw-bold text-primary">RecruitFlow</h2>
                    <p class="text-muted">Chào mừng trở lại! Vui lòng nhập thông tin đăng nhập.</p>
                </div>
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                
                <c:if test="${not empty error}">
                    <div class="alert alert-danger"><c:out value="${error}" /></div>
                </c:if>
                <c:if test="${not empty sessionScope.successMessage}">
                    <div class="alert alert-success"><c:out value="${sessionScope.successMessage}" /></div>
                    <c:remove var="successMessage" scope="session" />
                </c:if>

                <form action="${pageContext.request.contextPath}/login" method="post">
                    <div class="mb-3">
                        <label for="email" class="form-label">Email</label>
                        <input type="email" class="form-control" id="email" name="email" required maxlength="254" placeholder="email@example.com">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">Mật khẩu</label>
                        <input type="password" class="form-control" id="password" name="password" required maxlength="72" placeholder="********">
                    </div>
                    <button type="submit" class="btn btn-primary w-100 py-2 mt-2">Đăng nhập</button>
                </form>
                
                <div class="text-center mt-3">
                    <p class="text-muted">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
