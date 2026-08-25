<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Quản lý phòng ban | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Quản trị hệ thống</p><h4 class="mb-0">Quản lý phòng ban</h4></div><button class="btn btn-primary" type="button" data-bs-toggle="modal" data-bs-target="#departmentModal"><i class="bi bi-plus-lg me-1"></i> Thêm phòng ban</button></div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="successMessage" scope="session" /></c:if>
                <c:if test="${not empty sessionScope.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${sessionScope.error}" /><button class="btn-close" type="button" data-bs-dismiss="alert"></button></div><c:remove var="error" scope="session" /></c:if>
                <section class="card"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Danh sách phòng ban</h5><small class="text-muted">Không thể xóa phòng ban đang được tham chiếu bởi dữ liệu tuyển dụng.</small></div><div class="table-responsive"><table class="table table-hover align-middle mb-0"><thead class="table-light"><tr><th class="ps-4">ID</th><th>Tên phòng ban</th><th>Mô tả</th><th>Ngày tạo</th><th class="text-end pe-4">Thao tác</th></tr></thead><tbody>
                    <c:forEach var="department" items="${departments}"><tr><td class="ps-4">#<c:out value="${department.id}" /></td><td class="fw-semibold"><c:out value="${department.name}" /></td><td class="text-muted"><c:out value="${department.description}" /></td><td><c:out value="${department.createdAt}" /></td><td class="text-end pe-4"><button class="btn btn-sm btn-outline-primary edit-department" type="button" data-id="${department.id}" data-name="<c:out value='${department.name}'/>" data-description="<c:out value='${department.description}'/>" data-bs-toggle="modal" data-bs-target="#departmentModal" aria-label="Chỉnh sửa phòng ban"><i class="bi bi-pencil"></i></button><form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/departments/delete" data-confirm="Bạn có chắc muốn xóa phòng ban này?"><input type="hidden" name="id" value="${department.id}"><button class="btn btn-sm btn-outline-danger" type="submit" aria-label="Xóa phòng ban"><i class="bi bi-trash"></i></button></form></td></tr></c:forEach>
                    <c:if test="${empty departments}"><tr><td colspan="5" class="py-5 text-center text-muted"><i class="bi bi-buildings d-block fs-3 mb-2"></i>Chưa có phòng ban nào.</td></tr></c:if>
                </tbody></table></div></section>
            </div>
        </main>
    </div>
</div>

<div class="modal fade" id="departmentModal" tabindex="-1" aria-labelledby="departmentModalLabel" aria-hidden="true">
    <div class="modal-dialog"><div class="modal-content"><form id="departmentForm" method="post" action="${pageContext.request.contextPath}/admin/departments/create"><div class="modal-header"><h5 class="modal-title" id="departmentModalLabel">Thêm phòng ban</h5><button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Đóng"></button></div><div class="modal-body"><input id="departmentId" name="id" type="hidden"><div class="mb-3"><label class="form-label" for="departmentName">Tên phòng ban <span class="text-danger">*</span></label><input class="form-control" id="departmentName" name="name" maxlength="100" required></div><div class="mb-0"><label class="form-label" for="departmentDescription">Mô tả</label><textarea class="form-control" id="departmentDescription" name="description" rows="4"></textarea></div></div><div class="modal-footer"><button class="btn btn-light" type="button" data-bs-dismiss="modal">Hủy</button><button class="btn btn-primary" type="submit">Lưu phòng ban</button></div></form></div></div>
</div>
<script>
    document.querySelectorAll('.edit-department').forEach(function (button) {
        button.addEventListener('click', function () {
            document.getElementById('departmentModalLabel').textContent = 'Chỉnh sửa phòng ban';
            document.getElementById('departmentForm').action = '${pageContext.request.contextPath}/admin/departments/update';
            document.getElementById('departmentId').value = button.dataset.id;
            document.getElementById('departmentName').value = button.dataset.name;
            document.getElementById('departmentDescription').value = button.dataset.description;
        });
    });
    document.getElementById('departmentModal').addEventListener('show.bs.modal', function (event) {
        if (event.relatedTarget && !event.relatedTarget.classList.contains('edit-department')) {
            document.getElementById('departmentModalLabel').textContent = 'Thêm phòng ban';
            document.getElementById('departmentForm').action = '${pageContext.request.contextPath}/admin/departments/create';
            document.getElementById('departmentForm').reset();
            document.getElementById('departmentId').value = '';
        }
    });
</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />

