<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Tiếp nhận nhân sự | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Tuyển dụng</p><h4 class="mb-0">Tiếp nhận nhân sự</h4></div><span class="text-muted small">Theo dõi tiến độ nhân sự mới</span></div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="successMessage" scope="session" /></c:if>
                <c:if test="${not empty sessionScope.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${sessionScope.error}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="error" scope="session" /></c:if>

                <div class="row g-4">
                    <div class="col-xl-8">
                        <section class="card"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Nhân sự đang được tiếp nhận</h5><small class="text-muted">Quy trình tiếp nhận được tạo tự động khi thư mời được chấp nhận.</small></div><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th class="ps-4">Nhân sự</th><th>Vị trí</th><th>Tiến độ</th><th>Trạng thái</th><th class="text-end pe-4">Chi tiết</th></tr></thead><tbody>
                            <c:forEach var="item" items="${onboardings}">
                                <tr><td class="ps-4"><div class="fw-semibold"><c:out value="${item.candidateName}" /></div><small class="text-muted">Đơn ứng tuyển #<c:out value="${item.applicationId}" /></small></td><td><c:out value="${item.jobTitle}" /></td><td style="min-width:160px;"><div class="d-flex justify-content-between small mb-1"><span><c:out value="${item.progress}" />%</span></div><div class="progress" role="progressbar" aria-valuenow="${item.progress}" aria-valuemin="0" aria-valuemax="100"><div class="progress-bar ${item.progress eq 100 ? 'bg-success' : ''}" style="width:${item.progress}%"></div></div></td><td><c:choose><c:when test="${item.status eq 'COMPLETED'}"><span class="badge text-bg-success">Hoàn thành</span></c:when><c:when test="${item.status eq 'IN_PROGRESS'}"><span class="badge text-bg-primary">Đang thực hiện</span></c:when><c:otherwise><span class="badge text-bg-secondary">Chưa bắt đầu</span></c:otherwise></c:choose></td><td class="text-end pe-4"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/hr/onboarding?id=${item.id}">Xem đầu việc</a></td></tr>
                            </c:forEach>
                            <c:if test="${empty onboardings}"><tr><td colspan="5" class="py-5 text-center text-muted"><i class="bi bi-rocket-takeoff d-block fs-3 mb-2"></i>Chưa có nhân sự nào trong giai đoạn tiếp nhận.</td></tr></c:if>
                        </tbody></table></div></section>
                    </div>
                    <div class="col-xl-4">
                        <section class="card mb-4"><div class="card-body p-4"><h5 class="mb-3">Thêm đầu việc</h5><form method="post" action="${pageContext.request.contextPath}/hr/onboarding/tasks/create"><input type="hidden" name="onboardingId" value="${param.id}"><div class="mb-3"><label class="form-label" for="taskName">Tên đầu việc</label><input class="form-control" id="taskName" name="taskName" required maxlength="255" placeholder="Ví dụ: Ký hợp đồng"></div><div class="form-check mb-3"><input class="form-check-input" id="required" name="required" type="checkbox" value="true" checked><label class="form-check-label" for="required">Đầu việc bắt buộc</label></div><button class="btn btn-primary w-100" type="submit" ${empty param.id ? 'disabled' : ''}>Thêm đầu việc</button><c:if test="${empty param.id}"><p class="form-text mb-0 mt-2">Chọn một nhân sự ở bảng bên trái để quản lý danh sách đầu việc.</p></c:if></form></div></section>
                        <section class="card"><div class="card-body p-4"><h5 class="mb-3">Danh sách đầu việc</h5><c:forEach var="task" items="${tasks}"><div class="d-flex align-items-start gap-2 py-2 border-bottom"><i class="bi ${task.status eq 'DONE' ? 'bi-check-circle-fill text-success' : 'bi-circle text-muted'} mt-1"></i><div class="flex-grow-1"><div class="${task.status eq 'DONE' ? 'text-decoration-line-through text-muted' : ''}"><c:out value="${task.taskName}" /></div><small class="text-muted"><c:out value="${task.isRequired ? 'Bắt buộc' : 'Tùy chọn'}" /> · <c:out value="${task.status}" /></small></div></div></c:forEach><c:if test="${empty tasks}"><p class="text-muted mb-0">Chưa có đầu việc để hiển thị.</p></c:if></div></section>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

