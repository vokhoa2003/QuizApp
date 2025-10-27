-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 23, 2025 lúc 02:10 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `quiz_system`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `account`
--

CREATE TABLE `account` (
  `id` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `role` enum('admin','student','teacher') DEFAULT NULL,
  `Status` enum('Active','Blocked') NOT NULL,
  `GoogleID` varchar(255) DEFAULT NULL,
  `CreateDate` datetime DEFAULT current_timestamp(),
  `UpdateDate` datetime DEFAULT current_timestamp(),
  `FullName` varchar(255) DEFAULT NULL,
  `Phone` varchar(255) DEFAULT NULL,
  `Address` varchar(255) DEFAULT NULL,
  `IdentityNumber` varchar(255) DEFAULT NULL,
  `BirthDate` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `admin`
--

CREATE TABLE `admin` (
  `Id` int(11) NOT NULL,
  `IdAccount` int(11) DEFAULT NULL,
  `CreateDate` datetime DEFAULT NULL,
  `UpdateDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `adminpermission`
--

CREATE TABLE `adminpermission` (
  `Id` int(11) NOT NULL,
  `IdAdmin` int(11) DEFAULT NULL,
  `IdPermission` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `answers`
--

CREATE TABLE `answers` (
  `id` int(11) NOT NULL,
  `QuestionId` int(11) DEFAULT NULL,
  `Answer` text NOT NULL,
  `IsCorrect` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `api_token`
--

CREATE TABLE `api_token` (
  `id` int(11) NOT NULL,
  `IdAccount` int(11) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `EndTime` datetime DEFAULT NULL,
  `CreateDate` datetime DEFAULT NULL,
  `UpdateDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `classes`
--

CREATE TABLE `classes` (
  `Id` int(10) UNSIGNED NOT NULL,
  `Name` varchar(255) NOT NULL,
  `Description` text DEFAULT NULL,
  `CreateDate` datetime NOT NULL DEFAULT current_timestamp(),
  `UpdateDate` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `customer`
--

CREATE TABLE `customer` (
  `Id` int(11) NOT NULL,
  `IdAccount` int(11) DEFAULT NULL,
  `CreateDate` datetime DEFAULT NULL,
  `UpdateDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `exams`
--

CREATE TABLE `exams` (
  `id` int(11) NOT NULL,
  `ClassId` int(10) UNSIGNED DEFAULT NULL,
  `ExamName` text NOT NULL,
  `NumberQuestion` int(11) NOT NULL,
  `Description` text DEFAULT NULL,
  `CreateDate` datetime DEFAULT current_timestamp(),
  `UpdateDate` datetime DEFAULT NULL,
  `PublishDate` datetime DEFAULT NULL,
  `ExpireDate` datetime DEFAULT NULL,
  `TeacherId` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `exam_answers`
--

CREATE TABLE `exam_answers` (
  `id` int(11) NOT NULL,
  `StudentId` int(11) DEFAULT NULL,
  `exam_questions_id` int(11) DEFAULT NULL,
  `QuestionId` int(11) DEFAULT NULL,
  `AnswerId` int(11) DEFAULT NULL,
  `AttemptId` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `exam_attempts`
--

CREATE TABLE `exam_attempts` (
  `id` int(11) NOT NULL,
  `ExamId` int(11) NOT NULL,
  `StudentId` int(11) NOT NULL,
  `StartTime` datetime DEFAULT current_timestamp(),
  `EndTime` datetime DEFAULT NULL,
  `SubmitTime` datetime DEFAULT NULL,
  `Status` enum('in_progress','submitted','expired') DEFAULT 'in_progress'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `exam_questions`
--

CREATE TABLE `exam_questions` (
  `id` int(11) NOT NULL,
  `ExamId` int(11) DEFAULT NULL,
  `ClassId` int(10) UNSIGNED DEFAULT NULL,
  `QuestionId` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `exam_results`
--

CREATE TABLE `exam_results` (
  `id` int(11) NOT NULL,
  `ExamId` int(11) DEFAULT NULL,
  `StudentId` int(11) DEFAULT NULL,
  `Score` decimal(5,2) DEFAULT NULL,
  `SubmittedDate` datetime DEFAULT current_timestamp(),
  `AttemptId` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `grade`
--

CREATE TABLE `grade` (
  `Id` int(11) NOT NULL,
  `StudentId` int(11) NOT NULL,
  `Subject` varchar(100) NOT NULL,
  `Score` decimal(5,2) DEFAULT NULL,
  `CreateDate` datetime DEFAULT NULL,
  `UpdateDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `permission`
--

CREATE TABLE `permission` (
  `id` int(11) NOT NULL,
  `PermissionName` varchar(255) DEFAULT NULL,
  `Description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `questions`
--

CREATE TABLE `questions` (
  `id` int(11) NOT NULL,
  `TestNumber` int(11) DEFAULT NULL,
  `ClassId` int(10) UNSIGNED DEFAULT NULL,
  `Question` text NOT NULL,
  `CreateDate` datetime DEFAULT current_timestamp(),
  `UpdateDate` datetime DEFAULT NULL,
  `PublishDate` datetime DEFAULT NULL,
  `ExpireDate` datetime DEFAULT NULL,
  `TeacherId` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `student`
--

CREATE TABLE `student` (
  `Id` int(11) NOT NULL,
  `IdAccount` int(11) NOT NULL,
  `Name` varchar(100) NOT NULL,
  `ClassId` int(10) UNSIGNED DEFAULT NULL,
  `CreateDate` datetime DEFAULT NULL,
  `UpdateDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `teacher`
--

CREATE TABLE `teacher` (
  `Id` int(11) NOT NULL,
  `IdAccount` int(11) NOT NULL,
  `Name` varchar(100) NOT NULL,
  `ClassId` int(10) UNSIGNED DEFAULT NULL,
  `CreateDate` datetime DEFAULT NULL,
  `UpdateDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `teacher_student`
--

CREATE TABLE `teacher_student` (
  `Id` int(11) NOT NULL,
  `TeacherId` int(11) NOT NULL,
  `StudentId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `user_tokens`
--

CREATE TABLE `user_tokens` (
  `id` int(11) UNSIGNED NOT NULL,
  `google_id` varchar(255) NOT NULL,
  `refresh_token` text NOT NULL,
  `expires_at` datetime NOT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `account`
--
ALTER TABLE `account`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_email` (`email`),
  ADD KEY `GoogleID` (`GoogleID`);

--
-- Chỉ mục cho bảng `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `idx_admin_account` (`IdAccount`);

--
-- Chỉ mục cho bảng `adminpermission`
--
ALTER TABLE `adminpermission`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `idx_adminpermission_admin` (`IdAdmin`),
  ADD KEY `idx_adminpermission_permission` (`IdPermission`);

--
-- Chỉ mục cho bảng `answers`
--
ALTER TABLE `answers`
  ADD PRIMARY KEY (`id`),
  ADD KEY `QuestionId` (`QuestionId`);

--
-- Chỉ mục cho bảng `api_token`
--
ALTER TABLE `api_token`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_token_account` (`IdAccount`);

--
-- Chỉ mục cho bảng `classes`
--
ALTER TABLE `classes`
  ADD PRIMARY KEY (`Id`),
  ADD UNIQUE KEY `Name` (`Name`);

--
-- Chỉ mục cho bảng `customer`
--
ALTER TABLE `customer`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `idx_customer_account` (`IdAccount`);

--
-- Chỉ mục cho bảng `exams`
--
ALTER TABLE `exams`
  ADD PRIMARY KEY (`id`),
  ADD KEY `ClassId` (`ClassId`),
  ADD KEY `TeacherId` (`TeacherId`);

--
-- Chỉ mục cho bảng `exam_answers`
--
ALTER TABLE `exam_answers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_question` (`StudentId`,`QuestionId`),
  ADD UNIQUE KEY `unique_attempt_question` (`AttemptId`,`QuestionId`),
  ADD KEY `AnswerId` (`AnswerId`),
  ADD KEY `StudentId` (`StudentId`),
  ADD KEY `exam_questions_id` (`exam_questions_id`),
  ADD KEY `idx_examanswers_student_attempt` (`StudentId`,`AttemptId`),
  ADD KEY `idx_examanswers_question_attempt` (`QuestionId`,`AttemptId`);

--
-- Chỉ mục cho bảng `exam_attempts`
--
ALTER TABLE `exam_attempts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `ExamId` (`ExamId`),
  ADD KEY `StudentId` (`StudentId`),
  ADD UNIQUE KEY `unique_exam_student` (`ExamId`,`StudentId`);

--
-- Chỉ mục cho bảng `exam_questions`
--
ALTER TABLE `exam_questions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `ExamId` (`ExamId`),
  ADD KEY `QuestionId` (`QuestionId`),
  ADD KEY `ClassId` (`ClassId`);

--
-- Chỉ mục cho bảng `exam_results`
--
ALTER TABLE `exam_results`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ExamId` (`ExamId`,`StudentId`),
  ADD KEY `AttemptId` (`AttemptId`);

--
-- Chỉ mục cho bảng `grade`
--
ALTER TABLE `grade`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `idx_grade_student` (`StudentId`);

--
-- Chỉ mục cho bảng `permission`
--
ALTER TABLE `permission`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `questions`
--
ALTER TABLE `questions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `ClassId` (`ClassId`);

--
-- Chỉ mục cho bảng `student`
--
ALTER TABLE `student`
  ADD PRIMARY KEY (`Id`),
  ADD UNIQUE KEY `IdAccount_3` (`IdAccount`,`ClassId`),
  ADD KEY `IdAccount` (`IdAccount`),
  ADD KEY `ClassId` (`ClassId`);

--
-- Chỉ mục cho bảng `teacher`
--
ALTER TABLE `teacher`
  ADD PRIMARY KEY (`Id`),
  ADD UNIQUE KEY `unique_teacher_account_class_check` (`IdAccount`,`ClassId`),
  ADD KEY `ClassId` (`ClassId`);

--
-- Chỉ mục cho bảng `teacher_student`
--
ALTER TABLE `teacher_student`
  ADD PRIMARY KEY (`Id`),
  ADD KEY `idx_teacher_student_teacher` (`TeacherId`),
  ADD KEY `idx_teacher_student_student` (`StudentId`);

--
-- Chỉ mục cho bảng `user_tokens`
--
ALTER TABLE `user_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `google_id` (`google_id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `account`
--
ALTER TABLE `account`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `admin`
--
ALTER TABLE `admin`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `adminpermission`
--
ALTER TABLE `adminpermission`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `answers`
--
ALTER TABLE `answers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `api_token`
--
ALTER TABLE `api_token`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `classes`
--
ALTER TABLE `classes`
  MODIFY `Id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `customer`
--
ALTER TABLE `customer`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `exams`
--
ALTER TABLE `exams`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `exam_answers`
--
ALTER TABLE `exam_answers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `exam_attempts`
--
ALTER TABLE `exam_attempts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `exam_questions`
--
ALTER TABLE `exam_questions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `exam_results`
--
ALTER TABLE `exam_results`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `grade`
--
ALTER TABLE `grade`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `permission`
--
ALTER TABLE `permission`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `questions`
--
ALTER TABLE `questions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `student`
--
ALTER TABLE `student`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `teacher`
--
ALTER TABLE `teacher`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `teacher_student`
--
ALTER TABLE `teacher_student`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `user_tokens`
--
ALTER TABLE `user_tokens`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `admin`
--
ALTER TABLE `admin`
  ADD CONSTRAINT `admin_ibfk_1` FOREIGN KEY (`IdAccount`) REFERENCES `account` (`id`);

--
-- Các ràng buộc cho bảng `adminpermission`
--
ALTER TABLE `adminpermission`
  ADD CONSTRAINT `adminpermission_ibfk_1` FOREIGN KEY (`IdAdmin`) REFERENCES `admin` (`Id`),
  ADD CONSTRAINT `adminpermission_ibfk_2` FOREIGN KEY (`IdPermission`) REFERENCES `permission` (`id`);

--
-- Các ràng buộc cho bảng `answers`
--
ALTER TABLE `answers`
  ADD CONSTRAINT `answers_ibfk_1` FOREIGN KEY (`QuestionId`) REFERENCES `questions` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `api_token`
--
ALTER TABLE `api_token`
  ADD CONSTRAINT `api_token_ibfk_1` FOREIGN KEY (`IdAccount`) REFERENCES `account` (`id`);

--
-- Các ràng buộc cho bảng `customer`
--
ALTER TABLE `customer`
  ADD CONSTRAINT `customer_ibfk_1` FOREIGN KEY (`IdAccount`) REFERENCES `account` (`id`);

--
-- Các ràng buộc cho bảng `exams`
--
ALTER TABLE `exams`
  ADD CONSTRAINT `exams_ibfk_1` FOREIGN KEY (`TeacherId`) REFERENCES `teacher` (`Id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_exams_classid` FOREIGN KEY (`ClassId`) REFERENCES `classes` (`Id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `exam_answers`
--
ALTER TABLE `exam_answers`
  ADD CONSTRAINT `exam_answers_ibfk_2` FOREIGN KEY (`QuestionId`) REFERENCES `questions` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `exam_answers_ibfk_3` FOREIGN KEY (`AnswerId`) REFERENCES `answers` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `exam_answers_ibfk_4` FOREIGN KEY (`exam_questions_id`) REFERENCES `exam_questions` (`id`),
  ADD CONSTRAINT `exam_answers_ibfk_5` FOREIGN KEY (`AttemptId`) REFERENCES `exam_attempts` (`id`),
  ADD CONSTRAINT `exam_answers_ibfk_6` FOREIGN KEY (`StudentId`) REFERENCES `student` (`Id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `exam_attempts`
--
ALTER TABLE `exam_attempts`
  ADD CONSTRAINT `exam_attempts_ibfk_1` FOREIGN KEY (`ExamId`) REFERENCES `exams` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `exam_attempts_ibfk_2` FOREIGN KEY (`StudentId`) REFERENCES `student` (`Id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `exam_questions`
--
ALTER TABLE `exam_questions`
  ADD CONSTRAINT `exam_questions_ibfk_1` FOREIGN KEY (`ExamId`) REFERENCES `exams` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `exam_questions_ibfk_2` FOREIGN KEY (`QuestionId`) REFERENCES `questions` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `exam_questions_ibfk_3` FOREIGN KEY (`ClassId`) REFERENCES `classes` (`Id`);

--
-- Các ràng buộc cho bảng `exam_results`
--
ALTER TABLE `exam_results`
  ADD CONSTRAINT `exam_results_ibfk_1` FOREIGN KEY (`ExamId`) REFERENCES `exams` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `exam_results_ibfk_2` FOREIGN KEY (`AttemptId`) REFERENCES `exam_attempts` (`id`);

--
-- Các ràng buộc cho bảng `grade`
--
ALTER TABLE `grade`
  ADD CONSTRAINT `grade_ibfk_1` FOREIGN KEY (`StudentId`) REFERENCES `student` (`Id`);

--
-- Các ràng buộc cho bảng `questions`
--
ALTER TABLE `questions`
  ADD CONSTRAINT `fk_questions_classid` FOREIGN KEY (`ClassId`) REFERENCES `classes` (`Id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `student`
--
ALTER TABLE `student`
  ADD CONSTRAINT `fk_student_classid` FOREIGN KEY (`ClassId`) REFERENCES `classes` (`Id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `student_ibfk_1` FOREIGN KEY (`IdAccount`) REFERENCES `account` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `teacher`
--
ALTER TABLE `teacher`
  ADD CONSTRAINT `teacher_ibfk_1` FOREIGN KEY (`ClassId`) REFERENCES `classes` (`Id`) ON DELETE CASCADE,
  ADD CONSTRAINT `teacher_ibfk_2` FOREIGN KEY (`IdAccount`) REFERENCES `account` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `teacher_student`
--
ALTER TABLE `teacher_student`
  ADD CONSTRAINT `fk_teacher_student_student` FOREIGN KEY (`StudentId`) REFERENCES `student` (`Id`),
  ADD CONSTRAINT `fk_teacher_student_teacher` FOREIGN KEY (`TeacherId`) REFERENCES `teacher` (`Id`);

--
-- Các ràng buộc cho bảng `user_tokens`
--
ALTER TABLE `user_tokens`
  ADD CONSTRAINT `user_tokens_ibfk_1` FOREIGN KEY (`google_id`) REFERENCES `account` (`GoogleID`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
