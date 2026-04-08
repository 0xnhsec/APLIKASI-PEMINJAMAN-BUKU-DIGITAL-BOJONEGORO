/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pusdigg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author dppra
 */
public class dashboard_admin extends javax.swing.JPanel {

    /**
     * Creates new form dashboard_admin
     */
    Connection conn = null;
    ResultSet rs =null;
    PreparedStatement pst = null;
    
    private DefaultTableModel model;
    private String sql;
    String usernamee = session.getU_username();
    public dashboard_admin() {
        initComponents();
        username.setText("ANDA LOGIN SEBAGAI"+usernamee);
        
        conn = koneksi.koneksi.koneksiDB();
        
        try {
        Statement st = conn.createStatement();

        // Hitung Users
        ResultSet rsUsers = st.executeQuery("SELECT COUNT(*) AS total_users FROM user");
        if(rsUsers.next()) {
            int totalUsers = rsUsers.getInt("total_users");  // ambil value sebagai int
            total_user.setText("" + totalUsers); // baru digabung dengan string
        }
        
        // Hitung Buku
        ResultSet rsBuku = st.executeQuery("SELECT COUNT(*) AS total_buku FROM buku");
        if(rsBuku.next()) {
            int totalBuku = rsBuku.getInt("total_buku");  // ambil value sebagai int
            total_buku.setText("" + totalBuku); // baru digabung dengan string
        }
        
        // Hitung Kategori Buku
        ResultSet rsKategori = st.executeQuery("SELECT COUNT(*) AS total_buku FROM kategori");
        if(rsKategori.next()) {
            int totalKategori = rsKategori.getInt("total_buku");  // ambil value sebagai int
            total_kategori.setText("" + totalKategori); // baru digabung dengan string
        }     
        
        // Hitung Peminjaman Sedang Berlangsung
        ResultSet rsSedang = st.executeQuery(
            "SELECT COUNT(*) AS total_sedang FROM peminjaman WHERE status in ('pending','dipinjam','ditolak','diterima','diperpanjang')"
        );
        if (rsSedang.next()) {
            int totalSedang = rsSedang.getInt("total_sedang");
            peminjaman_sedang.setText("" + totalSedang); // set ke label/komponen
        }

        // Hitung Peminjaman Selesai
        ResultSet rsSelesai = st.executeQuery(
            "SELECT COUNT(*) AS total_selesai FROM peminjaman WHERE status = 'selesai'"
        );
        if (rsSelesai.next()) {
            int totalSelesai = rsSelesai.getInt("total_selesai");
            peminjaman_selesai.setText("" + totalSelesai); // set ke label/komponen
        }

            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        
        // Panggil Data Peminjaman yang terbaru
        model = new DefaultTableModel();
        jTable1.setModel(model);

        // Tambahkan kolom tabel
        model.addColumn("ID"); // ⬅ peminjaman_id (hidden)
        model.addColumn("Kode Peminjaman");
        model.addColumn("Name");
        model.addColumn("Judul Buku");
        model.addColumn("Kategori Buku");
        model.addColumn("Jumlah Pinjam");
        model.addColumn("Tanggal Pinjam");
        model.addColumn("Tanggal Kembali");
        model.addColumn("Status");

        // sembunyikan kolom ID
        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        getData();
    }

    void getData() {
        model.setRowCount(0);

        try {
          String sql =
            "SELECT p.peminjaman_id, p.kode_peminjaman, u.fullname, b.judul, " +
            "k.name_kategori, p.jumlah_pinjam, p.tanggal_pinjam, " +
            "p.tanggal_kembali, p.status " +
            "FROM peminjaman p " +
            "JOIN user u ON p.user_id = u.user_id " +
            "JOIN buku b ON p.Buku_id = b.Buku_id " +
            "JOIN kategori k ON b.kategori_id = k.kategori_id " +
            "WHERE p.status <> 'selesai'" +
            "ORDER BY p.tanggal_pinjam DESC";



            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
               Object[] row = {
                rs.getInt("peminjaman_id"),
                rs.getString("kode_peminjaman"),
                rs.getString("fullname"),
                rs.getString("judul"),
                rs.getString("name_kategori"),
                rs.getInt("jumlah_pinjam"),
                rs.getDate("tanggal_pinjam"),
                rs.getDate("tanggal_kembali"),
                rs.getString("status")
            };
            model.addRow(row);

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        total_user = new javax.swing.JLabel();
        total_buku = new javax.swing.JLabel();
        total_kategori = new javax.swing.JLabel();
        peminjaman_sedang = new javax.swing.JLabel();
        peminjaman_selesai = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(197, 216, 157));
        setMinimumSize(new java.awt.Dimension(1860, 1080));
        setRequestFocusEnabled(false);
        setVerifyInputWhenFocusTarget(false);

        jPanel1.setBackground(new java.awt.Color(197, 216, 157));
        jPanel1.setPreferredSize(new java.awt.Dimension(1860, 1080));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("Peminjaman Selesai");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1310, 820, -1, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel4.setText("Peminjaman berlangsung");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 800, 140, 30));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setText("Total Kategori Buku :");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 810, -1, 20));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setText("Total Buku :");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 800, -1, -1));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Total User :");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 800, -1, -1));

        total_user.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
        jPanel1.add(total_user, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 870, 139, 115));

        total_buku.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
        jPanel1.add(total_buku, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 860, 144, 115));

        total_kategori.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
        jPanel1.add(total_kategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 870, 162, 114));

        peminjaman_sedang.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
        jPanel1.add(peminjaman_sedang, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 880, 140, 100));

        peminjaman_selesai.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
        jPanel1.add(peminjaman_selesai, new org.netbeans.lib.awtextra.AbsoluteConstraints(1320, 880, 150, 104));

        username.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        username.setText("Username");
        jPanel1.add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(1020, 630, -1, -1));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 260, 1300, 230));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/1860/Dashboard.png"))); // NOI18N
        jLabel7.setText("jLabel7");
        jLabel7.setMaximumSize(new java.awt.Dimension(1860, 1080));
        jLabel7.setPreferredSize(new java.awt.Dimension(1860, 1080));
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1900, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel peminjaman_sedang;
    private javax.swing.JLabel peminjaman_selesai;
    private javax.swing.JLabel total_buku;
    private javax.swing.JLabel total_kategori;
    private javax.swing.JLabel total_user;
    private javax.swing.JLabel username;
    // End of variables declaration//GEN-END:variables
}
