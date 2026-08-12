<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Không có quyền truy cập | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<main class="error-page"><div class="container text-center"><div class="error-code">403</div><h1 class="page-title mt-4">Bạn không có quyền truy cập trang này</h1><p class="text-muted mx-auto" style="max-width: 38rem;">Tài khoản hiện tại không được cấp quyền cho chức năng bạn vừa yêu cầu. Hãy quay về khu vực phù hợp với vai trò của bạn.</p><div class="d-flex justify-content-center gap-2 mt-4"><a class="btn btn-primary" href="${pageContext.request.contextPath}/">Về trang chủ</a><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/login">Đăng nhập lại</a></div></div></main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
