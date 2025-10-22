-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 21, 2025 lúc 03:11 PM
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

--
-- Đang đổ dữ liệu cho bảng `account`
--


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

--
-- Đang đổ dữ liệu cho bảng `admin`
--

INSERT INTO `admin` (`Id`, `IdAccount`, `CreateDate`, `UpdateDate`) VALUES
(1, 2, '2025-10-14 14:11:59', '2025-10-14 14:11:59');

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

--
-- Đang đổ dữ liệu cho bảng `answers`
--

INSERT INTO `answers` (`id`, `QuestionId`, `Answer`, `IsCorrect`) VALUES
(281, 111, 'Paris', 1),
(282, 111, 'London', 0),
(283, 111, 'Rome', 0),
(284, 111, 'Berlin', 0),
(285, 112, '4', 1),
(286, 112, '3', 0),
(287, 112, '5', 0),
(288, 112, '22', 0),
(289, 113, 'Thomas Edison', 1),
(290, 113, 'Albert Einstein', 0),
(291, 113, 'Isaac Newton', 0),
(292, 113, 'Nikola Tesla', 0),
(293, 114, 'H2O', 1),
(294, 114, 'CO2', 0),
(295, 114, 'NaCl', 0),
(296, 114, 'O2', 0),
(297, 115, '300000 km/s', 1),
(298, 115, '150000 km/s', 0),
(299, 115, '100000 km/s', 0),
(300, 115, '250000 km/s', 0),
(301, 116, '366', 1),
(302, 116, '365', 0),
(303, 116, '360', 0),
(304, 116, '364', 0),
(305, 117, 'Mặt Trời', 1),
(306, 117, 'Mặt Trăng', 0),
(307, 117, 'Sao Hỏa', 0),
(308, 117, 'Sao Kim', 0),
(309, 118, 'Châu Á', 1),
(310, 118, 'Châu Âu', 0),
(311, 118, 'Châu Phi', 0),
(312, 118, 'Châu Mỹ', 0),
(313, 119, 'Sông Nile', 1),
(314, 119, 'Sông Amazon', 0),
(315, 119, 'Sông Hồng', 0),
(316, 119, 'Sông Mekong', 0),
(317, 120, '1000', 1),
(318, 120, '100', 0),
(319, 120, '10000', 0),
(320, 120, '10', 0),
(321, 121, 'Nguyễn Du', 1),
(322, 121, 'Nguyễn Trãi', 0),
(323, 121, 'Hồ Xuân Hương', 0),
(324, 121, 'Ngô Tất Tố', 0),
(325, 122, 'Java', 1),
(326, 122, 'Python', 0),
(327, 122, 'C#', 0),
(328, 122, 'Swift', 0),
(329, 123, 'HyperText Markup Language', 1),
(330, 123, 'HighText Markdown Language', 0),
(331, 123, 'HyperText Making Logic', 0),
(332, 123, 'Hyper Transfer Markup Language', 0),
(333, 124, 'Định dạng giao diện web', 1),
(334, 124, 'Lưu dữ liệu', 0),
(335, 124, 'Kết nối server', 0),
(336, 124, 'Chạy logic', 0),
(337, 125, 'Bộ xử lý trung tâm', 1),
(338, 125, 'Thẻ nhớ', 0),
(339, 125, 'Ổ cứng', 0),
(340, 125, 'Màn hình', 0),
(341, 126, 'Lưu trữ tạm thời', 1),
(342, 126, 'Lưu trữ vĩnh viễn', 0),
(343, 126, 'Xử lý đồ họa', 0),
(344, 126, 'Kết nối mạng', 0),
(345, 127, 'JavaScript', 1),
(346, 127, 'JavaSystem', 0),
(347, 127, 'JustScript', 0),
(348, 127, 'JsonScript', 0),
(349, 128, 'Giao tiếp giữa các hệ thống', 1),
(350, 128, 'Lưu dữ liệu', 0),
(351, 128, 'Thiết kế giao diện', 0),
(352, 128, 'Nén ảnh', 0),
(353, 129, 'HyperText Transfer Protocol', 1),
(354, 129, 'HighText Transfer Platform', 0),
(355, 129, 'Hyper Transfer Text Protocol', 0),
(356, 129, 'Host Transfer Type Protocol', 0),
(357, 130, 'Ngôn ngữ truy vấn cơ sở dữ liệu', 1),
(358, 130, 'Hệ điều hành', 0),
(359, 130, 'Trình duyệt', 0),
(360, 130, 'Công cụ đồ họa', 0),
(361, 131, 'Phần mềm truy cập Internet', 1),
(362, 131, 'Hệ điều hành', 0),
(363, 131, 'Phần mềm đồ họa', 0),
(364, 131, 'Trình phát nhạc', 0),
(365, 132, 'Chọn phần tử HTML để áp dụng style', 1),
(366, 132, 'Công cụ gỡ lỗi', 0),
(367, 132, 'Thư viện JavaScript', 0),
(368, 132, 'Thẻ HTML', 0),
(369, 133, 'Có mã hóa bảo mật SSL/TLS', 1),
(370, 133, 'Nhanh hơn', 0),
(371, 133, 'Chạy offline', 0),
(372, 133, 'Không cần Internet', 0),
(373, 134, 'Trao đổi dữ liệu giữa client và server', 1),
(374, 134, 'Tạo bảng', 0),
(375, 134, 'Vẽ đồ họa', 0),
(376, 134, 'Quản lý bộ nhớ', 0),
(377, 135, 'Hệ thống quản lý phiên bản mã nguồn', 1),
(378, 135, 'Ngôn ngữ lập trình', 0),
(379, 135, 'CSDL', 0),
(380, 135, 'Framework', 0),
(381, 136, 'Môi trường phát triển tích hợp', 1),
(382, 136, 'Trình duyệt web', 0),
(383, 136, 'Hệ điều hành', 0),
(384, 136, 'Plugin CSS', 0),
(385, 137, 'Khung làm việc hỗ trợ lập trình', 1),
(386, 137, 'Trình phát nhạc', 0),
(387, 137, 'Công cụ nén file', 0),
(388, 137, 'Công cụ test', 0),
(389, 138, 'Tìm và sửa lỗi chương trình', 1),
(390, 138, 'Viết tài liệu', 0),
(391, 138, 'Thiết kế UI', 0),
(392, 138, 'Cài đặt hệ điều hành', 0),
(393, 139, 'Địa chỉ của tài nguyên trên Internet', 1),
(394, 139, 'Công cụ tìm kiếm', 0),
(395, 139, 'Ngôn ngữ lập trình', 0),
(396, 139, 'Định dạng ảnh', 0),
(397, 140, 'Xử lý logic và dữ liệu', 1),
(398, 140, 'Thiết kế giao diện', 0),
(399, 140, 'Quảng cáo', 0),
(400, 140, 'Bảo trì phần cứng', 0);

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

