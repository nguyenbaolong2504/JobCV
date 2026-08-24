<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Không tìm thấy trang | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<main class="error-page"><div class="container text-center"><div class="error-code">404</div><h1 class="page-title mt-4">Trang bạn tìm không tồn tại</h1><p class="text-muted mx-auto" style="max-width: 38rem;">Liên kết có thể đã thay đổi hoặc trang này không còn khả dụng.</p><a class="btn btn-primary mt-3" href="${pageContext.request.contextPath}/">Về trang chủ</a></div></main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
