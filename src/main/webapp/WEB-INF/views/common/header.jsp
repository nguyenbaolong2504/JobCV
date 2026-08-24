<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <meta name="description" content="RecruitFlow - Hệ thống quản lý tuyển dụng và tiếp nhận nhân sự">
            <title>
                <c:out value="${empty requestScope.pageTitle ? 'RecruitFlow' : requestScope.pageTitle}" />
            </title>
            <link href="${pageContext.request.contextPath}/webjars/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
            <link href="${pageContext.request.contextPath}/webjars/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
            <link href="${pageContext.request.contextPath}/assets/css/app.css?v=20260824-release-v1" rel="stylesheet">
        </head>

        <body data-csrf-token="<c:out value='${requestScope.csrfToken}'/>" data-context-path="${pageContext.request.contextPath}">
