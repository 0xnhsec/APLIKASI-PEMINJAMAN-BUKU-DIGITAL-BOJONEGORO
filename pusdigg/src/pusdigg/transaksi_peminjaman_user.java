/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pusdigg;
import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import pusdigg.session;

/**
 *
 * @author ASUS vivobook
 */
public class transaksi_peminjaman_user extends javax.swing.JPanel {
Connection conn;
    PreparedStatement pst;
    ResultSet rs;
    DefaultTableModel model;

    String id = session.getU_id();
    String usernamee = session.getU_username();
    private boolean reminderSudahTampil = false; // ✅ INI YANG KURANG

    public transaksi_peminjaman_user() {
        initComponents();
     // ComboBox status
        jButton2.setEnabled(false);
        jComboBox1.removeAllItems();
        jComboBox1.addItem("Semua");
        jComboBox1.addItem("dipinjam");
        jComboBox1.addItem("diperpanjang");
        jComboBox1.addItem("diterima");
        jComboBox1.addItem("ditolak");
        jComboBox1.addItem("pending");

        id_user.setText("SELAMAT DATANG " + id);
        username.setText("ANDA LOGIN SEBAGAI " + usernamee);

        conn = koneksi.koneksi.koneksiDB();

   // ===== MODEL TABEL (11 KOLOM) =====
        model = new DefaultTableModel(new String[]{
            "Kode Peminjaman",   // 0
            "Kode Buku",         // 1
            "Username",          // 2
            "Judul Buku",        // 3
            "Kategori",          // 4
            "Jumlah",            // 5
            "Tanggal Pinjam",    // 6
            "Tanggal Kembali",   // 7
            "Status",            // 8
            "Denda",             // 9
            "Catatan"            // 10 (disembunyikan)
    }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    tblPeminjaman.setModel(model);

    // ===== RENDERER WARNA + ICON =====
    tblPeminjaman.setDefaultRenderer(Object.class,
    new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel c = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            c.setForeground(Color.BLACK);

            if (column == 7) { // tanggal kembali
                Object obj = table.getValueAt(row, 7);
                if (obj != null) {
                    Date tgl = (Date) obj;
                    LocalDate today = LocalDate.now();
                    LocalDate kembali = tgl.toLocalDate();

                   long selisih = ChronoUnit.DAYS.between(today, kembali);

                    if (selisih <= 0) {
                        c.setText("⏰ TELAT (" + kembali + ")");
                        c.setForeground(Color.RED);
                    } else if (selisih == 1) {
                        c.setText("⚠ BESOK (" + kembali + ")");
                        c.setForeground(Color.ORANGE);
                    } else {
                        c.setText(kembali.toString());
                    }

                }
            }
            return c;
        }
    });

    // ===== TOOLTIP SELURUH BARIS =====
    tblPeminjaman.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
        @Override
        public void mouseMoved(java.awt.event.MouseEvent e) {

            int row = tblPeminjaman.rowAtPoint(e.getPoint());
            if (row == -1) {
                tblPeminjaman.setToolTipText(null);
                return;
            }

            String status = tblPeminjaman
                    .getValueAt(row, 8)
                    .toString().toLowerCase();

            String judul = "";
            String isi = "";

           switch (status) {
                case "pending":
                    judul = "Status";
                    isi = "Menunggu validasi admin";
                    break;

                case "dipinjam":
                    judul = "Status";
                    isi = "Buku sedang dipinjam";
                    break;

                case "diterima":
                case "ditolak":
                case "diperpanjang":
                    judul = "Catatan";
                    isi = getIsi(tblPeminjaman.getValueAt(row, 10)); // ✅ dari DB
                    break;

                default:
                    tblPeminjaman.setToolTipText(null);
                    return;
                        }

            tblPeminjaman.setToolTipText(
                "<html><b>" + judul + ":</b><br>" + isi + "</html>"
            );
        }

        private String getIsi(Object obj) {
            return (obj == null || obj.toString().trim().isEmpty())
                    ? "-" : obj.toString();
        }
    });

   


       
        // TextField readonly
        jTextField2.setEditable(false);
        jTextField3.setEditable(false);
        jTextField4.setEditable(false);
        jTextField5.setEditable(false);

        // Event cari otomatis
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cariData();
            }
        });
        jComboBox1.addActionListener(evt -> cariData());

        // Ambil data awal
        getData();
        SwingUtilities.invokeLater(() -> {
        cekReminder();
    });
    }

  private void getData() {

    model.setRowCount(0);

    try {
        String sql =
    "SELECT p.kode_peminjaman, b.buku_id, u.username, b.judul, " +
    "k.name_kategori, p.jumlah_pinjam, p.tanggal_pinjam, " +
    "p.tanggal_kembali, p.status, p.denda, p.catatan " +
    "FROM peminjaman p " +
    "JOIN user u ON p.user_id = u.user_id " +
    "JOIN buku b ON p.buku_id = b.buku_id " +
    "JOIN kategori k ON b.kategori_id = k.kategori_id " +
    "WHERE p.user_id = ?";


        pst = conn.prepareStatement(sql);
        pst.setString(1, id);
        rs = pst.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("kode_peminjaman"),
                rs.getInt("buku_id"),
                rs.getString("username"),
                rs.getString("judul"),
                rs.getString("name_kategori"),
                rs.getInt("jumlah_pinjam"),
                rs.getDate("tanggal_pinjam"),
                rs.getDate("tanggal_kembali"),
                rs.getString("status"),
                rs.getString("denda"),
                rs.getString("catatan") // ✅ tampil di tabel
            });
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Gagal mengambil data:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}

    void bersih() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");
        txtJumlah.setText("");
    }
  
    // ================= HITUNG SELISIH HARI =================
    private long hitungHari(Date tglKembali) {
        if (tglKembali == null) return Long.MAX_VALUE;
        LocalDate today = LocalDate.now();
        LocalDate batas = tglKembali.toLocalDate();
        return ChronoUnit.DAYS.between(today, batas);
    }

  private void cekReminder() {

    if (reminderSudahTampil) return;

    for (int i = 0; i < model.getRowCount(); i++) {

        String status = model.getValueAt(i, 8).toString().toLowerCase();
        Date tglKembali = (Date) model.getValueAt(i, 7);

        if (!status.equals("dipinjam") || tglKembali == null) continue;

        long sisaHari = ChronoUnit.DAYS.between(
                LocalDate.now(),
                tglKembali.toLocalDate()
        );

        // 🔴 HARI INI LANGSUNG TELAT
        if (sisaHari <= 0) {
            reminderSudahTampil = true;
            JOptionPane.showMessageDialog(this,
                "⛔ TERLAMBAT!\n\nBatas pengembalian buku adalah HARI INI.\nSegera kembalikan buku!",
                "Pengembalian Terlambat",
                JOptionPane.ERROR_MESSAGE
            );
            break;
        }

        // 🟠 Sisa 1 hari
        if (sisaHari == 1) {
            reminderSudahTampil = true;
            JOptionPane.showMessageDialog(this,
                "⚠ PERINGATAN!\n\nBesok adalah batas pengembalian buku.\nSegera kembalikan untuk menghindari denda.",
                "Reminder Pengembalian",
                JOptionPane.WARNING_MESSAGE
            );
            break;
        }
    }
}

    // ================= CEK DISABLE PERPANJANGAN =================
    private void cekDisablePerpanjangan(int row) {
        String status = model.getValueAt(row, 8).toString().toLowerCase();
        btnPerpanjang.setEnabled(status.equals("dipinjam"));
    }

  private void cariData() {

    model.setRowCount(0);

    String keyword = jTextField1.getText().trim();
    String statusFilter = jComboBox1.getSelectedItem().toString();

    try {
        String sql =
    "SELECT p.kode_peminjaman, b.buku_id, u.username, b.judul, " +
    "k.name_kategori, p.jumlah_pinjam, p.tanggal_pinjam, " +
    "p.tanggal_kembali, p.status, p.denda, p.catatan " +
    "FROM peminjaman p " +
    "JOIN user u ON p.user_id = u.user_id " +
    "JOIN buku b ON p.buku_id = b.buku_id " +
    "JOIN kategori k ON b.kategori_id = k.kategori_id " +
    "WHERE p.user_id = ?";


        if (!keyword.isEmpty()) {
            sql += "AND b.judul LIKE ? ";
        }

        if (!statusFilter.equals("Semua")) {
            sql += "AND p.status = ? ";
        }

        pst = conn.prepareStatement(sql);

        int i = 1;
        pst.setString(i++, id);

        if (!keyword.isEmpty()) {
            pst.setString(i++, "%" + keyword + "%");
        }

        if (!statusFilter.equals("Semua")) {
            pst.setString(i++, statusFilter);
        }

        rs = pst.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("kode_peminjaman"),
                rs.getInt("buku_id"),
                rs.getString("username"),
                rs.getString("judul"),
                rs.getString("name_kategori"),
                rs.getInt("jumlah_pinjam"),
                rs.getDate("tanggal_pinjam"),
                rs.getDate("tanggal_kembali"),
                rs.getString("status"),
                rs.getString("denda"),
                rs.getString("catatan") 
            });
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Gagal mencari data:\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}

   private void resetForm() {
    jTextField2.setText("");
    jTextField3.setText("");
    jTextField4.setText("");
    txtJumlah.setText("");

    tblPeminjaman.clearSelection();
    btnPerpanjang.setEnabled(false);
    jButton2.setEnabled(false);
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<String>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPeminjaman = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<String>();
        jButton2 = new javax.swing.JButton();
        btnPerpanjang = new javax.swing.JButton();
        btnselesaidikembalikan = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        id_user = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        btn_refresh = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtJumlah = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jList1);

        setBackground(new java.awt.Color(197, 216, 157));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblPeminjaman.setModel(new javax.swing.table.DefaultTableModel(
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
        tblPeminjaman.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPeminjamanMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPeminjaman);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 48, 963, 405));

        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField1FocusGained(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });
        add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 10, 718, -1));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "status", " " }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(737, 10, 238, -1));

        jButton2.setText("batalkan peminjaman");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 629, -1, -1));

        btnPerpanjang.setText("Ajukan Perpanjangan pengembalian");
        btnPerpanjang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPerpanjangActionPerformed(evt);
            }
        });
        add(btnPerpanjang, new org.netbeans.lib.awtextra.AbsoluteConstraints(174, 629, -1, -1));

        btnselesaidikembalikan.setText("Selesai Dikembalikan");
        btnselesaidikembalikan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnselesaidikembalikanActionPerformed(evt);
            }
        });
        add(btnselesaidikembalikan, new org.netbeans.lib.awtextra.AbsoluteConstraints(425, 629, 206, -1));

        jLabel1.setText("Kode Peminjaman");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 496, -1, -1));

        jLabel2.setText("Kode Buku");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 566, -1, -1));

        jLabel3.setText("Judul Buku");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(267, 496, -1, -1));

        jLabel4.setText("Name Peminjam");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(267, 566, -1, -1));

        id_user.setText("jLabel5");
        add(id_user, new org.netbeans.lib.awtextra.AbsoluteConstraints(687, 487, 300, 25));

        username.setText("Username");
        add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(687, 519, 300, 29));

        btn_refresh.setText("Refresh");
        btn_refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_refreshActionPerformed(evt);
            }
        });
        add(btn_refresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(649, 629, -1, -1));
        add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 519, 128, -1));
        add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 589, 128, -1));
        add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(267, 522, 128, -1));
        add(jTextField5, new org.netbeans.lib.awtextra.AbsoluteConstraints(267, 589, 128, -1));

        jLabel5.setText("Jumlah Buku yang dipinjam");
        add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(441, 496, -1, -1));
        add(txtJumlah, new org.netbeans.lib.awtextra.AbsoluteConstraints(441, 519, 136, -1));

        jLabel6.setText("jLabel6");
        add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1920, 1080));
    }// </editor-fold>//GEN-END:initComponents

    private void btnPerpanjangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPerpanjangActionPerformed
int row = tblPeminjaman.getSelectedRow();
if (row == -1) {
    JOptionPane.showMessageDialog(this,
        "Pilih data peminjaman terlebih dahulu!");
    return;
}

// ambil data dari JTable
String kode = model.getValueAt(row, 0).toString();
String bukuId = model.getValueAt(row, 1).toString();
Date tglPinjam = (Date) model.getValueAt(row, 6);
String user = model.getValueAt(row, 2).toString();

// validasi status
String status = model.getValueAt(row, 8).toString().toLowerCase();
if (!status.equals("dipinjam")) {
    JOptionPane.showMessageDialog(this,
        "❌ Tidak bisa diajukan perpanjangan!");
    return;
}

// dialog alasan
JTextArea txtAlasan = new JTextArea(3, 20);
int result = JOptionPane.showConfirmDialog(
    this,
    new JScrollPane(txtAlasan),
    "Ajukan Perpanjangan",
    JOptionPane.OK_CANCEL_OPTION
);

if (result != JOptionPane.OK_OPTION) return;

String alasan = txtAlasan.getText().trim();
if (alasan.isEmpty()) {
    JOptionPane.showMessageDialog(this,
        "Alasan wajib diisi!");
    return;
}

// ===== SQL TANPA UBAH STRUKTUR DB =====
String sql =
    "UPDATE peminjaman SET status = ?, catatan_pengajuan = ? " +
    "WHERE kode_peminjaman = ? " +
    "AND buku_id = ? " +
    "AND tanggal_pinjam = ? " +
    "AND user_id = ? " +
    "AND status = 'dipinjam'";

