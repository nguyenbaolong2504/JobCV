<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Đơn ứng tuyển | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<c:url var="boardUrl" value="/hr/applications">
    <c:param name="view" value="board" />
    <c:param name="keyword" value="${param.keyword}" />
    <c:param name="jobId" value="${param.jobId}" />
    <c:param name="status" value="${param.status}" />
    <c:param name="minMatchScore" value="${param.minMatchScore}" />
</c:url>
<c:url var="listUrl" value="/hr/applications">
    <c:param name="view" value="list" />
    <c:param name="keyword" value="${param.keyword}" />
    <c:param name="jobId" value="${param.jobId}" />
    <c:param name="status" value="${param.status}" />
    <c:param name="minMatchScore" value="${param.minMatchScore}" />
</c:url>
<c:set var="filtersActive" value="${not empty param.keyword or not empty param.jobId or not empty param.status or not empty param.minMatchScore}" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar">
                <div><p class="text-muted small mb-1">Không gian tuyển dụng</p><h4 class="mb-0">Quản lý ứng viên</h4></div>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/hr/interviews/create"><i class="bi bi-calendar-plus me-1"></i> Lên lịch phỏng vấn</a>
            </div>

            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <c:if test="${not empty sessionScope.successMessage}"><div class="alert alert-success alert-dismissible fade show" role="alert"><c:out value="${sessionScope.successMessage}" /><button class="btn-close" type="button" data-bs-dismiss="alert" aria-label="Đóng"></button></div><c:remove var="successMessage" scope="session" /></c:if>
                <c:if test="${not empty sessionScope.error}"><div class="alert alert-danger alert-dismissible fade show" role="alert"><c:out value="${sessionScope.error}" /><button class="btn-close" type="button" data-bs-dismiss="alert" aria-label="Đóng"></button></div><c:remove var="error" scope="session" /></c:if>

                <section class="card application-filter-card mb-4">
                    <div class="card-body p-4">
                        <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
                            <div><h6 class="mb-1"><i class="bi bi-funnel me-2 text-primary"></i>Lọc ứng viên</h6><p class="text-muted small mb-0">Tìm nhanh theo ứng viên, vị trí và mức độ phù hợp.</p></div>
                            <c:if test="${filtersActive}"><a class="btn btn-sm btn-light border" href="${pageContext.request.contextPath}/hr/applications?view=${boardView ? 'board' : 'list'}"><i class="bi bi-x-lg me-1"></i>Xóa bộ lọc</a></c:if>
                        </div>
                        <form class="row gy-3 gx-3 align-items-end" method="get" action="${pageContext.request.contextPath}/hr/applications">
                            <input type="hidden" name="view" value="${boardView ? 'board' : 'list'}">
                            <div class="col-xl-4 col-lg-6">
                                <label class="form-label small fw-semibold" for="applicationKeyword">Tìm kiếm</label>
                                <div class="input-group"><span class="input-group-text bg-white"><i class="bi bi-search text-muted"></i></span><input class="form-control border-start-0" id="applicationKeyword" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="Tên, email hoặc vị trí"></div>
                            </div>
                            <div class="col-xl-3 col-lg-6">
                                <label class="form-label small fw-semibold" for="applicationJob">Tin tuyển dụng</label>
                                <select class="form-select" id="applicationJob" name="jobId"><option value="">Tất cả vị trí</option><c:forEach var="job" items="${jobs}"><option value="${job.id}" <c:if test="${param.jobId eq job.id}">selected</c:if>><c:out value="${job.title}" /></option></c:forEach></select>
                            </div>
                            <div class="col-xl-2 col-lg-5">
                                <label class="form-label small fw-semibold" for="applicationStatus">Trạng thái</label>
                                <select class="form-select" id="applicationStatus" name="status">
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="SUBMITTED" <c:if test="${param.status eq 'SUBMITTED'}">selected</c:if>>Mới nhận</option>
                                    <option value="SCREENING" <c:if test="${param.status eq 'SCREENING'}">selected</c:if>>Sàng lọc</option>
                                    <option value="SHORTLISTED" <c:if test="${param.status eq 'SHORTLISTED'}">selected</c:if>>Danh sách ngắn</option>
                                    <option value="INTERVIEW_SCHEDULED" <c:if test="${param.status eq 'INTERVIEW_SCHEDULED'}">selected</c:if>>Đã hẹn phỏng vấn</option>
                                    <option value="INTERVIEWED" <c:if test="${param.status eq 'INTERVIEWED'}">selected</c:if>>Đã phỏng vấn</option>
                                    <option value="OFFERED" <c:if test="${param.status eq 'OFFERED'}">selected</c:if>>Đã gửi thư mời</option>
                                    <option value="HIRED" <c:if test="${param.status eq 'HIRED'}">selected</c:if>>Đã tuyển</option>
                                    <option value="REJECTED" <c:if test="${param.status eq 'REJECTED'}">selected</c:if>>Đã từ chối</option>
                                    <option value="WITHDRAWN" <c:if test="${param.status eq 'WITHDRAWN'}">selected</c:if>>Đã rút đơn</option>
                                </select>
                            </div>
                            <div class="col-xl-2 col-lg-4">
                                <label class="form-label small fw-semibold" for="applicationScore">Điểm phù hợp từ</label>
                                <div class="input-group"><input class="form-control" id="applicationScore" type="number" min="0" max="100" name="minMatchScore" value="<c:out value='${param.minMatchScore}'/>" placeholder="0"><span class="input-group-text bg-white">%</span></div>
                            </div>
                            <div class="col-xl-1 col-lg-3 d-grid"><button class="btn btn-primary" type="submit"><i class="bi bi-search me-1"></i>Lọc</button></div>
                        </form>
                    </div>
                </section>

                <c:if test="${boardView}">
                    <c:set var="activeTotal" value="0" /><c:set var="interviewTotal" value="0" /><c:set var="highMatchTotal" value="0" /><c:set var="closedTotal" value="0" /><c:set var="scoreTotal" value="0" /><c:set var="scoreCount" value="0" />
                    <c:forEach var="summaryItem" items="${applicationPage.items}">
                        <c:choose>
                            <c:when test="${summaryItem.status eq 'REJECTED' or summaryItem.status eq 'WITHDRAWN'}"><c:set var="closedTotal" value="${closedTotal + 1}" /></c:when>
                            <c:otherwise>
                                <c:set var="activeTotal" value="${activeTotal + 1}" />
                                <c:if test="${summaryItem.matchScore ge 70}"><c:set var="highMatchTotal" value="${highMatchTotal + 1}" /></c:if>
                                <c:if test="${summaryItem.status eq 'INTERVIEW_SCHEDULED' or summaryItem.status eq 'INTERVIEWED'}"><c:set var="interviewTotal" value="${interviewTotal + 1}" /></c:if>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${not empty summaryItem.matchScore}"><c:set var="scoreTotal" value="${scoreTotal + summaryItem.matchScore}" /><c:set var="scoreCount" value="${scoreCount + 1}" /></c:if>
                    </c:forEach>

                    <div class="row g-3 mb-4 application-kpis">
                        <div class="col-sm-6 col-xl-3"><article class="application-kpi"><span class="application-kpi-icon kpi-blue"><i class="bi bi-people"></i></span><div><small>Đang trong quy trình</small><strong><c:out value="${activeTotal}" /></strong></div></article></div>
                        <div class="col-sm-6 col-xl-3"><article class="application-kpi"><span class="application-kpi-icon kpi-violet"><i class="bi bi-calendar2-check"></i></span><div><small>Ở vòng phỏng vấn</small><strong><c:out value="${interviewTotal}" /></strong></div></article></div>
                        <div class="col-sm-6 col-xl-3"><article class="application-kpi"><span class="application-kpi-icon kpi-green"><i class="bi bi-stars"></i></span><div><small>Phù hợp từ 70%</small><strong><c:out value="${highMatchTotal}" /></strong></div></article></div>
                        <div class="col-sm-6 col-xl-3"><article class="application-kpi"><span class="application-kpi-icon kpi-amber"><i class="bi bi-speedometer2"></i></span><div><small>Điểm phù hợp trung bình</small><strong><fmt:formatNumber value="${scoreCount gt 0 ? scoreTotal / scoreCount : 0}" maxFractionDigits="0" />%</strong></div></article></div>
                    </div>
                </c:if>

                <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
                    <div><h5 class="mb-1">${boardView ? 'Quy trình tuyển dụng' : 'Danh sách ứng viên'}</h5><p class="text-muted small mb-0">${boardView ? 'Theo dõi ứng viên theo từng giai đoạn xử lý.' : 'Xem thông tin chi tiết và trạng thái từng hồ sơ.'}</p></div>
                    <div class="application-view-switch" role="group" aria-label="Kiểu hiển thị"><a class="${boardView ? 'active' : ''}" href="${boardUrl}"><i class="bi bi-kanban"></i>Quy trình</a><a class="${not boardView ? 'active' : ''}" href="${listUrl}"><i class="bi bi-list-ul"></i>Danh sách</a></div>
                </div>

                <c:if test="${boardView}">
                    <c:choose>
                        <c:when test="${activeTotal eq 0}">
                            <section class="pipeline-empty-state">
                                <span class="pipeline-empty-illustration"><i class="bi ${filtersActive ? 'bi-funnel' : 'bi-person-plus'}"></i></span>
                                <h5>${filtersActive ? 'Không tìm thấy hồ sơ phù hợp' : 'Quy trình đang chờ ứng viên đầu tiên'}</h5>
                                <p><c:choose><c:when test="${filtersActive}">Hãy thay đổi hoặc xóa bộ lọc để xem thêm ứng viên trong quy trình.</c:when><c:otherwise>Khi ứng viên nộp hồ sơ vào tin đang tuyển, họ sẽ tự động xuất hiện tại đây để bộ phận Nhân sự sàng lọc.</c:otherwise></c:choose></p>
                                <div class="d-flex flex-wrap justify-content-center gap-2"><c:if test="${filtersActive}"><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/hr/applications"><i class="bi bi-arrow-counterclockwise me-1"></i>Xóa bộ lọc</a></c:if><a class="btn btn-primary" href="${pageContext.request.contextPath}/hr/jobs"><i class="bi bi-briefcase me-1"></i>Quản lý tin tuyển dụng</a></div>
                                <c:if test="${closedTotal gt 0}"><small class="text-muted mt-3">Có <c:out value="${closedTotal}" /> hồ sơ đã kết thúc quy trình.</small></c:if>
                            </section>
                        </c:when>
                        <c:otherwise>
                            <section class="pipeline-board" aria-label="Quy trình tuyển dụng">
                                <c:set var="pipelineStatuses" value="SUBMITTED,SCREENING,SHORTLISTED,INTERVIEW_SCHEDULED,INTERVIEWED,OFFERED,HIRED" />
                                <c:forTokens var="columnStatus" items="${pipelineStatuses}" delims=",">
                                    <c:set var="columnCount" value="0" /><c:forEach var="countItem" items="${applicationPage.items}"><c:if test="${countItem.status eq columnStatus}"><c:set var="columnCount" value="${columnCount + 1}" /></c:if></c:forEach>
                                    <section class="pipeline-column pipeline-${columnStatus}">
                                        <header class="pipeline-column-header">
                                            <div class="pipeline-column-title"><span class="pipeline-status-icon"><c:choose><c:when test="${columnStatus eq 'SUBMITTED'}"><i class="bi bi-inbox"></i></c:when><c:when test="${columnStatus eq 'SCREENING'}"><i class="bi bi-funnel"></i></c:when><c:when test="${columnStatus eq 'SHORTLISTED'}"><i class="bi bi-bookmark-star"></i></c:when><c:when test="${columnStatus eq 'INTERVIEW_SCHEDULED'}"><i class="bi bi-calendar-event"></i></c:when><c:when test="${columnStatus eq 'INTERVIEWED'}"><i class="bi bi-chat-square-text"></i></c:when><c:when test="${columnStatus eq 'OFFERED'}"><i class="bi bi-envelope-paper"></i></c:when><c:otherwise><i class="bi bi-person-check"></i></c:otherwise></c:choose></span>
                                                <div><h6><c:choose><c:when test="${columnStatus eq 'SUBMITTED'}">Mới nhận</c:when><c:when test="${columnStatus eq 'SCREENING'}">Sàng lọc</c:when><c:when test="${columnStatus eq 'SHORTLISTED'}">Danh sách ngắn</c:when><c:when test="${columnStatus eq 'INTERVIEW_SCHEDULED'}">Đã hẹn phỏng vấn</c:when><c:when test="${columnStatus eq 'INTERVIEWED'}">Đã phỏng vấn</c:when><c:when test="${columnStatus eq 'OFFERED'}">Đã gửi thư mời</c:when><c:otherwise>Đã tuyển</c:otherwise></c:choose></h6><small><c:out value="${columnCount}" /> hồ sơ</small></div>
                                            </div>
                                            <span class="pipeline-count"><c:out value="${columnCount}" /></span>
                                        </header>
                                        <div class="pipeline-cards">
                                            <c:forEach var="item" items="${applicationPage.items}"><c:if test="${item.status eq columnStatus}">
                                                <a class="pipeline-card" href="${pageContext.request.contextPath}/hr/applications/detail?id=${item.id}">
                                                    <div class="pipeline-card-person"><span class="pipeline-avatar"><c:out value="${fn:toUpperCase(fn:substring(item.candidateName, 0, 1))}" /></span><div class="min-w-0"><strong class="text-truncate"><c:out value="${item.candidateName}" /></strong><small class="text-muted text-truncate"><c:out value="${item.candidateEmail}" /></small></div></div>
                                                    <div class="pipeline-job"><i class="bi bi-briefcase"></i><span class="text-truncate"><c:out value="${item.jobTitle}" /></span></div>
                                                    <div class="pipeline-match-row"><span>Độ phù hợp</span><strong class="${item.matchScore ge 70 ? 'text-success' : item.matchScore ge 45 ? 'text-warning' : 'text-danger'}"><c:out value="${item.matchScore}" />%</strong></div>
                                                    <div class="pipeline-progress" aria-hidden="true"><span class="${item.matchScore ge 70 ? 'score-high' : item.matchScore ge 45 ? 'score-medium' : 'score-low'}" style="width: ${item.matchScore}%"></span></div>
                                                    <footer><span><i class="bi bi-calendar3"></i><fmt:formatDate value="${item.appliedAt}" pattern="dd/MM/yyyy" /></span><span class="pipeline-card-link">Xem hồ sơ<i class="bi bi-arrow-right"></i></span></footer>
                                                </a>
                                            </c:if></c:forEach>
                                            <c:if test="${columnCount eq 0}"><div class="pipeline-column-empty"><i class="bi bi-inbox"></i><span>Chưa có hồ sơ</span></div></c:if>
                                        </div>
                                    </section>
                                </c:forTokens>
                            </section>
                            <c:if test="${closedTotal gt 0}"><details class="pipeline-closed mt-3"><summary><span><i class="bi bi-archive me-2"></i>Hồ sơ đã kết thúc</span><span class="badge rounded-pill text-bg-light border"><c:out value="${closedTotal}" /></span></summary><div class="d-flex flex-wrap gap-2 mt-3"><c:forEach var="item" items="${applicationPage.items}"><c:if test="${item.status eq 'REJECTED' or item.status eq 'WITHDRAWN'}"><a class="pipeline-closed-item" href="${pageContext.request.contextPath}/hr/applications/detail?id=${item.id}"><span class="pipeline-avatar pipeline-avatar-sm"><c:out value="${fn:toUpperCase(fn:substring(item.candidateName, 0, 1))}" /></span><span><strong><c:out value="${item.candidateName}" /></strong><small>${item.status eq 'REJECTED' ? 'Đã từ chối' : 'Đã rút đơn'}</small></span></a></c:if></c:forEach></div></details></c:if>
                        </c:otherwise>
                    </c:choose>
                </c:if>

                <c:if test="${not boardView}">
                    <section class="card application-table-card">
                        <div class="table-responsive"><table class="table table-hover align-middle mb-0">
                            <thead><tr><th class="ps-4">Ứng viên</th><th>Vị trí ứng tuyển</th><th>Ngày nộp</th><th>Điểm phù hợp</th><th>Trạng thái</th><th class="text-end pe-4">Thao tác</th></tr></thead>
                            <tbody>
                                <c:forEach var="item" items="${applicationPage.items}"><tr>
                                    <td class="ps-4"><div class="d-flex align-items-center gap-2"><span class="pipeline-avatar pipeline-avatar-sm"><c:out value="${fn:toUpperCase(fn:substring(item.candidateName, 0, 1))}" /></span><div><div class="fw-semibold"><c:out value="${item.candidateName}" /></div><small class="text-muted"><c:out value="${item.candidateEmail}" /></small></div></div></td>
                                    <td><div class="fw-semibold"><c:out value="${item.jobTitle}" /></div><small class="text-muted"><c:out value="${item.jobCode}" /></small></td>
                                    <td><fmt:formatDate value="${item.appliedAt}" pattern="dd/MM/yyyy" /></td>
                                    <td><span class="pipeline-score ${item.matchScore ge 70 ? 'score-high' : item.matchScore ge 45 ? 'score-medium' : 'score-low'}"><c:out value="${item.matchScore}" />%</span></td>
                                    <td><c:choose><c:when test="${item.status eq 'SUBMITTED'}"><span class="status-badge status-submitted">Mới nhận</span></c:when><c:when test="${item.status eq 'SCREENING'}"><span class="status-badge status-screening">Sàng lọc</span></c:when><c:when test="${item.status eq 'SHORTLISTED'}"><span class="status-badge status-shortlisted">Danh sách ngắn</span></c:when><c:when test="${item.status eq 'INTERVIEW_SCHEDULED'}"><span class="status-badge status-interview-scheduled">Đã hẹn phỏng vấn</span></c:when><c:when test="${item.status eq 'INTERVIEWED'}"><span class="status-badge status-interviewed">Đã phỏng vấn</span></c:when><c:when test="${item.status eq 'OFFERED'}"><span class="status-badge status-offered">Đã gửi thư mời</span></c:when><c:when test="${item.status eq 'HIRED'}"><span class="status-badge status-hired">Đã tuyển</span></c:when><c:when test="${item.status eq 'WITHDRAWN'}"><span class="status-badge status-withdrawn">Đã rút đơn</span></c:when><c:otherwise><span class="status-badge status-rejected">Đã từ chối</span></c:otherwise></c:choose></td>
                                    <td class="text-end pe-4"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/hr/applications/detail?id=${item.id}">Xem hồ sơ <i class="bi bi-arrow-right ms-1"></i></a></td>
                                </tr></c:forEach>
                                <c:if test="${empty applicationPage.items}"><tr><td colspan="6" class="text-center text-muted py-5"><i class="bi bi-people d-block fs-3 mb-2"></i>Không có đơn ứng tuyển phù hợp.</td></tr></c:if>
                            </tbody>
                        </table></div>
                        <c:if test="${not empty applicationPage and applicationPage.totalPages gt 1}"><nav class="p-3 border-top" aria-label="Phân trang đơn ứng tuyển"><ul class="pagination pagination-sm mb-0 justify-content-end"><c:forEach begin="1" end="${applicationPage.totalPages}" var="p"><c:url var="pageUrl" value="/hr/applications"><c:param name="view" value="list" /><c:param name="page" value="${p}" /><c:param name="keyword" value="${param.keyword}" /><c:param name="jobId" value="${param.jobId}" /><c:param name="status" value="${param.status}" /><c:param name="minMatchScore" value="${param.minMatchScore}" /></c:url><li class="page-item ${p eq applicationPage.currentPage ? 'active' : ''}"><a class="page-link" href="${pageUrl}">${p}</a></li></c:forEach></ul></nav></c:if>
                    </section>
                </c:if>
            </div>
        </main>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
