<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Báo cáo tuyển dụng | JobCV" />
<c:url var="exportUrl" value="/hr/reports/export">
    <c:param name="fromDate" value="${report.fromDate}" />
    <c:param name="toDate" value="${report.toDate}" />
</c:url>
<c:url var="overviewUrl" value="/hr/reports/overview"><c:param name="fromDate" value="${report.fromDate}" /><c:param name="toDate" value="${report.toDate}" /></c:url>
<c:url var="trendsUrl" value="/hr/reports/trends"><c:param name="fromDate" value="${report.fromDate}" /><c:param name="toDate" value="${report.toDate}" /></c:url>
<c:url var="pipelineUrl" value="/hr/reports/pipeline"><c:param name="fromDate" value="${report.fromDate}" /><c:param name="toDate" value="${report.toDate}" /></c:url>
<c:url var="topJobsUrl" value="/hr/reports/top-jobs"><c:param name="fromDate" value="${report.fromDate}" /><c:param name="toDate" value="${report.toDate}" /></c:url>
<c:url var="breakdownUrl" value="/hr/reports/breakdown"><c:param name="fromDate" value="${report.fromDate}" /><c:param name="toDate" value="${report.toDate}" /></c:url>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid p-0">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/hr-sidebar.jsp" />
        <main class="col-md-10 offset-md-2 bg-light min-vh-100 recruitment-report-page">
            <div class="topbar report-topbar">
                <div><p class="report-eyebrow mb-1">TRUNG TÂM PHÂN TÍCH</p><h4 class="mb-1">Báo cáo tuyển dụng</h4><span>Theo dõi hiệu suất nguồn ứng viên và chất lượng chuyển đổi theo từng chuyên mục.</span></div>
                <a class="btn btn-outline-primary" href="${exportUrl}"><i class="bi bi-download me-1"></i> Xuất báo cáo CSV</a>
            </div>
            <div class="main-content">
                <jsp:include page="/WEB-INF/views/common/flash.jsp" />
                <nav class="report-section-nav" aria-label="Điều hướng các mục báo cáo">
                    <span class="report-section-nav-label"><i class="bi bi-layout-text-sidebar-reverse"></i>Mục báo cáo</span>
                    <a class="${reportSection eq 'overview' ? 'active' : ''}" href="${overviewUrl}" aria-current="${reportSection eq 'overview' ? 'page' : ''}"><i class="bi bi-speedometer2"></i>Tổng quan</a>
                    <a class="${reportSection eq 'trends' ? 'active' : ''}" href="${trendsUrl}" aria-current="${reportSection eq 'trends' ? 'page' : ''}"><i class="bi bi-graph-up-arrow"></i>Xu hướng</a>
                    <a class="${reportSection eq 'pipeline' ? 'active' : ''}" href="${pipelineUrl}" aria-current="${reportSection eq 'pipeline' ? 'page' : ''}"><i class="bi bi-funnel"></i>Phễu tuyển dụng</a>
                    <a class="${reportSection eq 'top-jobs' ? 'active' : ''}" href="${topJobsUrl}" aria-current="${reportSection eq 'top-jobs' ? 'page' : ''}"><i class="bi bi-trophy"></i>Top 5 công việc</a>
                    <a class="${reportSection eq 'breakdown' ? 'active' : ''}" href="${breakdownUrl}" aria-current="${reportSection eq 'breakdown' ? 'page' : ''}"><i class="bi bi-pie-chart"></i>Phân tích chi tiết</a>
                </nav>
                <section class="report-filter-card">
                    <form class="row gy-3 gx-3 align-items-end" method="get" action="${pageContext.request.contextPath}${reportSectionPath}">
                        <div class="col-lg-4"><label class="form-label" for="reportFromDate">Từ ngày</label><div class="report-date-input"><i class="bi bi-calendar3"></i><input class="form-control" id="reportFromDate" type="date" name="fromDate" value="<c:out value='${report.fromDate}'/>"></div></div>
                        <div class="col-lg-4"><label class="form-label" for="reportToDate">Đến ngày</label><div class="report-date-input"><i class="bi bi-calendar-check"></i><input class="form-control" id="reportToDate" type="date" name="toDate" value="<c:out value='${report.toDate}'/>"></div></div>
                        <div class="col-lg-4 d-grid"><button class="btn btn-primary report-refresh-button" type="submit"><i class="bi bi-arrow-repeat me-1"></i>Cập nhật dữ liệu</button></div>
                    </form>
                    <div class="report-filter-note"><i class="bi bi-info-circle"></i>Dữ liệu được giới hạn theo công ty của tài khoản HR và ngày ứng viên nộp hồ sơ.</div>
                </section>

                <c:if test="${reportSection eq 'overview'}">
                <section class="report-kpi-grid report-page-section" id="report-overview" aria-label="Chỉ số tuyển dụng chính">
                    <article class="report-kpi"><span class="report-kpi-icon tone-blue"><i class="bi bi-file-earmark-person"></i></span><div><small>Tổng đơn ứng tuyển</small><strong><fmt:formatNumber value="${report.totalApplications}" type="number" /></strong><em><c:out value="${report.jobsReceivingApplications}" /> công việc có hồ sơ</em></div></article>
                    <article class="report-kpi"><span class="report-kpi-icon tone-violet"><i class="bi bi-people"></i></span><div><small>Ứng viên duy nhất</small><strong><fmt:formatNumber value="${report.uniqueCandidates}" type="number" /></strong><em>Không tính trùng ứng viên</em></div></article>
                    <article class="report-kpi"><span class="report-kpi-icon tone-cyan"><i class="bi bi-stars"></i></span><div><small>Điểm phù hợp TB</small><strong><c:out value="${report.averageMatchScore}" />%</strong><em>Điểm khớp hồ sơ và công việc</em></div></article>
                    <article class="report-kpi"><span class="report-kpi-icon tone-amber"><i class="bi bi-diagram-3"></i></span><div><small>Đang trong quy trình</small><strong><fmt:formatNumber value="${report.activePipeline}" type="number" /></strong><em>Chưa kết thúc xử lý</em></div></article>
                    <article class="report-kpi"><span class="report-kpi-icon tone-green"><i class="bi bi-person-check"></i></span><div><small>Đã tuyển thành công</small><strong><fmt:formatNumber value="${report.hired}" type="number" /></strong><em>Tỷ lệ tuyển <c:out value="${report.hireRate}" />%</em></div></article>
                    <article class="report-kpi"><span class="report-kpi-icon tone-red"><i class="bi bi-person-x"></i></span><div><small>Hồ sơ bị loại</small><strong><fmt:formatNumber value="${report.rejected}" type="number" /></strong><em>Tỷ lệ loại <c:out value="${report.rejectionRate}" />%</em></div></article>
                </section>
                </c:if>

                <c:if test="${reportSection eq 'trends'}">
                <div class="row g-4 report-page-section" id="report-trends">
                    <div class="col-xxl-8">
                        <section class="report-panel h-100">
                            <header class="report-panel-header"><div><span class="report-eyebrow">XU HƯỚNG HỒ SƠ</span><h5>Đơn ứng tuyển theo tháng</h5><p>Quan sát biến động nguồn ứng viên trong khoảng thời gian đã chọn.</p></div><span class="report-highlight"><i class="bi bi-graph-up-arrow"></i>Cao nhất: <strong><c:out value="${report.peakMonth}" /></strong> · <fmt:formatNumber value="${report.peakMonthApplications}" type="number" /> đơn</span></header>
                            <div class="report-chart-wrap"><canvas id="monthlyApplicationChart" aria-label="Biểu đồ đơn ứng tuyển theo tháng" role="img"></canvas></div>
                            <div class="visually-hidden" id="monthlyApplicationData"><c:forEach var="entry" items="${report.applicationsByMonth}"><span data-month-label="<c:out value='${entry.key}'/>" data-month-value="<c:out value='${entry.value}'/>"></span></c:forEach></div>
                            <c:if test="${empty report.applicationsByMonth}"><div class="report-empty"><i class="bi bi-bar-chart"></i><strong>Chưa có dữ liệu theo tháng</strong><span>Hãy chọn khoảng thời gian khác hoặc thêm dữ liệu ứng tuyển.</span></div></c:if>
                        </section>
                    </div>
                    <div class="col-xxl-4">
                        <section class="report-panel h-100">
                            <header class="report-panel-header"><div><span class="report-eyebrow">CƠ CẤU PIPELINE</span><h5>Phân bổ trạng thái</h5><p>Trạng thái hiện tại của toàn bộ hồ sơ.</p></div></header>
                            <div class="report-doughnut-wrap"><canvas id="statusChart" aria-label="Biểu đồ trạng thái ứng tuyển" role="img" data-submitted="<c:out value='${report.submitted}'/>" data-screening="<c:out value='${report.screening}'/>" data-shortlisted="<c:out value='${report.shortlisted}'/>" data-interview-scheduled="<c:out value='${report.interviewScheduled}'/>" data-interviewed="<c:out value='${report.interviewedOnly}'/>" data-offered="<c:out value='${report.offered}'/>" data-hired="<c:out value='${report.hired}'/>" data-rejected="<c:out value='${report.rejected}'/>" data-withdrawn="<c:out value='${report.withdrawn}'/>"></canvas><div class="report-doughnut-total"><strong><fmt:formatNumber value="${report.totalApplications}" type="number" /></strong><span>Tổng hồ sơ</span></div></div>
                        </section>
                    </div>
                </div>
                </c:if>

                <c:if test="${reportSection eq 'pipeline'}">
                <div class="row g-4 report-page-section" id="report-pipeline">
                    <div class="col-xl-7">
                        <section class="report-panel h-100">
                            <header class="report-panel-header"><div><span class="report-eyebrow">PHỄU TUYỂN DỤNG</span><h5>Hiệu suất từng giai đoạn</h5><p>Tỷ trọng hồ sơ đang ở mỗi bước so với tổng đơn đã nhận.</p></div></header>
                            <div class="report-funnel">
                                <div class="report-funnel-row"><div><span>Đã nộp đơn</span><strong><fmt:formatNumber value="${report.applied}" type="number" /></strong></div><div class="report-funnel-track"><span class="stage-applied" style="width:${report.applied gt 0 ? 100 : 0}%"></span></div><small>${report.applied gt 0 ? 100 : 0}%</small></div>
                                <div class="report-funnel-row"><div><span>Đang sàng lọc</span><strong><fmt:formatNumber value="${report.screening}" type="number" /></strong></div><div class="report-funnel-track"><span class="stage-screening" style="width:${report.screeningRate}%"></span></div><small><c:out value="${report.screeningRate}" />%</small></div>
                                <div class="report-funnel-row"><div><span>Danh sách ngắn</span><strong><fmt:formatNumber value="${report.shortlisted}" type="number" /></strong></div><div class="report-funnel-track"><span class="stage-shortlist" style="width:${report.shortlistRate}%"></span></div><small><c:out value="${report.shortlistRate}" />%</small></div>
                                <div class="report-funnel-row"><div><span>Phỏng vấn</span><strong><fmt:formatNumber value="${report.interview}" type="number" /></strong></div><div class="report-funnel-track"><span class="stage-interview" style="width:${report.interviewRate}%"></span></div><small><c:out value="${report.interviewRate}" />%</small></div>
                                <div class="report-funnel-row"><div><span>Thư mời</span><strong><fmt:formatNumber value="${report.offered}" type="number" /></strong></div><div class="report-funnel-track"><span class="stage-offer" style="width:${report.offerRate}%"></span></div><small><c:out value="${report.offerRate}" />%</small></div>
                                <div class="report-funnel-row"><div><span>Đã tuyển</span><strong><fmt:formatNumber value="${report.hired}" type="number" /></strong></div><div class="report-funnel-track"><span class="stage-hired" style="width:${report.hireRate}%"></span></div><small><c:out value="${report.hireRate}" />%</small></div>
                            </div>
                        </section>
                    </div>
                    <div class="col-xl-5">
                        <section class="report-panel h-100">
                            <header class="report-panel-header"><div><span class="report-eyebrow">CHỈ SỐ VẬN HÀNH</span><h5>Tóm tắt hiệu quả</h5><p>Các tín hiệu cần lưu ý trong kỳ báo cáo.</p></div></header>
                            <div class="report-insight-grid">
                                <article><i class="bi bi-briefcase"></i><span>Hồ sơ / công việc</span><strong><c:out value="${report.averageApplicationsPerJob}" /></strong><small>Mức cạnh tranh trung bình</small></article>
                                <article><i class="bi bi-envelope-paper"></i><span>Thư mời đã gửi</span><strong><fmt:formatNumber value="${report.offersSent}" type="number" /></strong><small>Tổng thư mời của công ty</small></article>
                                <article><i class="bi bi-box-arrow-left"></i><span>Ứng viên rút hồ sơ</span><strong><fmt:formatNumber value="${report.withdrawn}" type="number" /></strong><small><c:out value="${report.withdrawalRate}" />% tổng hồ sơ</small></article>
                                <article><i class="bi bi-calendar2-week"></i><span>Tháng cao điểm</span><strong><c:out value="${report.peakMonth}" /></strong><small><fmt:formatNumber value="${report.peakMonthApplications}" type="number" /> hồ sơ</small></article>
                            </div>
                        </section>
                    </div>
                </div>
                </c:if>

                <c:if test="${reportSection eq 'top-jobs'}">
                <section class="report-panel report-page-section" id="report-top-jobs">
                    <header class="report-panel-header report-panel-header-row"><div><span class="report-eyebrow">TOP 5 CÔNG VIỆC</span><h5>5 công việc được ứng tuyển nhiều nhất</h5><p>Xếp hạng theo số hồ sơ trong đúng khoảng thời gian báo cáo.</p></div><a href="${pageContext.request.contextPath}/hr/jobs" class="btn btn-sm btn-outline-primary">Quản lý tin tuyển dụng</a></header>
                    <div class="table-responsive"><table class="table report-ranking-table report-ranking-table-compact align-middle mb-0"><thead><tr><th class="ps-4">#</th><th>Công việc</th><th class="text-end pe-4">Lượt ứng tuyển</th></tr></thead><tbody>
                    <c:forEach var="item" items="${report.topJobs}" varStatus="rank"><tr><td class="ps-4"><span class="report-rank ${rank.index lt 3 ? 'is-top' : ''}">${rank.index + 1}</span></td><td><div class="report-job-cell"><img src="${pageContext.request.contextPath}/company-logo?id=${item.companyId}" alt="Logo ${item.companyName}" loading="lazy"><div><a href="${pageContext.request.contextPath}/jobs/detail?id=${item.jobId}"><c:out value="${item.title}" /></a><span><c:out value="${item.companyName}" /> · <c:out value="${item.jobCode}" /></span></div></div></td><td class="text-end pe-4"><strong class="report-application-count"><fmt:formatNumber value="${item.applications}" type="number" /></strong></td></tr></c:forEach>
                    <c:if test="${empty report.topJobs}"><tr><td colspan="3"><div class="report-empty py-5"><i class="bi bi-briefcase"></i><strong>Chưa có công việc nào nhận hồ sơ</strong><span>Dữ liệu xếp hạng sẽ xuất hiện sau khi ứng viên nộp đơn.</span></div></td></tr></c:if>
                    </tbody></table></div>
                </section>
                </c:if>

                <c:if test="${reportSection eq 'breakdown'}">
                <div class="row g-4 report-page-section" id="report-breakdown">
                    <div class="col-xl-7"><section class="report-panel h-100"><header class="report-panel-header"><div><span class="report-eyebrow">NHU CẦU THEO CHUYÊN MÔN</span><h5>Ứng tuyển theo phòng ban</h5><p>Nhóm chuyên môn thu hút nhiều hồ sơ nhất.</p></div></header><div class="report-dimension-list"><c:forEach var="entry" items="${report.applicationsByDepartment}"><div><div><span><c:out value="${entry.key}" /></span><strong><fmt:formatNumber value="${entry.value}" type="number" /></strong></div><div><span style="width:${report.totalApplications gt 0 ? entry.value * 100 / report.totalApplications : 0}%"></span></div></div></c:forEach><c:if test="${empty report.applicationsByDepartment}"><div class="report-empty"><span>Chưa có dữ liệu phòng ban.</span></div></c:if></div></section></div>
                    <div class="col-xl-5"><section class="report-panel h-100"><header class="report-panel-header"><div><span class="report-eyebrow">THỊ TRƯỜNG TUYỂN DỤNG</span><h5>Ứng tuyển theo địa điểm</h5><p>Khu vực được ứng viên quan tâm nhiều nhất.</p></div></header><div class="report-location-list"><c:forEach var="entry" items="${report.applicationsByLocation}"><div><span><i class="bi bi-geo-alt"></i><c:out value="${entry.key}" /></span><strong><fmt:formatNumber value="${entry.value}" type="number" /> hồ sơ</strong></div></c:forEach><c:if test="${empty report.applicationsByLocation}"><div class="report-empty"><span>Chưa có dữ liệu địa điểm.</span></div></c:if></div></section></div>
                </div>
                </c:if>
            </div>
        </main>
    </div>
