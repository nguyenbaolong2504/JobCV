<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<nav class="navbar navbar-expand-lg rf-navbar sticky-top">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${pageContext.request.contextPath}/">
            <span class="rf-brand-mark">R</span>
            <span class="rf-brand-name">RecruitFlow</span>
        </a>

        <div class="collapse navbar-collapse" id="publicNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <c:choose>
                    <c:when test="${sessionScope.role eq 'CANDIDATE'}">
                        <c:url var="salesJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Kinh doanh" /></c:url>
                        <c:url var="accountingJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Kế toán" /></c:url>
                        <c:url var="marketingJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Marketing" /></c:url>
                        <c:url var="hrJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Nhân sự" /></c:url>
                        <c:url var="itJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="IT" /></c:url>
                        <c:url var="designJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Thiết kế" /></c:url>
                        <c:url var="customerJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Chăm sóc khách hàng" /></c:url>
                        <c:url var="constructionJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Xây dựng" /></c:url>
                        <c:url var="javaJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Java" /></c:url>
                        <c:url var="testerJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Tester" /></c:url>
                        <c:url var="frontendJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Frontend" /></c:url>
                        <c:url var="analystJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Business Analyst" /></c:url>
                        <c:url var="internJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Intern" /></c:url>
                        <c:url var="softwareJobsUrl" value="/candidate/jobs"><c:param name="keyword" value="Software" /></c:url>
                        <c:url var="hanoiJobsUrl" value="/candidate/jobs"><c:param name="location" value="Hanoi" /></c:url>
                        <c:url var="hcmJobsUrl" value="/candidate/jobs"><c:param name="location" value="Ho Chi Minh City" /></c:url>
                        <c:url var="danangJobsUrl" value="/candidate/jobs"><c:param name="location" value="Da Nang" /></c:url>
                        <c:url var="fullTimeJobsUrl" value="/candidate/jobs"><c:param name="employmentType" value="FULL_TIME" /></c:url>
                        <c:url var="partTimeJobsUrl" value="/candidate/jobs"><c:param name="employmentType" value="PART_TIME" /></c:url>
                        <c:url var="internshipJobsUrl" value="/candidate/jobs"><c:param name="employmentType" value="INTERNSHIP" /></c:url>
                        <c:url var="contractJobsUrl" value="/candidate/jobs"><c:param name="employmentType" value="CONTRACT" /></c:url>
                        <c:url var="remoteJobsUrl" value="/candidate/jobs"><c:param name="employmentType" value="REMOTE" /></c:url>

                        <li class="nav-item dropdown position-static candidate-mega-nav">
                            <button class="nav-link rf-nav-link candidate-nav-toggle dropdown-toggle" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                                Việc làm
                            </button>
                            <div class="dropdown-menu candidate-jobs-mega-menu shadow-lg">
                                <div class="candidate-mega-menu-head">
                                    <div><span>KHÁM PHÁ CƠ HỘI</span><strong>Tìm công việc phù hợp với bạn</strong></div>
                                    <div class="candidate-mega-menu-actions">
                                        <form action="${pageContext.request.contextPath}/candidate/jobs" method="get">
                                            <label class="visually-hidden" for="megaMenuKeyword">Tìm nhanh việc làm</label>
                                            <i class="bi bi-search"></i><input id="megaMenuKeyword" name="keyword" type="search" placeholder="Chức danh, kỹ năng..."><button type="submit">Tìm việc</button>
                                        </form>
                                        <a href="${pageContext.request.contextPath}/candidate/jobs">Xem tất cả<i class="bi bi-arrow-right"></i></a>
                                    </div>
                                </div>
                                <div class="candidate-mega-menu-grid">
                                    <section class="candidate-mega-menu-primary">
                                        <h2>VIỆC LÀM</h2>
                                        <a href="${pageContext.request.contextPath}/candidate/jobs"><i class="bi bi-search"></i><span><strong>Tìm việc làm</strong><small>Tìm kiếm và lọc cơ hội mới</small></span></a>
                                        <a href="${pageContext.request.contextPath}/candidate/saved-jobs"><i class="bi bi-bookmark-heart"></i><span><strong>Việc làm đã lưu</strong><small>Xem lại các vị trí quan tâm</small></span></a>
                                        <a href="${pageContext.request.contextPath}/candidate/applications"><i class="bi bi-file-earmark-check"></i><span><strong>Việc làm đã ứng tuyển</strong><small>Theo dõi tiến trình hồ sơ</small></span></a>
                                        <a href="${pageContext.request.contextPath}/candidate/jobs?sort=newest"><i class="bi bi-hand-thumbs-up"></i><span><strong>Việc làm phù hợp</strong><small>Gợi ý dựa trên hồ sơ và CV</small></span></a>
                                        <a href="${pageContext.request.contextPath}/candidate/jobs?sort=salary"><i class="bi bi-cash-coin"></i><span><strong>Việc lương cao</strong><small>Ưu tiên theo mức lương hấp dẫn</small></span></a>
                                        <h2 class="mt-4">CÔNG CỤ TÌM VIỆC</h2>
                                        <a href="${pageContext.request.contextPath}/candidate/job-alerts"><i class="bi bi-bell"></i><span><strong>Thông báo việc làm</strong><small>Không bỏ lỡ cơ hội mới</small></span></a>
                                        <a href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-file-earmark-person"></i><span><strong>CV &amp; hồ sơ nghề nghiệp</strong><small>Tăng độ phù hợp khi tìm việc</small></span></a>
                                    </section>
                                    <section>
                                        <h2>VỊ TRÍ &amp; KỸ NĂNG PHỔ BIẾN</h2>
                                        <div class="candidate-mega-link-columns">
                                            <a href="${salesJobsUrl}">Nhân viên kinh doanh<i class="bi bi-chevron-right"></i></a>
                                            <a href="${accountingJobsUrl}">Kế toán<i class="bi bi-chevron-right"></i></a>
                                            <a href="${marketingJobsUrl}">Marketing<i class="bi bi-chevron-right"></i></a>
                                            <a href="${hrJobsUrl}">Hành chính nhân sự<i class="bi bi-chevron-right"></i></a>
                                            <a href="${itJobsUrl}">IT - Phần mềm<i class="bi bi-chevron-right"></i></a>
                                            <a href="${designJobsUrl}">Thiết kế đồ họa<i class="bi bi-chevron-right"></i></a>
                                            <a href="${customerJobsUrl}">Chăm sóc khách hàng<i class="bi bi-chevron-right"></i></a>
                                            <a href="${constructionJobsUrl}">Kỹ sư xây dựng<i class="bi bi-chevron-right"></i></a>
                                            <a href="${javaJobsUrl}">Java Developer<i class="bi bi-chevron-right"></i></a>
                                            <a href="${testerJobsUrl}">Software Tester<i class="bi bi-chevron-right"></i></a>
                                            <a href="${frontendJobsUrl}">Frontend Developer<i class="bi bi-chevron-right"></i></a>
                                            <a href="${analystJobsUrl}">Business Analyst<i class="bi bi-chevron-right"></i></a>
                                            <a href="${internJobsUrl}">Thực tập sinh<i class="bi bi-chevron-right"></i></a>
                                            <a href="${softwareJobsUrl}">Kỹ sư phần mềm<i class="bi bi-chevron-right"></i></a>
                                        </div>
                                    </section>
                                    <section class="candidate-mega-menu-discovery">
                                        <h2>KHÁM PHÁ NHANH</h2>
                                        <a href="${hanoiJobsUrl}"><span><i class="bi bi-geo-alt"></i></span><div><strong>Việc làm Hà Nội</strong><small>Cơ hội tại khu vực Hà Nội</small></div></a>
                                        <a href="${hcmJobsUrl}"><span><i class="bi bi-buildings"></i></span><div><strong>Việc làm TP.HCM</strong><small>Cơ hội tại Thành phố Hồ Chí Minh</small></div></a>
                                        <a href="${danangJobsUrl}"><span><i class="bi bi-water"></i></span><div><strong>Việc làm Đà Nẵng</strong><small>Cơ hội tại khu vực miền Trung</small></div></a>
                                        <a href="${fullTimeJobsUrl}"><span><i class="bi bi-briefcase"></i></span><div><strong>Việc làm toàn thời gian</strong><small>Các vị trí tuyển dụng chính thức</small></div></a>
                                        <a href="${internshipJobsUrl}"><span><i class="bi bi-mortarboard"></i></span><div><strong>Việc làm thực tập</strong><small>Cơ hội tích lũy kinh nghiệm</small></div></a>
                                        <a href="${partTimeJobsUrl}"><span><i class="bi bi-clock-history"></i></span><div><strong>Việc làm bán thời gian</strong><small>Linh hoạt thời gian làm việc</small></div></a>
                                        <a href="${contractJobsUrl}"><span><i class="bi bi-file-earmark-text"></i></span><div><strong>Việc làm hợp đồng</strong><small>Dự án và hợp đồng có thời hạn</small></div></a>
                                        <a href="${remoteJobsUrl}"><span><i class="bi bi-house-laptop"></i></span><div><strong>Việc làm từ xa</strong><small>Cơ hội làm việc linh hoạt</small></div></a>
                                    </section>
                                </div>
                            </div>
                        </li>

                        <li class="nav-item"><a class="nav-link rf-nav-link" href="${pageContext.request.contextPath}/companies">Công ty</a></li>

                        <li class="nav-item dropdown candidate-compact-nav">
                            <button class="nav-link rf-nav-link candidate-nav-toggle dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">Tạo CV</button>
                            <div class="dropdown-menu candidate-compact-menu shadow-lg">
                                <div class="candidate-compact-menu-title"><span><i class="bi bi-file-earmark-person"></i></span><div><strong>CV chuyên nghiệp</strong><small>Tạo lợi thế khi ứng tuyển</small></div></div>
                                <a href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-folder2-open"></i><span><strong>CV của tôi</strong><small>Quản lý CV đã tải lên</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/resumes#resumeFile"><i class="bi bi-cloud-arrow-up"></i><span><strong>Tải CV mới</strong><small>Hỗ trợ PDF, DOC và DOCX</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-stars"></i><span><strong>AI CV Coach</strong><small>Đánh giá và cải thiện CV</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/profile"><i class="bi bi-person-lines-fill"></i><span><strong>Hồ sơ nghề nghiệp</strong><small>Bổ sung kỹ năng và kinh nghiệm</small></span></a>
                            </div>
                        </li>

                        <li class="nav-item dropdown candidate-compact-nav">
                            <button class="nav-link rf-nav-link candidate-nav-toggle dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">Công cụ</button>
                            <div class="dropdown-menu candidate-compact-menu shadow-lg">
                                <div class="candidate-compact-menu-title"><span><i class="bi bi-grid"></i></span><div><strong>Công cụ sự nghiệp</strong><small>Quản lý quá trình tìm việc</small></div></div>
                                <a href="${pageContext.request.contextPath}/candidate/profile"><i class="bi bi-person-vcard"></i><span><strong>Hồ sơ cá nhân</strong><small>Cập nhật thông tin nghề nghiệp</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/dashboard"><i class="bi bi-speedometer2"></i><span><strong>Trung tâm quản lý</strong><small>Tổng quan hoạt động của bạn</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/job-alerts"><i class="bi bi-bell"></i><span><strong>Thông báo việc làm</strong><small>Tạo bộ lọc cơ hội phù hợp</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/saved-jobs"><i class="bi bi-bookmark-heart"></i><span><strong>Kho việc làm đã lưu</strong><small>Quản lý các cơ hội quan tâm</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/notifications"><i class="bi bi-bell-fill"></i><span><strong>Trung tâm thông báo</strong><small>Cập nhật mới từ nhà tuyển dụng</small></span></a>
                            </div>
                        </li>

                        <li class="nav-item dropdown candidate-compact-nav">
                            <button class="nav-link rf-nav-link candidate-nav-toggle dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">Hành trình ứng tuyển</button>
                            <div class="dropdown-menu candidate-compact-menu candidate-journey-menu shadow-lg">
                                <div class="candidate-compact-menu-title"><span><i class="bi bi-signpost-split"></i></span><div><strong>Tiến trình của bạn</strong><small>Theo dõi từ ứng tuyển đến nhận việc</small></div></div>
                                <a href="${pageContext.request.contextPath}/candidate/applications"><i class="bi bi-send-check"></i><span><strong>Đơn ứng tuyển</strong><small>Xem trạng thái từng hồ sơ</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/interviews"><i class="bi bi-calendar2-check"></i><span><strong>Lịch phỏng vấn</strong><small>Thời gian và thông tin buổi hẹn</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/offers"><i class="bi bi-envelope-paper-heart"></i><span><strong>Thư mời nhận việc</strong><small>Xem và phản hồi đề nghị</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/onboarding"><i class="bi bi-rocket-takeoff"></i><span><strong>Hội nhập công việc</strong><small>Hoàn thành nhiệm vụ nhận việc</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/notifications"><i class="bi bi-megaphone"></i><span><strong>Cập nhật tuyển dụng</strong><small>Tất cả thông báo quan trọng</small></span></a>
                            </div>
                        </li>

                        <li class="nav-item dropdown candidate-compact-nav">
                            <button class="nav-link rf-nav-link candidate-nav-toggle dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">Cẩm nang nghề nghiệp</button>
                            <div class="dropdown-menu candidate-compact-menu candidate-career-menu shadow-lg">
                                <div class="candidate-compact-menu-title"><span><i class="bi bi-compass"></i></span><div><strong>Hành trình nghề nghiệp</strong><small>Chủ động trong từng giai đoạn</small></div></div>
                                <a href="${pageContext.request.contextPath}/candidate/interviews"><i class="bi bi-calendar2-check"></i><span><strong>Lịch phỏng vấn</strong><small>Chuẩn bị cho buổi gặp nhà tuyển dụng</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/offers"><i class="bi bi-envelope-paper-heart"></i><span><strong>Thư mời nhận việc</strong><small>Xem và phản hồi đề nghị</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/onboarding"><i class="bi bi-rocket-takeoff"></i><span><strong>Bắt đầu công việc mới</strong><small>Theo dõi quá trình hội nhập</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/profile"><i class="bi bi-person-check"></i><span><strong>Chuẩn bị hồ sơ</strong><small>Hoàn thiện nền tảng nghề nghiệp</small></span></a>
                                <a href="${pageContext.request.contextPath}/candidate/resumes"><i class="bi bi-lightbulb"></i><span><strong>Tối ưu CV bằng AI</strong><small>Nhận gợi ý trước khi ứng tuyển</small></span></a>
                            </div>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item"><a class="nav-link rf-nav-link" href="${pageContext.request.contextPath}/">Trang chủ</a></li>
                        <li class="nav-item"><a class="nav-link rf-nav-link" href="${pageContext.request.contextPath}/jobs">Việc làm</a></li>
                        <li class="nav-item"><a class="nav-link rf-nav-link" href="${pageContext.request.contextPath}/companies">Công ty</a></li>
                        <li class="nav-item"><a class="nav-link rf-nav-link" href="${pageContext.request.contextPath}/register">Tạo CV</a></li>
                    </c:otherwise>
                </c:choose>
            </ul>

            <c:choose>
                <c:when test="${not empty sessionScope.userId and sessionScope.role ne 'CANDIDATE'}">
                    <div class="dropdown">
                        <button class="btn btn-light border dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false"><i class="bi bi-person-circle me-1"></i><c:out value="${sessionScope.fullName}" /></button>
                        <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                            <c:choose>
                                <c:when test="${sessionScope.role eq 'HR'}"><li><a class="dropdown-item" href="${pageContext.request.contextPath}/hr/dashboard">Bảng điều khiển nhân sự</a></li></c:when>
                                <c:when test="${sessionScope.role eq 'INTERVIEWER'}"><li><a class="dropdown-item" href="${pageContext.request.contextPath}/interviewer/dashboard">Bảng điều khiển phỏng vấn</a></li></c:when>
                                <c:when test="${sessionScope.role eq 'ADMIN'}"><li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/dashboard">Bảng điều khiển quản trị</a></li></c:when>
                            </c:choose>
                            <li><hr class="dropdown-divider"></li>
                            <li><form action="${pageContext.request.contextPath}/logout" method="post"><input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><button class="dropdown-item text-danger" type="submit"><i class="bi bi-box-arrow-right me-2"></i>Đăng xuất</button></form></li>
                        </ul>
                    </div>
                </c:when>
                <c:when test="${empty sessionScope.userId}">
                    <div class="d-flex gap-2"><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/login">Đăng nhập</a><a class="btn btn-primary" href="${pageContext.request.contextPath}/register">Đăng ký</a></div>
                </c:when>
            </c:choose>
        </div>

        <c:if test="${sessionScope.role eq 'CANDIDATE'}">
            <div class="dropdown candidate-profile-menu ms-auto">
                <button class="candidate-profile-trigger" type="button" data-bs-toggle="dropdown" aria-expanded="false" aria-label="Mở trang cá nhân">
                    <span class="candidate-profile-trigger-avatar"><c:out value="${fn:toUpperCase(fn:substring(sessionScope.fullName, 0, 1))}" /></span>
                    <span class="candidate-profile-trigger-copy"><small>Trang cá nhân</small><strong><c:out value="${sessionScope.fullName}" /></strong></span>
                    <i class="bi bi-chevron-down"></i>
                </button>
                <div class="dropdown-menu dropdown-menu-end candidate-profile-dropdown shadow-lg">
                    <div class="candidate-profile-dropdown-head"><span><c:out value="${fn:toUpperCase(fn:substring(sessionScope.fullName, 0, 1))}" /></span><div><strong><c:out value="${sessionScope.fullName}" /></strong><small>Tài khoản người tìm việc</small></div></div>
                    <div class="candidate-profile-dropdown-grid">
                        <a href="${pageContext.request.contextPath}/candidate/profile"><span><i class="bi bi-person-vcard"></i></span><strong>Hồ sơ cá nhân</strong><small>Thông tin nghề nghiệp</small></a>
                        <a href="${pageContext.request.contextPath}/candidate/resumes"><span><i class="bi bi-file-earmark-person"></i></span><strong>CV của tôi</strong><small>Quản lý và đánh giá CV</small></a>
                        <a href="${pageContext.request.contextPath}/candidate/saved-jobs"><span><i class="bi bi-bookmark-heart"></i></span><strong>Việc đã lưu</strong><small>Xem lại cơ hội quan tâm</small></a>
                        <a href="${pageContext.request.contextPath}/candidate/job-alerts"><span><i class="bi bi-bell-fill"></i></span><strong>Thông báo việc làm</strong><small>Bộ lọc tìm việc của bạn</small></a>
                    </div>
                    <div class="candidate-profile-dropdown-links">
                        <a href="${pageContext.request.contextPath}/candidate/applications"><i class="bi bi-send-check"></i>Việc làm đã ứng tuyển<i class="bi bi-chevron-right ms-auto"></i></a>
                        <a href="${pageContext.request.contextPath}/candidate/interviews"><i class="bi bi-calendar2-check"></i>Lịch phỏng vấn<i class="bi bi-chevron-right ms-auto"></i></a>
                        <a href="${pageContext.request.contextPath}/candidate/notifications"><i class="bi bi-bell"></i>Trung tâm thông báo<i class="bi bi-chevron-right ms-auto"></i></a>
                        <a href="${pageContext.request.contextPath}/candidate/dashboard"><i class="bi bi-grid-1x2"></i>Trung tâm quản lý<i class="bi bi-chevron-right ms-auto"></i></a>
                    </div>
                    <form class="candidate-profile-logout" action="${pageContext.request.contextPath}/logout" method="post"><input type="hidden" name="_csrf" value="<c:out value='${requestScope.csrfToken}'/>"><button type="submit"><i class="bi bi-box-arrow-right"></i>Đăng xuất</button></form>
                </div>
            </div>
        </c:if>

        <button class="navbar-toggler ${sessionScope.role eq 'CANDIDATE' ? 'ms-2' : 'ms-auto'}" type="button" data-bs-toggle="collapse" data-bs-target="#publicNavbar" aria-controls="publicNavbar" aria-expanded="false" aria-label="Mở điều hướng"><span class="navbar-toggler-icon"></span></button>
    </div>
</nav>
