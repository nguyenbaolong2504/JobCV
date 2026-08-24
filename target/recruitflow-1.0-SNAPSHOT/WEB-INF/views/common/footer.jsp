<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="currentUri" value="${pageContext.request.requestURI}" />
<c:if test="${not fn:contains(currentUri, '/candidate/') and not fn:contains(currentUri, '/hr/') and not fn:contains(currentUri, '/admin/') and not fn:contains(currentUri, '/interviewer/') and not fn:endsWith(currentUri, '/login') and not fn:endsWith(currentUri, '/register')}">
<footer class="rf-site-footer"><div class="container">
    <div class="rf-footer-main"><div class="rf-footer-brand"><a href="${pageContext.request.contextPath}/"><span>R</span><strong>RecruitFlow</strong></a><p>Kết nối đúng người với đúng cơ hội bằng trải nghiệm tuyển dụng minh bạch, có cấu trúc và thân thiện.</p><div><span><i class="bi bi-shield-check"></i>Tin tuyển dụng rõ ràng</span><span><i class="bi bi-patch-check"></i>Nhà tuyển dụng xác thực</span></div></div>
        <div class="rf-footer-links"><section><h2>Dành cho ứng viên</h2><a href="${pageContext.request.contextPath}/jobs">Tìm việc làm</a><a href="${pageContext.request.contextPath}/companies">Khám phá công ty</a><a href="${pageContext.request.contextPath}/register">Tạo tài khoản</a></section><section><h2>Dành cho nhà tuyển dụng</h2><a href="${pageContext.request.contextPath}/register">Đăng tin tuyển dụng</a><a href="${pageContext.request.contextPath}/login">Quản lý ứng viên</a><a href="${pageContext.request.contextPath}/login">Báo cáo tuyển dụng</a></section><section><h2>RecruitFlow</h2><a href="${pageContext.request.contextPath}/">Về chúng tôi</a><a href="${pageContext.request.contextPath}/companies">Đối tác tuyển dụng</a><a href="${pageContext.request.contextPath}/jobs">Cơ hội nổi bật</a></section></div>
    </div><div class="rf-footer-bottom"><span>© 2026 RecruitFlow · Nền tảng tuyển dụng EAUT</span><div><span>Quyền riêng tư</span><span>Điều khoản sử dụng</span><span>Hỗ trợ</span></div></div>
</div></footer>
</c:if>
<script src="${pageContext.request.contextPath}/webjars/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
<div class="rf-chatbot" id="rfChatbot">
    <section class="rf-chatbot-panel" id="rfChatbotPanel" aria-label="Tr&#7907; l&#253; RecruitFlow" hidden>
        <header class="rf-chatbot-header">
            <div class="rf-chatbot-avatar"><i class="bi bi-robot"></i></div>
            <div><strong>Tr&#7907; l&#253; RecruitFlow</strong><small><span></span> &#272;ang tr&#7921;c tuy&#7871;n</small></div>
            <button type="button" id="rfChatbotClose" aria-label="&#272;&#243;ng tr&#242; chuy&#7879;n"><i class="bi bi-x-lg"></i></button>
        </header>
        <div class="rf-chatbot-messages" id="rfChatbotMessages" aria-live="polite">
            <div class="rf-chatbot-message bot">Xin ch&#224;o! M&#236;nh c&#243; th&#7875; gi&#250;p b&#7841;n t&#236;m vi&#7879;c, &#7913;ng tuy&#7875;n v&#224; qu&#7843;n l&#253; CV.</div>
        </div>
        <div class="rf-chatbot-suggestions" id="rfChatbotSuggestions">
            <button type="button">T&#236;m vi&#7879;c</button><button type="button">C&#225;ch &#7913;ng tuy&#7875;n</button><button type="button">Qu&#7843;n l&#253; CV</button><button type="button">Tr&#7841;ng th&#225;i h&#7891; s&#417;</button>
        </div>
        <form class="rf-chatbot-form" id="rfChatbotForm">
            <input id="rfChatbotInput" type="text" maxlength="250" autocomplete="off" placeholder="Nh&#7853;p c&#226;u h&#7887;i..." aria-label="C&#226;u h&#7887;i">
            <button type="submit" aria-label="G&#7917;i"><i class="bi bi-send-fill"></i></button>
        </form>
    </section>
    <button class="rf-chatbot-toggle" id="rfChatbotToggle" type="button" aria-label="M&#7903; tr&#7907; l&#253;" aria-expanded="false">
        <i class="bi bi-chat-dots-fill"></i><span>1</span>
    </button>
</div>
<button class="portal-nav-toggle" id="portalNavToggle" type="button" aria-label="Mở menu chức năng" aria-expanded="false">
    <i class="bi bi-list" aria-hidden="true"></i>
</button>
<button class="portal-nav-backdrop" id="portalNavBackdrop" type="button" aria-label="Đóng menu chức năng" tabindex="-1"></button>
<script src="${pageContext.request.contextPath}/assets/js/app.js?v=20260824-release-v1"></script>
</body>

</html>
