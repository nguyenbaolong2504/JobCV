(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var csrfToken = document.body ? document.body.dataset.csrfToken : '';

        if (csrfToken) {
            document.querySelectorAll('form').forEach(function (form) {
                if ((form.getAttribute('method') || 'get').toLowerCase() !== 'post'
                        || form.querySelector('input[name="_csrf"]')) {
                    return;
                }
                var input = document.createElement('input');
                input.type = 'hidden';
                input.name = '_csrf';
                input.value = csrfToken;
                form.appendChild(input);
            });
        }

        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (!window.confirm(form.dataset.confirm)) {
                    event.preventDefault();
                }
            });
        });

        document.querySelectorAll('input[type="file"][data-resume-upload]').forEach(function (input) {
            input.addEventListener('change', function () {
                var file = input.files && input.files[0];
                var feedback = document.getElementById(input.dataset.feedbackTarget);
                var allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
                var allowedExtensions = /\.(pdf|doc|docx)$/i;

                if (!file) {
                    return;
                }

                var validSize = file.size <= 5 * 1024 * 1024;
                var validType = allowedTypes.indexOf(file.type) !== -1 || allowedExtensions.test(file.name);
                if (!validSize || !validType) {
                    input.value = '';
                    if (feedback) {
                        feedback.textContent = 'Chỉ nhận tệp PDF, DOC hoặc DOCX có dung lượng tối đa 5 MB.';
                        feedback.classList.remove('d-none');
                    }
                    return;
                }

                if (feedback) {
                    feedback.textContent = '';
                    feedback.classList.add('d-none');
                }
            });
        });

        var aiReviewModal = document.getElementById('aiReviewModal');
        var aiReviewForm = document.getElementById('aiReviewForm');
        if (aiReviewModal && aiReviewForm) {
            var aiResumeIdInput = document.getElementById('aiReviewResumeId');
            var aiSelectedName = document.getElementById('aiReviewSelectedName');
            var aiFeedback = document.getElementById('aiReviewFeedback');
            var aiSubmit = document.getElementById('aiReviewSubmit');
            var aiResult = document.getElementById('aiReviewResult');

            function setAiVisible(element, visible) {
                if (!element) {
                    return;
                }
                element.classList.toggle('d-none', !visible);
                element.setAttribute('aria-hidden', visible ? 'false' : 'true');
            }

            function aiItems(value) {
                if (Array.isArray(value)) {
                    return value.filter(function (item) {
                        return item !== null && item !== undefined && String(item).trim() !== '';
                    });
                }
                return value === null || value === undefined || String(value).trim() === '' ? [] : [value];
            }

            function renderAiList(listElement, columnElement, value) {
                var items = aiItems(value);
                if (listElement) {
                    listElement.replaceChildren();
                    items.forEach(function (item) {
                        var listItem = document.createElement('li');
                        listItem.textContent = String(item);
                        listElement.appendChild(listItem);
                    });
                }
                setAiVisible(columnElement, items.length > 0);
            }

            function renderAiKeywords(value) {
                var container = document.getElementById('aiReviewKeywords');
                var column = document.getElementById('aiReviewKeywordsColumn');
                var items = aiItems(value);
                if (container) {
                    container.replaceChildren();
                    items.forEach(function (item) {
                        var tag = document.createElement('span');
                        tag.className = 'badge rounded-pill text-bg-light border text-dark';
                        tag.textContent = String(item);
                        container.appendChild(tag);
                    });
                }
                setAiVisible(column, items.length > 0);
            }

            function showAiFeedback(message) {
                if (!aiFeedback) {
                    return;
                }
                aiFeedback.textContent = message;
                aiFeedback.classList.remove('d-none');
            }

            function clearAiFeedback() {
                if (!aiFeedback) {
                    return;
                }
                aiFeedback.textContent = '';
                aiFeedback.classList.add('d-none');
            }

            function renderAiReview(review, fallbackResumeName) {
                review = review || {};
                var score = review.overallScore;
                if (score === null || score === undefined || score === '') {
                    score = review.score;
                }
                var resumeName = review.resumeName || fallbackResumeName || 'CV đã chọn';
                var summary = review.summary || '';
                var rewrittenSummary = review.rewrittenSummary || '';
                var disclaimer = review.disclaimer || 'AI chỉ phân tích nội dung CV đã chọn; không tự thay đổi hoặc gửi CV thay bạn.';
                var scoreBox = document.getElementById('aiReviewScoreBox');
                var scoreValue = document.getElementById('aiReviewScore');
                var resultName = document.getElementById('aiReviewResumeName');
                var summaryBox = document.getElementById('aiReviewSummary');
                var rewriteBox = document.getElementById('aiReviewRewriteColumn');
                var rewriteText = document.getElementById('aiReviewRewrittenSummary');
                var disclaimerBox = document.getElementById('aiReviewDisclaimer');

                if (resultName) {
                    resultName.textContent = String(resumeName);
                }
                if (scoreValue) {
                    scoreValue.textContent = score === null || score === undefined || score === '' ? '' : String(score);
                }
                setAiVisible(scoreBox, score !== null && score !== undefined && score !== '');

                if (summaryBox) {
                    summaryBox.textContent = String(summary);
                }
                setAiVisible(summaryBox, String(summary).trim() !== '');

                renderAiList(document.getElementById('aiReviewStrengths'), document.getElementById('aiReviewStrengthsColumn'), review.strengths);
                renderAiList(document.getElementById('aiReviewImprovements'), document.getElementById('aiReviewImprovementsColumn'), review.improvements);
                renderAiList(document.getElementById('aiReviewMissingSections'), document.getElementById('aiReviewMissingColumn'), review.missingSections);
                renderAiList(document.getElementById('aiReviewSuggestedBullets'), document.getElementById('aiReviewBulletsColumn'), review.suggestedBullets);
                renderAiKeywords(review.keywordSuggestions);

                if (rewriteText) {
                    rewriteText.textContent = String(rewrittenSummary);
                }
                setAiVisible(rewriteBox, String(rewrittenSummary).trim() !== '');
                if (disclaimerBox) {
                    disclaimerBox.replaceChildren();
                    var icon = document.createElement('i');
                    icon.className = 'bi bi-shield-check me-1';
                    disclaimerBox.appendChild(icon);
                    disclaimerBox.appendChild(document.createTextNode(String(disclaimer)));
                }

                setAiVisible(aiResult, true);
                aiResult.focus();
                aiResult.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }

            aiReviewModal.addEventListener('show.bs.modal', function (event) {
                var trigger = event.relatedTarget;
                var resumeId = trigger ? (trigger.getAttribute('data-resume-id') || '') : '';
                var resumeName = trigger ? (trigger.getAttribute('data-resume-name') || 'CV của bạn') : 'CV của bạn';

                aiReviewForm.reset();
                aiReviewForm.classList.remove('was-validated');
                clearAiFeedback();
                if (aiResumeIdInput) {
                    aiResumeIdInput.value = resumeId;
                }
                if (aiSelectedName) {
                    aiSelectedName.textContent = resumeName;
                }
                if (aiSubmit) {
                    aiSubmit.disabled = false;
                    aiSubmit.innerHTML = '<i class="bi bi-stars me-1"></i>Nhận gợi ý từ AI';
                }
            });

            aiReviewForm.addEventListener('submit', function (event) {
                event.preventDefault();
                clearAiFeedback();

                if (!aiReviewForm.checkValidity()) {
                    aiReviewForm.classList.add('was-validated');
                    if (typeof aiReviewForm.reportValidity === 'function') {
                        aiReviewForm.reportValidity();
                    }
                    return;
                }
                if (!aiResumeIdInput || !aiResumeIdInput.value) {
                    showAiFeedback('Vui lòng chọn một CV trước khi yêu cầu đánh giá.');
                    return;
                }

                var formData = new FormData(aiReviewForm);
                if (csrfToken) {
                    formData.set('_csrf', csrfToken);
                }
                var requestBody = new URLSearchParams();
                formData.forEach(function (value, key) {
                    requestBody.append(key, value);
                });

                if (aiSubmit) {
                    aiSubmit.disabled = true;
                    aiSubmit.innerHTML = '<span class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>Đang phân tích…';
                }

                window.fetch(aiReviewForm.action, {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                        'X-Requested-With': 'XMLHttpRequest',
                        'X-CSRF-Token': csrfToken || ''
                    },
                    body: requestBody.toString()
                }).then(function (response) {
                    return response.text().then(function (body) {
                        var payload;
                        try {
                            payload = body ? JSON.parse(body) : {};
                        } catch (error) {
                            throw new Error('Máy chủ không trả về kết quả AI hợp lệ. Vui lòng thử lại.');
                        }
                        if (!response.ok || payload.ok === false) {
                            throw new Error(payload.message || payload.error || 'Không thể phân tích CV vào lúc này. Vui lòng thử lại.');
                        }
                        return payload;
                    });
                }).then(function (payload) {
                    var review = payload.review || payload;
                    if (!review || typeof review !== 'object') {
                        throw new Error('Không nhận được nội dung đánh giá từ AI.');
                    }
                    var selectedName = aiSelectedName ? aiSelectedName.textContent : '';
                    if (window.bootstrap && window.bootstrap.Modal) {
                        var modalInstance = window.bootstrap.Modal.getInstance(aiReviewModal);
                        if (modalInstance) {
                            modalInstance.hide();
                        }
                    }
                    renderAiReview(review, selectedName);
                }).catch(function (error) {
                    showAiFeedback(error && error.message ? error.message : 'Không thể kết nối đến dịch vụ AI. Vui lòng thử lại.');
                }).finally(function () {
                    if (aiSubmit) {
                        aiSubmit.disabled = false;
                        aiSubmit.innerHTML = '<i class="bi bi-stars me-1"></i>Nhận gợi ý từ AI';
                    }
                });
            });
        }

        document.querySelectorAll('form').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (event.defaultPrevented || !form.checkValidity()) {
                    return;
                }
                form.querySelectorAll('[data-loading-button]').forEach(function (button) {
                    window.setTimeout(function () {
                        button.disabled = true;
                    }, 0);
                });
            });
        });

        var chatToggle = document.getElementById('rfChatbotToggle');
        var chatPanel = document.getElementById('rfChatbotPanel');
        var chatClose = document.getElementById('rfChatbotClose');
        var chatForm = document.getElementById('rfChatbotForm');
        var chatInput = document.getElementById('rfChatbotInput');
        var chatMessages = document.getElementById('rfChatbotMessages');
        var chatSuggestions = document.getElementById('rfChatbotSuggestions');

        function toggleChat(open) {
            if (!chatPanel || !chatToggle) return;
            chatPanel.hidden = !open;
            chatToggle.setAttribute('aria-expanded', String(open));
            var badge = chatToggle.querySelector('span');
            if (badge && open) badge.hidden = true;
            if (open && chatInput) window.setTimeout(function () { chatInput.focus(); }, 50);
        }

        function addChatMessage(text, type) {
            var message = document.createElement('div');
            message.className = 'rf-chatbot-message ' + type;
            message.textContent = text;
            chatMessages.appendChild(message);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }

        function chatbotReply(question) {
            var value = question.toLowerCase();
            var context = document.body.dataset.contextPath || '';
            var normalized = value.normalize ? value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/\u0111/g, 'd') : value;
            if (/ung tuyen|apply|nop don/.test(normalized)) return 'B\u1ea1n m\u1edf m\u1ee5c Vi\u1ec7c l\u00e0m, ch\u1ecdn v\u1ecb tr\u00ed ph\u00f9 h\u1ee3p r\u1ed3i nh\u1ea5n \u1ee8ng tuy\u1ec3n. H\u00e3y \u0111\u0103ng nh\u1eadp Candidate v\u00e0 t\u1ea3i CV l\u00ean tr\u01b0\u1edbc.';
            if (/tai cv|upload cv|quan ly cv|ho so/.test(normalized)) return 'V\u00e0o Candidate \u2192 CV c\u1ee7a t\u00f4i \u0111\u1ec3 t\u1ea3i PDF, DOC ho\u1eb7c DOCX t\u1ed1i \u0111a 5 MB, \u0111\u1eb7t CV m\u1eb7c \u0111\u1ecbnh v\u00e0 d\u00f9ng AI CV Coach.';
            if (/cv|resume/.test(normalized)) return 'CV n\u00ean c\u00f3 th\u00f4ng tin li\u00ean h\u1ec7, m\u1ee5c ti\u00eau, k\u1ef9 n\u0103ng, kinh nghi\u1ec7m v\u00e0 h\u1ecdc v\u1ea5n. B\u1ea1n c\u00f3 th\u1ec3 d\u00f9ng AI CV Coach \u0111\u1ec3 nh\u1eadn g\u1ee3i \u00fd.';
            if (/trang thai|don cua toi|application/.test(normalized)) return 'V\u00e0o Candidate \u2192 \u0110\u01a1n \u1ee9ng tuy\u1ec3n \u0111\u1ec3 xem tr\u1ea1ng th\u00e1i v\u00e0 d\u00f2ng th\u1eddi gian x\u1eed l\u00fd c\u1ee7a t\u1eebng h\u1ed3 s\u01a1.';
            if (/viec|job|tuyen/.test(normalized)) return 'B\u1ea1n xem c\u00e1c v\u1ecb tr\u00ed \u0111ang tuy\u1ec3n t\u1ea1i ' + context + '/jobs v\u00e0 d\u00f9ng t\u1eeb kh\u00f3a, ph\u00f2ng ban ho\u1eb7c tr\u1ea1ng th\u00e1i \u0111\u1ec3 l\u1ecdc.';
            if (/dang ky|register/.test(normalized)) return 'Nh\u1ea5n \u0110\u0103ng k\u00fd, nh\u1eadp th\u00f4ng tin v\u00e0 t\u1ea1o t\u00e0i kho\u1ea3n Candidate. Sau \u0111\u00f3 b\u1ea1n c\u00f3 th\u1ec3 t\u1ea3i CV v\u00e0 \u1ee9ng tuy\u1ec3n.';
            if (/dang nhap|login|tai khoan|mat khau/.test(normalized)) return 'V\u00e0o trang \u0110\u0103ng nh\u1eadp v\u00e0 d\u00f9ng email, m\u1eadt kh\u1ea9u c\u1ee7a b\u1ea1n. T\u00e0i kho\u1ea3n demo d\u00f9ng m\u1eadt kh\u1ea9u 123456.';
            if (/phong van|interview|lich hen/.test(normalized)) return 'L\u1ecbch ph\u1ecfng v\u1ea5n n\u1eb1m trong khu v\u1ef1c Candidate. B\u1ea1n h\u00e3y ki\u1ec3m tra th\u1eddi gian, \u0111\u1ecba \u0111i\u1ec3m ho\u1eb7c li\u00ean k\u1ebft h\u1ecdp tr\u01b0\u1edbc bu\u1ed5i h\u1eb9n.';
            if (/offer|nhan viec|thu moi/.test(normalized)) return 'Khi HR g\u1eedi offer, b\u1ea1n s\u1ebd nh\u1eadn th\u00f4ng b\u00e1o v\u00e0 c\u00f3 th\u1ec3 xem, ch\u1ea5p nh\u1eadn ho\u1eb7c t\u1eeb ch\u1ed1i trong m\u1ee5c Offer.';
            if (/thong bao|notification/.test(normalized)) return 'M\u1ee5c Th\u00f4ng b\u00e1o hi\u1ec3n th\u1ecb c\u00e1c c\u1eadp nh\u1eadt v\u1ec1 \u0111\u01a1n, ph\u1ecfng v\u1ea5n v\u00e0 offer. B\u1ea1n c\u00f3 th\u1ec3 \u0111\u00e1nh d\u1ea5u \u0111\u00e3 \u0111\u1ecdc.';
            if (/onboarding|hoi nhap|nhan vien moi/.test(normalized)) return 'Sau khi ch\u1ea5p nh\u1eadn offer, h\u1ec7 th\u1ed1ng t\u1ea1o quy tr\u00ecnh onboarding. H\u00e3y ho\u00e0n th\u00e0nh t\u1ea5t c\u1ea3 nhi\u1ec7m v\u1ee5 b\u1eaft bu\u1ed9c.';
            if (/hr|nhan su|dang tin/.test(normalized)) return 'HR c\u00f3 th\u1ec3 t\u1ea1o v\u00e0 \u0111\u0103ng tin tuy\u1ec3n d\u1ee5ng, s\u00e0ng l\u1ecdc h\u1ed3 s\u01a1, x\u1ebfp l\u1ecbch ph\u1ecfng v\u1ea5n, g\u1eedi offer v\u00e0 theo d\u00f5i onboarding.';
            if (/admin|quan tri/.test(normalized)) return 'Admin qu\u1ea3n l\u00fd ng\u01b0\u1eddi d\u00f9ng, vai tr\u00f2, ph\u00f2ng ban v\u00e0 nh\u1eadt k\u00fd ho\u1ea1t \u0111\u1ed9ng trong khu v\u1ef1c qu\u1ea3n tr\u1ecb.';
            if (/xin chao|hello|(^| )hi($| )|chao/.test(normalized)) return 'Ch\u00e0o b\u1ea1n! B\u1ea1n mu\u1ed1n t\u00ecm vi\u1ec7c, chu\u1ea9n b\u1ecb CV hay ki\u1ec3m tra \u0111\u01a1n \u1ee9ng tuy\u1ec3n?';
            if (/cam on|thanks|thank you/.test(normalized)) return 'Kh\u00f4ng c\u00f3 g\u00ec! N\u1ebfu c\u1ea7n, b\u1ea1n c\u1ee9 h\u1ecfi th\u00eam nh\u00e9.';
            return 'M\u00ecnh ch\u01b0a hi\u1ec3u r\u00f5. B\u1ea1n c\u00f3 th\u1ec3 h\u1ecfi v\u1ec1: t\u00ecm vi\u1ec7c, \u1ee9ng tuy\u1ec3n, CV, tr\u1ea1ng th\u00e1i h\u1ed3 s\u01a1, ph\u1ecfng v\u1ea5n, offer, onboarding ho\u1eb7c t\u00e0i kho\u1ea3n.';
        }

        if (chatToggle && chatPanel && chatForm && chatMessages) {
            chatToggle.addEventListener('click', function () { toggleChat(chatPanel.hidden); });
            chatClose.addEventListener('click', function () { toggleChat(false); });
            chatForm.addEventListener('submit', function (event) {
                event.preventDefault();
                var question = chatInput.value.trim();
                if (!question) return;
                addChatMessage(question, 'user');
                chatInput.value = '';
                window.setTimeout(function () { addChatMessage(chatbotReply(question), 'bot'); }, 350);
            });
            chatSuggestions.addEventListener('click', function (event) {
                if (event.target.tagName !== 'BUTTON') return;
                chatInput.value = event.target.textContent;
                chatForm.requestSubmit();
            });
        }
    });
}());
