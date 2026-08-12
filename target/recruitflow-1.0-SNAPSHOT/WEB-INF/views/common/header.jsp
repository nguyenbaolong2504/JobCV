<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <meta name="description" content="RecruitFlow - Recruitment & Employee Onboarding Management System">
            <title>
                <c:out value="${empty requestScope.pageTitle ? 'RecruitFlow' : requestScope.pageTitle}" />
            </title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
            <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css"
                rel="stylesheet">
            <link href="${pageContext.request.contextPath}/assets/css/app.css?v=20260811-ai-cv-coach" rel="stylesheet">
        </head>

        <body data-csrf-token="<c:out value='${requestScope.csrfToken}'/>">
