<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
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
            <button type="button">T&#236;m vi&#7879;c Intern Backend</button><button type="button">Vi&#7871;t CV nh&#432; th&#7871; n&#224;o?</button><button type="button">CV m&#7851;u</button><button type="button">Tr&#7841;ng th&#225;i h&#7891; s&#417;</button>
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
<script src="${pageContext.request.contextPath}/assets/js/app.js?v=20260824-chatbot-intent-fix"></script>
</body>

</html>
