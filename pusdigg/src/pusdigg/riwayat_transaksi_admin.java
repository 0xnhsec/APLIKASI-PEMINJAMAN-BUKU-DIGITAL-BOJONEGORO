/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pusdigg;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.swingx.prompt.PromptSupport;
import java.awt.Color;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.IDateEvaluator;
import java.util.HashSet;
import java.util.Calendar;


/**
 *
 * @author dppra
 */
public class riwayat_transaksi_admin extends javax.swing.JPanel {
    private DefaultTableModel model; // global
    private Connection conn;
    private PreparedStatement pst;
    private ResultSet rs;
 private HashSet<java.util.Date> tanggalPeminjaman = new HashSet<>();

    /**
     * Creates new form riwayat_transaksi_admin
     */
    
    public riwayat_transaksi_admin() {
        initComponents();
        conn = koneksi.koneksi.koneksiDB();
        
        AutoCompleteDecorator.decorate(cmbKategori);
        
        // TABLE
        loadRiwayatPeminjaman(); 
         autoSearch();   // 🔥 ini panggil method auto search
         hubunganTanggal(); // 🔥 ini panggil hubungan tanggal
        // Panggil function Kategori
        loadKategori();
        tandaiKalender();
        loadTanggalPeminjaman();
        JCalendar jc = datePinjam.getJCalendar();
        jc.getDayChooser().addDateEvaluator(new TanggalEvaluator());
        // PLACEHOLDER
        PromptSupport.setPrompt("Masukkan kode peminjaman", txtKode);
        PromptSupport.setPrompt("Masukkan username", txtUser);
        PromptSupport.setPrompt("Masukkan judul buku", txtJudul);
        
    }
    class TanggalEvaluator implements IDateEvaluator {

    @Override
    public boolean isSpecial(Date date) {
        return tanggalPeminjaman.contains(date);
    }

    @Override
    public Color getSpecialForegroundColor() {
        return Color.WHITE;
    }

    @Override
    public Color getSpecialBackroundColor() {
        return new Color(0, 153, 0); // hijau
    }

    @Override
    public String getSpecialTooltip() {
        return "Ada peminjaman";
    }

    @Override public boolean isInvalid(Date date) { return false; }
    @Override public Color getInvalidForegroundColor() { return null; }
    @Override public Color getInvalidBackroundColor() { return null; }

