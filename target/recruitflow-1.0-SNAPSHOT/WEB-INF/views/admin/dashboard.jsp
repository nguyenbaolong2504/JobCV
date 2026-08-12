<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

        <c:set var="pageTitle" value="Admin Dashboard | RecruitFlow" />

        <jsp:include page="/WEB-INF/views/common/header.jsp" />

        <div class="app-layout">

            <!-- SIDEBAR -->
            <jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />

            <!-- MAIN -->
            <main class="app-main bg-light">

                <!-- TOPBAR -->
                <div class="topbar">

                    <div>
                        <p class="text-muted small mb-1">
                            Quản trị hệ thống
                        </p>

                        <h4 class="mb-0">
                            Admin Dashboard
                        </h4>
                    </div>

                    <span class="fw-semibold">
                        <c:out value="${sessionScope.fullName}" />
                    </span>

                </div>


                <!-- CONTENT -->
                <div class="main-content">

                    <jsp:include page="/WEB-INF/views/common/flash.jsp" />


                    <!-- THỐNG KÊ -->
                    <div class="row g-3 mb-4">

                        <!-- Tổng người dùng -->
                        <div class="col-sm-6 col-xl-3">

                            <div class="card p-3 h-100">

                                <div class="d-flex justify-content-between">

                                    <div>
                                        <p class="text-muted mb-1">
                                            Tổng người dùng
                                        </p>

                                        <h3 class="mb-0">
                                            <c:out value="${stats.totalUsers}" />
                                        </h3>
                                    </div>

                                    <span class="icon-shape bg-primary-subtle text-primary">
                                        <i class="bi bi-people fs-4"></i>
                                    </span>

                                </div>

                            </div>

                        </div>


                        <!-- Active -->
                        <div class="col-sm-6 col-xl-3">

                            <div class="card p-3 h-100">

                                <div class="d-flex justify-content-between">

                                    <div>
                                        <p class="text-muted mb-1">
                                            Tài khoản hoạt động
                                        </p>

                                        <h3 class="mb-0 text-success">
                                            <c:out value="${stats.activeUsers}" />
                                        </h3>
                                    </div>

                                    <span class="icon-shape bg-success-subtle text-success">
                                        <i class="bi bi-person-check fs-4"></i>
                                    </span>

                                </div>

                            </div>

                        </div>


                        <!-- Locked -->
                        <div class="col-sm-6 col-xl-3">

                            <div class="card p-3 h-100">

                                <div class="d-flex justify-content-between">

                                    <div>
                                        <p class="text-muted mb-1">
                                            Tài khoản bị khóa
                                        </p>

                                        <h3 class="mb-0 text-danger">
                                            <c:out value="${stats.lockedUsers}" />
                                        </h3>
                                    </div>

                                    <span class="icon-shape bg-danger-subtle text-danger">
                                        <i class="bi bi-person-lock fs-4"></i>
                                    </span>

                                </div>

                            </div>

                        </div>


                        <!-- Department -->
                        <div class="col-sm-6 col-xl-3">

                            <div class="card p-3 h-100">

                                <div class="d-flex justify-content-between">

                                    <div>
                                        <p class="text-muted mb-1">
                                            Phòng ban
                                        </p>

                                        <h3 class="mb-0">
                                            <c:out value="${stats.totalDepartments}" />
                                        </h3>
                                    </div>

                                    <span class="icon-shape bg-info-subtle text-info">
                                        <i class="bi bi-buildings fs-4"></i>
                                    </span>

                                </div>

                            </div>

                        </div>

                    </div>


                    <!-- HÀNG NỘI DUNG -->
                    <div class="row g-4">

                        <!-- TÁC VỤ NHANH -->
                        <div class="col-lg-7">

                            <section class="card h-100">

                                <div class="card-header bg-white border-0 px-4 pt-4">

                                    <h5 class="mb-1">
                                        Tác vụ nhanh
                                    </h5>

                                    <small class="text-muted">
                                        Các công việc quản trị thường dùng.
                                    </small>

                                </div>


                                <div class="card-body px-4 pb-4">

                                    <div class="row g-3">

                                        <!-- User -->
                                        <div class="col-md-6">

                                            <a class="border rounded-3 p-3 d-flex gap-3 text-decoration-none h-100"
                                                href="${pageContext.request.contextPath}/admin/users">

                                                <i class="bi bi-people fs-3 text-primary"></i>

                                                <div>

                                                    <strong class="d-block text-dark">
                                                        Quản lý người dùng
                                                    </strong>

                                                    <small class="text-muted">
                                                        Khóa hoặc kích hoạt lại tài khoản
                                                    </small>

                                                </div>

                                            </a>

                                        </div>


                                        <!-- Department -->
                                        <div class="col-md-6">

                                            <a class="border rounded-3 p-3 d-flex gap-3 text-decoration-none h-100"
                                                href="${pageContext.request.contextPath}/admin/departments">

                                                <i class="bi bi-buildings fs-3 text-success"></i>

                                                <div>

                                                    <strong class="d-block text-dark">
                                                        Quản lý phòng ban
                                                    </strong>

                                                    <small class="text-muted">
                                                        Thêm, sửa và quản lý cơ cấu
                                                    </small>

                                                </div>

                                            </a>

                                        </div>


                                        <!-- Audit -->
                                        <div class="col-md-6">

                                            <a class="border rounded-3 p-3 d-flex gap-3 text-decoration-none h-100"
                                                href="${pageContext.request.contextPath}/admin/audit-logs">

                                                <i class="bi bi-journal-text fs-3 text-warning"></i>

                                                <div>

                                                    <strong class="d-block text-dark">
                                                        Audit logs
                                                    </strong>

                                                    <small class="text-muted">
                                                        Theo dõi hoạt động trong hệ thống
                                                    </small>

                                                </div>

                                            </a>

                                        </div>

                                    </div>

                                </div>

                            </section>

                        </div>


                        <!-- AN TOÀN HỆ THỐNG -->
                        <div class="col-lg-5">

                            <section class="card h-100">

                                <div class="card-header bg-white border-0 px-4 pt-4">

                                    <h5 class="mb-1">
                                        An toàn hệ thống
                                    </h5>

                                    <small class="text-muted">
                                        Tóm tắt tình trạng tài khoản.
                                    </small>

                                </div>


                                <div class="card-body px-4 pb-4">

                                    <div class="d-flex justify-content-between border-bottom py-3">

                                        <span>
                                            Người dùng hoạt động
                                        </span>

                                        <strong class="text-success">
                                            <c:out value="${stats.activeUsers}" />
                                        </strong>

                                    </div>


                                    <div class="d-flex justify-content-between border-bottom py-3">

                                        <span>
                                            Người dùng bị khóa
                                        </span>

                                        <strong class="text-danger">
                                            <c:out value="${stats.lockedUsers}" />
                                        </strong>

                                    </div>


                                    <div class="d-flex justify-content-between py-3">

                                        <span>
                                            Tổng đơn ứng tuyển
                                        </span>

                                        <strong>
                                            <c:out value="${stats.totalApplications}" />
                                        </strong>

                                    </div>


                                    <a class="btn btn-outline-primary w-100 mt-2"
                                        href="${pageContext.request.contextPath}/admin/audit-logs">
                                        Xem nhật ký hoạt động
                                    </a>

                                </div>

                            </section>

                        </div>

                    </div>

                </div>

            </main>

        </div>

        <jsp:include page="/WEB-INF/views/common/footer.jsp" />