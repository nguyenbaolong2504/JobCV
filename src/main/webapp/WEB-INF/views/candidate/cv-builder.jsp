<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Tạo CV theo mẫu | JobCV" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container-fluid candidate-shell">
    <div class="row g-0">
        <jsp:include page="/WEB-INF/views/common/candidate-sidebar.jsp" />

        <main class="candidate-main col-lg-9 col-xl-10">
            <jsp:include page="/WEB-INF/views/common/flash.jsp" />

            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
                <div>
                    <p class="text-primary text-uppercase small fw-bold mb-1">CV Builder</p>
                    <h1 class="page-title mb-1">Tạo CV theo mẫu</h1>
                    <p class="text-muted mb-0">Chọn mẫu, điền thông tin thật của bạn và tạo CV DOCX có thể dùng ngay khi ứng tuyển.</p>
                </div>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/candidate/resumes">
                    <i class="bi bi-folder2-open me-1"></i>CV của tôi
                </a>
            </div>

            <section class="content-card cv-builder-intro mb-4">
                <div class="row align-items-center g-3">
                    <div class="col-lg">
                        <h2 class="h5 fw-bold mb-2">3 mẫu CV sẵn sàng cho ứng tuyển</h2>
                        <p class="text-muted mb-0">CV sẽ được tạo dưới dạng DOCX, lưu trong khu vực CV của bạn và có thể chọn làm CV mặc định.</p>
                    </div>
                    <div class="col-lg-auto">
                        <span class="badge text-bg-light border text-dark px-3 py-2"><i class="bi bi-shield-check text-success me-1"></i>Không tự nộp đơn thay bạn</span>
                    </div>
                </div>
            </section>

            <form id="cvBuilderForm" method="post" action="${pageContext.request.contextPath}/candidate/cv-builder/create" class="row g-4">
                <input id="cvTemplateInput" type="hidden" name="template" value="MODERN">

                <div class="col-xl-8">
                    <section class="content-card mb-4">
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                            <div>
                                <h2 class="h5 fw-bold mb-1">1. Chọn mẫu CV</h2>
                                <p class="small text-muted mb-0">Bạn có thể xem trước và đổi mẫu trước khi tạo.</p>
                            </div>
                            <span class="small text-muted"><i class="bi bi-file-earmark-word me-1"></i>Xuất DOCX</span>
                        </div>
                        <div class="row g-3" role="radiogroup" aria-label="Mẫu CV">
                            <div class="col-md-4">
                                <button type="button" class="cv-template-card is-selected w-100" data-cv-template="MODERN" role="radio" aria-checked="true">
                                    <span class="cv-template-preview modern"><span></span><strong>NGUYỄN MINH AN</strong><small>Backend Developer</small><i></i><b></b></span>
                                    <strong class="d-block mt-3">Hiện đại</strong>
                                    <small class="text-muted">Gọn gàng, có điểm nhấn màu xanh</small>
                                </button>
                            </div>
                            <div class="col-md-4">
                                <button type="button" class="cv-template-card w-100" data-cv-template="CLASSIC" role="radio" aria-checked="false">
                                    <span class="cv-template-preview classic"><strong>NGUYỄN MINH AN</strong><small>Backend Developer</small><i></i><b></b></span>
                                    <strong class="d-block mt-3">Chuyên nghiệp</strong>
                                    <small class="text-muted">Trang trọng, phù hợp doanh nghiệp</small>
                                </button>
                            </div>
                            <div class="col-md-4">
                                <button type="button" class="cv-template-card w-100" data-cv-template="MINIMAL" role="radio" aria-checked="false">
                                    <span class="cv-template-preview minimal"><strong>Nguyễn Minh An</strong><small>Backend Developer</small><i></i><b></b></span>
                                    <strong class="d-block mt-3">Tối giản</strong>
                                    <small class="text-muted">Tập trung vào nội dung và kỹ năng</small>
                                </button>
                            </div>
                        </div>
                    </section>

                    <section class="content-card mb-4">
                        <h2 class="h5 fw-bold mb-3">2. Thông tin liên hệ</h2>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label" for="cvFullName">Họ và tên <span class="text-danger">*</span></label>
                                <input class="form-control" id="cvFullName" name="fullName" required maxlength="100" data-cv-preview="name"
                                       value="<c:out value='${user.fullName}'/>" placeholder="Nguyễn Văn A">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="cvTargetRole">Vị trí mục tiêu <span class="text-danger">*</span></label>
                                <input class="form-control" id="cvTargetRole" name="targetRole" required maxlength="120" data-cv-preview="role"
                                       placeholder="Ví dụ: Java Backend Developer">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="cvEmail">Email</label>
                                <input class="form-control" id="cvEmail" name="email" type="email" maxlength="254" data-cv-preview="email"
                                       value="<c:out value='${user.email}'/>" placeholder="email@example.com">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label" for="cvPhone">Số điện thoại</label>
                                <input class="form-control" id="cvPhone" name="phone" maxlength="30" data-cv-preview="phone" placeholder="09xx xxx xxx">
                            </div>
                            <div class="col-md-3">
                                <label class="form-label" for="cvLocation">Địa điểm</label>
                                <input class="form-control" id="cvLocation" name="location" maxlength="120" data-cv-preview="location"
                                       value="<c:out value='${profile.address}'/>" placeholder="Hà Nội">
                            </div>
                        </div>
                    </section>

                    <section class="content-card mb-4">
                        <h2 class="h5 fw-bold mb-3">3. Nội dung CV</h2>
                        <div class="mb-3">
                            <label class="form-label" for="cvSummary">Giới thiệu ngắn <span class="text-danger">*</span></label>
                            <textarea class="form-control" id="cvSummary" name="summary" rows="4" required maxlength="2000" data-cv-preview="summary"
                                      placeholder="Tóm tắt năng lực, mục tiêu nghề nghiệp và giá trị bạn có thể mang lại."><c:out value="${profile.summary}" /></textarea>
                            <div class="form-text">Viết ngắn gọn, trung thực và bám sát vị trí mục tiêu.</div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="cvExperience">Kinh nghiệm làm việc</label>
                            <textarea class="form-control" id="cvExperience" name="experience" rows="5" maxlength="6000" data-cv-preview="experience"
                                      placeholder="2024 – nay | Công ty ABC | Java Intern&#10;Phát triển REST API, viết unit test và tối ưu truy vấn MySQL."></textarea>
                            <div class="form-text">Mỗi dòng sẽ trở thành một gạch đầu dòng trong CV.</div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="cvEducation">Học vấn</label>
                            <textarea class="form-control" id="cvEducation" name="education" rows="3" maxlength="4000" data-cv-preview="education"
                                      placeholder="2021 – 2025 | Đại học ABC | Công nghệ thông tin&#10;Chuyên ngành: Kỹ thuật phần mềm"></textarea>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="cvSkills">Kỹ năng</label>
                            <textarea class="form-control" id="cvSkills" name="skills" rows="3" maxlength="2000" data-cv-preview="skills"
                                      placeholder="Java, Spring Boot, JDBC, MySQL, Git, REST API"><c:out value="${profile.skills}" /></textarea>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="cvProjects">Dự án nổi bật</label>
                            <textarea class="form-control" id="cvProjects" name="projects" rows="4" maxlength="5000" data-cv-preview="projects"
                                      placeholder="JobCV | Java Servlet, JSP, MySQL&#10;Xây dựng quy trình tuyển dụng, phân quyền và quản lý onboarding."></textarea>
                        </div>
                        <div>
                            <label class="form-label" for="cvCertifications">Chứng chỉ / hoạt động</label>
                            <textarea class="form-control" id="cvCertifications" name="certifications" rows="3" maxlength="3000" data-cv-preview="certifications"
                                      placeholder="TOEIC 800 | AWS Cloud Practitioner | Thành viên CLB lập trình"></textarea>
                        </div>
                    </section>

                    <section class="content-card d-flex flex-wrap justify-content-between align-items-center gap-3">
                        <div class="form-check mb-0">
                            <input class="form-check-input" type="checkbox" name="makeDefault" value="true" id="makeDefault" checked>
                            <label class="form-check-label" for="makeDefault">Đặt CV vừa tạo làm CV mặc định</label>
                        </div>
                        <button class="btn btn-primary px-4" type="submit" data-loading-button>
                            <i class="bi bi-file-earmark-plus me-1"></i>Tạo CV DOCX
                        </button>
                    </section>
                </div>

                <aside class="col-xl-4">
                    <div class="cv-live-preview sticky-xl-top" style="top: 5.5rem;">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h2 class="h6 fw-bold mb-0"><i class="bi bi-eye me-1"></i>Xem trước nội dung</h2>
                            <span class="badge text-bg-light border text-dark" id="previewTemplateLabel">Hiện đại</span>
                        </div>
                        <article class="cv-paper cv-paper-modern" id="cvLivePaper">
                            <header>
                                <h3 id="previewName"><c:out value="${user.fullName}" /></h3>
                                <p id="previewRole">Vị trí mục tiêu</p>
                                <small id="previewContact">Email · Số điện thoại · Địa điểm</small>
                            </header>
                            <section>
                                <h4>GIỚI THIỆU</h4>
                                <p id="previewSummary">Hãy tóm tắt năng lực, mục tiêu nghề nghiệp và điểm mạnh của bạn.</p>
                            </section>
                            <section>
                                <h4>KINH NGHIỆM</h4>
                                <p id="previewExperience">Kinh nghiệm làm việc của bạn sẽ hiển thị tại đây.</p>
                            </section>
                            <section>
                                <h4>KỸ NĂNG</h4>
                                <p id="previewSkills">Các kỹ năng chuyên môn phù hợp vị trí.</p>
                            </section>
                        </article>
                        <p class="small text-muted mt-3 mb-0"><i class="bi bi-info-circle me-1"></i>Bản xem trước chỉ để định hướng bố cục; file DOCX sẽ chứa toàn bộ phần bạn điền.</p>
                    </div>
                </aside>
            </form>
        </main>
    </div>