--
-- Đang đổ dữ liệu cho bảng `classes`
--

INSERT INTO `classes` (`Id`, `Name`, `Description`, `CreateDate`, `UpdateDate`) VALUES
(3, 'Lớp A', NULL, '2025-10-06 16:57:20', '2025-10-06 16:57:20'),
(4, 'Lớp B', NULL, '2025-10-06 16:57:20', '2025-10-06 16:57:20'),
(5, 'Lớp C', NULL, '2025-10-20 10:38:39', '2025-10-20 10:38:39'),
(6, 'Lớp D', NULL, '2025-10-20 10:38:39', '2025-10-20 10:38:39'),
(7, 'Lớp E', NULL, '2025-10-20 10:38:54', '2025-10-20 10:38:54'),
(8, 'Lớp F', NULL, '2025-10-20 10:38:54', '2025-10-20 10:38:54');

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

--
-- Đang đổ dữ liệu cho bảng `exams`
--

INSERT INTO `exams` (`id`, `ClassId`, `ExamName`, `NumberQuestion`, `Description`, `CreateDate`, `UpdateDate`, `PublishDate`, `ExpireDate`, `TeacherId`) VALUES
(3, 3, 'A Test', 20, 'Đề thi của lớp A', '2025-10-20 13:59:38', '2025-10-20 15:03:59', '2025-10-20 13:43:57', '2025-10-24 13:43:57', 1);

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

