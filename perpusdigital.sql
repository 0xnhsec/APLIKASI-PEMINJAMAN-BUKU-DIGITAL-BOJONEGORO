-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 02, 2026 at 01:21 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `perpusdigital`
--

-- --------------------------------------------------------

--
-- Table structure for table `buku`
--

CREATE TABLE `buku` (
  `Buku_id` int(11) NOT NULL,
  `Judul` varchar(200) NOT NULL,
  `Penulis` varchar(100) NOT NULL,
  `Penerbit` varchar(100) DEFAULT NULL,
  `Tahun_terbit` date DEFAULT NULL,
  `stok` int(225) NOT NULL DEFAULT 0,
  `kategori_id` int(11) NOT NULL,
  `rak_buku` varchar(255) NOT NULL,
  `imgsampul` varchar(255) DEFAULT NULL,
  `deskripsi` text DEFAULT NULL,
  `update_by` int(11) DEFAULT NULL,
  `update_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_by` int(11) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `buku`
--

INSERT INTO `buku` (`Buku_id`, `Judul`, `Penulis`, `Penerbit`, `Tahun_terbit`, `stok`, `kategori_id`, `rak_buku`, `imgsampul`, `deskripsi`, `update_by`, `update_at`, `created_by`, `created_at`) VALUES
(25, 'B.INDONESIA', 'bangkit', 'bangkit', '2026-02-02', 30, 2, '20', 'C:\\katbuk\\B.INDONESIA-merdeka.jpg', 'JADI LAH ANAK INDONESIA HEBAT', NULL, '2026-02-24 02:33:58', NULL, '2026-02-02 03:59:28'),
(47, 'memasak', 'kkk', 'kkk', '2026-02-02', 90, 20, '2', 'C:\\wallr3x\\pfp-opensrc\\anime1.jpg', 'jwenkjnfserjrsrigbjrigbjagjkb', 26, '2026-02-27 19:17:28', NULL, '2026-02-09 14:18:34'),
(48, 'BAHASA INGGRIS\r\n', 'BANGKIT', 'BANGKIT', '2026-02-11', 99, 2, 'RAK 3', 'C:\\katbuk\\B.INGGRIS-merdeka.jpg', 'BAHASA INGGRIS EZ', NULL, '2026-02-25 09:43:13', NULL, '2026-02-11 03:28:56'),
(49, 'MATEMATIKA', 'BANGKIT', 'BANGKIT', '2026-02-11', 81, 2, '2\r\n', 'C:\\katbuk\\MTK-merdeka.jpg', 'MATEMATIKA', NULL, '2026-02-25 09:40:48', NULL, '2026-02-11 14:13:43'),
(50, 'INFORMATIKA\n', 'BANGKIT', 'BANGKIT', '2026-02-12', 77, 2, 'Rak No.12', 'C:\\katbuk\\INFORMATIKA-merdeka.png', 'JADI HEMKER', NULL, '2026-02-27 19:05:19', NULL, '2026-02-12 02:12:59'),
(51, 'Sinar Gama', 'snxx', 'ardx', '2026-02-09', 7, 2, 'RAK No.3', 'C:\\katbuk\\HK.JPG', 'KROCO', 26, '2026-02-27 19:26:23', 26, '2026-02-27 17:06:42');

-- --------------------------------------------------------

--
-- Table structure for table `buku_item`
--

CREATE TABLE `buku_item` (
  `bukuitem_id` int(11) NOT NULL,
  `buku_id` int(11) NOT NULL,
  `kode_buku` varchar(20) NOT NULL,
  `status` enum('tersedia','dipinjam','rusak','hilang') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `update_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `buku_item`
--

INSERT INTO `buku_item` (`bukuitem_id`, `buku_id`, `kode_buku`, `status`, `created_at`, `update_at`) VALUES
(14, 25, 'N/A', 'rusak', '2026-02-01 17:00:00', '2026-02-27 19:20:30'),
(24, 47, 'BK-0005', 'dipinjam', '2026-02-09 19:40:13', '2026-02-25 09:43:47'),
(26, 47, 'BK-0007', 'tersedia', '2026-02-09 19:40:13', '2026-02-09 19:40:13'),
(27, 48, 'BK-0008', 'tersedia', '2026-02-10 17:00:00', '2026-02-25 09:40:48'),
(28, 48, 'BK-0009', 'tersedia', '2026-02-10 17:00:00', '2026-02-11 03:28:56'),
(29, 48, 'BK-0010', 'tersedia', '2026-02-10 17:00:00', '2026-02-11 03:28:56'),
(30, 48, 'BK-0011', 'tersedia', '2026-02-10 17:00:00', '2026-02-11 03:28:56'),
(31, 49, 'BK-0012', 'tersedia', '2026-02-10 17:00:00', '2026-02-11 14:13:43'),
(32, 49, 'BK-0013', 'tersedia', '2026-02-10 17:00:00', '2026-02-11 14:13:43'),
(33, 50, 'BK-0014', 'tersedia', '2026-02-11 17:00:00', '2026-02-12 02:12:59'),
(34, 50, 'BK-0015', 'tersedia', '2026-02-11 17:00:00', '2026-02-12 02:12:59'),
(35, 50, 'BK-0016', 'tersedia', '2026-02-11 17:00:00', '2026-02-12 02:12:59'),
(36, 50, 'BK-0017', 'tersedia', '2026-02-11 17:00:00', '2026-02-12 02:12:59'),
(37, 50, 'BK-0018', 'tersedia', '2026-02-11 17:00:00', '2026-02-12 02:12:59'),
(38, 51, 'BK-0019', 'tersedia', '2026-02-08 17:00:00', '2026-02-27 17:06:42'),
(39, 51, 'BK-0020', 'tersedia', '2026-02-08 17:00:00', '2026-02-27 19:26:23'),
(40, 51, 'BK-0021', 'tersedia', '2026-02-08 17:00:00', '2026-02-27 17:06:42'),
(41, 51, 'BK-0022', 'tersedia', '2026-02-08 17:00:00', '2026-02-27 17:06:42'),
(42, 51, 'BK-0023', 'tersedia', '2026-02-08 17:00:00', '2026-02-27 17:06:42'),
(43, 51, 'BK-0024', 'tersedia', '2026-02-08 17:00:00', '2026-02-27 19:26:23'),
(44, 51, 'BK-0025', 'tersedia', '2026-02-08 17:00:00', '2026-02-27 19:26:23'),
(45, 47, 'BK-0026', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(46, 47, 'BK-0027', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(47, 47, 'BK-0028', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(48, 47, 'BK-0029', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(49, 47, 'BK-0030', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(50, 47, 'BK-0031', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(51, 47, 'BK-0032', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(52, 47, 'BK-0033', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(53, 47, 'BK-0034', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(54, 47, 'BK-0035', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(55, 47, 'BK-0036', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(56, 47, 'BK-0037', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(57, 47, 'BK-0038', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(58, 47, 'BK-0039', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(59, 47, 'BK-0040', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(60, 47, 'BK-0041', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(61, 47, 'BK-0042', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(62, 47, 'BK-0043', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(63, 47, 'BK-0044', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(64, 47, 'BK-0045', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(65, 47, 'BK-0046', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(66, 47, 'BK-0047', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(67, 47, 'BK-0048', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(68, 47, 'BK-0049', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(69, 47, 'BK-0050', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(70, 47, 'BK-0051', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(71, 47, 'BK-0052', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(72, 47, 'BK-0053', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(73, 47, 'BK-0054', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(74, 47, 'BK-0055', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(75, 47, 'BK-0056', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(76, 47, 'BK-0057', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(77, 47, 'BK-0058', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(78, 47, 'BK-0059', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(79, 47, 'BK-0060', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(80, 47, 'BK-0061', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(81, 47, 'BK-0062', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(82, 47, 'BK-0063', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(83, 47, 'BK-0064', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(84, 47, 'BK-0065', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(85, 47, 'BK-0066', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(86, 47, 'BK-0067', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(87, 47, 'BK-0068', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(88, 47, 'BK-0069', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(89, 47, 'BK-0070', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(90, 47, 'BK-0071', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(91, 47, 'BK-0072', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(92, 47, 'BK-0073', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(93, 47, 'BK-0074', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(94, 47, 'BK-0075', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(95, 47, 'BK-0076', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(96, 47, 'BK-0077', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(97, 47, 'BK-0078', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(98, 47, 'BK-0079', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(99, 47, 'BK-0080', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(100, 47, 'BK-0081', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(101, 47, 'BK-0082', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(102, 47, 'BK-0083', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(103, 47, 'BK-0084', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(104, 47, 'BK-0085', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(105, 47, 'BK-0086', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(106, 47, 'BK-0087', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(107, 47, 'BK-0088', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(108, 47, 'BK-0089', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(109, 47, 'BK-0090', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(110, 47, 'BK-0091', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(111, 47, 'BK-0092', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(112, 47, 'BK-0093', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(113, 47, 'BK-0094', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(114, 47, 'BK-0095', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(115, 47, 'BK-0096', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(116, 47, 'BK-0097', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(117, 47, 'BK-0098', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(118, 47, 'BK-0099', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(119, 47, 'BK-0100', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(120, 47, 'BK-0101', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(121, 47, 'BK-0102', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(122, 47, 'BK-0103', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(123, 47, 'BK-0104', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(124, 47, 'BK-0105', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(125, 47, 'BK-0106', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(126, 47, 'BK-0107', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(127, 47, 'BK-0108', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(128, 47, 'BK-0109', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28'),
(129, 47, 'BK-0110', 'tersedia', '2026-02-27 19:17:28', '2026-02-27 19:17:28');

-- --------------------------------------------------------

--
-- Table structure for table `kategori`
--

CREATE TABLE `kategori` (
  `kategori_id` int(11) NOT NULL,
  `name_kategori` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `created_by` int(11) DEFAULT NULL,
  `update_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `update_by` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `kategori`
--

INSERT INTO `kategori` (`kategori_id`, `name_kategori`, `created_at`, `created_by`, `update_at`, `update_by`) VALUES
(1, 'FIKSI', '2026-01-16 17:00:00', NULL, '2026-02-24 02:27:53', NULL),
(2, 'SEKOLAH', '2026-01-16 17:00:00', NULL, '2026-02-24 02:27:48', NULL),
(4, 'Referensi', '2026-01-16 17:00:00', NULL, NULL, NULL),
(5, 'Makalah', '2026-01-16 17:00:00', NULL, '2026-01-28 01:53:22', NULL),
(9, 'Matematik', '2026-01-26 17:00:00', NULL, '2026-01-28 01:53:38', NULL),
(10, 'MTK', '2026-01-26 17:00:00', NULL, NULL, NULL),
(14, 'CERITA RAKYAT', '2026-02-09 04:22:15', NULL, '2026-02-09 04:22:52', NULL),
(15, 'IPAS', '2026-02-11 03:27:25', NULL, '2026-02-27 17:02:35', 26),
(17, 'KIK', '2026-02-12 02:11:56', NULL, '2026-02-12 02:11:56', NULL),
(19, 'PENTEST', '2026-02-27 19:11:31', 26, '2026-02-27 19:11:54', 26),
(20, 'ayam', '2026-02-27 19:11:44', 26, '2026-02-27 19:11:44', 26);

-- --------------------------------------------------------

--
-- Table structure for table `peminjaman`
--

CREATE TABLE `peminjaman` (
  `peminjaman_id` int(11) NOT NULL,
  `kode_peminjaman` varchar(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `buku_id` int(11) NOT NULL,
  `kd_bk1` varchar(9) DEFAULT NULL,
  `kd_bk2` varchar(9) DEFAULT NULL,
  `kd_bk3` varchar(9) DEFAULT NULL,
  `jumlah_pinjam` int(11) NOT NULL DEFAULT 1,
  `tanggal_pinjam` date NOT NULL,
  `tanggal_kembali` date DEFAULT NULL,
  `Status` enum('pending','dipinjam','selesai','ditolak','diterima','diperpanjang') NOT NULL DEFAULT 'pending',
  `denda` int(11) DEFAULT 0,
  `bayar` int(255) DEFAULT NULL,
  `kembali` int(255) DEFAULT NULL,
  `total` int(255) DEFAULT NULL,
  `catatan` varchar(255) DEFAULT NULL,
  `catatan_pengajuan` varchar(225) DEFAULT NULL,
  `update_by` int(11) DEFAULT NULL,
  `update_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_by` int(11) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `riwayat_peminjaman`
--

CREATE TABLE `riwayat_peminjaman` (
  `riwayat_id` int(11) NOT NULL,
  `peminjaman_id` varchar(20) NOT NULL,
  `kode_peminjaman` varchar(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `buku_id` int(11) NOT NULL,
  `kd_bk1` varchar(9) DEFAULT NULL,
  `kd_bk2` varchar(9) DEFAULT NULL,
  `kd_bk3` varchar(9) DEFAULT NULL,
  `jumlah_pinjam` int(11) NOT NULL DEFAULT 1,
  `tanggal_pinjam` date NOT NULL,
  `tanggal_kembali` date DEFAULT NULL,
  `Status` enum('pending','dipinjam','selesai','ditolak','diterima','diperpanjang') NOT NULL DEFAULT 'pending',
  `denda` int(11) DEFAULT 0,
  `bayar` int(11) DEFAULT NULL,
  `kembali` int(11) DEFAULT NULL,
  `total` int(11) DEFAULT NULL,
  `catatan` varchar(255) DEFAULT NULL,
  `catatan_pengajuan` varchar(255) DEFAULT NULL,
  `update_by` int(11) DEFAULT NULL,
  `update_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `riwayat_peminjaman`
--

INSERT INTO `riwayat_peminjaman` (`riwayat_id`, `peminjaman_id`, `kode_peminjaman`, `user_id`, `buku_id`, `kd_bk1`, `kd_bk2`, `kd_bk3`, `jumlah_pinjam`, `tanggal_pinjam`, `tanggal_kembali`, `Status`, `denda`, `bayar`, `kembali`, `total`, `catatan`, `catatan_pengajuan`, `update_by`, `update_at`, `created_by`, `created_at`) VALUES
(5, '44', 'PJM-260210-000-18', 18, 47, NULL, NULL, NULL, 1, '2026-02-10', '2026-02-17', 'selesai', 0, NULL, NULL, NULL, NULL, NULL, NULL, '2026-02-10 00:56:37', 18, '2026-02-09 22:21:45'),
(6, '51', 'PJM-260211-003-18', 18, 47, 'BK-0004', 'BK-0008', 'N/A', 2, '2026-02-11', '2026-02-12', 'selesai', 0, 0, 0, 0, 'belum selesai membaca', 'belum selesai membaca', 14, '2026-02-11 17:14:47', 18, '2026-02-11 13:09:08'),
(7, '53', 'PJM-260212-001-18', 18, 47, 'BK-0004', 'BK-0005', 'BK-0006', 3, '2026-02-02', '2026-02-11', 'selesai', 1000, 2000, 1000, 1000, '', 'belum selesai dibaca', 14, '2026-02-12 02:19:11', 18, '2026-02-12 02:10:19'),
(8, '56', 'PJM-260212-002-25', 25, 48, 'BK-0008', 'N/A', 'N/A', 1, '2026-02-12', '2026-02-24', 'selesai', 0, 0, 0, 0, 'OKE', 'wetrtyt', 14, '2026-02-12 03:38:03', 25, '2026-02-12 02:29:11'),
(9, '57', 'PJM-260212-003-25', 25, 49, 'BK-0008', 'N/A', 'N/A', 1, '2026-02-12', '2026-02-19', 'selesai', 6000, 0, 0, 0, '', NULL, 14, '2026-02-25 09:40:39', 25, '2026-02-12 02:29:20'),
(10, '62', 'PJM-260227-000-27', 27, 50, 'BK-0004', 'N/A', 'N/A', 3, '2026-02-27', '2026-03-06', 'selesai', 0, 1000000000, 991000000, 9000000, '', NULL, 26, '2026-02-27 17:42:38', 27, '2026-02-27 16:57:21'),
(11, '63', 'PJM-260228-000-27', 27, 51, 'BK-0020', 'BK-0025', 'BK-0024', 3, '2026-02-28', '2026-03-12', 'selesai', 0, 10000, 1000, 9000, '', 'org kaya', 26, '2026-02-27 19:26:14', 27, '2026-02-27 19:02:31');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `user_id` int(11) NOT NULL,
  `nomor` varchar(255) NOT NULL,
  `password` varchar(8) NOT NULL,
  `fullname` varchar(100) NOT NULL,
  `role` enum('admin','user') NOT NULL DEFAULT 'user',
  `status` enum('guru','siswa','pengunjung') NOT NULL DEFAULT 'siswa',
  `alamat` varchar(255) DEFAULT NULL,
  `telp` varchar(15) DEFAULT NULL,
  `email` varchar(225) DEFAULT NULL,
  `update_by` int(11) DEFAULT NULL,
  `update_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_by` int(11) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`user_id`, `nomor`, `password`, `fullname`, `role`, `status`, `alamat`, `telp`, `email`, `update_by`, `update_at`, `created_by`, `created_at`) VALUES
(24, '87654321', '12345678', 'pino', 'admin', 'guru', 'jl.pendidikan', '096243216547', 'pino@gmail.com', NULL, '2026-02-12 01:17:24', NULL, '2026-02-12 01:17:24'),
(26, '1234567890', 'admin', 'admin', 'admin', 'guru', 'aaaaaaaaaaaaaa', '12345678990', 'admin@gmail.com', NULL, '2026-02-26 14:12:20', NULL, '2026-02-26 14:12:20'),
(27, '11111111', 'user', 'userpro', 'user', 'siswa', 'kkkk', '99999', 'ayambakar@gmail.com', NULL, '2026-02-27 16:54:04', NULL, '2026-02-27 16:54:04');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `buku`
--
ALTER TABLE `buku`
  ADD PRIMARY KEY (`Buku_id`),
  ADD KEY `kategori_id` (`kategori_id`),
  ADD KEY `update_by` (`update_by`),
  ADD KEY `created_by` (`created_by`);

--
-- Indexes for table `buku_item`
--
ALTER TABLE `buku_item`
  ADD PRIMARY KEY (`bukuitem_id`),
  ADD UNIQUE KEY `kode_buku` (`kode_buku`),
  ADD KEY `buku_id` (`buku_id`);

--
-- Indexes for table `kategori`
--
ALTER TABLE `kategori`
  ADD PRIMARY KEY (`kategori_id`),
  ADD UNIQUE KEY `name_kategori` (`name_kategori`),
  ADD KEY `created_by` (`created_by`),
  ADD KEY `update_by` (`update_by`);

--
-- Indexes for table `peminjaman`
--
ALTER TABLE `peminjaman`
  ADD PRIMARY KEY (`peminjaman_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `buku_id` (`buku_id`),
  ADD KEY `update_by` (`update_by`),
  ADD KEY `created_by` (`created_by`);

--
-- Indexes for table `riwayat_peminjaman`
--
ALTER TABLE `riwayat_peminjaman`
  ADD PRIMARY KEY (`riwayat_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`user_id`),
  ADD KEY `update_by` (`update_by`),
  ADD KEY `created_by` (`created_by`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `buku`
--
ALTER TABLE `buku`
  MODIFY `Buku_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=52;

--
-- AUTO_INCREMENT for table `buku_item`
--
ALTER TABLE `buku_item`
  MODIFY `bukuitem_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=130;

--
-- AUTO_INCREMENT for table `kategori`
--
ALTER TABLE `kategori`
  MODIFY `kategori_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `peminjaman`
--
ALTER TABLE `peminjaman`
  MODIFY `peminjaman_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=65;

--
-- AUTO_INCREMENT for table `riwayat_peminjaman`
--
ALTER TABLE `riwayat_peminjaman`
  MODIFY `riwayat_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `buku`
--
ALTER TABLE `buku`
  ADD CONSTRAINT `buku_ibfk_1` FOREIGN KEY (`kategori_id`) REFERENCES `kategori` (`kategori_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `buku_ibfk_2` FOREIGN KEY (`update_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `buku_ibfk_3` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `buku_item`
--
ALTER TABLE `buku_item`
  ADD CONSTRAINT `buku_item_ibfk_1` FOREIGN KEY (`buku_id`) REFERENCES `buku` (`Buku_id`);

--
-- Constraints for table `kategori`
--
ALTER TABLE `kategori`
  ADD CONSTRAINT `kategori_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `kategori_ibfk_2` FOREIGN KEY (`update_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `peminjaman`
--
ALTER TABLE `peminjaman`
  ADD CONSTRAINT `peminjaman_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `peminjaman_ibfk_2` FOREIGN KEY (`buku_id`) REFERENCES `buku` (`Buku_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `peminjaman_ibfk_3` FOREIGN KEY (`update_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `peminjaman_ibfk_4` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `user`
--
ALTER TABLE `user`
  ADD CONSTRAINT `user_ibfk_1` FOREIGN KEY (`update_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `user_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
