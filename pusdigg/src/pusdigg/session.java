/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pusdigg;

/**
 * Kelas untuk menyimpan dan mengelola data sesi pengguna yang sedang login.
 * <p>
 * Menggunakan pola <b>Singleton static</b> — data sesi tersimpan selama
 * aplikasi berjalan dan dapat diakses dari semua form tanpa membuat objek baru.
 * Sesi akan dihapus saat pengguna melakukan logout.
 * </p>
 *
 * @author dppra
 * @version 1.0
 * @since 2026
 */
public class session {

    /** ID unik pengguna yang sedang login (Primary Key dari tabel user) */
    private static String U_id;

    /** Username (NISN/NIP/NIK) pengguna yang sedang login */
    private static String U_username;

    /** Nomor identitas pengguna yang sedang login */
    private static String U_nomor;

    /** Status pengguna: guru, siswa, atau pengunjung */
    private static String U_status;

    /**
     * Mengambil ID pengguna yang sedang login.
     * @return ID pengguna sebagai String, atau {@code null} jika belum login
     */
    public static String getU_id() {
        return U_id;
    }

    /**
     * Menyimpan ID pengguna ke sesi setelah login berhasil.
     * @param U_id ID pengguna dari database
     */
    public static void setU_id(String U_id) {
        session.U_id = U_id;
    }

    /**
     * Mengambil username pengguna yang sedang login.
     * @return Username pengguna, atau {@code null} jika belum login
     */
    public static String getU_username() {
        return U_username;
    }

    /**
     * Menyimpan username pengguna ke sesi setelah login berhasil.
     * @param U_username Username (NISN/NIP/NIK) pengguna
     */
    public static void setU_username(String U_username) {
        session.U_username = U_username;
    }

    /**
     * Mengambil nomor identitas pengguna yang sedang login.
     * @return Nomor identitas pengguna, atau {@code null} jika belum login
     */
    public static String getU_nomor() {
        return U_nomor;
    }

    /**
     * Menyimpan nomor identitas pengguna ke sesi.
     * @param U_nomor Nomor identitas pengguna
     */
    public static void setU_nomor(String U_nomor) {
        session.U_nomor = U_nomor;
    }

    /**
     * Mengambil status pengguna yang sedang login.
     * @return Status pengguna (guru/siswa/pengunjung), atau {@code null} jika belum login
     */
    public static String getU_status() {
        return U_status;
    }

    /**
     * Menyimpan status pengguna ke sesi.
     * @param U_status Status pengguna: "guru", "siswa", atau "pengunjung"
     */
    public static void setU_status(String U_status) {
        session.U_status = U_status;
    }

    /**
     * Menghapus semua data sesi pengguna (logout).
     * <p>
     * Setelah method ini dipanggil, semua data sesi akan bernilai {@code null}.
     * Biasanya dipanggil saat pengguna menekan tombol Logout.
     * </p>
     */
    public static void logout() {
        U_id = null;
        U_username = null;
        U_nomor = null;
        U_status = null;
    }
}
