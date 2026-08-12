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
    });
}());
