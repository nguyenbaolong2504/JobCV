(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var routeProgress = document.createElement('div');
        routeProgress.className = 'rf-route-progress';
        routeProgress.setAttribute('aria-hidden', 'true');
        document.body.appendChild(routeProgress);
        function showRouteProgress() { document.documentElement.classList.add('rf-is-navigating'); }
        function hideRouteProgress() { document.documentElement.classList.remove('rf-is-navigating'); }
        window.addEventListener('pageshow', hideRouteProgress);
        document.addEventListener('click', function (event) {
            var link = event.target.closest('a[href]');
            if (!link || event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey
                    || link.target === '_blank' || link.hasAttribute('download') || link.hasAttribute('data-bs-toggle')) return;
            var href = link.getAttribute('href') || '';
            if (!href || href.charAt(0) === '#' || href.indexOf('javascript:') === 0) return;
            try {
                var target = new URL(link.href, window.location.href);
                if (target.origin === window.location.origin && target.href !== window.location.href) showRouteProgress();
            } catch (ignored) { /* Ignore malformed third-party links. */ }
        });
        var csrfToken = document.body ? document.body.dataset.csrfToken : '';

        var portalSidebar = document.querySelector('.app-sidebar, .candidate-sidebar, aside.sidebar');
        var portalNavToggle = document.getElementById('portalNavToggle');
        var portalNavBackdrop = document.getElementById('portalNavBackdrop');
        var portalMobileQuery = window.matchMedia('(max-width: 991.98px)');

        function setPortalNavigation(open) {
            if (!portalSidebar || !portalNavToggle) {
                return;
            }
            if (!open && portalSidebar.contains(document.activeElement)) {
                portalNavToggle.focus();
            }
            document.body.classList.toggle('portal-nav-open', open);
            portalNavToggle.setAttribute('aria-expanded', String(open));
            portalNavToggle.setAttribute('aria-label', open ? '\u0110\u00F3ng menu ch\u1EE9c n\u0103ng' : 'M\u1EDF menu ch\u1EE9c n\u0103ng');
            portalNavToggle.innerHTML = open ? '<i class="bi bi-x-lg" aria-hidden="true"></i>' : '<i class="bi bi-list" aria-hidden="true"></i>';
            if (portalMobileQuery.matches) {
                portalSidebar.setAttribute('aria-hidden', String(!open));
                portalSidebar.toggleAttribute('inert', !open);
            } else {
                portalSidebar.removeAttribute('aria-hidden');
                portalSidebar.removeAttribute('inert');
            }
        }

        if (portalSidebar && portalNavToggle && portalNavBackdrop) {
            if (!portalSidebar.id) {
                portalSidebar.id = 'portalSidebar';
            }
            document.body.classList.add('has-portal-sidebar');
            portalNavToggle.setAttribute('aria-controls', portalSidebar.id);
            portalNavToggle.addEventListener('click', function () {
                setPortalNavigation(!document.body.classList.contains('portal-nav-open'));
            });
            portalNavBackdrop.addEventListener('click', function () { setPortalNavigation(false); });
            portalSidebar.querySelectorAll('a').forEach(function (link) {
                link.addEventListener('click', function () { setPortalNavigation(false); });
            });
            document.addEventListener('keydown', function (event) {
                if (event.key === 'Escape') {
                    setPortalNavigation(false);
                }
            });
            if (portalMobileQuery.addEventListener) {
                portalMobileQuery.addEventListener('change', function () { setPortalNavigation(false); });
            }
            setPortalNavigation(false);
        }

        var interfaceLabels = {
            SUBMITTED: 'M\u1EDBi nh\u1EADn', SCREENING: '\u0110ang s\u00E0ng l\u1ECDc', SHORTLISTED: 'Danh s\u00E1ch ng\u1EAFn',
            INTERVIEW_SCHEDULED: '\u0110\u00E3 h\u1EB9n ph\u1ECFng v\u1EA5n', INTERVIEWED: '\u0110\u00E3 ph\u1ECFng v\u1EA5n', OFFERED: '\u0110\u00E3 g\u1EEDi th\u01B0 m\u1EDDi',
            HIRED: '\u0110\u00E3 tuy\u1EC3n', REJECTED: '\u0110\u00E3 t\u1EEB ch\u1ED1i', WITHDRAWN: '\u0110\u00E3 r\u00FAt \u0111\u01A1n',
            DRAFT: 'B\u1EA3n nh\u00E1p', SENT: '\u0110\u00E3 g\u1EEDi', ACCEPTED: '\u0110\u00E3 ch\u1EA5p nh\u1EADn', DECLINED: '\u0110\u00E3 t\u1EEB ch\u1ED1i', EXPIRED: '\u0110\u00E3 h\u1EBFt h\u1EA1n',
            SCHEDULED: '\u0110\u00E3 l\u00EAn l\u1ECBch', RESCHEDULED: '\u0110\u00E3 \u0111\u1ED5i l\u1ECBch', COMPLETED: 'Ho\u00E0n th\u00E0nh', CANCELLED: '\u0110\u00E3 h\u1EE7y',
            NOT_STARTED: 'Ch\u01B0a b\u1EAFt \u0111\u1EA7u', IN_PROGRESS: '\u0110ang th\u1EF1c hi\u1EC7n', TODO: 'C\u1EA7n l\u00E0m', DONE: 'Ho\u00E0n th\u00E0nh',
            FULL_TIME: 'To\u00E0n th\u1EDDi gian', PART_TIME: 'B\u00E1n th\u1EDDi gian', INTERNSHIP: 'Th\u1EF1c t\u1EADp', CONTRACT: 'H\u1EE3p \u0111\u1ED3ng', REMOTE: 'L\u00E0m t\u1EEB xa',
            ONLINE: 'Tr\u1EF1c tuy\u1EBFn', OFFLINE: 'Tr\u1EF1c ti\u1EBFp', ONSITE: 'Tr\u1EF1c ti\u1EBFp', PHONE: '\u0110i\u1EC7n tho\u1EA1i',
            STRONG_HIRE: 'R\u1EA5t ph\u00F9 h\u1EE3p', HIRE: 'N\u00EAn tuy\u1EC3n', CONSIDER: 'C\u00E2n nh\u1EAFc', NO_HIRE: 'Kh\u00F4ng ph\u00F9 h\u1EE3p',
            PUBLISHED: '\u0110ang \u0111\u0103ng tuy\u1EC3n', CLOSED: '\u0110\u00E3 \u0111\u00F3ng', ARCHIVED: '\u0110\u00E3 l\u01B0u tr\u1EEF',
            ACTIVE: '\u0110ang ho\u1EA1t \u0111\u1ED9ng', LOCKED: '\u0110\u00E3 kh\u00F3a', INACTIVE: 'Ng\u1EEBng ho\u1EA1t \u0111\u1ED9ng',
            ADMIN: 'Qu\u1EA3n tr\u1ECB vi\u00EAn', HR: 'Nh\u00E2n s\u1EF1', INTERVIEWER: 'Ng\u01B0\u1EDDi ph\u1ECFng v\u1EA5n', CANDIDATE: '\u1EE8ng vi\u00EAn',
            MALE: 'Nam', FEMALE: 'N\u1EEF', OTHER: 'Kh\u00E1c'
        };

        document.querySelectorAll('.status-badge, [data-enum-label]').forEach(function (element) {
            var key = (element.dataset.enumLabel || element.textContent || '').trim().toUpperCase();
            if (interfaceLabels[key]) {
                element.textContent = interfaceLabels[key];
            }
        });

        // Translate only standalone enum labels. Never rewrite names or user-entered content
        // such as "HR Manager", because those values belong to the user/database.
        var textWalker = document.createTreeWalker(document.body, window.NodeFilter.SHOW_TEXT);
        var textNode;
        while ((textNode = textWalker.nextNode())) {
            var parentTag = textNode.parentElement ? textNode.parentElement.tagName : '';
            if (parentTag === 'SCRIPT' || parentTag === 'STYLE' || parentTag === 'CODE' || parentTag === 'PRE') {
                continue;
            }
            var enumValue = textNode.nodeValue.trim();
            var enumKey = enumValue.toUpperCase();
            if (enumValue && interfaceLabels[enumKey]) {
                textNode.nodeValue = textNode.nodeValue.replace(enumValue, interfaceLabels[enumKey]);
            }
        }

        var employerShowcase = document.querySelector('[data-employer-showcase]');
        if (employerShowcase) {
            var employerGrid = employerShowcase.querySelector('[data-employer-grid]');
            var employerCards = Array.prototype.slice.call(employerShowcase.querySelectorAll('[data-employer-industry]'));
            var employerFilters = Array.prototype.slice.call(employerShowcase.querySelectorAll('[data-employer-filter]'));
            var employerAutoplayButton = employerShowcase.querySelector('[data-employer-autoplay]');
            var employerReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
            var employerAutoplayEnabled = !employerReducedMotion;
            var employerInteractionPaused = false;
            var employerTouchResumeTimer;

            function advanceEmployerGrid(direction) {
                if (!employerGrid) {
                    return;
                }
                var maximumScroll = Math.max(0, employerGrid.scrollWidth - employerGrid.clientWidth);
                if (maximumScroll < 8) {
                    return;
                }
                if (direction > 0 && employerGrid.scrollLeft >= maximumScroll - 8) {
                    employerGrid.scrollTo({ left: 0, behavior: 'smooth' });
                    return;
                }
                if (direction < 0 && employerGrid.scrollLeft <= 8) {
                    employerGrid.scrollTo({ left: maximumScroll, behavior: 'smooth' });
                    return;
                }
                employerGrid.scrollBy({ left: direction * Math.max(280, employerGrid.clientWidth * .82), behavior: 'smooth' });
            }

            function updateEmployerAutoplayButton() {
                if (!employerAutoplayButton) {
                    return;
                }
                employerAutoplayButton.classList.toggle('is-playing', employerAutoplayEnabled);
                employerAutoplayButton.setAttribute('aria-pressed', String(employerAutoplayEnabled));
                employerAutoplayButton.setAttribute('aria-label', employerAutoplayEnabled
                    ? 'T\u1EA1m d\u1EEBng t\u1EF1 \u0111\u1ED9ng tr\u01B0\u1EE3t'
                    : 'B\u1EADt t\u1EF1 \u0111\u1ED9ng tr\u01B0\u1EE3t');
                employerAutoplayButton.innerHTML = employerAutoplayEnabled
                    ? '<i class="bi bi-pause-fill" aria-hidden="true"></i>'
                    : '<i class="bi bi-play-fill" aria-hidden="true"></i>';
            }

            function filterEmployers(filterValue) {
                employerFilters.forEach(function (button) {
                    var selected = button.dataset.employerFilter === filterValue;
                    button.classList.toggle('active', selected);
                    button.setAttribute('aria-pressed', String(selected));
                });
                employerCards.forEach(function (card) {
                    card.hidden = filterValue !== 'all' && card.dataset.employerIndustry !== filterValue;
                });
                if (employerGrid) {
                    employerGrid.scrollTo({ left: 0, behavior: 'smooth' });
                }
            }

            employerFilters.forEach(function (button) {
                button.addEventListener('click', function () {
                    filterEmployers(button.dataset.employerFilter || 'all');
                });
            });

            employerShowcase.querySelectorAll('[data-employer-scroll]').forEach(function (button) {
                button.addEventListener('click', function () {
                    var direction = button.dataset.employerScroll === 'previous' ? -1 : 1;
                    advanceEmployerGrid(direction);
                });
            });

            if (employerAutoplayButton) {
                employerAutoplayButton.addEventListener('click', function () {
                    employerAutoplayEnabled = !employerAutoplayEnabled;
                    updateEmployerAutoplayButton();
                });
            }

            employerShowcase.addEventListener('mouseenter', function () { employerInteractionPaused = true; });
            employerShowcase.addEventListener('mouseleave', function () { employerInteractionPaused = false; });
            employerShowcase.addEventListener('focusin', function () { employerInteractionPaused = true; });
            employerShowcase.addEventListener('focusout', function () {
                window.setTimeout(function () {
                    employerInteractionPaused = employerShowcase.contains(document.activeElement);
                }, 0);
            });
            employerShowcase.addEventListener('touchstart', function () {
                employerInteractionPaused = true;
                window.clearTimeout(employerTouchResumeTimer);
            }, { passive: true });
            employerShowcase.addEventListener('touchend', function () {
                window.clearTimeout(employerTouchResumeTimer);
                employerTouchResumeTimer = window.setTimeout(function () { employerInteractionPaused = false; }, 5000);
            }, { passive: true });

            window.setInterval(function () {
                if (employerAutoplayEnabled && !employerInteractionPaused && !document.hidden) {
                    advanceEmployerGrid(1);
                }
            }, 3600);
            updateEmployerAutoplayButton();
        }

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
                        feedback.textContent = 'Ch\u1EC9 nh\u1EADn t\u1EC7p PDF, DOC ho\u1EB7c DOCX c\u00F3 dung l\u01B0\u1EE3ng t\u1ED1i \u0111a 5 MB.';
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

        document.querySelectorAll('[data-avatar-image]').forEach(function (image) {
            function showAvatarFallback() {
                image.classList.add('d-none');
                var fallback = document.getElementById(image.dataset.fallbackTarget);
                if (fallback) {
                    fallback.classList.remove('d-none');
                }
            }
            image.addEventListener('error', showAvatarFallback);
            if (image.complete && image.naturalWidth === 0) {
                showAvatarFallback();
            }
        });

        document.querySelectorAll('input[type="file"][data-avatar-upload]').forEach(function (input) {
            input.addEventListener('change', function () {
                var file = input.files && input.files[0];
                var preview = document.getElementById(input.dataset.previewTarget);
                var fallback = document.getElementById(input.dataset.fallbackTarget);
                var feedback = document.getElementById(input.dataset.feedbackTarget);
                var fileName = document.getElementById(input.dataset.nameTarget);
                var saveButton = document.getElementById(input.dataset.saveTarget);
                var allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
                var allowedExtensions = /\.(jpe?g|png|webp)$/i;

                if (saveButton) {
                    saveButton.disabled = true;
                }
                if (!file) {
                    return;
                }

                var validSize = file.size <= 2 * 1024 * 1024;
                var validType = allowedTypes.indexOf(file.type) !== -1 && allowedExtensions.test(file.name);
                if (!validSize || !validType) {
                    input.value = '';
                    if (feedback) {
                        feedback.textContent = 'Ch\u1EC9 nh\u1EADn \u1EA3nh JPG, PNG ho\u1EB7c WEBP c\u00F3 dung l\u01B0\u1EE3ng t\u1ED1i \u0111a 2 MB.';
                        feedback.classList.remove('d-none');
                    }
                    return;
                }

                if (feedback) {
                    feedback.textContent = '';
                    feedback.classList.add('d-none');
                }
                if (fileName) {
                    fileName.textContent = file.name;
                }
                if (preview) {
                    if (preview.dataset.objectUrl) {
                        window.URL.revokeObjectURL(preview.dataset.objectUrl);
                    }
                    var objectUrl = window.URL.createObjectURL(file);
                    preview.dataset.objectUrl = objectUrl;
                    preview.src = objectUrl;
                    preview.classList.remove('d-none');
                }
                if (fallback) {
                    fallback.classList.add('d-none');
                }
                if (saveButton) {
                    saveButton.disabled = false;
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
                var resumeName = review.resumeName || fallbackResumeName || 'CV \u0111\u00E3 ch\u1ECDn';
                var summary = review.summary || '';
                var rewrittenSummary = review.rewrittenSummary || '';
                var disclaimer = review.disclaimer || 'AI ch\u1EC9 ph\u00E2n t\u00EDch n\u1ED9i dung CV \u0111\u00E3 ch\u1ECDn; kh\u00F4ng t\u1EF1 thay \u0111\u1ED5i ho\u1EB7c g\u1EEDi CV thay b\u1EA1n.';
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
                var resumeName = trigger ? (trigger.getAttribute('data-resume-name') || 'CV c\u1EE7a b\u1EA1n') : 'CV c\u1EE7a b\u1EA1n';

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
                    aiSubmit.innerHTML = '<i class="bi bi-stars me-1"></i>Nh\u1EADn g\u1EE3i \u00FD t\u1EEB AI';
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
                    showAiFeedback('Vui l\u00F2ng ch\u1ECDn m\u1ED9t CV tr\u01B0\u1EDBc khi y\u00EAu c\u1EA7u \u0111\u00E1nh gi\u00E1.');
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
                    aiSubmit.innerHTML = '<span class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>\u0110ang ph\u00E2n t\u00EDch\u2026';
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
                            throw new Error('M\u00E1y ch\u1EE7 kh\u00F4ng tr\u1EA3 v\u1EC1 k\u1EBFt qu\u1EA3 AI h\u1EE3p l\u1EC7. Vui l\u00F2ng th\u1EED l\u1EA1i.');
                        }
                        if (!response.ok || payload.ok === false) {
                            throw new Error(payload.message || payload.error || 'Kh\u00F4ng th\u1EC3 ph\u00E2n t\u00EDch CV v\u00E0o l\u00FAc n\u00E0y. Vui l\u00F2ng th\u1EED l\u1EA1i.');
                        }
                        return payload;
                    });
                }).then(function (payload) {
                    var review = payload.review || payload;
                    if (!review || typeof review !== 'object') {
                        throw new Error('Kh\u00F4ng nh\u1EADn \u0111\u01B0\u1EE3c n\u1ED9i dung \u0111\u00E1nh gi\u00E1 t\u1EEB AI.');
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
                    showAiFeedback(error && error.message ? error.message : 'Kh\u00F4ng th\u1EC3 k\u1EBFt n\u1ED1i \u0111\u1EBFn d\u1ECBch v\u1EE5 AI. Vui l\u00F2ng th\u1EED l\u1EA1i.');
                }).finally(function () {
                    if (aiSubmit) {
                        aiSubmit.disabled = false;
                        aiSubmit.innerHTML = '<i class="bi bi-stars me-1"></i>Nh\u1EADn g\u1EE3i \u00FD t\u1EEB AI';
                    }
                });
            });
        }

        document.querySelectorAll('[data-character-counter]').forEach(function (field) {
            var counter = document.getElementById(field.getAttribute('data-character-counter'));
            var refreshCounter = function () {
                if (counter) counter.textContent = field.value.length + '/' + (field.maxLength || 0);
            };
            field.addEventListener('input', refreshCounter);
            refreshCounter();
        });

        document.querySelectorAll('[data-password-toggle]').forEach(function (toggle) {
            toggle.addEventListener('click', function () {
                var field = document.getElementById(toggle.getAttribute('data-password-toggle'));
                if (!field) return;
                var revealing = field.type === 'password';
                field.type = revealing ? 'text' : 'password';
                toggle.setAttribute('aria-label', revealing ? '\u1EA8n m\u1EADt kh\u1EA9u' : 'Hi\u1EC7n m\u1EADt kh\u1EA9u');
                var icon = toggle.querySelector('i');
                if (icon) icon.className = revealing ? 'bi bi-eye-slash' : 'bi bi-eye';
            });
        });

        var newPassword = document.getElementById('password');
        var confirmPassword = document.getElementById('confirmPassword');
        var strength = document.querySelector('[data-password-strength]');
        function updatePasswordUi() {
            if (confirmPassword && newPassword) {
                confirmPassword.setCustomValidity(confirmPassword.value && confirmPassword.value !== newPassword.value
                    ? 'M\u1EADt kh\u1EA9u x\u00E1c nh\u1EADn kh\u00F4ng kh\u1EDBp.' : '');
            }
            if (!strength || !newPassword) return;
            var value = newPassword.value;
            var score = 0;
            if (value.length >= 6) score++;
            if (value.length >= 10) score++;
            if (/[A-Z]/.test(value) && /[a-z]/.test(value)) score++;
            if (/\d/.test(value) && /[^A-Za-z0-9]/.test(value)) score++;
            strength.setAttribute('data-score', String(score));
            var label = strength.querySelector('small');
            if (label) label.textContent = value ? ['R\u1EA5t y\u1EBFu', 'Y\u1EBFu', 'Trung b\u00ECnh', 'T\u1ED1t', 'M\u1EA1nh'][score] : '\u0110\u1ED9 m\u1EA1nh m\u1EADt kh\u1EA9u';
        }
        if (newPassword) newPassword.addEventListener('input', updatePasswordUi);
        if (confirmPassword) confirmPassword.addEventListener('input', updatePasswordUi);

        var rememberedEmailField = document.getElementById('email');
        var rememberEmail = document.querySelector('input[name="rememberEmail"]');
        if (rememberEmail && rememberedEmailField) {
            try {
                var storedEmail = window.localStorage.getItem('recruitflowRememberedEmail');
                if (storedEmail) { rememberedEmailField.value = storedEmail; rememberEmail.checked = true; }
            } catch (ignored) { /* Local storage can be disabled by the browser. */ }
            rememberEmail.form.addEventListener('submit', function () {
                try {
                    if (rememberEmail.checked) window.localStorage.setItem('recruitflowRememberedEmail', rememberedEmailField.value.trim());
                    else window.localStorage.removeItem('recruitflowRememberedEmail');
                } catch (ignored) { /* Login remains available without storage. */ }
            });
        }

        document.querySelectorAll('[data-validate-form]').forEach(function (form) {
            form.addEventListener('submit', function () {
                form.classList.add('was-validated');
            });
        });

        if (window.location.hash === '#apply-now') {
            var applyModalElement = document.getElementById('applyJobModal');
            if (applyModalElement && window.bootstrap && window.bootstrap.Modal) {
                window.setTimeout(function () {
                    window.bootstrap.Modal.getOrCreateInstance(applyModalElement).show();
                }, 250);
            }
        }

        document.querySelectorAll('form').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (event.defaultPrevented || !form.checkValidity()) {
                    return;
                }
                form.classList.add('rf-form-submitting');
                form.querySelectorAll('[data-loading-button]').forEach(function (button) {
                    window.setTimeout(function () {
                        button.disabled = true;
                        if (!button.querySelector('.spinner-border')) {
                            var spinner = document.createElement('span');
                            spinner.className = 'spinner-border spinner-border-sm me-1';
                            spinner.setAttribute('aria-hidden', 'true');
                            button.prepend(spinner);
                        }
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