try (Connection conn = koneksi.koneksi.koneksiDB();
     PreparedStatement ps = conn.prepareStatement(sql)) {

    ps.setString(1, "diperpanjang");
    ps.setString(2, alasan);
    ps.setString(3, kode);
    ps.setString(4, bukuId);
    ps.setDate(5, tglPinjam);
    ps.setString(6, session.getU_id());

    int affected = ps.executeUpdate();

    if (affected == 0) {
        JOptionPane.showMessageDialog(this,
            "⚠ Data tidak ditemukan atau sudah berubah.");
        return;
    }

    // update JTable (1 baris saja)
    model.setValueAt("diperpanjang", row, 8);
    btnPerpanjang.setEnabled(false);

    JOptionPane.showMessageDialog(this,
        "📨 Pengajuan perpanjangan dikirim.");

} catch (Exception e) {
    JOptionPane.showMessageDialog(this,
        e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
}




    }//GEN-LAST:event_btnPerpanjangActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

    int row = tblPeminjaman.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!");
        return;
    }

    String status = model.getValueAt(row, 8).toString().toLowerCase();
    if (!status.equals("pending")) {
        JOptionPane.showMessageDialog(this,
            "❌ Hanya peminjaman dengan status PENDING yang bisa dibatalkan.");
        return;
    }

    String kodePinjam = model.getValueAt(row, 0).toString();
    String bukuId = model.getValueAt(row, 1).toString();
    int jumlahLama = Integer.parseInt(model.getValueAt(row, 5).toString());

    JTextField txtJumlahBaru = new JTextField();

    Object[] pesan = {
        "Jumlah yang ingin dipertahankan:",
        txtJumlahBaru,
        "\nKosongkan jika ingin membatalkan seluruh peminjaman"
    };

    Object[] opsi = {"Ubah Jumlah", "Batalkan Semua", "Batal"};

    int pilih = JOptionPane.showOptionDialog(
        this,
        pesan,
        "Batalkan Peminjaman",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        opsi,
        opsi[0]
    );

    if (pilih == 2 || pilih == JOptionPane.CLOSED_OPTION) return;

    try (Connection conn = koneksi.koneksi.koneksiDB()) {

        // ================= BATALKAN SEMUA =================
        if (pilih == 1) {

            String sqlDelete = "DELETE FROM peminjaman WHERE kode_peminjaman = ?";
            PreparedStatement ps = conn.prepareStatement(sqlDelete);
            ps.setString(1, kodePinjam);
            ps.executeUpdate();

            String sqlStok =
                "UPDATE buku SET stok = stok + ? WHERE buku_id = ?";
            ps = conn.prepareStatement(sqlStok);
            ps.setInt(1, jumlahLama);
            ps.setString(2, bukuId);
            ps.executeUpdate();

            model.removeRow(row);

            JOptionPane.showMessageDialog(this,
                "✅ Peminjaman berhasil dibatalkan sepenuhnya.");

        }
        // ================= UBAH JUMLAH =================
        else if (pilih == 0) {

            if (txtJumlahBaru.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Jumlah tidak boleh kosong!");
                return;
            }

            int jumlahBaru = Integer.parseInt(txtJumlahBaru.getText());

            if (jumlahBaru <= 0 || jumlahBaru >= jumlahLama) {
                JOptionPane.showMessageDialog(this,
                    "Jumlah harus lebih kecil dari jumlah lama!");
                return;
            }

            int selisih = jumlahLama - jumlahBaru;

            String sqlUpdate =
                "UPDATE peminjaman SET jumlah_pinjam = ? WHERE kode_peminjaman = ?";
            PreparedStatement ps = conn.prepareStatement(sqlUpdate);
            ps.setInt(1, jumlahBaru);
            ps.setString(2, kodePinjam);
            ps.executeUpdate();

            String sqlStok =
                "UPDATE buku SET stok = stok + ? WHERE buku_id = ?";
            ps = conn.prepareStatement(sqlStok);
            ps.setInt(1, selisih);
            ps.setString(2, bukuId);
            ps.executeUpdate();

            model.setValueAt(jumlahBaru, row, 5);

            JOptionPane.showMessageDialog(this,
                "✏ Jumlah peminjaman berhasil diperbarui.");
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void tblPeminjamanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPeminjamanMouseClicked
int row = tblPeminjaman.getSelectedRow();
    if (row == -1) return;

    // isi field
    jTextField2.setText(model.getValueAt(row, 0).toString());
    jTextField3.setText(model.getValueAt(row, 1).toString());
    jTextField4.setText(model.getValueAt(row, 3).toString());
    jTextField5.setText(usernamee);
    txtJumlah.setText(model.getValueAt(row, 5).toString());

    String status = model.getValueAt(row, 8).toString().toLowerCase();

    // 🔒 LOGIKA TOMBOL
    jButton2.setEnabled(status.equals("pending"));
    btnPerpanjang.setEnabled(status.equals("dipinjam"));
           // TODO add your handling code here:
    }//GEN-LAST:event_tblPeminjamanMouseClicked

    private void btnselesaidikembalikanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnselesaidikembalikanActionPerformed
int row = tblPeminjaman.getSelectedRow();
if (row == -1) {
    JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!");
    return;
}

// ====== CEK STATUS ======
Object val = model.getValueAt(row, 8); // index 8 untuk Status di JTable
String status = (val != null) ? val.toString().trim().toLowerCase() : "";

if (!status.equals("selesai")) {
    JOptionPane.showMessageDialog(this,
        "Buku belum dikonfirmasi admin!\nStatus saat ini: " + status.toUpperCase());
    return;
}

int confirm = JOptionPane.showConfirmDialog(
    this,
    "Buku sudah selesai dikembalikan.\nKirim ke Riwayat Transaksi?",
    "Konfirmasi",
    JOptionPane.YES_NO_OPTION
);
if (confirm != JOptionPane.YES_OPTION) return;

// ====== DATABASE TRANSAKSI ======
Connection conn = null;
PreparedStatement pstSelect = null;
PreparedStatement pstInsert = null;
PreparedStatement pstDelete = null;
ResultSet rs = null;

try {
    conn = koneksi.koneksi.koneksiDB();
    if (conn == null) {
        JOptionPane.showMessageDialog(this, "Koneksi database gagal!");
        return;
    }

    conn.setAutoCommit(false); // mulai transaksi

    // ====== Ambil data lengkap dari DB berdasarkan kode_peminjaman ======
    String kodePeminjaman = model.getValueAt(row, 0).toString(); // kolom kode_peminjaman
    String sqlSelect = "SELECT * FROM peminjaman WHERE kode_peminjaman = ?";
    pstSelect = conn.prepareStatement(sqlSelect);
    pstSelect.setString(1, kodePeminjaman);
    rs = pstSelect.executeQuery();

    if (!rs.next()) {
        JOptionPane.showMessageDialog(this, "Data peminjaman tidak ditemukan di database!");
        return;
    }

    // Ambil semua field dari rs
    String peminjaman_id      = rs.getString("peminjaman_id");
    String user_id            = rs.getString("user_id");
    String buku_id            = rs.getString("buku_id");
    String kd_bk1             = rs.getString("kd_bk1");
    String kd_bk2             = rs.getString("kd_bk2");
    String kd_bk3             = rs.getString("kd_bk3");
    String jumlah_pinjam      = rs.getString("jumlah_pinjam");
    String tanggal_pinjam     = rs.getString("tanggal_pinjam");
    String tanggal_kembali    = rs.getString("tanggal_kembali");
    String Status             = rs.getString("Status");
    String denda              = rs.getString("denda");
    String bayar              = rs.getString("bayar");
    String kembali            = rs.getString("kembali");
    String total              = rs.getString("total");
    String catatan            = rs.getString("catatan");
    String catatan_pengajuan  = rs.getString("catatan_pengajuan");
    String update_by          = rs.getString("update_by");
    String update_at          = rs.getString("update_at");
    String created_by         = rs.getString("created_by");
    String created_at         = rs.getString("created_at");

    // ====== INSERT ke riwayat_peminjaman ======
    String sqlInsert = "INSERT INTO riwayat_peminjaman(" +
            "peminjaman_id,kode_peminjaman,user_id,buku_id,kd_bk1,kd_bk2,kd_bk3," +
            "jumlah_pinjam,tanggal_pinjam,tanggal_kembali,Status,denda,bayar,kembali,total," +
            "catatan,catatan_pengajuan,update_by,update_at,created_by,created_at) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    pstInsert = conn.prepareStatement(sqlInsert);
    pstInsert.setString(1, peminjaman_id);
    pstInsert.setString(2, kodePeminjaman);
    pstInsert.setString(3, user_id);
    pstInsert.setString(4, buku_id);
    pstInsert.setString(5, kd_bk1);
    pstInsert.setString(6, kd_bk2);
    pstInsert.setString(7, kd_bk3);
    pstInsert.setString(8, jumlah_pinjam);
    pstInsert.setString(9, tanggal_pinjam);
    pstInsert.setString(10, tanggal_kembali);
    pstInsert.setString(11, Status);
    pstInsert.setString(12, denda);
    pstInsert.setString(13, bayar);
    pstInsert.setString(14, kembali);
    pstInsert.setString(15, total);
    pstInsert.setString(16, catatan);
    pstInsert.setString(17, catatan_pengajuan);
    pstInsert.setString(18, update_by);
    pstInsert.setString(19, update_at);
    pstInsert.setString(20, created_by);
    pstInsert.setString(21, created_at);

    pstInsert.executeUpdate();

    // ====== DELETE dari peminjaman ======
    String sqlDelete = "DELETE FROM peminjaman WHERE peminjaman_id = ?";
    pstDelete = conn.prepareStatement(sqlDelete);
    pstDelete.setString(1, peminjaman_id);
    pstDelete.executeUpdate();

    conn.commit(); // commit transaksi

    // ====== UPDATE UI ======
    model.removeRow(row);
    jTextField2.setText("");
    jTextField3.setText("");
    jTextField4.setText("");
    jTextField5.setText("");

    // Reload JTable supaya data terbaru terlihat
   // loadPeminjaman(); 

    JOptionPane.showMessageDialog(this, "📚 Data berhasil dipindahkan ke Riwayat Transaksi.");

} catch (Exception e) {
    try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
    e.printStackTrace(); // tampilkan error di console
    JOptionPane.showMessageDialog(this, "Terjadi error: " + e.getMessage());
} finally {
    try { if (rs != null) rs.close(); } catch (Exception ex) {}
    try { if (pstSelect != null) pstSelect.close(); } catch (Exception ex) {}
    try { if (pstInsert != null) pstInsert.close(); } catch (Exception ex) {}
    try { if (pstDelete != null) pstDelete.close(); } catch (Exception ex) {}
    try { if (conn != null) conn.close(); } catch (Exception ex) {}
}

        // TODO add your handling code here:
    }//GEN-LAST:event_btnselesaidikembalikanActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
 
        cariData();
    
    // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jTextField1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusGained

        if (jTextField1.getText().equals("Cari username...")) {
            jTextField1.setText("");
        }
            // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1FocusGained

    private void btn_refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_refreshActionPerformed
        getData();          
    }//GEN-LAST:event_btn_refreshActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPerpanjang;
    private javax.swing.JButton btn_refresh;
    private javax.swing.JButton btnselesaidikembalikan;
    private javax.swing.JLabel id_user;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTable tblPeminjaman;
    private javax.swing.JTextField txtJumlah;
    private javax.swing.JLabel username;
    // End of variables declaration//GEN-END:variables
}