</div>
<script src="${pageContext.request.contextPath}/webjars/chart.js/4.4.4/dist/chart.umd.js"></script>
<script>
    (function () {
        if (typeof Chart === 'undefined') return;
        const statusElement = document.getElementById('statusChart');
        if (statusElement) {
            const keys = ['submitted', 'screening', 'shortlisted', 'interviewScheduled', 'interviewed', 'offered', 'hired', 'rejected', 'withdrawn'];
            const values = keys.map(function (key) { return Number(statusElement.dataset[key] || 0); });
            new Chart(statusElement, { type: 'doughnut', data: { labels: ['Đã nộp', 'Sàng lọc', 'Danh sách ngắn', 'Hẹn phỏng vấn', 'Đã phỏng vấn', 'Thư mời', 'Đã tuyển', 'Bị loại', 'Đã rút'], datasets: [{ data: values, backgroundColor: ['#94a3b8', '#f59e0b', '#8b5cf6', '#38bdf8', '#0ea5e9', '#2563eb', '#16a34a', '#ef4444', '#cbd5e1'], borderWidth: 0, hoverOffset: 5 }] }, options: { maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, boxWidth: 8, padding: 13, font: { size: 10 } } } }, cutout: '70%' } });
        }
        const monthCanvas = document.getElementById('monthlyApplicationChart');
        const monthNodes = Array.from(document.querySelectorAll('#monthlyApplicationData [data-month-label]'));
        if (monthCanvas && monthNodes.length) {
            new Chart(monthCanvas, { type: 'line', data: { labels: monthNodes.map(function (node) { return node.dataset.monthLabel; }), datasets: [{ label: 'Đơn ứng tuyển', data: monthNodes.map(function (node) { return Number(node.dataset.monthValue || 0); }), borderColor: '#00a849', backgroundColor: 'rgba(0,168,73,.12)', pointBackgroundColor: '#00a849', pointBorderColor: '#fff', pointBorderWidth: 2, pointRadius: 4, pointHoverRadius: 6, borderWidth: 3, fill: true, tension: .35 }] }, options: { maintainAspectRatio: false, interaction: { intersect: false, mode: 'index' }, plugins: { legend: { display: false } }, scales: { x: { grid: { display: false }, ticks: { maxTicksLimit: 12 } }, y: { beginAtZero: true, ticks: { precision: 0 }, grid: { color: '#eef2f6' } } } } });
        }
    }());
</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
