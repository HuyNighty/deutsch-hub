# Learning Context – Security Bootstrap cho Aggregate `Course`

Tài liệu này mô tả các lớp bảo mật cần có **ngay từ giai đoạn đầu** cho `Course` trong Learning Context.

## 1) Security boundaries cho Course

### 1.1 Asset cần bảo vệ
- Quyền sửa metadata khóa học (`title`, `description`, `level`, `price`).
- Quyền cấu trúc nội dung (`Section`, `Lesson`, thứ tự, thời lượng).
- Quyền publish/unpublish.
- Tính toàn vẹn ownership (`instructorId` là chủ sở hữu business).

### 1.2 Actor chính
- `INSTRUCTOR` (owner của course).
- `ADMIN` (quyền override có audit bắt buộc).
- `STUDENT` (chỉ read khóa học đã publish hoặc được cấp quyền preview).
- `SYSTEM` (job/background worker, phải định danh riêng).

## 2) Authorization matrix tối thiểu (MVP)

| Action | Instructor (owner) | Admin | Student |
|---|---|---|---|
| Create course | ✅ | ✅ | ❌ |
| Update course metadata | ✅ | ✅ | ❌ |
| Add/remove section/lesson | ✅ | ✅ | ❌ |
| Publish course | ✅ | ✅ | ❌ |
| Delete (soft delete) | ✅ | ✅ | ❌ |
| View draft course | ✅ | ✅ | ❌ (trừ khi có preview token) |
| View published course | ✅ | ✅ | ✅ |

> Khuyến nghị: kiểm tra quyền ở **application layer** và xác nhận invariant ownership ở **domain aggregate**.

## 3) Domain invariants liên quan security cho Course

1. Course đã `published` thì không được chỉnh sửa cấu trúc nếu chưa chuyển về draft.
2. Chỉ owner/admin mới được mutate aggregate.
3. Không cho mutate aggregate đã `softDelete`.
4. `instructorId` không được null, không được đổi tùy tiện sau create (nếu cần transfer ownership phải có use case riêng + audit).
5. `publish()` yêu cầu nội dung tối thiểu (ít nhất 1 section, 1 lesson, tổng duration > 0).

## 4) Input validation / anti-abuse tối thiểu

- Chuẩn hóa và giới hạn độ dài:
  - `title`: 3–120 ký tự.
  - `description`: tối đa 2000 ký tự.
- Chặn ký tự điều khiển nguy hiểm ở input text.
- Validate `Money`: currency whitelist, amount >= 0, precision cố định.
- Idempotency cho endpoint publish (tránh race call lặp).

## 5) Audit & traceability (rất quan trọng ở giai đoạn đầu)

Tối thiểu log các event:
- `COURSE_CREATED`
- `COURSE_UPDATED`
- `COURSE_SECTION_ADDED`
- `COURSE_SECTION_REMOVED`
- `COURSE_PUBLISHED`
- `COURSE_SOFT_DELETED`

Mỗi event cần:
- `actorId`, `actorRole`, `courseId`, `timestamp`, `before/after` (field-level nếu có thể), `correlationId`.

## 6) API security checklist cho Course

1. Mọi endpoint mutate cần JWT + role claim.
2. Bắt buộc kiểm tra owner theo `course.instructorId == principal.userId` (trừ admin).
3. Dùng optimistic locking (`version`) để chặn lost update.
4. Rate limit các endpoint mutate (`publish`, `update`, `add section`).
5. Trả lỗi chuẩn hóa, không lộ thông tin nhạy cảm nội bộ.

## 7) Data security checklist

- Soft delete filter mặc định ở query read.
- Không expose draft course cho student query public.
- Nếu có media/private content: dùng signed URL có TTL ngắn.
- Tách quyền read model (public) và write model (owner/admin).

## 8) Test security bắt buộc cho Course (nên làm trước)

### Unit test (domain)
- Non-owner mutate course -> bị từ chối.
- Course published -> không add/remove section.
- Course deleted -> mọi mutate bị từ chối.
- Publish khi thiếu section/lesson -> bị từ chối.

### Integration test (application/API)
- Student gọi endpoint update -> 403.
- Instructor A không thể sửa Course của Instructor B -> 403.
- Admin sửa được Course bất kỳ -> 200/204 + có audit log.
- Concurrent update với version cũ -> 409.

## 9) Roadmap 3 phase để “đi chắc” cho Course

### Phase 1 (ngay bây giờ)
- Chốt authorization matrix.
- Chốt invariants domain quan trọng.
- Bổ sung audit event cơ bản.
- Viết security unit/integration tests trọng yếu.

### Phase 2
- Thêm preview token cho draft sharing.
- Hardening input validation + rate limiting nâng cao.
- Policy-as-code (ví dụ Casbin/OPA) nếu rule phức tạp.

### Phase 3
- Threat modeling chính thức theo STRIDE.
- Security monitoring dashboard cho abuse pattern.
- Bổ sung anomaly detection cho hành vi publish/update bất thường.

## 10) Đề xuất bắt đầu thực thi trong code hiện tại

Bắt đầu từ 4 việc nhỏ, tạo giá trị ngay:
1. Thêm policy check owner/admin ở use case mutate `Course`.
2. Bổ sung optimistic locking ở persistence layer.
3. Bổ sung audit event khi publish/delete/update.
4. Viết 6 test bảo mật cốt lõi (unit + integration).

---

Nếu cần, bước tiếp theo có thể chuyển checklist này thành backlog theo format user story + acceptance criteria cho từng use case của `Course`.
