/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.*;

/**
 * Kelas untuk mengelola koneksi ke database MySQL.
 * <p>
 * Kelas ini menyediakan koneksi ke database <b>perpusdigital</b>
 * yang digunakan oleh seluruh modul aplikasi Sistem Perpustakaan Digital (pusdigg).
 * </p>
 *
 * @author BOJSEC
 * @version 1.0
 * @since 2026
 */
public class koneksi {

    /**
     * Membuat koneksi ke database MySQL perpusdigital.
     * <p>
     * Menggunakan JDBC Driver {@code com.mysql.jdbc.Driver} untuk terhubung
     * ke database lokal. Jika koneksi gagal, akan menampilkan pesan error
     * melalui dialog.
     * </p>
     *
     * @return objek {@link java.sql.Connection} jika koneksi berhasil,
     *         {@code null} jika koneksi gagal
     */
    public static Connection koneksiDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver"); // memanggil driver JDBC
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost/perpusdigital", "root", ""
            );
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        return null;
    }
}