</div>

<script>
    (function () {
        function textOrFallback(value, fallback) {
            return value && value.trim() ? value.trim() : fallback;
        }
        function updateContact() {
            var parts = ['email', 'phone', 'location'].map(function (key) {
                var field = document.querySelector('[data-cv-preview="' + key + '"]');
                return field ? field.value.trim() : '';
            }).filter(Boolean);
            document.getElementById('previewContact').textContent = parts.length ? parts.join(' · ') : 'Email · Số điện thoại · Địa điểm';
        }
        function updatePreview(event) {
            var field = event.target;
            var key = field.dataset.cvPreview;
            if (!key) return;
            var targets = {
                name: ['previewName', 'Họ và tên'],
                role: ['previewRole', 'Vị trí mục tiêu'],
                summary: ['previewSummary', 'Hãy tóm tắt năng lực, mục tiêu nghề nghiệp và điểm mạnh của bạn.'],
                experience: ['previewExperience', 'Kinh nghiệm làm việc của bạn sẽ hiển thị tại đây.'],
                skills: ['previewSkills', 'Các kỹ năng chuyên môn phù hợp vị trí.']
            };
            if (targets[key]) {
                document.getElementById(targets[key][0]).textContent = textOrFallback(field.value, targets[key][1]);
            }
            if (key === 'email' || key === 'phone' || key === 'location') updateContact();
        }
        document.querySelectorAll('[data-cv-preview]').forEach(function (field) {
            field.addEventListener('input', updatePreview);
        });
        document.querySelectorAll('[data-cv-template]').forEach(function (button) {
            button.addEventListener('click', function () {
                var template = button.dataset.cvTemplate;
                document.getElementById('cvTemplateInput').value = template;
                document.querySelectorAll('[data-cv-template]').forEach(function (card) {
                    var selected = card === button;
                    card.classList.toggle('is-selected', selected);
                    card.setAttribute('aria-checked', String(selected));
                });
                var paper = document.getElementById('cvLivePaper');
                paper.className = 'cv-paper cv-paper-' + template.toLowerCase();
                document.getElementById('previewTemplateLabel').textContent = {
                    MODERN: 'Hiện đại', CLASSIC: 'Chuyên nghiệp', MINIMAL: 'Tối giản'
                }[template];
            });
        });
        updateContact();
    }());
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
