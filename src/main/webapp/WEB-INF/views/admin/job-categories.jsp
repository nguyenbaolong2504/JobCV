<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Danh mục nghề nghiệp | RecruitFlow" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/admin-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div>
                    <p class="text-muted small mb-1">Quản trị hệ thống</p>
                    <h4 class="mb-0">Danh mục nghề nghiệp</h4>
                </div>
                <button class="btn btn-primary" type="button" data-bs-toggle="modal" data-bs-target="#jobCategoryModal">
                    <i class="bi bi-plus-lg me-1"></i> Thêm danh mục
                </button>
            </div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}" /></div></c:if>

                <div class="alert alert-info border-0 shadow-sm mb-4" role="note">
                    <i class="bi bi-diagram-3 me-2"></i>
                    Tạo <strong>danh mục chính</strong> (ví dụ: Công nghệ thông tin), sau đó tạo <strong>danh mục con</strong>
                    (ví dụ: Phát triển phần mềm). Tin tuyển dụng chỉ gán vào danh mục con hoặc danh mục chính chưa có danh mục con.
                </div>

                <section class="card shadow-sm border-0">
                    <div class="card-header bg-white border-0 px-4 pt-4">
                        <h5 class="mb-1">Cấu trúc danh mục hiển thị cho người tìm việc</h5>
                        <small class="text-muted">Danh mục ngừng hiển thị hoặc đang được dùng bởi tin tuyển dụng sẽ được bảo vệ khỏi thao tác gây mất dữ liệu.</small>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th class="ps-4">Danh mục</th>
                                    <th>Cấp / danh mục cha</th>
                                    <th>Thứ tự</th>
                                    <th>Hiển thị</th>
                                    <th>Mô tả</th>
                                    <th class="text-end pe-4">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="category" items="${jobCategories}">
                                    <tr>
                                        <td class="ps-4">
                                            <div class="d-flex align-items-center gap-2">
                                                <span class="rounded-circle d-inline-flex align-items-center justify-content-center ${empty category.parentId ? 'bg-primary-subtle text-primary' : 'bg-light text-secondary'}" style="width: 2rem; height: 2rem;">
                                                    <i class="bi ${empty category.parentId ? 'bi-folder2-open' : 'bi-tag'}"></i>
                                                </span>
                                                <div>
                                                    <div class="fw-semibold"><c:out value="${category.name}" /></div>
                                                    <small class="text-muted">#<c:out value="${category.id}" /></small>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty category.parentId}"><span class="badge text-bg-primary-subtle text-primary">Danh mục chính</span></c:when>
                                                <c:otherwise><span class="small text-muted"><i class="bi bi-arrow-return-right me-1"></i><c:out value="${category.parentName}" /></span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><c:out value="${category.displayOrder}" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${category.active}"><span class="badge text-bg-success">Đang hiển thị</span></c:when>
                                                <c:otherwise><span class="badge text-bg-secondary">Đã ẩn</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-muted"><c:out value="${category.description}" /></td>
                                        <td class="text-end pe-4 text-nowrap">
                                            <button class="btn btn-sm btn-outline-primary edit-job-category" type="button"
                                                data-id="<c:out value='${category.id}'/>"
                                                data-name="<c:out value='${category.name}'/>"
                                                data-parent-id="<c:out value='${category.parentId}'/>"
                                                data-description="<c:out value='${category.description}'/>"
                                                data-display-order="<c:out value='${category.displayOrder}'/>"
                                                data-active="${category.active}"
                                                data-bs-toggle="modal" data-bs-target="#jobCategoryModal"
                                                aria-label="Chỉnh sửa <c:out value='${category.name}'/>">
                                                <i class="bi bi-pencil"></i>
                                            </button>
                                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/job-categories/delete" onsubmit="return confirm('Xóa danh mục này? Danh mục có danh mục con hoặc tin tuyển dụng sẽ không thể xóa.');">
                                                <input type="hidden" name="id" value="<c:out value='${category.id}'/>">
                                                <button class="btn btn-sm btn-outline-danger" type="submit" aria-label="Xóa <c:out value='${category.name}'/>"><i class="bi bi-trash"></i></button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty jobCategories}">
                                    <tr><td colspan="6" class="py-5 text-center text-muted"><i class="bi bi-folder-plus d-block fs-3 mb-2"></i>Chưa có danh mục nghề nghiệp. Hãy tạo danh mục chính đầu tiên.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </section>
            </div>
        </main>
    </div>
