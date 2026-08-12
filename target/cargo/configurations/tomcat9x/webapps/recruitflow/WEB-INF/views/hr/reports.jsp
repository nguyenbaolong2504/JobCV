<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Báo cáo tuyển dụng | RecruitFlow" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100">
            <div class="topbar"><div><p class="text-muted small mb-1">Phân tích</p><h4 class="mb-0">Báo cáo tuyển dụng</h4></div><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/hr/reports/export"><i class="bi bi-download me-1"></i> Xuất báo cáo</a></div>
            <div class="main-content">
                <section class="card mb-4"><div class="card-body"><form class="row gy-2 gx-3 align-items-end" method="get" action="${pageContext.request.contextPath}/hr/reports"><div class="col-md-4"><label class="form-label small text-muted">Từ ngày</label><input class="form-control" type="date" name="fromDate" value="<c:out value='${param.fromDate}'/>"></div><div class="col-md-4"><label class="form-label small text-muted">Đến ngày</label><input class="form-control" type="date" name="toDate" value="<c:out value='${param.toDate}'/>"></div><div class="col-md-4"><button class="btn btn-primary" type="submit"><i class="bi bi-bar-chart me-1"></i> Cập nhật báo cáo</button></div></form></div></section>
                <div class="row g-3 mb-4"><div class="col-sm-6 col-xl-3"><div class="card p-3 h-100"><p class="text-muted mb-1">Tổng đơn</p><h3 class="mb-0"><c:out value="${report.totalApplications}" /></h3></div></div><div class="col-sm-6 col-xl-3"><div class="card p-3 h-100"><p class="text-muted mb-1">Tỷ lệ shortlist</p><h3 class="mb-0"><c:out value="${report.shortlistRate}" />%</h3></div></div><div class="col-sm-6 col-xl-3"><div class="card p-3 h-100"><p class="text-muted mb-1">Offer đã gửi</p><h3 class="mb-0"><c:out value="${report.offersSent}" /></h3></div></div><div class="col-sm-6 col-xl-3"><div class="card p-3 h-100"><p class="text-muted mb-1">Tỷ lệ tuyển dụng</p><h3 class="mb-0 text-success"><c:out value="${report.hireRate}" />%</h3></div></div></div>
                <div class="row g-4">
                    <div class="col-xl-7"><section class="card h-100"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Recruitment funnel</h5><small class="text-muted">Số lượng ứng viên theo mỗi giai đoạn.</small></div><div class="card-body px-4 pb-4"><div class="vstack gap-3"><div><div class="d-flex justify-content-between small mb-1"><span>Applied</span><strong><c:out value="${report.applied}" /></strong></div><div class="progress"><div class="progress-bar" style="width:100%"></div></div></div><div><div class="d-flex justify-content-between small mb-1"><span>Screening</span><strong><c:out value="${report.screening}" /></strong></div><div class="progress"><div class="progress-bar bg-warning" style="width:${report.screeningRate}%"></div></div></div><div><div class="d-flex justify-content-between small mb-1"><span>Interview</span><strong><c:out value="${report.interview}" /></strong></div><div class="progress"><div class="progress-bar bg-info" style="width:${report.interviewRate}%"></div></div></div><div><div class="d-flex justify-content-between small mb-1"><span>Offer</span><strong><c:out value="${report.offered}" /></strong></div><div class="progress"><div class="progress-bar bg-primary" style="width:${report.offerRate}%"></div></div></div><div><div class="d-flex justify-content-between small mb-1"><span>Hired</span><strong><c:out value="${report.hired}" /></strong></div><div class="progress"><div class="progress-bar bg-success" style="width:${report.hireRate}%"></div></div></div></div></div></section></div>
                    <div class="col-xl-5"><section class="card h-100"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Phân bổ trạng thái</h5><small class="text-muted">Tình trạng đơn trong khoảng thời gian chọn.</small></div><div class="card-body px-4 pb-4"><canvas id="statusChart" aria-label="Biểu đồ trạng thái ứng tuyển" role="img" data-submitted="<c:out value='${report.submitted}'/>" data-screening="<c:out value='${report.screening}'/>" data-interviewed="<c:out value='${report.interviewed}'/>" data-offered="<c:out value='${report.offered}'/>" data-hired="<c:out value='${report.hired}'/>"></canvas></div></section></div>
                    <div class="col-12"><section class="card"><div class="card-header bg-white border-0 px-4 pt-4"><h5 class="mb-1">Đơn ứng tuyển theo tháng</h5><small class="text-muted">Dữ liệu tổng hợp từ các đơn đã nộp.</small></div><div class="table-responsive"><table class="table table-sm mb-0"><thead class="table-light"><tr><th class="ps-4">Tháng</th><th class="pe-4 text-end">Số đơn</th></tr></thead><tbody><c:forEach var="entry" items="${report.applicationsByMonth}"><tr><td class="ps-4"><c:out value="${entry.key}" /></td><td class="pe-4 text-end fw-semibold"><c:out value="${entry.value}" /></td></tr></c:forEach><c:if test="${empty report.applicationsByMonth}"><tr><td colspan="2" class="text-center text-muted py-4">Chưa có dữ liệu trong khoảng thời gian đã chọn.</td></tr></c:if></tbody></table></div></section></div>
                </div>
            </div>
        </main>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
<script>
    (function () {
        const chartElement = document.getElementById('statusChart');
        if (!chartElement || typeof Chart === 'undefined') return;
        const values = ['submitted', 'screening', 'interviewed', 'offered', 'hired']
            .map(function (key) { return Number(chartElement.dataset[key] || 0); });
        new Chart(chartElement, {
            type: 'doughnut',
            data: { labels: ['Đã nộp', 'Sàng lọc', 'Đã phỏng vấn', 'Offer', 'Đã tuyển'], datasets: [{ data: values, backgroundColor: ['#6c757d', '#ffc107', '#0dcaf0', '#0d6efd', '#198754'], borderWidth: 0 }] },
            options: { plugins: { legend: { position: 'bottom' } }, cutout: '65%' }
        });
    }());
</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />


