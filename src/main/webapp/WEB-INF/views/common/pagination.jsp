<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty page and page.totalPages gt 1}">
    <c:set var="currentPage" value="${page.currentPage}" />
    <c:url var="firstUrl" value="${requestScope.paginationPath}">
        <c:param name="keyword" value="${param.keyword}" />
        <c:param name="title" value="${param.title}" />
        <c:param name="departmentId" value="${param.departmentId}" />
        <c:param name="categoryId" value="${param.categoryId}" />
        <c:param name="location" value="${param.location}" />
        <c:param name="employmentType" value="${param.employmentType}" />
        <c:param name="salaryMin" value="${param.salaryMin}" />
        <c:param name="salaryMax" value="${param.salaryMax}" />
        <c:param name="experienceMin" value="${param.experienceMin}" />
        <c:param name="experienceMax" value="${param.experienceMax}" />
        <c:param name="deadlineFrom" value="${param.deadlineFrom}" />
        <c:param name="deadlineTo" value="${param.deadlineTo}" />
        <c:param name="status" value="${param.status}" />
        <c:param name="sort" value="${param.sort}" />
        <c:param name="pageSize" value="${empty param.pageSize ? page.pageSize : param.pageSize}" />
        <c:param name="page" value="1" />
    </c:url>
    <nav class="d-flex flex-wrap align-items-center justify-content-between gap-2 mt-4" aria-label="Phân trang">
        <small class="text-muted">Tổng cộng <c:out value="${page.totalItems}" /> kết quả</small>
        <ul class="pagination pagination-sm mb-0">
            <li class="page-item ${currentPage le 1 ? 'disabled' : ''}"><a class="page-link" href="${firstUrl}" aria-label="Trang đầu"><i class="bi bi-chevron-bar-left"></i></a></li>
            <c:forEach var="pageNumber" begin="1" end="${page.totalPages}">
                <c:url var="pageUrl" value="${requestScope.paginationPath}">
                    <c:param name="keyword" value="${param.keyword}" />
                    <c:param name="title" value="${param.title}" />
                    <c:param name="departmentId" value="${param.departmentId}" />
                    <c:param name="categoryId" value="${param.categoryId}" />
                    <c:param name="location" value="${param.location}" />
                    <c:param name="employmentType" value="${param.employmentType}" />
                    <c:param name="salaryMin" value="${param.salaryMin}" />
                    <c:param name="salaryMax" value="${param.salaryMax}" />
                    <c:param name="experienceMin" value="${param.experienceMin}" />
                    <c:param name="experienceMax" value="${param.experienceMax}" />
                    <c:param name="deadlineFrom" value="${param.deadlineFrom}" />
                    <c:param name="deadlineTo" value="${param.deadlineTo}" />
                    <c:param name="status" value="${param.status}" />
                    <c:param name="sort" value="${param.sort}" />
                    <c:param name="pageSize" value="${empty param.pageSize ? page.pageSize : param.pageSize}" />
                    <c:param name="page" value="${pageNumber}" />
                </c:url>
                <li class="page-item ${pageNumber eq currentPage ? 'active' : ''}"><a class="page-link" href="${pageUrl}"><c:out value="${pageNumber}" /></a></li>
            </c:forEach>
            <c:url var="lastUrl" value="${requestScope.paginationPath}">
                <c:param name="keyword" value="${param.keyword}" />
                <c:param name="title" value="${param.title}" />
                <c:param name="departmentId" value="${param.departmentId}" />
                <c:param name="categoryId" value="${param.categoryId}" />
                <c:param name="location" value="${param.location}" />
                <c:param name="employmentType" value="${param.employmentType}" />
                <c:param name="salaryMin" value="${param.salaryMin}" />
                <c:param name="salaryMax" value="${param.salaryMax}" />
                <c:param name="experienceMin" value="${param.experienceMin}" />
                <c:param name="experienceMax" value="${param.experienceMax}" />
                <c:param name="deadlineFrom" value="${param.deadlineFrom}" />
                <c:param name="deadlineTo" value="${param.deadlineTo}" />
                <c:param name="status" value="${param.status}" />
                <c:param name="sort" value="${param.sort}" />
                <c:param name="pageSize" value="${empty param.pageSize ? page.pageSize : param.pageSize}" />
                <c:param name="page" value="${page.totalPages}" />
            </c:url>
            <li class="page-item ${currentPage ge page.totalPages ? 'disabled' : ''}"><a class="page-link" href="${lastUrl}" aria-label="Trang cuối"><i class="bi bi-chevron-bar-right"></i></a></li>
        </ul>
    </nav>
</c:if>
