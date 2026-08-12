# Workflow và nghiệp vụ

## Actor

| Actor | Phạm vi |
| --- | --- |
| GUEST | Xem home/jobs, search/filter, register/login. |
| CANDIDATE | Profile, CV, apply, application/interview/offer/onboarding/notification. |
| HR | Jobs, pipeline ứng viên, interview, offer, onboarding, reports. |
| INTERVIEWER | Lịch được phân công và feedback. |
| ADMIN | User lock/unlock, department, audit log, dashboard. |

## Luồng tuyển dụng

~~~mermaid
flowchart LR
    A[HR tạo Job: DRAFT] --> B[Publish]
    B --> C[PUBLISHED]
    C --> D[Candidate chọn CV và Apply]
    D --> E[SUBMITTED]
    E --> F[SCREENING]
    F --> G[SHORTLISTED]
    G --> H[INTERVIEW_SCHEDULED]
    H --> I[Interviewer submit feedback]
    I --> J[INTERVIEWED]
    J --> K[HR gửi Offer]
    K --> L[OFFERED]
    L --> M[Candidate Accept]
    M --> N[HIRED]
    N --> O[Onboarding]
    O --> P[COMPLETED]
~~~

Các nhánh kết thúc: SUBMITTED → WITHDRAWN; SCREENING/SHORTLISTED/INTERVIEW_SCHEDULED/INTERVIEWED → REJECTED; OFFERED → REJECTED khi Candidate decline theo workflow hiện tại.

## Job status

| Status | Ý nghĩa |
| --- | --- |
| DRAFT | Chỉ nội bộ HR/Admin. |
| PUBLISHED | Hiển thị public/candidate và có thể apply nếu còn hạn. |
| CLOSED | Dừng nhận hồ sơ. |
| ARCHIVED | Lưu trữ job thay cho hard delete. |

## Application state machine

| Từ | Có thể đến |
| --- | --- |
| SUBMITTED | SCREENING, WITHDRAWN |
| SCREENING | SHORTLISTED, REJECTED |
| SHORTLISTED | INTERVIEW_SCHEDULED, REJECTED |
| INTERVIEW_SCHEDULED | INTERVIEWED, REJECTED |
| INTERVIEWED | OFFERED, REJECTED |
| OFFERED | HIRED, REJECTED |
| HIRED, REJECTED, WITHDRAWN | Kết thúc |

Mỗi update application status phải insert application_status_history: old/new status, actor, remarks, timestamp. Không đổi status trực tiếp từ controller/JSP.

## Interview và feedback

Interview status: SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED.

HR tạo lịch phải kiểm tra startTime < endTime, interviewer không trùng lịch và application hợp lệ; sau đó tạo notification cho Candidate/Interviewer và đưa application sang INTERVIEW_SCHEDULED.

~~~text
technicalScore
communicationScore
experienceScore
attitudeScore
overallScore = (technical + communication + experience + attitude) / 4
comment
recommendation: STRONG_HIRE | HIRE | CONSIDER | NO_HIRE
~~~

Chỉ interviewer được gán mới submit feedback. Submit thành công cùng transaction: feedback được lưu, interview thành COMPLETED, application thành INTERVIEWED, timeline được ghi.

## Offer

| Status | Ý nghĩa |
| --- | --- |
| DRAFT | HR đang soạn. |
| SENT | Candidate được phản hồi trước expiryDate. |
| ACCEPTED | Application thành HIRED. |
| DECLINED | Candidate từ chối. |
| EXPIRED | Hết hạn phản hồi. |

Chỉ application INTERVIEWED mới tạo/gửi offer.

- Send: offer SENT, application OFFERED.
- Accept: offer ACCEPTED, application HIRED, history, onboarding và notification.
- Decline: offer DECLINED, application REJECTED theo workflow hiện tại.

Candidate chỉ phản hồi offer của chính mình.

## Onboarding

Offer accept tạo onboarding có các task required mặc định: CCCD, thông tin ngân hàng, ký hợp đồng, laptop, tài khoản công ty, quy định, orientation và gặp quản lý.

- Onboarding: NOT_STARTED, IN_PROGRESS, COMPLETED.
- Task: TODO, IN_PROGRESS, DONE.
- progress = doneRequiredTasks / totalRequiredTasks × 100.
- Chỉ khi mọi required task DONE, onboarding mới COMPLETED.
- Candidate chỉ cập nhật task của mình; HR/Admin có thể thêm task.

## CV matching

job_skills có skill_name, weight, is_required. MatchingService lấy extracted CV text, chuẩn hóa lowercase, kiểm tra từng skill, rồi trả:

~~~text
matchScore = matchedWeight / totalWeight × 100
matchedSkills
missingSkills
~~~

Ví dụ Java 5, JDBC 4, MySQL 4, Git 2; CV có Java/MySQL/Git đạt (5 + 4 + 2) / 15 × 100 = 73.33%.

## Business rules BR01–BR12

| Mã | Luật | Điểm kiểm soát |
| --- | --- | --- |
| BR01 | Không apply cùng job hai lần. | Unique candidate_id + job_id, ApplicationService.apply. |
| BR02 | Chỉ PUBLISHED được apply. | ApplicationService.apply. |
| BR03 | Không apply quá deadline. | ApplicationService.apply. |
| BR04 | Candidate phải có CV thuộc mình. | ApplicationService.apply + ResumeDAO. |
| BR05 | Chỉ HR/Admin tạo/chỉnh job. | Filter + JobService. |
| BR06 | Interviewer chỉ feedback lịch được gán. | InterviewService. |
| BR07 | Chỉ INTERVIEWED tạo offer. | OfferService. |
| BR08 | Candidate chỉ phản hồi offer của chính mình. | OfferService.respond. |
| BR09 | Accept offer đưa application sang HIRED. | OfferService.respond. |
| BR10 | HIRED tạo onboarding. | OfferService + OnboardingService. |
| BR11 | Hoàn tất onboarding khi mọi required task DONE. | OnboardingService. |
| BR12 | Không hard delete job đã có application. | JobService archive. |

## Notification và audit

Notification được tạo cho apply, lịch interview, offer, accept/decline offer và onboarding. Candidate xem tại /candidate/notifications. Action quản trị dùng AuditLogService để tạo audit log.

