<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="RecruitFlow | Tìm đúng công việc, xây đúng sự nghiệp" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<c:set var="homeSearchTarget" value="${candidateHome ? '/candidate/jobs' : '/jobs'}" />
<div class="container public-home-flash"><jsp:include page="/WEB-INF/views/common/flash.jsp" /></div>

<main>
    <section class="public-hero public-hero-v2 topcv-inspired-hero">
        <div class="container position-relative">
            <div class="row align-items-center g-5">
                <div class="col-lg-7">
                    <span class="hero-kicker"><i class="bi bi-stars"></i>${candidateHome ? 'Không gian tìm việc dành riêng cho bạn' : 'Nền tảng tuyển dụng minh bạch, liền mạch'}</span>
                    <c:choose><c:when test="${candidateHome}"><h1>Chào mừng trở lại,<br><span><c:out value="${sessionScope.fullName}" />.</span></h1><p class="hero-copy">Hồ sơ, CV, việc làm phù hợp và toàn bộ tiến trình ứng tuyển của bạn đã sẵn sàng ngay tại trang chủ.</p></c:when><c:otherwise><h1>Tìm đúng công việc.<br><span>Phát triển đúng tương lai.</span></h1><p class="hero-copy">Khám phá cơ hội chất lượng, ứng tuyển bằng CV tốt nhất và theo dõi toàn bộ hành trình tuyển dụng trong một không gian duy nhất.</p></c:otherwise></c:choose>
                    <form class="hero-search-card hero-search-v2" action="${pageContext.request.contextPath}${homeSearchTarget}" method="get">
                        <div class="row g-2 align-items-center">
                            <div class="col-lg-5"><label class="visually-hidden" for="homeKeyword">Từ khóa công việc</label><div class="input-group"><span class="input-group-text bg-white border-end-0"><i class="bi bi-search"></i></span><input class="form-control border-start-0" id="homeKeyword" type="search" name="keyword" value="<c:out value='${param.keyword}'/>" placeholder="Vị trí tuyển dụng hoặc kỹ năng"></div></div>
                            <div class="col-lg-3"><label class="visually-hidden" for="homeDepartment">Phòng ban</label><select class="form-select" id="homeDepartment" name="departmentId"><option value="">Mọi phòng ban</option><c:forEach var="department" items="${departments}"><option value="<c:out value='${department.id}'/>" ${param.departmentId eq department.id ? 'selected' : ''}><c:out value="${department.displayName}" /></option></c:forEach></select></div>
                            <div class="col-lg-2"><label class="visually-hidden" for="homeLocation">Địa điểm</label><input class="form-control" id="homeLocation" name="location" value="<c:out value='${param.location}'/>" placeholder="Địa điểm"></div>
                            <div class="col-lg-2 d-grid"><button class="btn btn-primary" type="submit"><i class="bi bi-search me-1"></i>Tìm kiếm</button></div>
                        </div>
                    </form>
                    <div class="hero-trending"><span>Gợi ý:</span><c:forEach var="department" items="${departments}" end="3"><c:url var="quickDepartmentUrl" value="${homeSearchTarget}"><c:param name="departmentId" value="${department.id}" /></c:url><a href="${quickDepartmentUrl}"><c:out value="${department.displayName}" /></a></c:forEach><a href="${pageContext.request.contextPath}${homeSearchTarget}">Tất cả việc làm</a></div>
                    <div class="hero-proof">
                        <span><strong><c:out value="${publishedJobCount}" />+</strong>Việc làm đang mở</span>
                        <span><strong><c:out value="${fn:length(departments)}" />+</strong>Lĩnh vực nghề nghiệp</span>
                        <span><strong>100%</strong>Theo dõi trực tuyến</span>
                    </div>
                </div>
                <div class="col-lg-5 d-none d-lg-block">
                    <c:choose><c:when test="${candidateHome}">
                    <div class="candidate-home-summary-card">
                        <div class="candidate-home-summary-head"><span><i class="bi bi-person-check-fill"></i></span><div><small>HỒ SƠ NGHỀ NGHIỆP</small><strong><c:out value="${candidateStats.profileCompletion}" />% hoàn thiện</strong></div><a href="${pageContext.request.contextPath}/candidate/profile">Cập nhật</a></div>
                        <div class="candidate-home-progress"><span style="width: ${candidateStats.profileCompletion}%"></span></div>
                        <div class="candidate-home-summary-grid"><a href="${pageContext.request.contextPath}/candidate/applications"><strong><c:out value="${candidateStats.totalApplications}" /></strong><span>Đã ứng tuyển</span></a><a href="${pageContext.request.contextPath}/candidate/saved-jobs"><strong><c:out value="${candidateSavedJobCount}" /></strong><span>Việc đã lưu</span></a><a href="${pageContext.request.contextPath}/candidate/interviews"><strong><c:out value="${candidateStats.upcomingInterviews}" /></strong><span>Phỏng vấn</span></a><a href="${pageContext.request.contextPath}/candidate/job-alerts"><strong><c:out value="${candidateJobAlertCount}" /></strong><span>Thông báo việc</span></a></div>
                        <div class="candidate-home-summary-actions"><a class="btn btn-light" href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-file-earmark-person me-1"></i>Quản lý CV</a><a class="btn btn-outline-light" href="${pageContext.request.contextPath}/candidate/dashboard">Trung tâm quản lý</a></div>
                    </div>
                    </c:when><c:otherwise><div class="hero-career-card">
                        <div class="hero-career-top"><span class="hero-career-logo"><i class="bi bi-briefcase"></i></span><div><small>CƠ HỘI DÀNH CHO BẠN</small><strong>Hành trình nghề nghiệp</strong></div><span class="hero-live"><i></i>Đang tuyển</span></div>
                        <div class="hero-career-steps">
                            <div class="done"><span><i class="bi bi-check-lg"></i></span><div><strong>Tạo hồ sơ chuyên nghiệp</strong><small>Thông tin của bạn được lưu an toàn</small></div></div>
                            <div class="active"><span><i class="bi bi-file-earmark-person"></i></span><div><strong>Ứng tuyển bằng CV tốt nhất</strong><small>Hệ thống tự đánh giá độ phù hợp</small></div></div>
                            <div><span><i class="bi bi-calendar-event"></i></span><div><strong>Phỏng vấn và nhận thư mời</strong><small>Mọi cập nhật đều ở một nơi</small></div></div>
                        </div>
                        <div class="hero-career-footer"><span><i class="bi bi-shield-check"></i>Dữ liệu được bảo vệ</span><span><i class="bi bi-lightning-charge"></i>Phản hồi nhanh</span></div>
                    </div></c:otherwise></c:choose>
                </div>
            </div>
        </div>
    </section>

    <c:if test="${candidateHome}">
    <section class="candidate-home-section">
        <div class="container">
            <div class="section-header candidate-home-jobs-header">
                <div><span class="section-kicker">GỢI Ý CÁ NHÂN HÓA</span><h2>Việc làm dành cho bạn</h2><p>Xếp hạng dựa trên hồ sơ và CV hiện tại; hãy cập nhật CV để điểm phù hợp chính xác hơn.</p></div>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/candidate/jobs">Xem tất cả<i class="bi bi-arrow-right ms-1"></i></a>
            </div>
            <div class="row g-4">
                <c:forEach var="jobMatch" items="${candidateJobs}">
                    <c:url var="candidateHomeJobUrl" value="/jobs/detail"><c:param name="id" value="${jobMatch.job.id}" /></c:url>
                    <c:set var="candidateHomeJobSaved" value="${candidateSavedJobIds.contains(jobMatch.job.id)}" />
                    <div class="col-md-6 col-xl-4"><article class="job-card job-card-v2 candidate-home-job-card"><div class="job-card-brand"><span><c:out value="${fn:toUpperCase(fn:substring(jobMatch.job.title, 0, 1))}" /></span><div><small><c:out value="${jobMatch.job.departmentName}" /></small><strong><c:out value="${jobMatch.job.jobCode}" /></strong></div><c:choose><c:when test="${candidateHasResume}"><b class="candidate-home-match"><fmt:formatNumber value="${jobMatch.matchScore}" maxFractionDigits="0" />% phù hợp</b></c:when><c:otherwise><a class="candidate-home-match is-pending" href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-file-earmark-plus"></i> Thêm CV để chấm điểm</a></c:otherwise></c:choose></div><a class="job-card-title d-block" href="${candidateHomeJobUrl}"><c:out value="${jobMatch.job.title}" /></a><div class="job-meta-grid"><span><i class="bi bi-geo-alt"></i><c:out value="${jobMatch.job.location}" /></span><span><i class="bi bi-briefcase"></i><span data-enum-label="${jobMatch.job.employmentType}"><c:out value="${jobMatch.job.employmentType}" /></span></span><span><i class="bi bi-people"></i><c:out value="${jobMatch.job.numberOfPositions}" /> vị trí</span><span><i class="bi bi-calendar3"></i>Hạn <fmt:formatDate value="${jobMatch.job.deadline}" pattern="dd/MM/yyyy" /></span></div><div class="job-card-footer"><span><fmt:formatNumber value="${jobMatch.job.salaryMin}" type="number" /> – <fmt:formatNumber value="${jobMatch.job.salaryMax}" type="number" /> ₫</span><div class="d-flex gap-2"><form action="${pageContext.request.contextPath}/candidate/saved-jobs/${candidateHomeJobSaved ? 'remove' : 'save'}" method="post"><input type="hidden" name="jobId" value="<c:out value='${jobMatch.job.id}'/>"><input type="hidden" name="returnTo" value="/home"><button class="candidate-home-card-action ${candidateHomeJobSaved ? 'is-saved' : ''}" type="submit" aria-label="${candidateHomeJobSaved ? 'Bỏ lưu việc làm' : 'Lưu việc làm'}"><i class="bi ${candidateHomeJobSaved ? 'bi-bookmark-heart-fill' : 'bi-bookmark-heart'}"></i></button></form><a href="${candidateHomeJobUrl}" aria-label="Xem việc ${jobMatch.job.title}"><i class="bi bi-arrow-up-right"></i></a></div></div></article></div>
                </c:forEach>
            </div>
            <div class="candidate-home-alert-cta"><span><i class="bi bi-bell-fill"></i></span><div><strong>Không muốn bỏ lỡ công việc mới?</strong><p>Tạo thông báo theo vị trí, phòng ban, địa điểm và loại hình bạn quan tâm.</p></div><a class="btn btn-primary" href="${pageContext.request.contextPath}/candidate/job-alerts">Tạo thông báo việc làm</a></div>
        </div>
    </section>
    </c:if>

    <c:if test="${not candidateHome}">
    <section class="public-section">
        <div class="container">
            <div class="section-header">
                <div><span class="section-kicker">VỊ TRÍ NỔI BẬT</span><h2>Cơ hội mới dành cho bạn</h2><p>Khám phá những vị trí đang được ưu tiên tuyển dụng.</p></div>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/jobs">Xem tất cả<i class="bi bi-arrow-right ms-1"></i></a>
            </div>
            <div class="row g-4">
                <c:forEach var="job" items="${featuredJobs}">
                    <c:url var="jobDetailUrl" value="/jobs/detail"><c:param name="id" value="${job.id}" /></c:url>
                    <div class="col-md-6 col-xl-4">
                        <article class="job-card job-card-v2">
                            <div class="job-card-brand"><span><c:out value="${fn:toUpperCase(fn:substring(job.title, 0, 1))}" /></span><div><small><c:out value="${job.departmentName}" /></small><strong><c:out value="${job.jobCode}" /></strong></div><span class="job-fresh"><i class="bi bi-lightning-fill"></i>Mới</span></div>
                            <a class="job-card-title d-block" href="${jobDetailUrl}"><c:out value="${job.title}" /></a>
                            <div class="job-meta-grid">
                                <span><i class="bi bi-geo-alt"></i><c:out value="${job.location}" /></span>
                                <span><i class="bi bi-briefcase"></i><span data-enum-label="${job.employmentType}"><c:out value="${job.employmentType}" /></span></span>
                                <span><i class="bi bi-people"></i><c:out value="${job.numberOfPositions}" /> vị trí</span>
                                <span><i class="bi bi-calendar3"></i>Hạn <fmt:formatDate value="${job.deadline}" pattern="dd/MM/yyyy" /></span>
                            </div>
                            <div class="job-card-footer"><span><fmt:formatNumber value="${job.salaryMin}" type="number" /> – <fmt:formatNumber value="${job.salaryMax}" type="number" /> ₫</span><a href="${jobDetailUrl}" aria-label="Xem việc ${job.title}"><i class="bi bi-arrow-up-right"></i></a></div>
                        </article>
                    </div>
                </c:forEach>
                <c:if test="${empty featuredJobs}"><div class="col-12"><div class="rf-card empty-state"><div class="empty-icon"><i class="bi bi-briefcase"></i></div><h5>Chưa có việc làm nổi bật</h5><p class="mb-0">Các cơ hội mới sẽ được cập nhật tại đây.</p></div></div></c:if>
            </div>
        </div>
    </section>
    </c:if>

    <c:url var="featuredEmployerAllUrl" value="${homeSearchTarget}" />
    <c:url var="featuredInternshipUrl" value="${homeSearchTarget}"><c:param name="employmentType" value="INTERNSHIP" /></c:url>
    <c:url var="featuredRemoteUrl" value="${homeSearchTarget}"><c:param name="employmentType" value="REMOTE" /></c:url>
    <c:url var="featuredContractUrl" value="${homeSearchTarget}"><c:param name="employmentType" value="CONTRACT" /></c:url>
    <section class="public-section featured-employers-section" aria-labelledby="featuredEmployersTitle" data-employer-showcase>
        <div class="container">
            <div class="featured-employers-shell">
                <header class="featured-employers-banner">
                    <div><span class="featured-employers-kicker"><i class="bi bi-patch-check-fill"></i>ĐỐI TÁC TUYỂN DỤNG</span><h2 id="featuredEmployersTitle">Thương hiệu tuyển dụng nổi bật</h2><p>Khám phá môi trường làm việc và cơ hội nghề nghiệp từ những doanh nghiệp tiêu biểu.</p></div>
                    <a href="${featuredEmployerAllUrl}">Khám phá tất cả<i class="bi bi-arrow-up-right"></i></a>
                    <span class="featured-employers-pro-badge"><i class="bi bi-stars"></i>Top Employer</span>
                </header>

                <div class="featured-employers-toolbar">
                    <div class="featured-employer-filters" role="group" aria-label="Lọc thương hiệu theo lĩnh vực">
                        <button class="active" type="button" data-employer-filter="all" aria-pressed="true">Tất cả</button>
                        <c:forEach var="employerDepartment" items="${departments}">
                            <c:set var="employerFilterLabel" value="${employerDepartment.name}" />
                            <c:choose>
                                <c:when test="${employerDepartment.name eq 'Information Technology'}"><c:set var="employerFilterLabel" value="IT - Phần mềm" /></c:when>
                                <c:when test="${employerDepartment.name eq 'Finance'}"><c:set var="employerFilterLabel" value="Tài chính" /></c:when>
                                <c:when test="${employerDepartment.name eq 'Human Resources'}"><c:set var="employerFilterLabel" value="Nhân sự" /></c:when>
                                <c:when test="${employerDepartment.name eq 'Sales'}"><c:set var="employerFilterLabel" value="Kinh doanh" /></c:when>
                            </c:choose>
                            <button type="button" data-employer-filter="department-${employerDepartment.id}" aria-pressed="false"><c:out value="${employerFilterLabel}" /></button>
                        </c:forEach>
                        <button type="button" data-employer-filter="internship" aria-pressed="false">Thực tập</button>
                        <button type="button" data-employer-filter="remote" aria-pressed="false">Làm từ xa</button>
                        <button type="button" data-employer-filter="contract" aria-pressed="false">Hợp đồng</button>
                    </div>
                    <div class="featured-employer-controls" aria-label="Điều khiển danh sách doanh nghiệp">
                        <button class="featured-employer-autoplay is-playing" type="button" data-employer-autoplay aria-label="Tạm dừng tự động trượt" aria-pressed="true"><i class="bi bi-pause-fill"></i></button>
                        <button type="button" data-employer-scroll="previous" aria-label="Xem doanh nghiệp phía trước"><i class="bi bi-chevron-left"></i></button>
                        <button type="button" data-employer-scroll="next" aria-label="Xem thêm doanh nghiệp"><i class="bi bi-chevron-right"></i></button>
                    </div>
                </div>

                <div class="featured-employers-layout">
                    <a class="featured-employer-spotlight" href="${featuredEmployerAllUrl}">
                        <span class="featured-employer-spotlight-label"><i class="bi bi-lightning-charge-fill"></i>Đang tuyển nổi bật</span>
                        <span class="featured-employer-spotlight-logo"><img src="${pageContext.request.contextPath}/assets/images/employers/recruitflow-tech.svg" alt="Logo RecruitFlow Technology"></span>
                        <div><h3>RecruitFlow Technology</h3><p>Công nghệ tuyển dụng &amp; phần mềm</p><span><i class="bi bi-briefcase"></i><c:out value="${publishedJobCount}" /> vị trí đang mở</span></div>
                        <strong>Top Employer <i class="bi bi-patch-check-fill"></i></strong>
                    </a>

                    <div class="featured-employer-grid" data-employer-grid>
                        <c:forEach var="employerDepartment" items="${departments}" varStatus="employerStatus">
                            <c:set var="employerName" value="${employerDepartment.name} Careers" />
                            <c:set var="employerMark" value="${fn:toUpperCase(fn:substring(employerDepartment.name, 0, 1))}" />
                            <c:set var="employerTheme" value="theme-${(employerStatus.index mod 5) + 1}" />
                            <c:set var="employerIndustryLabel" value="${employerDepartment.name}" />
                            <c:set var="employerLogoFile" value="generic-careers.svg" />
                            <c:choose>
                                <c:when test="${employerDepartment.name eq 'Information Technology'}"><c:set var="employerName" value="NovaTech Solutions" /><c:set var="employerMark" value="NT" /><c:set var="employerIndustryLabel" value="IT - Phần mềm" /><c:set var="employerLogoFile" value="novatech.svg" /></c:when>
                                <c:when test="${employerDepartment.name eq 'Finance'}"><c:set var="employerName" value="Horizon Finance" /><c:set var="employerMark" value="HF" /><c:set var="employerIndustryLabel" value="Tài chính" /><c:set var="employerLogoFile" value="horizon-finance.svg" /></c:when>
                                <c:when test="${employerDepartment.name eq 'Human Resources'}"><c:set var="employerName" value="PeopleFirst Group" /><c:set var="employerMark" value="PF" /><c:set var="employerIndustryLabel" value="Nhân sự" /><c:set var="employerLogoFile" value="peoplefirst.svg" /></c:when>
                                <c:when test="${employerDepartment.name eq 'Marketing'}"><c:set var="employerName" value="Aurora Media" /><c:set var="employerMark" value="AM" /><c:set var="employerLogoFile" value="aurora-media.svg" /></c:when>
                                <c:when test="${employerDepartment.name eq 'Sales'}"><c:set var="employerName" value="NextCommerce" /><c:set var="employerMark" value="NC" /><c:set var="employerIndustryLabel" value="Kinh doanh" /><c:set var="employerLogoFile" value="nextcommerce.svg" /></c:when>
                            </c:choose>
                            <c:url var="employerDepartmentUrl" value="/companies/detail"><c:param name="id" value="${employerDepartment.id}" /></c:url>
                            <a class="featured-employer-card" href="${employerDepartmentUrl}" data-employer-industry="department-${employerDepartment.id}">
                                <span class="featured-employer-logo ${employerTheme}"><img src="${pageContext.request.contextPath}/assets/images/employers/${employerLogoFile}" alt="Logo ${employerName}" loading="lazy"></span>
                                <div><h3><c:out value="${employerName}" /></h3><p><c:out value="${employerIndustryLabel}" /></p><span><i class="bi bi-briefcase"></i>Xem việc đang tuyển</span></div>
                                <i class="bi bi-arrow-up-right featured-employer-card-arrow"></i>
                            </a>
                        </c:forEach>

                        <a class="featured-employer-card" href="${featuredInternshipUrl}" data-employer-industry="internship">
                            <span class="featured-employer-logo theme-6"><img src="${pageContext.request.contextPath}/assets/images/employers/futureskills.svg" alt="Logo FutureSkills Academy" loading="lazy"></span><div><h3>FutureSkills Academy</h3><p>Đào tạo &amp; phát triển tài năng</p><span><i class="bi bi-mortarboard"></i>Cơ hội thực tập</span></div><i class="bi bi-arrow-up-right featured-employer-card-arrow"></i>
                        </a>
                        <a class="featured-employer-card" href="${featuredRemoteUrl}" data-employer-industry="remote">
                            <span class="featured-employer-logo theme-7"><img src="${pageContext.request.contextPath}/assets/images/employers/remoteworks.svg" alt="Logo RemoteWorks Asia" loading="lazy"></span><div><h3>RemoteWorks Asia</h3><p>Công nghệ &amp; làm việc linh hoạt</p><span><i class="bi bi-house-laptop"></i>Việc làm từ xa</span></div><i class="bi bi-arrow-up-right featured-employer-card-arrow"></i>
                        </a>
                        <a class="featured-employer-card" href="${featuredContractUrl}" data-employer-industry="contract">
                            <span class="featured-employer-logo theme-8"><img src="${pageContext.request.contextPath}/assets/images/employers/projectlink.svg" alt="Logo ProjectLink Partners" loading="lazy"></span><div><h3>ProjectLink Partners</h3><p>Tư vấn &amp; triển khai dự án</p><span><i class="bi bi-file-earmark-text"></i>Việc làm hợp đồng</span></div><i class="bi bi-arrow-up-right featured-employer-card-arrow"></i>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="public-section public-how">
        <div class="container">
            <div class="text-center mx-auto public-section-intro"><span class="section-kicker">QUY TRÌNH ĐƠN GIẢN</span><h2>Ba bước đến gần công việc mới</h2><p>RecruitFlow giúp bạn chủ động ở mọi giai đoạn của hành trình ứng tuyển.</p></div>
            <div class="row g-4 mt-2">
                <div class="col-md-4"><article class="how-card"><span>01</span><div class="how-icon"><i class="bi bi-person-vcard"></i></div><h3>Hoàn thiện hồ sơ</h3><p>Cập nhật kỹ năng, kinh nghiệm và tải CV để hệ thống hiểu rõ năng lực của bạn.</p></article></div>
                <div class="col-md-4"><article class="how-card"><span>02</span><div class="how-icon"><i class="bi bi-stars"></i></div><h3>Chọn việc phù hợp</h3><p>Nhận điểm phù hợp theo CV, xem thông tin minh bạch và ứng tuyển chỉ trong vài bước.</p></article></div>
                <div class="col-md-4"><article class="how-card"><span>03</span><div class="how-icon"><i class="bi bi-graph-up-arrow"></i></div><h3>Theo dõi tiến trình</h3><p>Nhận lịch phỏng vấn, phản hồi thư mời và hoàn tất tiếp nhận trên cùng một nền tảng.</p></article></div>
            </div>
        </div>
    </section>

    <section class="public-section bg-white border-top border-bottom">
        <div class="container">
            <div class="section-header"><div><span class="section-kicker">LĨNH VỰC NGHỀ NGHIỆP</span><h2>Khám phá theo phòng ban</h2><p>Đi thẳng đến nhóm công việc phù hợp với chuyên môn của bạn.</p></div></div>
            <div class="row g-3"><c:forEach var="department" items="${departments}"><c:url var="departmentJobsUrl" value="${homeSearchTarget}"><c:param name="departmentId" value="${department.id}" /></c:url><div class="col-sm-6 col-lg-4 col-xl-3"><a class="department-tile department-tile-v2" href="${departmentJobsUrl}"><span><i class="bi bi-building"></i></span><div><strong><c:out value="${department.displayName}" /></strong><small>Xem vị trí đang tuyển</small></div><i class="bi bi-arrow-right ms-auto"></i></a></div></c:forEach></div>
        </div>
    </section>

    <section class="public-section">
        <div class="container"><div class="public-cta"><div><span class="section-kicker text-white-50">BẮT ĐẦU NGAY HÔM NAY</span><h2>Sẵn sàng cho cơ hội tiếp theo?</h2><p>Tạo hồ sơ, tải CV và để RecruitFlow đồng hành cùng bước tiến sự nghiệp của bạn.</p></div><c:choose><c:when test="${sessionScope.role eq 'CANDIDATE'}"><a class="btn btn-light" href="${pageContext.request.contextPath}/candidate/jobs">Khám phá việc làm<i class="bi bi-arrow-right ms-1"></i></a></c:when><c:otherwise><a class="btn btn-light" href="${pageContext.request.contextPath}/register">Tạo tài khoản miễn phí<i class="bi bi-arrow-right ms-1"></i></a></c:otherwise></c:choose></div></div>
    </section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
