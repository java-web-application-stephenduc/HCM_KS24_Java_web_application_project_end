<div align="center">
  <h1>🎓 Smart Academic & Lab Support Platform (SALSP)</h1>
  <p><i>Nền tảng tập trung quản lý cố vấn học tập và thiết bị phòng thí nghiệm thông minh</i></p>
  
  ![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
  ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
  ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
  ![Hibernate](https://img.shields.io/badge/Hibernate-6.x-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
  ![Status](https://img.shields.io/badge/Code_Review-Complete-success?style=for-the-badge)
</div>

---

## 📑 Mục lục
1. [Giới thiệu dự án](#1-giới-thiệu-dự-án)
2. [Các phân hệ chính](#2-các-phân-hệ-chính)
3. [Kiến trúc hệ thống](#3-kiến-trúc-hệ-thống)
4. [Công nghệ sử dụng](#4-công-nghệ-sử-dụng)
5. [Danh sách các subsystems & modules](#5-danh-sách-các-subsystems--modules)
6. [Tính năng theo từng vai trò](#6-tính-năng-theo-từng-vai-trò)
7. [Luồng nghiệp vụ chính](#7-luồng-nghiệp-vụ-chính)
8. [Database Design](#8-database-design)
9. [Cấu trúc thư mục & Package Layout](#9-cấu-trúc-thư-mục--package-layout)
10. [Chi tiết từng Layer kiến trúc](#10-chi-tiết-từng-layer-kiến-trúc)
11. [Thông tin Route & Endpoint Chính](#11-thông-tin-route--endpoint-chính)
12. [Hướng dẫn chạy project](#12-hướng-dẫn-chạy-project)
13. [Tài khoản demo](#13-tài-khoản-demo)
14. [Điểm nổi bật kỹ thuật](#14-điểm-nổi-bật-kỹ-thuật)
15. [Ghi chú bảo trì & phát triển tương lai](#15-ghi-chú-bảo-trì--phát-triển-tương-lai)

---

## 1. 🌟 Giới thiệu dự án

**Mục tiêu hệ thống:**
Xây dựng một nền tảng tập trung, tự động hóa nhằm số hóa toàn bộ quy trình cố vấn học tập và mượn trả trang thiết bị thực hành tại trường Đại học.

**Bài toán giải quyết:**
- Giảm thiểu thủ tục giấy tờ trong việc đăng ký lịch hẹn cố vấn học tập giữa sinh viên và giảng viên
- Số hóa quy trình quản lý kho thiết bị phòng Lab, tránh thất thoát và khó khăn
- Đảm bảo tính minh bạch thông qua hệ thống đánh giá (Evaluation) sau mỗi buổi

**Đối tượng sử dụng:**
- 👨‍🎓 **Sinh viên (STUDENT)**: Cần hỗ trợ học tập, mượn thiết bị thực hành
- 👨‍🏫 **Giảng viên (LECTURER)**: Tiếp nhận cố vấn, phê duyệt yêu cầu, đánh giá
- 🛡️ **Quản trị viên (ADMIN)**: Quản lý toàn bộ hệ thống, kho thiết bị, người dùng

---

## 2. 🔑 Các phân hệ chính

Dự án được phân chia thành **10 phân hệ chuyên biệt** (각 subsystem):

| # | Phân hệ | Chức năng | Files Chính |
|---|---------|----------|-----------|
| **1** | **Authentication & User Management** | Xác thực, đăng ký, quản lý tài khoản | `AuthController`, `AuthService`, `User`, `UserRole` |
| **2** | **Equipment Management** | Quản lý kho thiết bị, inventory tracking | `EquipmentService`, `Equipment`, `EquipmentRepository` |
| **3** | **Mentoring Session Booking** | Đặt lịch cố vấn, kiểm tra xung đột | `BookingService`, `MentoringSession`, `SlotDto` |
| **4** | **Academic Evaluation** | Đánh giá học tập, ghi nhận kết quả | `EvaluationService`, `AcademicEvaluation` |
| **5** | **Borrowing & Equipment Request** | Mượn/trả thiết bị, kiểm kho | `BorrowingRecord`, `BorrowingDetail` |
| **6** | **Lecturer Management** | Hồ sơ giảng viên, lịch sử | `Lecturer`, `LecturerDashboardService` |
| **7** | **Student Dashboard & History** | Dashboard sinh viên, lịch sử học tập | `StudentDashboardService`, `StudentHistoryService` |
| **8** | **Admin Dashboard & Dispatch** | Thống kê, quản lý phê duyệt | `AdminDashboardService`, `DispatchService` |
| **9** | **Profile & Common Management** | Hồ sơ chung, bộ phận, thông tin | `ProfileService`, `UserProfile`, `Department` |
| **10** | **Configuration & Security** | Cấu hình bảo mật, exception handling | `SecurityConfig`, `GlobalExceptionHandler` |

---

## 3. 🏗 Kiến trúc hệ thống

Dự án áp dụng kiến trúc **Spring Boot Monolithic** theo mô hình **3-Tier (3-Layer Architecture)**:

```
┌─────────────────────────────────────┐
│  Controller Layer (HTTP Handler)    │ ← Thymeleaf Rendering / REST API
│  - AuthController                  │
│  - StudentBookingController         │
│  - AdminEquipmentController         │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Service Layer (Business Logic)     │ ← Transaction Management
│  - AuthService                      │   Data Validation
│  - BookingService                   │   Workflow Orchestration
│  - EquipmentService                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Repository/DAO Layer (Data Access) │ ← JPA / Hibernate
│  - UserRepository                   │   Optimized Queries
│  - MentoringSessionRepository        │   Relationship Mapping
│  - EquipmentRepository              │
└─────────────────────────────────────┘
        │
        ▼
┌──────────────────────────────────────┐
│  MySQL Database (Persistent Store)   │
└──────────────────────────────────────┘
```

**Key Design Patterns:**
- **DTO (Data Transfer Object)**: Tách Entity (DB) từ DTO (API) để bảo mật
- **Repository Pattern**: Abstraction data access logic
- **Service Layer**: Centralize business rules & transactions
- **Global Exception Handling**: `@ControllerAdvice` catch tất cả lỗi

---

## 4. 🛠 Công nghệ sử dụng

| Công nghệ | Phiên bản | Vai trò | Chi tiết |
|-----------|----------|--------|---------|
| **Java** | 21 | Ngôn ngữ lập trình | Virtual Threads, Pattern Matching |
| **Spring Boot** | 4.0.6 | Backend Framework | Auto-config, MVC, Starter Dependencies |
| **Spring Data JPA** | 3.x | ORM Framework | Hibernate 6 + Spring Data |
| **Spring Security** | 6.x | Authentication/Authorization | Form Login, Role-based Access Control |
| **MySQL** | 8.0 | Database | ACID compliance, InnoDB |
| **Thymeleaf** | 3.x | Template Engine | Server-side rendering, fragments |
| **Bootstrap 5** | 5.x | Frontend Framework | Responsive UI, components |
| **Gradle** | 8.x | Build Tool | Dependency management |
| **Lombok** | 1.x | Code Generation | Reduce boilerplate (@Getter, @Setter) |

---

## 5. 📋 Danh sách các Subsystems & Modules

### **Phân hệ 1: Authentication & User Management**
- **Mục đích**: Xác thực, đăng ký, phân quyền role-based
- **Key Components**:
  - `User` entity: Tài khoản + vai trò
  - `UserProfile`: Thông tin chi tiết (FHO, SĐT, avatar)
  - `SecurityConfig`: Spring Security setup, authorization rules
  - `AuthService`: Business logic register/validate email
  - `AuthController`: GET /auth/login, POST /auth/register, GET /auth/locked
- **Validation Rules**:
  - Email: unique, format hợp lệ
  - Password: >= 8 ký tự, BCrypt encoded
  - Role: STUDENT, LECTURER, ADMIN

### **Phân hệ 2: Equipment Management**
- **Mục đích**: Quản lý kho thiết bị Lab, tồn kho
- **Key Components**:
  - `Equipment` entity: Master data thiết bị
  - `EquipmentRepository`: Custom queries (lock, low stock)
  - `EquipmentService`: CRUD, inventory update
  - `AdminEquipmentController`: Quản lý kho
- **Business Rules**:
  - `quantityAvailable` cập nhật khi mượn/trả
  - Soft delete (deleted flag) để audit trail
  - Pessimistic lock tránh race condition

### **Phân hệ 3: Mentoring Session Booking**
- **Mục đích**: Sinh viên đặt lịch cố vấn, kiểm soát xung đột
- **Key Components**:
  - `MentoringSession` entity: Buổi cố vấn (PENDING → COMPLETED)
  - `BookingService`: Conflict detection, slot management
  - `StudentBookingController`: UI gợi ý khung giờ, đặt lịch
- **Conflict Detection**:
  - Kiểm tra: `startTime < other.endTime AND endTime > other.startTime`
  - Query: `existsConflict()` loại trừ CANCELLED/REJECTED
- **Slot System**:
  - 8 khung giờ cố định: 08:00-09:00, 09:00-10:00, ... 16:00-17:00
  - Check availability trên từng slot

### **Phân hệ 4: Academic Evaluation**
- **Mục đích**: Giảng viên đánh giá kết quả buổi học
- **Key Components**:
  - `AcademicEvaluation`: 1-1 với MentoringSession
  - `EvaluationService`: Create/update evaluation
  - `LecturerController`: Form đánh giá, điểm số, feedback
- **Workflow**:
  - PENDING → CONFIRMED → COMPLETED → Giảng viên enter score/feedback

### **Phân hệ 5: Borrowing & Equipment Request**
- **Mục đích**: Quản lý phiếu mượn thiết bị + trả kho
- **Key Components**:
  - `BorrowingRecord`: Header (phiếu mượn)
  - `BorrowingDetail`: Line items (chi tiết từng thiết bị)
  - `BorrowingStatus`: PENDING → APPROVED → BORROWED → RETURNED / OVERDUE
  - `BorrowingOverdueScheduler`: Daily task check quá hạn
- **Transaction Management**:
  - APPROVED: Lock + giảm `quantityAvailable`
  - RETURNED: Tăng `quantityAvailable`
  - Cascade delete + orphanRemoval

### **Phân hệ 6: Lecturer Management**
- **Mục đích**: Quản lý hồ sơ & thống kê giảng viên
- **Key Components**:
  - `Lecturer` entity: Department, Title, Bio
  - `LecturerRepository`: Custom queries (by department)
  - `LecturerDashboardService`: Thống kê buổi/tháng
- **Dashboard Features**:
  - Số buổi PENDING, CONFIRMED, COMPLETED
  - Top giảng viên (nhiều buổi hoàn thành nhất)

### **Phân hệ 7: Student Dashboard & History**
- **Mục đích**: Sinh viên xem lịch sử học tập + mượn trả
- **Key Components**:
  - `StudentDashboardService`: Thống kê sinh viên
  - `StudentHistoryService`: Lịch sử chi tiết
  - Native query JOIN 6 bảng → `AcademicHistoryProjection`
- **History Data**:
  - Mentoring sessions + evaluation score + borrowing details + equipment names

### **Phân hệ 8: Admin Dashboard & Dispatch**
- **Mục đích**: Thống kê hệ thống, phê duyệt phiếu mượn
- **Key Components**:
  - `AdminDashboardService`: Tổng hợp statistics
  - `DispatchService`: Queue quản lý phiếu mượn
  - `AdminDispatchController`: UI phê duyệt, từ chối
- **Statistics**:
  - Tổng user, buổi, thiết bị
  - Biểu đồ xu hướng tháng

### **Phân hệ 9: Profile & Common Management**
- **Mục đích**: Quản lý hồ sơ người dùng, thông tin bộ phận
- **Key Components**:
  - `UserProfile`: Họ tên, SĐT, avatar
  - `Department`: Bộ phận (CNIT, KTVT, ...)
  - `ProfileService / ProfileController`: CRUD profile
- **Form Validation**:
  - `@NotBlank`, `@Email`, `@Size` annotations

### **Phân hệ 10: Configuration & Security**
- **Mục đích**: Cấu hình toàn hệ thống, exception handling
- **Key Components**:
  - `SecurityConfig`: Authorization rules, password encoder
  - `GlobalExceptionHandler`: Catch all exceptions
  - `WebMvcConfig`: Interceptor, CORS, message converter
  - `JacksonConfig`: JSON serialization config
  - `LoginAttemptService`: Account lock after failed attempts
  - `DataSeeder`: Initialize demo data
- **Authorization Levels**:
  - PUBLIC: /auth/**, /error
  - ADMIN: /admin/**
  - LECTURER: /lecturer/**
  - STUDENT: /student/**
  - AUTHENTICATED: /profile/**, /dashboard

---

## 6. 👥 Tính năng theo từng vai trò

### 🎓 **Sinh viên (STUDENT)**
| Tính năng | Chi tiết | Status |
|-----------|---------|--------|
| Đăng ký tài khoản | Email, password, profile | ✅ |
| Đặt lịch cố vấn | Chọn khoa, giảng viên, khung giờ | ✅ |
| Xem khung giờ trống | Gợi ý slot sáng chiều | ✅ |
| Hủy lịch hẹn | Hủy trước buổi (status PENDING) | ✅ |
| Yêu cầu mượn thiết bị | Liên kết với session CONFIRMED | ✅ |
| Xem lịch sử học tập | All sessions + evaluations | ✅ |
| Xem lịch sử mượn trả | Tất cả borrowing records | ✅ |
| Xem hồ sơ cá nhân | Update thông tin profile | ✅ |
| Dashboard thống kê | Của riêng sinh viên | ✅ |

### 👨‍🏫 **Giảng viên (LECTURER)**
| Tính năng | Chi tiết | Status |
|-----------|---------|--------|
| Quản lý hồ sơ | Title, department, bio | ✅ |
| Xem danh sách booking | Các sinh viên đặt lịch | ✅ |
| Phê duyệt / Từ chối | Status: CONFIRMED / REJECTED | ✅ |
| Ghi chú chuyên môn | Update note/feedback | ✅ |
| Đánh giá học tập | Score (0-100) + feedback | ✅ |
| Dashboard lịch sử | Buổi hôm nay, tháng này | ✅ |
| Xem chi tiết session | Student name, note, evaluation | ✅ |
| Quản lý phiếu mượn | View requests from sessions | ⚠️ |

### 🛡️ **Quản trị viên (ADMIN)**
| Tính năng | Chi tiết | Status |
|-----------|---------|--------|
| Quản lý user | Create/edit/delete (CRD) | ✅ |
| Quản lý bộ phận | CRUD department | ⚠️ |
| Quản lý thiết bị | Add/edit/delete, qty | ✅ |
| Phê duyệt phiếu mượn | Queue + approve/reject | ✅ |
| Xem báo cáo thống kê | Dashboard numbers | ✅ |
| Khóa tài khoản user | Set active = false | ✅ |
| Xem log hành động | Audit trail (created_at, updated_at) | ⚠️ |
| Cảnh báo tồn kho thấp | Danh sách equipment low stock | ⚠️ |

---

## 7. 🔄 Luồng nghiệp vụ chính

### **Luồng 1: Sinh viên Đặt Lịch & Hoàn Thành Buổi Học**

```
1️⃣ Sinh viên Login
   ↓
2️⃣ Chọn Khoa → Chọn Giảng viên → Chọn Khung Giờ
   - BookingService.getDepartments()
   - BookingService.getLecturersByDepartment(deptId)
   - BookingService.getAvailableSlots(lecturerId, date)
   ↓
3️⃣ Kiểm Tra Xung Đột
   - sessionRepository.existsConflict()
   - Nếu conflict → throw SlotConflictException
   - Nếu OK → continue
   ↓
4️⃣ Tạo MentoringSession (Status = PENDING)
   - BookingService.createBooking(dto, studentEmail)
   - sessionRepository.save()
   ↓
5️⃣ Giảng viên Xem & Phê Duyệt
   - LecturerController receive booking notification
   - Confirm / Reject (status → CONFIRMED / REJECTED)
   ↓
6️⃣ (Optional) Sinh viên Yêu Cầu Mươn Thiết Bị
   - BorrowingRecord.create() (Status = PENDING)
   - BorrowingDetail.add items
   ↓
7️⃣ Admin Phê Duyệt & Xuất Kho
   - Status: PENDING → APPROVED
   - Equipment.quantityAvailable -= qty_requested
   - UPDATE database (pessimistic lock)
   ↓
8️⃣ Buổi Cố Vấn Diễn Ra
   - Sinh viên nhận thiết bị
   - BorrowingRecord.status = BORROWED
   ↓
9️⃣ Giảng viên Đánh Giá
   - Create AcademicEvaluation
   - Set score, feedback
   - MentoringSession.status = COMPLETED
   ↓
🔟 Sinh viên Trả Thiết Bị
   - Confirm return
   - BorrowingRecord.status = RETURNED
   - Equipment.quantityAvailable += qty_returned
```

### **Luồng 2: Kiểm Tra Xung Đột Lịch (Conflict Detection)**

```
BookingService.createBooking() 
   ↓
MentoringSessionRepository.existsConflict(lecturerId, date, startTime, endTime)
   ↓
SELECT COUNT(ms) > 0 FROM mentoring_sessions ms
WHERE lecturer_id = :lecturerid
  AND session_date = :date
  AND start_time < :endTime
  AND end_time > :startTime
  AND status NOT IN ('CANCELLED', 'REJECTED', 'CANCELED_BY_STUDENT')
```

---

## 8. 🗄 Database Design

### **Entity Relationship Diagram (ERD)**

```
┌─────────────────────────┐
│       USERS             │
│ (Primary User Account)  │
├─────────────────────────┤
│ id (PK)                 │
│ email (UNIQUE)          │
│ password_hash           │
│ role (ENUM)             │
│ active (BOOLEAN)        │
│ created_at              │
└─────────┬───────────────┘
          │
          │ 1-1 (cascade)
          ▼
┌─────────────────────────┐
│    USER_PROFILES        │
│ (Detailed User Info)    │
├─────────────────────────┤
│ id, user_id (FK)        │
│ full_name               │
│ phone                   │
│ avatar_url              │
│ updated_at              │
└─────────────────────────┘


┌─────────────────────────┐      ┌──────────────────────────┐
│    DEPARTMENTS          │      │     LECTURERS            │
│ (Khoa/Bộ phận)          │      │ (Thông tin Giảng viên)   │
├─────────────────────────┤      ├──────────────────────────┤
│ id (PK)                 │      │ id (PK) = user_id (FK)   │
│ name                    │      │ department_id (FK)       │◄────┐
│ code                    │      │ title                    │     │
└──────────┬──────────────┘      │ bio                      │     │
           │                     └──────────────────────────┘     │
           └────────────────────────────────────┼────────────────┘


┌──────────────────────────────┐
│   MENTORING_SESSIONS         │
│ (Buổi Cố Vấn)               │
├──────────────────────────────┤
│ id (PK)                      │
│ student_id (FK) → Users      │
│ lecturer_id (FK) → Users     │
│ session_date                 │
│ start_time, end_time         │
│ status (ENUM)                │
│ note, rejection_reason       │
│ created_at                   │
└──────────┬───────────────────┘
           │
           │ 1-1
           ▼
┌──────────────────────────────┐
│  ACADEMIC_EVALUATIONS        │
│ (Kết quả đánh giá)           │
├──────────────────────────────┤
│ id (PK)                      │
│ session_id (FK)              │
│ score (0-100)                │
│ feedback                     │
│ created_at                   │
└──────────────────────────────┘

           │ 1-N
           ▼
┌──────────────────────────────┐
│   BORROWING_RECORDS          │
│ (Phiếu Mượn)                 │
├──────────────────────────────┤
│ id (PK)                      │
│ session_id (FK)              │
│ status (ENUM)                │
│ lecturer_note, admin_note    │
│ created_at, updated_at       │
└──────────┬───────────────────┘
           │
           │ 1-N (cascade + orphanRemoval)
           ▼
┌──────────────────────────────┐
│   BORROWING_DETAILS          │
│ (Chi Tiết Thiết Bị Mượn)    │
├──────────────────────────────┤
│ id (PK)                      │
│ record_id (FK)               │
│ equipment_id (FK) ──────────┐│
│ quantity_requested           ││
│ returned_date                ││
└──────────────────────────────┘│
                                │
                                │
                 ┌──────────────┘
                 │
┌────────────────▼──────────────┐
│      EQUIPMENTS              │
│ (Thiết Bị Kho)               │
├──────────────────────────────┤
│ id (PK)                      │
│ name                         │
│ description                  │
│ category                     │
│ quantity_total               │
│ quantity_available           │
│ deleted (SOFT DELETE)        │
│ created_at, updated_at       │
└──────────────────────────────┘
```

---

## 9. 📁 Cấu trúc thư mục & Package Layout

```
Smart_Academic_Lab_Support_Platform/
│
├── src/main/java/com/rikkei/salsp/
│   ├── config/                           # Cấu hình Spring
│   │   ├── SecurityConfig.java           # Authorization rules, password encoder
│   │   ├── WebMvcConfig.java             # Interceptor, message converter
│   │   ├── JacksonConfig.java            # JSON serialization
│   │   ├── CustomAuthSuccessHandler.java # Post-login redirect
│   │   ├── CustomAuthFailureHandler.java # Post-login fail handler
│   │   ├── LoginAttemptService.java      # Account lock logic
│   │   ├── UserStatusInterceptor.java    # Request interceptor
│   │   └── DataSeeder.java               # Demo data initialization
│   │
│   ├── controller/                       # HTTP Handlers
│   │   ├── auth/
│   │   │   └── AuthController.java       # /auth/login, /auth/register
│   │   ├── admin/
│   │   │   ├── AdminUserController.java
│   │   │   ├── AdminEquipmentController.java
│   │   │   └── AdminDispatchController.java
│   │   ├── lecturer/
│   │   │   └── LecturerController.java
│   │   ├── student/
│   │   │   ├── StudentBookingController.java
│   │   │   └── StudentHistoryController.java
│   │   └── common/
│   │       ├── DashboardController.java
│   │       └── ProfileController.java
│   │
│   ├── service/                          # Business Logic
│   │   ├── auth/
│   │   │   ├── AuthService.java          # Register, email validation
│   │   │   └── CustomUserDetailsService.java
│   │   ├── admin/
│   │   │   ├── AdminUserService.java
│   │   │   ├── AdminDashboardService.java
│   │   │   ├── EquipmentService.java
│   │   │   ├── DispatchService.java
│   │   │   └── BorrowingOverdueScheduler.java # @Scheduled task
│   │   ├── lecturer/
│   │   │   ├── LecturerDashboardService.java
│   │   │   └── EvaluationService.java
│   │   ├── student/
│   │   │   ├── BookingService.java       # Conflict detection, slots
│   │   │   ├── StudentDashboardService.java
│   │   │   ├── StudentHistoryService.java
│   │   │   └── CancellationService.java
│   │   └── common/
│   │       └── ProfileService.java
│   │
│   ├── repository/                       # Data Access (JPA)
│   │   ├── user/
│   │   │   ├── UserRepository.java
│   │   │   ├── UserProfileRepository.java
│   │   │   └── LecturerRepository.java
│   │   ├── equipment/
│   │   │   ├── EquipmentRepository.java  # Pessimistic lock, low stock
│   │   │   ├── BorrowingRecordRepository.java
│   │   │   └── BorrowingDetailRepository.java
│   │   ├── session/
│   │   │   ├── MentoringSessionRepository.java # Conflict check
│   │   │   └── AcademicEvaluationRepository.java
│   │   └── common/
│   │       └── DepartmentRepository.java
│   │
│   ├── entity/                           # JPA Entities
│   │   ├── user/
│   │   │   ├── User.java                 # @Entity Table users
│   │   │   ├── UserProfile.java
│   │   │   ├── UserRole.java             # Enum: STUDENT, LECTURER, ADMIN
│   │   │   └── Lecturer.java
│   │   ├── equipment/
│   │   │   ├── Equipment.java            # @Entity Table equipments
│   │   │   ├── BorrowingRecord.java      # @Entity Table borrowing_records
│   │   │   ├── BorrowingDetail.java
│   │   │   └── BorrowingStatus.java      # Enum: PENDING, APPROVED, RETURNED...
│   │   ├── session/
│   │   │   ├── MentoringSession.java
│   │   │   ├── AcademicEvaluation.java
│   │   │   └── SessionStatus.java        # Enum: PENDING, CONFIRMED, COMPLETED...
│   │   └── common/
│   │       └── Department.java
│   │
│   ├── dto/                              # Data Transfer Objects
│   │   ├── auth/
│   │   │   └── RegisterDto.java
│   │   ├── admin/
│   │   │   ├── EquipmentDto.java
│   │   │   ├── AdminDashboardDto.java
│   │   │   └── DispatchQueueItemDto.java
│   │   ├── lecturer/
│   │   │   ├── LecturerSummaryDto.java
│   │   │   ├── SessionDetailDto.java
│   │   │   ├── EvaluationFormDto.java
│   │   │   └── LecturerDashboardDto.java
│   │   ├── student/
│   │   │   ├── BookingRequestDto.java
│   │   │   ├── SlotDto.java
│   │   │   ├── StudentDashboardDto.java
│   │   │   └── BorrowedEquipmentDto.java
│   │   └── common/
│   │       ├── DepartmentDto.java
│   │       ├── ProfileUpdateDto.java
│   │       └── EquipmentRequestDto.java
│   │
│   ├── exception/                        # Custom Exceptions
│   │   ├── BusinessException.java
│   │   ├── DuplicateEmailException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── SlotConflictException.java
│   │   ├── InsufficientStockException.java
│   │   └── GlobalExceptionHandler.java   # @ControllerAdvice
│   │
│   └── Application.java                  # Spring Boot Main
│
├── src/main/resources/
│   ├── application.properties             # DB config, JPA settings, port
│   ├── schema.sql                         # Database init script
│   ├── data.sql                           # Demo data seed
│   ├── banner.txt                         # ASCII art startup
│   ├── static/                            # CSS, JS, images
│   │   ├── css/
│   │   ├── js/
│   │   └── images/
│   └── templates/                         # Thymeleaf HTML views
│       ├── auth/
│       │   ├── login.html
│       │   ├── register.html
│       │   └── locked.html
│       ├── admin/
│       ├── lecturer/
│       ├── student/
│       ├── profile/
│       ├── fragment/                      # Components (header, footer, sidebar)
│       ├── layout/                        # Base layouts
│       └── error/                         # 403.html, 404.html...
│
├── build.gradle                           # Gradle build file
├── gradlew / gradlew.bat                  # Gradle wrapper
├── settings.gradle
├── README.md                              # This file
└── HELP.md
```

---

## 10. 🔍 Chi tiết từng Layer kiến trúc

### **Layer 1: Controller (HTTP Handler & Request Routing)**

**Trách nhiệm:**
- Nhận HTTP request từ Thymeleaf form hoặc AJAX
- Validate input (@Valid, BindingResult)
- Gọi Service để xử lý nghiệp vụ
- Trả về View (template name) hoặc redirect/JSON response

**Example - AuthController:**
```java
@PostMapping("/auth/register")
public String register(
    @Valid @ModelAttribute("registerDto") RegisterDto dto,  // Auto-bind + validate
    BindingResult errors,                                    // Validation results
    RedirectAttributes flash
) {
    if (errors.hasErrors()) {
        return "auth/register";  // Quay lại form
    }
    authService.register(dto);   // Gọi Service
    flash.addFlashAttribute("success", "Đăng ký thành công");
    return "redirect:/auth/login";  // Redirect + flash message
}
```

**Validation Constraints:**
- `@NotBlank`: Không được NULL/empty
- `@Email`: Format email hợp lệ
- `@Size(min=8)`: Đạt độ dài tối thiểu
- Server-side validate + client-side validate (JS)

---

### **Layer 2: Service (Business Logic & Transaction Management)**

**Trách nhiệm:**
- Xử lý complex business logic
- Orchestrate multiple repositories
- Transaction management (`@Transactional`)
- Exception handling & custom error messages

**Example - BookingService.createBooking():**
```java
@Transactional  // Write transaction
public MentoringSession createBooking(BookingRequestDto dto, String studentEmail) {
    // 1. VALIDATE: Time logical (start < end)
    if (!dto.getStartTime().isBefore(dto.getEndTime())) {
        throw new BusinessException("Giờ kết thúc phải sau giờ bắt đầu");
    }
    
    // 2. VALIDATE: Not in past
    LocalDateTime bookingDateTime = LocalDateTime.of(dto.getSessionDate(), dto.getStartTime());
    if (bookingDateTime.isBefore(LocalDateTime.now())) {
        throw new BusinessException("Không thể đặt lịch trong quá khứ");
    }
    
    // 3. CHECK CONFLICT: Repository query
    if (sessionRepository.existsConflict(...)) {
        throw new SlotConflictException("Khung giờ này đã có lịch");
    }
    
    // 4. LOAD & VERIFY: User role check
    User lecturer = userRepository.findById(dto.getLecturerId())
        .orElseThrow(...);
    if (lecturer.getRole() != UserRole.LECTURER) {
        throw new BusinessException("ID không phải giảng viên");
    }
    
    // 5. CREATE & SAVE: JPA persist + flush
    MentoringSession session = new MentoringSession();
    session.setStudent(...);
    session.setStatus(SessionStatus.PENDING);
    return sessionRepository.save(session);  // Auto ID generation
}
```

---

### **Layer 3: Repository (Data Access & Query Optimization)**

**Trách nhiệm:**
- CRUD operations (Spring Data JPA auto-implement)
- Custom queries (JPQL, Native SQL)
- Query optimization (@EntityGraph, JOIN FETCH)
- Concurrency control (@Lock)

**Example - EquipmentRepository:**
```java
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    
    // Simple: Spring Data generates SQL
    List<Equipment> findByDeletedFalse();
    
    // Custom JPQL with JOIN FETCH (against N+1)
    @Query("""
        SELECT e FROM Equipment e 
        WHERE e.deleted = false AND e.quantityAvailable <= :threshold
    """)
    List<Equipment> findLowStock(@Param("threshold") int threshold);
    
    // Pessimistic lock cho concurrent updates
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id = :id")
    Optional<Equipment> findByIdWithLock(@Param("id") Long id);
}

**N+1 Problem & Solution:**
```
PROBLEM:
Query 1: SELECT * FROM users;        // 100 users
Loop 100x:
  Query 2-101: SELECT * FROM profiles WHERE user_id = ?;  // 100 queries!
Total: 101 queries!

SOLUTION - JOIN FETCH:
@Query("""
    SELECT u FROM User u
    JOIN FETCH u.profile p
    WHERE u.role = :role
""")
// Result: 1 query returns users + profiles
```

---

### **Layer 4: Entity & DTO (Data Models)**

**Entity**: JPA mapped objects (database rows)
```java
@Entity
@Table(name = "users")
public class User {
    @Id private Long id;
    @Column(unique = true) private String email;
    private String passwordHash;
    @Enumerated(EnumType.STRING) private UserRole role;
}
```

**DTO**: API request/response objects (NOT mapped by JPA)
```java
public class RegisterDto {
    @Email @NotBlank private String email;
    @Size(min = 8) @NotBlank private String password;
    @NotBlank private String fullName;
}
```

**Why separate?**
- Security: Don't expose all attributes
- Flexibility: Different DTOs for different endpoints
- Decoupling: API contract independent of DB schema

---

## 11. 🌐 Thông tin Route & Endpoint Chính

### **Public Routes (No Authentication)**
| Method | Endpoint | Handler | View/Response |
|--------|----------|---------|---------------|
| GET | `/` | Redirect | → /auth/login |
| GET | `/auth/login` | AuthController.login() | auth/login.html |
| POST | `/auth/login` | Spring Security Form Login | Redirect to role dashboard |
| GET | `/auth/register` | AuthController.registerForm() | auth/register.html |
| POST | `/auth/register` | AuthController.register() | Redirect: /auth/login |
| GET | `/auth/locked` | AuthController.locked() | auth/locked.html |
| GET | `/error/403` | - | error/403.html |

### **Student Routes** (`/student/**`)
| Method | Endpoint | Handler | Use Case |
|--------|----------|---------|----------|
| GET | `/student/booking` | StudentBookingController.booking() | Form đặt lịch (bước 1) |
| GET | `/student/booking/lecturers` | AJAX backend | Lấy danh sách giảng viên (bước 2) |
| GET | `/student/booking/slots` | AJAX backend | Lấy khung giờ trống (bước 3) |
| POST | `/student/booking/create` | StudentBookingController.create() | Tạo booking |
| GET | `/student/sessions` | StudentHistoryController.sessions() | Xem lịch sử buổi |
| GET | `/student/borrowing` | StudentHistoryController.borrowing() | Xem lịch sử mượn |
| POST | `/student/cancel/{id}` | StudentBookingController.cancel() | Hủy lịch hẹn |

### **Lecturer Routes** (`/lecturer/**`)
| Method | Endpoint | Handler | Use Case |
|--------|----------|---------|----------|
| GET | `/lecturer/sessions/pending` | LecturerController.pending() | Xem lịch chờ duyệt |
| POST | `/lecturer/sessions/{id}/confirm` | LecturerController.confirm() | Phê duyệt lịch |
| POST | `/lecturer/sessions/{id}/reject` | LecturerController.reject() | Từ chối lịch |
| GET | `/lecturer/sessions/{id}` | LecturerController.detail() | Chi tiết 1 buổi |
| POST | `/lecturer/evaluation/{sessionId}` | EvaluationController.save() | Nộp đánh giá |
| GET | `/lecturer/dashboard` | LecturerController.dashboard() | Thống kê riêng |

### **Admin Routes** (`/admin/**`)
| Method | Endpoint | Handler | Use Case |
|--------|----------|---------|----------|
| GET | `/admin/users` | AdminUserController.list() | Danh sách user |
| GET | `/admin/users/new` | AdminUserController.form() | Form tạo user |
| POST | `/admin/users` | AdminUserController.create() | Tạo user mới |
| POST | `/admin/users/{id}/lock` | AdminUserController.lock() | Khóa tài khoản |
| GET | `/admin/equipment` | AdminEquipmentController.list() | Danh sách thiết bị |
| POST | `/admin/equipment` | AdminEquipmentController.create() | Thêm thiết bị |
| PUT | `/admin/equipment/{id}` | AdminEquipmentController.update() | Sửa thiết bị |
| GET | `/admin/dispatch` | AdminDispatchController.queue() | Hàng đợi phiếu mượn |
| POST | `/admin/dispatch/{recordId}/approve` | AdminDispatchController.approve() | Phê duyệt phiếu |
| POST | `/admin/dispatch/{recordId}/reject` | AdminDispatchController.reject() | Từ chối phiếu |
| GET | `/admin/dashboard` | AdminDashboardController.dashboard() | Thống kê hệ thống |

### **Common Routes** (Authenticated)
| Method | Endpoint | Handler | View/Response |
|--------|----------|---------|---------------|
| GET | `/dashboard` | DashboardController | Redirect to role-specific dashboard |
| GET | `/profile` | ProfileController.view() | Xem hồ sơ |
| POST | `/profile/update` | ProfileController.update() | Cập nhật hồ sơ |
| GET | `/auth/logout` | Spring Security | Xóa session + redirect /auth/login |

---

## 12. 🚀 Hướng dẫn chạy project

### **Bước 1: Chuẩn bị môi trường**

**Yêu cầu hệ thống:**
- Java 21+ (với virtual threads)
- MySQL 8.0+ (running trên port 3306)
- Gradle 8.x (included: gradlew)
- IDE: IntelliJ IDEA hoặc VS Code

**Kiểm tra cài đặt:**
```bash
java -version           # Phải là 21+
mysql --version         # Phải là 8.0+
./gradlew --version     # Phải là 8.x
```

### **Bước 2: Clone dự án**
```bash
git clone https://github.com/your-username/Smart_Academic_Lab_Support_Platform.git
cd Smart_Academic_Lab_Support_Platform
```

### **Bước 3: Cấu hình Database**

**Option A: Tự động (Recommended)**
- Hệ thống tự create database (cấu hình `createDatabaseIfNotExist=true`)
- Đảm bảo MySQL đang chạy

**Option B: Thủ công**
```sql
-- Login MySQL
mysql -u root -p

-- Tạo database
CREATE DATABASE salsp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user & grant permission
CREATE USER 'salsp_user'@'localhost' IDENTIFIED BY 'salsp_password';
GRANT ALL PRIVILEGES ON salsp.* TO 'salsp_user'@'localhost';
FLUSH PRIVILEGES;
```

### **Bước 4: Cấu hình application.properties**

Edit file `src/main/resources/application.properties`:
```properties
# Server
server.port=8080
server.servlet.context-path=/

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/salsp?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=yourPassword

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate     # Hoặc: create-drop (dev), update (prod)
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.defer-datasource-initialization=false

# Thymeleaf
spring.thymeleaf.cache=false               # Dev mode

# Logging
logging.level.root=INFO
logging.level.com.rikkei.salsp=DEBUG       # Project logs
```

### **Bước 5: Chạy ứng dụng**

**Option A: Gradle (Command Line)**
```bash
# Linux/Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

**Option B: IDE (IntelliJ IDEA)**
1. Right-click `Application.java` class
2. Select "Run 'Application.main()'"
3. Hoặc nhấn `Shift + F10`

**Option C: Build JAR & Run**
```bash
./gradlew build
java -jar build/libs/SALSP-0.0.1-SNAPSHOT.jar
```

### **Bước 6: Truy cập ứng dụng**
```
URL: http://localhost:8080
```

**Nếu thấy trang login → OK, ứng dụng đang chạy!**

### **Troubleshooting**

| Lỗi | Nguyên nhân | Giải pháp |
|-----|------------|----------|
| `Connection refused` | MySQL không chạy | Khởi động MySQL service |
| `Table doesn't exist` | Schema chưa init | Check `schema.sql` đã chạy |
| `Unknown database salsp` | Database chưa tạo | Set `createDatabaseIfNotExist=true` |
| `Port 8080 already in use` | Port bị chiếm | Đổi port ở `application.properties` |

---

## 13. 🔑 Tài khoản demo

Sử dụng các tài khoản sau để test:

| Vai trò | Email | Password | Ghi chú |
|--------|-------|----------|--------|
| **Admin** | `admin@salsp.edu.vn` | `admin123` | Full system access |
| **Giảng viên** | `lecturer01@salsp.edu.vn` | `123456` | CNIT Department |
| **Sinh viên** | `student01@salsp.edu.vn` | `123456` | Year 3, No. 1 |

**Demo Flow:**
1. Login với `student01@salsp.edu.vn`
2. Đặt lịch cố vấn (chọn lecturer01, khung giờ trống)
3. Logout & login với `lecturer01@salsp.edu.vn`
4. Xem booking pending, phê duyệt
5. Đánh giá buổi học
6. Login với `admin@salsp.edu.vn`
7. Xem stats dashboard

---

## 14. 💡 Điểm nổi bật kỹ thuật

### 1. **Pessimistic Locking (Tránh Race Condition)**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT e FROM Equipment e WHERE e.id = :id")
Optional<Equipment> findByIdWithLock(@Param("id") Long id);
```
**Problem:** 2 sinh viên cùng mượn → dữ liệu inventory sai
**Solution:** Khóa row ở DB, request thứ 2 phải chờ

### 2. **@EntityGraph (Solve N+1 Query)**
```java
@EntityGraph(attributePaths = {"student", "lecturer", "profile"})
Page<MentoringSession> findAll(Pageable pageable);
```
**Benefit:** 1 query JOIN thay vì N query

### 3. **Slot Conflict Detection Algorithm**
```java
// Check: start_time < other.endTime AND end_time > other.startTime
boolean conflict = sessionRepository.existsConflict(
    lecturerId, date, startTime, endTime
);
```

### 4. **Transaction Management (@Transactional)**
```java
@Transactional
public void createBooking(...) {
    // If exception thrown → rollback ALL
    // If success → auto commit
}
```

### 5. **Soft Delete Pattern**
```java
@Column(nullable = false)
private boolean deleted = false;

// Query: SELECT * FROM equipments WHERE deleted = false
```
**Benefit:** Audit trail, undo-able, no FK violations

### 6. **Custom Exception Handling**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handle(BusinessException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
```

### 7. **Role-Based Access Control (RBAC)**
```java
@GetMapping("/admin/**")
.hasRole("ADMIN")

@GetMapping("/lecturer/**")
.hasRole("LECTURER")
```

### 8. **DTO Pattern (Security + Flexibility)**
- Separate API contract from DB schema
- Hide sensitive fields (password hash)
- MapStruct for conversion

---

## 15. 🔮 Ghi chú bảo trì & phát triển tương lai

### **Tính năng đã hoàn thành** ✅
- Authentication & authorization (Form-based)
- Booking conflict detection
- Equipment inventory management
- Academic evaluation system
- Borrowing record management
- Dashboard statistics
- Role-based access control

### **Tính năng đang phát triển** ⚠️
- Report generation (PDF export)
- Equipment category management
- Department CRUD

### **Tính năng chưa làm** ❌
- Email notifications (async)
- Automated overdue reminders  
- Export history to Excel
- SMS alerts
- API REST (non-Thymeleaf)

### **Giải pháp mở rộng tương lai:**
1. **Microservices**: Tách Booking + Inventory thành riêng biệt
2. **JWT + OAuth2**: Thay thế form-login
3. **WebSocket**: Real-time notifications
4. **Docker**: containerization
5. **CI/CD**: GitHub Actions → AWS deployment
6. **Caching**: Redis cho session + frequently accessed data
7. **Message Queue**: RabbitMQ/Kafka for async tasks
8. **API Gateway**: Kong/Spring Cloud Gateway

---

**Tác giả**: [Team SALSP - IT210 Project]  
**Ngôn ngữ**: Tiếng Việt + English (code comments)  
**License**: MIT (cho mục đích giáo dục)  
**Last Updated**: 2025-05-26