--
-- Đang đổ dữ liệu cho bảng `exam_answers`
--

INSERT INTO `exam_answers` (`id`, `StudentId`, `exam_questions_id`, `QuestionId`, `AnswerId`, `AttemptId`) VALUES
(304, 46, NULL, 111, 281, 2),
(305, 46, NULL, 112, 285, 2),
(306, 46, NULL, 113, 289, 2),
(307, 46, NULL, 114, 293, 2),
(308, 46, NULL, 115, NULL, 2),
(309, 46, NULL, 116, NULL, 2),
(310, 46, NULL, 117, NULL, 2),
(311, 46, NULL, 118, NULL, 2),
(312, 46, NULL, 119, NULL, 2),
(313, 46, NULL, 120, NULL, 2),
(314, 46, NULL, 121, NULL, 2),
(315, 46, NULL, 122, NULL, 2),
(316, 46, NULL, 123, NULL, 2),
(317, 46, NULL, 124, NULL, 2),
(318, 46, NULL, 125, NULL, 2),
(319, 46, NULL, 126, NULL, 2),
(320, 46, NULL, 127, NULL, 2),
(321, 46, NULL, 128, NULL, 2),
(322, 46, NULL, 129, NULL, 2),
(323, 46, NULL, 130, NULL, 2);

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

--
-- Đang đổ dữ liệu cho bảng `exam_attempts`
--

INSERT INTO `exam_attempts` (`id`, `ExamId`, `StudentId`, `StartTime`, `EndTime`, `SubmitTime`, `Status`) VALUES
(2, 3, 46, '2025-10-21 12:02:10', NULL, NULL, 'in_progress');

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

--
-- Đang đổ dữ liệu cho bảng `exam_results`
--

INSERT INTO `exam_results` (`id`, `ExamId`, `StudentId`, `Score`, `SubmittedDate`, `AttemptId`) VALUES
(6, 3, 46, 2.00, '2025-10-21 12:13:11', 2);

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

--
-- Đang đổ dữ liệu cho bảng `questions`
--