        @Override
        public String getInvalidTooltip() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    void loadKategori() {
        try {
            cmbKategori.removeAllItems();

            cmbKategori.addItem("Semua Kategori");

            String sql = "SELECT name_kategori FROM kategori ORDER BY name_kategori ASC";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                cmbKategori.addItem(rs.getString("name_kategori"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Load kategori gagal: " + e.getMessage());
        }
    }
private void tandaiTanggal() {
    try {
        String sql = "SELECT DISTINCT tgl_pinjam FROM peminjaman WHERE status='selesai'";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Date tanggal = rs.getDate("tgl_pinjam");

            // Tandai tanggal dengan warna
            ((JTextField)datePinjam.getDateEditor().getUiComponent())
                .setBackground(Color.GREEN);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void exportRiwayatPenting() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultFileName = "RiwayatPeminjaman_" + timeStamp + ".xls";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File Excel");
        fileChooser.setSelectedFile(new java.io.File(defaultFileName));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {

            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".xls")) filePath += ".xls";

            try (Connection conn = koneksi.koneksi.koneksiDB();
                 PreparedStatement pst = conn.prepareStatement(
                    "SELECT p.kode_peminjaman, u.fullname, b.judul, k.name_kategori, " +
                    "p.jumlah_pinjam, p.tanggal_pinjam, p.tanggal_kembali, " +
                    "p.status, p.denda, p.bayar, p.total " +
                    "FROM riwayat_peminjaman p " +
                    "JOIN user u ON p.user_id = u.user_id " +
                    "JOIN buku b ON p.buku_id = b.buku_id " +
                    "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                    "ORDER BY p.tanggal_pinjam DESC");
                 ResultSet rs = pst.executeQuery()) {

                Workbook workbook = new HSSFWorkbook();
                Sheet sheet = workbook.createSheet("Riwayat Peminjaman");

                // Header
                String[] headers = {
                    "Kode Peminjaman", "User", "Judul Buku", "Kategori",
                    "Jumlah Pinjam", "Tanggal Pinjam", "Tanggal Kembali",
                    "Status", "Denda", "Bayar", "Total"
                };

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                int rowIndex = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(rs.getString("kode_peminjaman"));
                    row.createCell(1).setCellValue(rs.getString("fullname"));
                    row.createCell(2).setCellValue(rs.getString("judul"));
                    row.createCell(3).setCellValue(rs.getString("name_kategori"));
                    row.createCell(4).setCellValue(rs.getInt("jumlah_pinjam"));
                    row.createCell(5).setCellValue(rs.getDate("tanggal_pinjam").toString());
                    // tanggal kembali
                    Date tglKembali = rs.getDate("tanggal_kembali");
                    row.createCell(6).setCellValue(tglKembali != null ? tglKembali.toString() : "-");
                    row.createCell(7).setCellValue(rs.getString("status")); // ← ini yang kurang
                    // denda
                    BigDecimal denda = rs.getBigDecimal("denda");
                    row.createCell(8).setCellValue(denda != null ? denda.toString() : "0");

                    // bayar
                    BigDecimal bayar = rs.getBigDecimal("bayar");
                    row.createCell(9).setCellValue(bayar != null ? bayar.toString() : "0");

                    // total
                    BigDecimal total = rs.getBigDecimal("total");
                    row.createCell(10).setCellValue(total != null ? total.toString() : "0");

                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    workbook.write(fos);
                }
                workbook.close();

                JOptionPane.showMessageDialog(null, "Export sukses!\nFile: " + filePath);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error export: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
private void hubunganTanggal() {

    datePinjam.addPropertyChangeListener("date", evt -> {
        if (datePinjam.getDate() != null) {
            dateKembali.setMinSelectableDate(datePinjam.getDate());
        }
    });

}

  private void cariData() {
    try {

        StringBuilder sql = new StringBuilder(
            "SELECT p.kode_peminjaman, u.fullname, b.judul, k.name_kategori, " +
            "p.jumlah_pinjam, p.tanggal_pinjam, p.tanggal_kembali, " +
            "p.status, p.denda, p.bayar, p.total " +
            "FROM riwayat_peminjaman p " +
            "JOIN user u ON p.user_id = u.user_id " +
            "JOIN buku b ON p.buku_id = b.buku_id " +
            "JOIN kategori k ON b.kategori_id = k.kategori_id " +
            "WHERE 1=1 "
        );

        // FILTER DINAMIS
        if (!txtKode.getText().trim().isEmpty())
            sql.append(" AND p.kode_peminjaman LIKE ?");

        if (!txtJudul.getText().trim().isEmpty())
            sql.append(" AND b.judul LIKE ?");

        if (!cmbKategori.getSelectedItem().toString().equals("Semua Kategori"))
            sql.append(" AND k.name_kategori = ?");

        if (!txtUser.getText().trim().isEmpty())
            sql.append(" AND u.fullname LIKE ?");

        // 🔥 FILTER PERIODE YANG BENAR
        if (datePinjam.getDate() != null)
            sql.append(" AND p.tanggal_pinjam >= ?");

        if (dateKembali.getDate() != null)
            sql.append(" AND p.tanggal_kembali <= ?");

        sql.append(" ORDER BY p.tanggal_pinjam DESC");

        PreparedStatement ps = conn.prepareStatement(sql.toString());

        int index = 1;

        if (!txtKode.getText().trim().isEmpty())
            ps.setString(index++, "%" + txtKode.getText().trim() + "%");

        if (!txtJudul.getText().trim().isEmpty())
            ps.setString(index++, "%" + txtJudul.getText().trim() + "%");

        if (!cmbKategori.getSelectedItem().toString().equals("Semua Kategori"))
            ps.setString(index++, cmbKategori.getSelectedItem().toString());

        if (!txtUser.getText().trim().isEmpty())
            ps.setString(index++, "%" + txtUser.getText().trim() + "%");

        if (datePinjam.getDate() != null)
            ps.setDate(index++, new java.sql.Date(datePinjam.getDate().getTime()));

        if (dateKembali.getDate() != null)
            ps.setDate(index++, new java.sql.Date(dateKembali.getDate().getTime()));

        ResultSet rs = ps.executeQuery();

        model.setRowCount(0);

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("kode_peminjaman"),
                rs.getString("fullname"),
                rs.getString("judul"),
                rs.getString("name_kategori"),
                rs.getInt("jumlah_pinjam"),
                rs.getDate("tanggal_pinjam"),
                rs.getDate("tanggal_kembali"),
                rs.getString("status"),
                rs.getBigDecimal("denda"),
                rs.getBigDecimal("bayar"),
                rs.getBigDecimal("total")
            });
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        e.printStackTrace();
    }
}


    private void autoSearch() {

    DocumentListener dl = new DocumentListener() {
        public void insertUpdate(DocumentEvent e) { cariData(); }
        public void removeUpdate(DocumentEvent e) { cariData(); }
        public void changedUpdate(DocumentEvent e) { cariData(); }
    };

    txtKode.getDocument().addDocumentListener(dl);
    txtJudul.getDocument().addDocumentListener(dl);
    txtUser.getDocument().addDocumentListener(dl);

    cmbKategori.addActionListener(e -> cariData());

    datePinjam.addPropertyChangeListener("date", evt -> cariData());
    dateKembali.addPropertyChangeListener("date", evt -> cariData());
}
private void loadTanggalPeminjaman() {
    try {
        tanggalPeminjaman.clear();

        String sql = "SELECT DISTINCT tanggal_pinjam FROM riwayat_peminjaman";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            java.util.Date tgl = rs.getDate("tanggal_pinjam");
            tanggalPeminjaman.add(tgl);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void tandaiKalender() {

    loadTanggalPeminjaman();

    JCalendar jc = datePinjam.getJCalendar();
    jc.getDayChooser().addDateEvaluator(new TanggalEvaluator());

}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtKode = new javax.swing.JTextField();
        datePinjam = new com.toedter.calendar.JDateChooser();
        dateKembali = new com.toedter.calendar.JDateChooser();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        cmbKategori = new javax.swing.JComboBox<String>();
        txtJudul = new javax.swing.JTextField();
        txtUser = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(1860, 1080));
        setPreferredSize(new java.awt.Dimension(1860, 1080));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtKode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtKodeActionPerformed(evt);
            }
        });
        add(txtKode, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, 290, -1));
        add(datePinjam, new org.netbeans.lib.awtextra.AbsoluteConstraints(1250, 130, 160, -1));
        add(dateKembali, new org.netbeans.lib.awtextra.AbsoluteConstraints(1440, 130, 160, -1));