</div>

<div class="modal fade" id="jobCategoryModal" tabindex="-1" aria-labelledby="jobCategoryModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="jobCategoryForm" method="post" action="${pageContext.request.contextPath}/admin/job-categories/create">
                <div class="modal-header">
                    <h5 class="modal-title" id="jobCategoryModalLabel">Thêm danh mục nghề nghiệp</h5>
                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Đóng"></button>
                </div>
                <div class="modal-body">
                    <input id="jobCategoryId" name="id" type="hidden">
                    <div class="mb-3">
                        <label class="form-label" for="jobCategoryName">Tên danh mục <span class="text-danger">*</span></label>
                        <input class="form-control" id="jobCategoryName" name="name" maxlength="120" required autocomplete="off">
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="jobCategoryParent">Danh mục cha</label>
                        <select class="form-select" id="jobCategoryParent" name="parentId">
                            <option value="">-- Danh mục chính --</option>
                            <c:forEach var="category" items="${jobCategories}">
                                <c:if test="${empty category.parentId}">
                                    <option value="<c:out value='${category.id}'/>"><c:out value="${category.name}" /></option>
                                </c:if>
                            </c:forEach>
                        </select>
                        <div class="form-text">Chỉ chọn danh mục chính; hệ thống giới hạn một cấp danh mục con để menu rõ ràng.</div>
                    </div>
                    <div class="row g-3 mb-3">
                        <div class="col-sm-6">
                            <label class="form-label" for="jobCategoryOrder">Thứ tự hiển thị</label>
                            <input class="form-control" id="jobCategoryOrder" name="displayOrder" type="number" min="0" max="10000" value="0">
                        </div>
                        <div class="col-sm-6 d-flex align-items-end">
                            <div class="form-check form-switch mb-2">
                                <input class="form-check-input" id="jobCategoryActive" name="active" type="checkbox" value="true" checked>
                                <label class="form-check-label" for="jobCategoryActive">Hiển thị cho người tìm việc</label>
                            </div>
                        </div>
                    </div>
                    <div class="mb-0">
                        <label class="form-label" for="jobCategoryDescription">Mô tả</label>
                        <textarea class="form-control" id="jobCategoryDescription" name="description" rows="3" maxlength="500"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-light" type="button" data-bs-dismiss="modal">Hủy</button>
                    <button class="btn btn-primary" type="submit"><i class="bi bi-save me-1"></i>Lưu danh mục</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    (function () {
        var modal = document.getElementById('jobCategoryModal');
        var form = document.getElementById('jobCategoryForm');
        var title = document.getElementById('jobCategoryModalLabel');
        var id = document.getElementById('jobCategoryId');
        var name = document.getElementById('jobCategoryName');
        var parent = document.getElementById('jobCategoryParent');
        var order = document.getElementById('jobCategoryOrder');
        var description = document.getElementById('jobCategoryDescription');
        var active = document.getElementById('jobCategoryActive');

        document.querySelectorAll('.edit-job-category').forEach(function (button) {
            button.addEventListener('click', function () {
                title.textContent = 'Chỉnh sửa danh mục nghề nghiệp';
                form.action = '${pageContext.request.contextPath}/admin/job-categories/update';
                id.value = button.dataset.id;
                name.value = button.dataset.name || '';
                parent.value = button.dataset.parentId || '';
                order.value = button.dataset.displayOrder || 0;
                description.value = button.dataset.description || '';
                active.checked = button.dataset.active === 'true';
                Array.prototype.forEach.call(parent.options, function (option) {
                    option.hidden = option.value === button.dataset.id;
                });
            });
        });

        modal.addEventListener('show.bs.modal', function (event) {
            if (event.relatedTarget && !event.relatedTarget.classList.contains('edit-job-category')) {
                title.textContent = 'Thêm danh mục nghề nghiệp';
                form.action = '${pageContext.request.contextPath}/admin/job-categories/create';
                form.reset();
                id.value = '';
                order.value = 0;
                active.checked = true;
                Array.prototype.forEach.call(parent.options, function (option) { option.hidden = false; });
            }
        });
    }());
</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
