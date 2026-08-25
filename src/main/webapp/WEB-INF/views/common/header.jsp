<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <meta name="description" content="<c:out value="${empty requestScope.pageDescription ? 'RecruitFlow - Nền tảng tìm việc và quản lý tuyển dụng minh bạch' : requestScope.pageDescription}" />">
            <meta name="theme-color" content="#00a859">
            <meta property="og:type" content="website">
            <meta property="og:site_name" content="RecruitFlow">
            <meta property="og:title" content="<c:out value="${empty requestScope.pageTitle ? 'RecruitFlow' : requestScope.pageTitle}" />">
            <meta property="og:description" content="<c:out value="${empty requestScope.pageDescription ? 'Nền tảng tìm việc và quản lý tuyển dụng minh bạch.' : requestScope.pageDescription}" />">
            <title>
                <c:out value="${empty requestScope.pageTitle ? 'RecruitFlow' : requestScope.pageTitle}" />
            </title>
            <link href="${pageContext.request.contextPath}/webjars/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
            <link href="${pageContext.request.contextPath}/webjars/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
            <link href="${pageContext.request.contextPath}/assets/css/app.css?v=20260825-brands-v3" rel="stylesheet">
        </head>

        <body data-csrf-token="<c:out value='${requestScope.csrfToken}'/>" data-context-path="${pageContext.request.contextPath}">