        jButton1.setText(".");
        jButton1.setContentAreaFilled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 850, 60, 50));

        jButton2.setText(".");
        jButton2.setContentAreaFilled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 850, 50, 70));

        table.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(table);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 220, 1720, 513));

        jButton3.setText(".");
        jButton3.setContentAreaFilled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 850, 60, 70));

        add(cmbKategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(990, 130, 220, -1));
        add(txtJudul, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 130, 230, -1));

        txtUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUserActionPerformed(evt);
            }
        });
        add(txtUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 130, 300, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/1860/Laporan Riwayat.png"))); // NOI18N
        jLabel1.setText("jLabel1");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
  cariData();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Kosongkan semua field pencarian
    txtKode.setText("");
    txtJudul.setText("");
    txtUser.setText("");

    // Reset combo ke default
    cmbKategori.setSelectedIndex(0); // pastikan index 0 = Semua Kategori

    // Kosongkan tanggal
    datePinjam.setDate(null);
    dateKembali.setDate(null);

    // Refresh tabel (tampilkan semua data)
    cariData();
     loadRiwayatPeminjaman();  // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    exportRiwayatPenting();        // TODO add your handling code here:
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void txtKodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtKodeActionPerformed

    private void txtUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUserActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cmbKategori;
    private com.toedter.calendar.JDateChooser dateKembali;
    private com.toedter.calendar.JDateChooser datePinjam;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtJudul;
    private javax.swing.JTextField txtKode;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables

    
    // AMBIL DATA UNTUK DITMPILKAN DI TABLE
private void loadRiwayatPeminjaman() {
    model = new DefaultTableModel();
    model.addColumn("Kode Peminjaman");
    model.addColumn("User");
    model.addColumn("Judul Buku");
    model.addColumn("Kategori");
    model.addColumn("Jumlah Pinjam");
    model.addColumn("Tanggal Pinjam");
    model.addColumn("Tanggal Kembali");
    model.addColumn("Status");
    model.addColumn("Denda");
    model.addColumn("Bayar");
    model.addColumn("Total");

    try {
        Connection conn = koneksi.koneksi.koneksiDB();

        String sql =
            "SELECT p.kode_peminjaman, u.fullname, b.judul, k.name_kategori, " +
            "p.jumlah_pinjam, p.tanggal_pinjam, p.tanggal_kembali, " +
            "p.status, p.denda, p.bayar, p.total " +
            "FROM riwayat_peminjaman p " +
            "JOIN user u ON p.user_id = u.user_id " +
            "JOIN buku b ON p.buku_id = b.buku_id " +
            "JOIN kategori k ON b.kategori_id = k.kategori_id " +
            "ORDER BY p.tanggal_pinjam DESC";

        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("kode_peminjaman"),
                rs.getString("fullname"),
                rs.getString("judul"),
                rs.getString("name_kategori"),
                rs.getInt("jumlah_pinjam"),
                rs.getDate("tanggal_pinjam"),
                rs.getDate("tanggal_kembali"),
                rs.getString("status"),
                rs.getBigDecimal("denda"),
                rs.getBigDecimal("bayar"),
                rs.getBigDecimal("total")
            });
        }

        table.setModel(model);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        e.printStackTrace();
    }
}

}