INSERT INTO `questions` (`id`, `TestNumber`, `ClassId`, `Question`, `CreateDate`, `UpdateDate`, `PublishDate`, `ExpireDate`, `TeacherId`) VALUES
(111, 4, 3, 'Thủ đô của Pháp là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(112, 2, 3, '2 + 2 bằng bao nhiêu?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(113, 9, 3, 'Ai phát minh ra bóng đèn?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(114, 6, 3, 'Công thức hóa học của nước là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(115, 4, 3, 'Tốc độ ánh sáng gần bằng bao nhiêu?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(116, 1, 3, 'Năm nhuận có bao nhiêu ngày?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(117, 3, 3, 'Trái Đất quay quanh gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(118, 10, 3, 'Việt Nam nằm ở châu lục nào?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(119, 3, 3, 'Sông dài nhất thế giới là?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(120, 2, 3, '1 km bằng bao nhiêu mét?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(121, 9, 3, 'Ai là tác giả “Truyện Kiều”?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(122, 1, 3, 'Ngôn ngữ lập trình chính cho Android?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(123, 9, 3, 'HTML viết tắt của?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(124, 10, 3, 'CSS dùng để làm gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(125, 1, 3, 'CPU là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(126, 4, 3, 'RAM có chức năng gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(127, 7, 3, 'JS viết tắt của?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(128, 4, 3, 'API dùng để?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(129, 8, 3, 'HTTP viết tắt của?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(130, 6, 3, 'SQL là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(131, 6, 3, 'Trình duyệt web là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(132, 1, 3, 'CSS selector là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(133, 5, 3, 'HTTPS khác HTTP ở điểm nào?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(134, 4, 3, 'JSON dùng để?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(135, 3, 3, 'Git là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(136, 1, 3, 'IDE là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(137, 9, 3, 'Framework là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(138, 9, 3, 'Debug nghĩa là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(139, 7, 3, 'URL là gì?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4),
(140, 9, 3, 'Backend chịu trách nhiệm về?', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-20 08:29:42', '2025-10-24 08:29:42', 4);

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

--
-- Đang đổ dữ liệu cho bảng `student`
--

INSERT INTO `student` (`Id`, `IdAccount`, `Name`, `ClassId`, `CreateDate`, `UpdateDate`) VALUES
(46, 6, 'Đăng Khoa Võ Phạm', 3, '2025-10-19 15:18:40', '2025-10-19 15:18:40'),
(47, 9, 'CamTuNguyen Alexia', NULL, '2025-10-19 16:09:30', '2025-10-19 16:09:30'),
(51, 6, '', 4, NULL, NULL),
(52, 6, 'Võ Phạm Đăng Khoa', 5, NULL, NULL);

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

--
-- Đang đổ dữ liệu cho bảng `teacher`
--

INSERT INTO `teacher` (`Id`, `IdAccount`, `Name`, `ClassId`, `CreateDate`, `UpdateDate`) VALUES
(1, 4, 'Đăng Khoa Võ Phạm', 3, '2025-10-06 16:13:47', '2025-10-06 16:13:47'),
(44, 4, 'Võ Khoa Teacher', 4, '2025-10-21 14:54:26', '2025-10-21 14:54:26');

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
-- Đang đổ dữ liệu cho bảng `user_tokens`
--

INSERT INTO `user_tokens` (`id`, `google_id`, `refresh_token`, `expires_at`, `created_at`, `updated_at`) VALUES
(5, '106924843908906768657', 'google_1760934194028', '2025-10-20 12:23:14', '2025-09-10 17:34:30', '2025-10-20 11:23:14'),
(74, '112137929294811663800', 'google_1761034235847', '2025-10-21 16:10:35', '2025-09-21 13:21:23', '2025-10-21 15:10:35'),
(190, '111186508142808754018', 'google_1761025501019', '2025-10-21 13:45:01', '2025-10-19 13:39:58', '2025-10-21 12:45:01'),
(203, '108932244388986679360', 'ya29.A0AQQ_BDTDRscVX4CcXj22FUG9-fy1V2rWGGtEh25cT-n6HEMYPynb8jM-umLL_pJhKIuGZmvSQQm7dDnKEvkFGDnCLhrqkarUuwI7-9_hOetwUo21VfsZQH-5fdKMRyk-5WK5GjaYHmX9w_LJYqd7PZzJ2a_FOIMDV_anZV8Q2a0CbQaeXolSDotp3sKOemzu5p5Thgx1i9-QZMkZVaXMXxz7Qhp9N73gXbhbSeyeqMgU1p8X8H7vLfq5V2sNyyKTJ4KDFRKxB-IIYcbkUKpePfxOUBwaCgYKAY4SARYSFQHGX2MiNL6LnnhWC3ZbyIYd4I3ETw0290', '2025-10-19 17:09:28', '2025-10-19 16:09:30', '2025-10-19 16:09:30');

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
  ADD KEY `StudentId` (`StudentId`);

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT cho bảng `admin`
--
ALTER TABLE `admin`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `adminpermission`
--
ALTER TABLE `adminpermission`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `answers`
--
ALTER TABLE `answers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=401;

--
-- AUTO_INCREMENT cho bảng `api_token`
--
ALTER TABLE `api_token`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `classes`
--
ALTER TABLE `classes`
  MODIFY `Id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT cho bảng `customer`
--
ALTER TABLE `customer`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `exams`
--
ALTER TABLE `exams`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `exam_answers`
--
ALTER TABLE `exam_answers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=349;

--
-- AUTO_INCREMENT cho bảng `exam_attempts`
--
ALTER TABLE `exam_attempts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `exam_questions`
--
ALTER TABLE `exam_questions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `exam_results`
--
ALTER TABLE `exam_results`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=141;

--
-- AUTO_INCREMENT cho bảng `student`
--
ALTER TABLE `student`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=53;

--
-- AUTO_INCREMENT cho bảng `teacher`
--
ALTER TABLE `teacher`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=45;

--
-- AUTO_INCREMENT cho bảng `teacher_student`
--
ALTER TABLE `teacher_student`
  MODIFY `Id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `user_tokens`
--
ALTER TABLE `user_tokens`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=281;

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
  ADD CONSTRAINT `exam_answers_ibfk_5` FOREIGN KEY (`AttemptId`) REFERENCES `exam_attempts` (`id`);

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
