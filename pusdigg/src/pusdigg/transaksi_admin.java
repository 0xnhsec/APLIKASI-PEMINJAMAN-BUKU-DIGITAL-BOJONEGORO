package pusdigg;

// ── iText PDF ──────────────────────────────────────────────
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.*;

// ── Java SQL ───────────────────────────────────────────────
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// ── Java util / IO ─────────────────────────────────────────
import java.awt.Desktop;
import java.awt.event.ItemEvent;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// ── Swing ──────────────────────────────────────────────────
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 * Panel admin untuk mengelola transaksi peminjaman buku.
 */
public class transaksi_admin extends javax.swing.JPanel {

    // ── Konstanta ──────────────────────────────────────────
    private static final int DENDA_PER_HARI = 1000;

    // ── State ──────────────────────────────────────────────
    private Connection conn;
    private ResultSet rs;
    private PreparedStatement pst;

    private DefaultTableModel model;
    private int peminjamanIdAktif = -1;
    List<String[]> daftarBuku = new ArrayList<>();

    // Data sesi login
    private final String idUser       = session.getU_id();
    private final String usernameUser = session.getU_username();

    // ──────────────────────────────────────────────────────
    //  Konstruktor
    // ──────────────────────────────────────────────────────
    public transaksi_admin() {
        initComponents();

        id_user.setText("SELAMAT DATANG " + idUser);
        username.setText("ANDA LOGIN SEBAGAI " + usernameUser);

        AutoCompleteDecorator.decorate(cmb_kd1);
        AutoCompleteDecorator.decorate(cmb_kd2);
        AutoCompleteDecorator.decorate(cmb_kd3);

        conn = koneksi.koneksi.koneksiDB();

        initTable();
        getData();
        loadKodeBuku();
        pasangListenerKodeBuku();
    }

    // ──────────────────────────────────────────────────────
    //  Inisialisasi tabel
    // ──────────────────────────────────────────────────────
    private void initTable() {
        model = new DefaultTableModel();
        jTable1.setModel(model);

        model.addColumn("ID");              // 0 – hidden
        model.addColumn("Kode Peminjaman"); // 1
        model.addColumn("buku_id");         // 2 – hidden (narrow)
        model.addColumn("Name");            // 3
        model.addColumn("Judul Buku");      // 4
        model.addColumn("Kategori Buku");   // 5
        model.addColumn("Jumlah Pinjam");   // 6
        model.addColumn("Tanggal Pinjam");  // 7
        model.addColumn("Tanggal Kembali"); // 8
        model.addColumn("Status");          // 9

        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(2).setMinWidth(0);
        jTable1.getColumnModel().getColumn(2).setMaxWidth(0);
    }

    // ──────────────────────────────────────────────────────
    //  Data: load & search
    // ──────────────────────────────────────────────────────
    void getData() {
        model.setRowCount(0);
        String sql =
            "SELECT p.peminjaman_id, p.kode_peminjaman, b.buku_id, u.fullname, b.judul, " +
            "       k.name_kategori, p.jumlah_pinjam, p.tanggal_pinjam, " +
            "       p.tanggal_kembali, p.status " +
            "FROM peminjaman p " +
            "JOIN user     u ON p.user_id     = u.user_id " +
            "JOIN buku     b ON p.buku_id     = b.buku_id " +
            "JOIN kategori k ON b.kategori_id = k.kategori_id";
        isiTabelDariQuery(sql, new Object[0]);
    }

    private void search() {
        model.setRowCount(0);

        String nama   = txt_cari.getText().trim();
        String status = cmb_transaksi.getSelectedItem().toString().trim();
        boolean filterStatus = !status.equalsIgnoreCase("Semua");

        String sql =
            "SELECT p.peminjaman_id, p.kode_peminjaman, p.buku_id, u.fullname, b.judul, " +
            "       k.name_kategori, p.jumlah_pinjam, p.tanggal_pinjam, " +
            "       p.tanggal_kembali, p.status " +
            "FROM peminjaman p " +
            "JOIN user     u ON p.user_id     = u.user_id " +
            "JOIN buku     b ON p.buku_id     = b.buku_id " +
            "JOIN kategori k ON b.kategori_id = k.kategori_id " +
            "WHERE u.fullname LIKE ?" +
            (filterStatus ? " AND p.status = ?" : "");

        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + nama + "%");
            if (filterStatus) pst.setString(2, status);

            rs = pst.executeQuery();
            while (rs.next()) model.addRow(buatRowDariRS(rs));

        } catch (Exception e) {
            tampilkanError("Error Search", e);
        }
    }

    /** Memetakan satu baris ResultSet ke array Object[] untuk tabel. */
    private Object[] buatRowDariRS(ResultSet rs) throws Exception {
        return new Object[]{
            rs.getInt("peminjaman_id"),
            rs.getString("kode_peminjaman"),
            rs.getString("buku_id"),
            rs.getString("fullname"),
            rs.getString("judul"),
            rs.getString("name_kategori"),
            rs.getInt("jumlah_pinjam"),
            rs.getDate("tanggal_pinjam"),
            rs.getDate("tanggal_kembali"),
            rs.getString("status")
        };
    }

    /**
     * Menjalankan query dan mengisi tabel; params opsional (gunakan array kosong
     * jika tidak ada parameter).
     */
    private void isiTabelDariQuery(String sql, Object[] params) {
        try {
            pst = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                pst.setObject(i + 1, params[i]);
            }
            rs = pst.executeQuery();
            while (rs.next()) model.addRow(buatRowDariRS(rs));
        } catch (Exception e) {
            tampilkanError("Gagal memuat data", e);
        }
    }

    // ──────────────────────────────────────────────────────
    //  Load combo kode buku
    // ──────────────────────────────────────────────────────
    void loadKodeBuku() {
        try {
            for (JComboBox<String> cmb : new JComboBox[]{cmb_kd1, cmb_kd2, cmb_kd3}) {
                cmb.removeAllItems();
                cmb.addItem("-- Pilih Kode --");
            }

            pst = conn.prepareStatement(
                "SELECT kode_buku FROM buku_item ORDER BY kode_buku ASC");
            rs = pst.executeQuery();

            while (rs.next()) {
                String kode = rs.getString("kode_buku");
                cmb_kd1.addItem(kode);
                cmb_kd2.addItem(kode);
                cmb_kd3.addItem(kode);
            }
        } catch (Exception e) {
            tampilkanError("Load kode buku gagal", e);
        }
    }

    private void pasangListenerKodeBuku() {
        for (JComboBox<String> cmb : new JComboBox[]{cmb_kd1, cmb_kd2, cmb_kd3}) {
            cmb.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    validasiKodeBuku(cmb);
                }
            });
        }
    }

    // ──────────────────────────────────────────────────────
    //  Pilih baris tabel
    // ──────────────────────────────────────────────────────
    void pilihData() {
        int i = jTable1.getSelectedRow();
        if (i == -1) return;

        peminjamanIdAktif = Integer.parseInt(model.getValueAt(i, 0).toString());
        kode_peminjaman.setText(model.getValueAt(i, 1).toString());

        try {
            pst = conn.prepareStatement(
                "SELECT kd_bk1, kd_bk2, kd_bk3, jumlah_pinjam, status, denda, " +
                "       tanggal_kembali, catatan_pengajuan " +
                "FROM peminjaman WHERE peminjaman_id = ?");
            pst.setInt(1, peminjamanIdAktif);
            rs = pst.executeQuery();

            if (rs.next()) {
                cmb_kd1.setSelectedItem(rs.getString("kd_bk1"));
                cmb_kd2.setSelectedItem(rs.getString("kd_bk2"));
                cmb_kd3.setSelectedItem(rs.getString("kd_bk3"));

                cmbstatus.setSelectedItem(rs.getString("status"));
                denda.setText(String.valueOf(hitungDenda(rs.getDate("tanggal_kembali"))));
                txtcatatan.setText(rs.getString("catatan_pengajuan"));
                aturFormKodeBuku(rs.getInt("jumlah_pinjam"));
            }
        } catch (Exception e) {
            tampilkanError("Gagal memuat detail peminjaman", e);
        }
    }

    // ──────────────────────────────────────────────────────
    //  Logika bisnis
    // ──────────────────────────────────────────────────────
    int hitungDenda(Date tanggalKembali) {
        LocalDate hariIni  = LocalDate.now();
        LocalDate kembali  = tanggalKembali.toLocalDate();
        if (hariIni.isAfter(kembali)) {
            long terlambat = ChronoUnit.DAYS.between(kembali, hariIni);
            return (int) (terlambat * DENDA_PER_HARI);
        }
        return 0;
    }

    /** Update status satu item buku di tabel buku_item. */
    private void updateStatusBukuItem(Connection c, String kodeBuku, String status) throws Exception {
        PreparedStatement ps = c.prepareStatement(
            "UPDATE buku_item SET status = ? WHERE kode_buku = ?");
        ps.setString(1, status);
        ps.setString(2, kodeBuku);
        ps.executeUpdate();
    }

    /** Update status beberapa kode buku sekaligus (abaikan "N/A"). */
    private void updateStatusBanyakItem(Connection c, String[] kodeBukus, String status) throws Exception {
        for (String kode : kodeBukus) {
            if (kode != null && !kode.equals("N/A")) {
                updateStatusBukuItem(c, kode, status);
            }
        }
    }

    void resetForm() {
        peminjamanIdAktif = -1;
        kode_peminjaman.setText("");
        denda.setText("0");
        cmbstatus.setSelectedIndex(0);
        cmb_kd1.setSelectedIndex(0);
        cmb_kd2.setSelectedIndex(0);
        cmb_kd3.setSelectedIndex(0);
        txtcatatan.setText("");
    }

    private void aturFormKodeBuku(int jumlah) {
        cmb_kd1.setVisible(false); jLabel2.setVisible(false);
        cmb_kd2.setVisible(false); jLabel3.setVisible(false);
        cmb_kd3.setVisible(false); jLabel4.setVisible(false);

        if (jumlah >= 1) { cmb_kd1.setVisible(true); jLabel2.setVisible(true); }
        if (jumlah >= 2) { cmb_kd2.setVisible(true); jLabel3.setVisible(true); }
        if (jumlah >= 3) { cmb_kd3.setVisible(true); jLabel4.setVisible(true); }
    }

    private void updateBtnPerpanjang() {
        int row = jTable1.getSelectedRow();
        if (row == -1) { btnPerpanjang.setEnabled(false); return; }

        String status    = model.getValueAt(row, 9).toString().toLowerCase();
        Date tglKembali  = (Date) model.getValueAt(row, 8);
        Date today       = new Date(System.currentTimeMillis());

        btnPerpanjang.setEnabled(status.equals("dipinjam") && !today.after(tglKembali));
    }

    // ──────────────────────────────────────────────────────
    //  Validasi kode buku (tidak boleh duplikat kecuali N/A)
    // ──────────────────────────────────────────────────────
    void validasiKodeBuku(JComboBox<String> sumber) {
        String v1 = getKode(cmb_kd1);
        String v2 = getKode(cmb_kd2);
        String v3 = getKode(cmb_kd3);

        long terisi = java.util.stream.Stream.of(v1, v2, v3).filter(s -> !s.isEmpty()).count();
        if (terisi < 2) return;

        if (!v1.equals("N/A") && (v1.equals(v2) || v1.equals(v3))) { duplikat(sumber); return; }
        if (!v2.equals("N/A") &&  v2.equals(v3))                   { duplikat(sumber); }
    }

    private String getKode(JComboBox<String> cmb) {
        if (cmb.getSelectedItem() == null) return "";
        String val = cmb.getSelectedItem().toString();
        return val.equals("-- Pilih Kode --") ? "" : val;
    }

    private void duplikat(JComboBox<String> sumber) {
        JOptionPane.showMessageDialog(this, "Kode buku tidak boleh sama (kecuali N/A)");
        sumber.setSelectedItem("N/A");
    }

    // ──────────────────────────────────────────────────────
    //  Batalkan / ubah jumlah peminjaman
    // ──────────────────────────────────────────────────────
    private void batalkanAtauUbahJumlah() {
        int row = jTable1.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!"); return; }

        String status = model.getValueAt(row, 9).toString().toLowerCase();
        if (!status.equals("pending")) {
            JOptionPane.showMessageDialog(this,
                "❌ Hanya peminjaman berstatus PENDING yang bisa dibatalkan.");
            return;
        }

        String kodePinjam = model.getValueAt(row, 1).toString();
        String bukuId     = model.getValueAt(row, 2).toString();
        int jumlahLama    = Integer.parseInt(model.getValueAt(row, 6).toString());

        JTextField txtJumlahBaru = new JTextField();
        Object[] pesan = {"Jumlah yang ingin dipertahankan:", txtJumlahBaru,
                          "\nKosongkan jika ingin membatalkan seluruh peminjaman"};
        Object[] opsi  = {"Ubah Jumlah", "Batalkan Semua", "Batal"};

        int pilih = JOptionPane.showOptionDialog(
            this, pesan, "Batalkan Peminjaman",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, opsi, opsi[0]);

        if (pilih == 2 || pilih == JOptionPane.CLOSED_OPTION) return;

        try (Connection c = koneksi.koneksi.koneksiDB()) {
            if (c == null) { JOptionPane.showMessageDialog(this, "❌ Koneksi gagal!"); return; }
            c.setAutoCommit(false);

            if (pilih == 1) {          // ── Batalkan semua ──
                batalkanSemua(c, row, kodePinjam, bukuId, jumlahLama);
            } else if (pilih == 0) {   // ── Ubah jumlah ──
                ubahJumlah(c, row, kodePinjam, bukuId, jumlahLama, txtJumlahBaru.getText().trim());
            }
        } catch (Exception e) {
            tampilkanError("Error koneksi", e);
        }
    }

    private void batalkanSemua(Connection c, int row, String kode, String bukuId, int jumlah) throws Exception {
        try {
            hapusPeminjaman(c, kode);
            kembalikanStok(c, bukuId, jumlah);
            kembalikanBukuItem(c, bukuId, jumlah, "dipilih", "tersedia");
            c.commit();
            model.removeRow(row);
            JOptionPane.showMessageDialog(this, "✅ Peminjaman berhasil dibatalkan.");
        } catch (Exception e) {
            c.rollback();
            tampilkanError("Gagal membatalkan", e);
        }
    }

    private void ubahJumlah(Connection c, int row, String kode, String bukuId, int jumlahLama, String inputBaru) throws Exception {
        if (inputBaru.isEmpty()) { JOptionPane.showMessageDialog(this, "Jumlah tidak boleh kosong!"); return; }

        int jumlahBaru;
        try { jumlahBaru = Integer.parseInt(inputBaru); }
        catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "❌ Jumlah harus angka!"); return; }

        if (jumlahBaru <= 0 || jumlahBaru >= jumlahLama) {
            JOptionPane.showMessageDialog(this, "Jumlah harus lebih kecil dari jumlah lama!");
            return;
        }

        int selisih = jumlahLama - jumlahBaru;
        try {
            PreparedStatement ps = c.prepareStatement(
                "UPDATE peminjaman SET jumlah_pinjam = ? WHERE kode_peminjaman = ?");
            ps.setInt(1, jumlahBaru);
            ps.setString(2, kode);
            if (ps.executeUpdate() == 0) { c.rollback(); JOptionPane.showMessageDialog(this, "❌ Data tidak ditemukan!"); return; }

            kembalikanStok(c, bukuId, selisih);
            kembalikanBukuItem(c, bukuId, selisih, "dipilih", "tersedia");
            c.commit();
            model.setValueAt(jumlahBaru, row, 6);
            JOptionPane.showMessageDialog(this, "✏ Jumlah berhasil diperbarui.");
        } catch (Exception e) {
            c.rollback();
            tampilkanError("Gagal ubah jumlah", e);
        }
    }

    // ── helper DB ──────────────────────────────────────────
    private void hapusPeminjaman(Connection c, String kode) throws Exception {
        PreparedStatement ps = c.prepareStatement(
            "DELETE FROM peminjaman WHERE kode_peminjaman = ?");
        ps.setString(1, kode);
        if (ps.executeUpdate() == 0) throw new Exception("Data peminjaman tidak ditemukan!");
    }

    private void kembalikanStok(Connection c, String bukuId, int jumlah) throws Exception {
        PreparedStatement ps = c.prepareStatement(
            "UPDATE buku SET stok = stok + ? WHERE buku_id = ?");
        ps.setInt(1, jumlah);
        ps.setString(2, bukuId);
        if (ps.executeUpdate() == 0) throw new Exception("Data buku tidak ditemukan!");
    }

    /** Mengambil sejumlah item berdasarkan statusLama lalu mengubahnya ke statusBaru. */
    private void kembalikanBukuItem(Connection c, String bukuId, int limit,
                                    String statusLama, String statusBaru) throws Exception {
        PreparedStatement psAmbil = c.prepareStatement(
            "SELECT bukuitem_id FROM buku_item WHERE buku_id = ? AND status = ? LIMIT ?");
        psAmbil.setString(1, bukuId);
        psAmbil.setString(2, statusLama);
        psAmbil.setInt(3, limit);
        ResultSet rsItem = psAmbil.executeQuery();

        PreparedStatement psUpdate = c.prepareStatement(
            "UPDATE buku_item SET status = ? WHERE bukuitem_id = ?");
        while (rsItem.next()) {
            psUpdate.setString(1, statusBaru);
            psUpdate.setInt(2, rsItem.getInt("bukuitem_id"));
            psUpdate.executeUpdate();
        }
    }

    // ──────────────────────────────────────────────────────
    //  Bayar denda
    // ──────────────────────────────────────────────────────
    private void bayarDenda() {
        JTextField txtDenda = new JTextField(denda.getText());
        txtDenda.setEditable(false);
        JTextField txtBayar = new JTextField(10);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Denda yang Harus Dibayar:"));
        panel.add(txtDenda);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Jumlah Uang Bayar:"));
        panel.add(txtBayar);

        if (JOptionPane.showConfirmDialog(this, panel, "Input Transaksi Denda",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            conn.setAutoCommit(false);

            double totalDenda = Double.parseDouble(txtDenda.getText());
            double bayar      = Double.parseDouble(txtBayar.getText());

            if (bayar < totalDenda) {
                JOptionPane.showMessageDialog(this, "Uang bayar kurang!");
                return;
            }
            double kembalian = bayar - totalDenda;

            pst = conn.prepareStatement(
                "UPDATE peminjaman SET denda=?, bayar=?, kembali=?, total=?, status=? " +
                "WHERE peminjaman_id=?");
            pst.setDouble(1, totalDenda);
            pst.setDouble(2, bayar);
            pst.setDouble(3, kembalian);
            pst.setDouble(4, totalDenda);
            pst.setString(5, "selesai");
            pst.setInt(6, peminjamanIdAktif);
            pst.executeUpdate();
            conn.commit();

            // Cetak slip denda
            cetakSlipDenda(bayar, totalDenda, kembalian);

            JOptionPane.showMessageDialog(this,
                "Pembayaran berhasil!\n" +
                "Denda   : " + formatRupiah(totalDenda) + "\n" +
                "Bayar   : " + formatRupiah(bayar)      + "\n" +
                "Kembali : " + formatRupiah(kembalian));

            getData();
            resetForm();
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { /* abaikan */ }
            tampilkanError("Gagal simpan pembayaran", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (Exception e) { /* abaikan */ }
        }
    }

    private void cetakSlipDenda(double bayar, double totalDenda, double kembalian) {
        int row = jTable1.getSelectedRow();
        if (row == -1) return;

        String kodePinjam = model.getValueAt(row, 1).toString();
        String fullname   = model.getValueAt(row, 3).toString();
        String bukuId     = model.getValueAt(row, 2).toString();
        String judul      = model.getValueAt(row, 4).toString();
        String kategori   = model.getValueAt(row, 5).toString();
        int    jumlah     = Integer.parseInt(model.getValueAt(row, 6).toString());
        String tglPinjam  = model.getValueAt(row, 7).toString();
        String tglKembali = model.getValueAt(row, 8).toString();

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Slip Denda");
        fc.setSelectedFile(new File("Slip_Denda_" + kodePinjam + ".pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            PdfWriter   writer  = new PdfWriter(fc.getSelectedFile().getAbsolutePath());
            PdfDocument pdfDoc  = new PdfDocument(writer);
            Document    doc     = new Document(pdfDoc);

            doc.add(new Paragraph("SLIP DENDA PEMINJAMAN BUKU").setBold().setFontSize(16).setMarginBottom(10));

            Table info = new Table(2);
            tambahBarisTabel(info, "Kode Peminjaman",  kodePinjam);
            tambahBarisTabel(info, "Nama Peminjam",    fullname);
            tambahBarisTabel(info, "Status",           "Selesai");
            tambahBarisTabel(info, "Denda",            formatRupiah(totalDenda));
            tambahBarisTabel(info, "Bayar",            formatRupiah(bayar));
            tambahBarisTabel(info, "Kembali",          formatRupiah(kembalian));
            doc.add(info);

            doc.add(new Paragraph("\nDetail Buku:").setBold());
            Table tblBuku = new Table(5);
            tblBuku.addHeaderCell("Buku ID");
            tblBuku.addHeaderCell("Judul");
            tblBuku.addHeaderCell("Kategori");
            tblBuku.addHeaderCell("Jumlah");
            tblBuku.addHeaderCell("Tanggal Kembali");
            tblBuku.addCell(bukuId);
            tblBuku.addCell(judul);
            tblBuku.addCell(kategori);
            tblBuku.addCell(String.valueOf(jumlah));
            tblBuku.addCell(tglKembali);
            doc.add(tblBuku);

            doc.add(new Paragraph("\nTanggal Pinjam: " + tglPinjam));
            doc.add(new Paragraph("Tanggal Pengembalian: " + LocalDate.now()));
            doc.close();

            JOptionPane.showMessageDialog(this, "✅ Slip denda berhasil dibuat:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception e) {
            tampilkanError("Gagal membuat slip denda", e);
        }
    }

    // ──────────────────────────────────────────────────────
    //  Simpan / update transaksi
    // ──────────────────────────────────────────────────────
    private void simpanTransaksi() {
        if (kode_peminjaman.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi terlebih dahulu!");
            return;
        }
        if (peminjamanIdAktif == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi terlebih dahulu!");
            return;
        }

        String kd1 = cmb_kd1.getSelectedItem() != null ? cmb_kd1.getSelectedItem().toString() : "N/A";
        String kd2 = (cmb_kd2.isVisible() && cmb_kd2.getSelectedItem() != null)
                     ? cmb_kd2.getSelectedItem().toString() : "N/A";
        String kd3 = (cmb_kd3.isVisible() && cmb_kd3.getSelectedItem() != null)
                     ? cmb_kd3.getSelectedItem().toString() : "N/A";

        if (kd1.equals("-- Pilih Kode --")) {
            JOptionPane.showMessageDialog(this, "Kode buku minimal 1 harus diisi!");
            return;
        }
        if (kd1.equals("N/A") && kd2.equals("N/A") && kd3.equals("N/A")) {
            JOptionPane.showMessageDialog(this, "Minimal 1 buku harus dipilih!");
            return;
        }

        try {
            conn.setAutoCommit(false);

            pst = conn.prepareStatement(
                "UPDATE peminjaman SET kd_bk1=?, kd_bk2=?, kd_bk3=?, status=?, " +
                "denda=?, catatan=?, update_by=? WHERE peminjaman_id=?");
            pst.setString(1, kd1);
            pst.setString(2, kd2);
            pst.setString(3, kd3);
            pst.setString(4, cmbstatus.getSelectedItem().toString());
            pst.setInt(5, Integer.parseInt(denda.getText()));
            pst.setString(6, txtcatatan.getText());
            pst.setString(7, session.getU_id());
            pst.setInt(8, peminjamanIdAktif);
            pst.executeUpdate();

            updateStatusBanyakItem(conn, new String[]{kd1, kd2, kd3}, "dipinjam");

            conn.commit();
            JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan");
            getData();
            resetForm();

        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ex) { /* abaikan */ }
            tampilkanError("Gagal menyimpan", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (Exception e) { /* abaikan */ }
        }
    }

    // ──────────────────────────────────────────────────────
    //  Slip peminjaman
    // ──────────────────────────────────────────────────────\
    // private void cetakSlipPeminjaman() {
    //    int row = jTable1.getSelectedRow();
    //    if (row == -1) { JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!"); return; }
//
     //   String kodePinjam = model.getValueAt(row, 1).toString();
    //    String bukuId     = model.getValueAt(row, 2).toString();
    //    String fullname   = model.getValueAt(row, 3).toString();
    //    String judul      = model.getValueAt(row, 4).toString();
    //    String kategori   = model.getValueAt(row, 5).toString();
     //   int    jumlah     = Integer.parseInt(model.getValueAt(row, 6).toString());
     //   String tglPinjam  = model.getValueAt(row, 7).toString();
     //   String tglKembali = model.getValueAt(row, 8).toString();
     //   String status     = model.getValueAt(row, 9).toString();

      //  JFileChooser fc = new JFileChooser();
      //  fc.setDialogTitle("Simpan Slip Peminjaman");
      //  fc.setSelectedFile(new File("Slip_Peminjaman_" + kodePinjam + ".pdf"));
      //  if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

      //  try {
      //      Document doc = buatDokumenPdf(fc.getSelectedFile().getAbsolutePath());

        //    doc.add(new Paragraph("SLIP PEMINJAMAN BUKU").setBold().setFontSize(16).setMarginBottom(10));

          //  Table info = new Table(2);
          //  tambahBarisTabel(info, "Kode Peminjaman", kodePinjam);
          //  tambahBarisTabel(info, "Nama Peminjam",   fullname);
          //  tambahBarisTabel(info, "Status",          status);
          //  doc.add(info);

          //  doc.add(new Paragraph("\nDetail Buku:").setBold());
          //  doc.add(buatTabelBuku(bukuId, judul, kategori, jumlah, tglKembali));
          //  doc.add(new Paragraph("\nTanggal Pinjam: " + tglPinjam));

          //  doc.close();
          //  JOptionPane.showMessageDialog(this, "✅ Slip berhasil dibuat:\n" + fc.getSelectedFile().getAbsolutePath());
     //   } catch (Exception e) {
     //       tampilkanError("Gagal membuat slip peminjaman", e);
     //   }
   // }
    private void cetakSlipPeminjaman() {
    int row = jTable1.getSelectedRow();
    if (row == -1) { 
        JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!"); 
        return; 
    }

    String kodePinjam = model.getValueAt(row, 1).toString();
    String noAnggota  = model.getValueAt(row, 2).toString();
    String fullname   = model.getValueAt(row, 3).toString();
    String judulBuku  = model.getValueAt(row, 4).toString();
    String tglPinjam  = model.getValueAt(row, 7).toString();
    String tglKembali = model.getValueAt(row, 8).toString();

    List<String[]> listBuku = new ArrayList<>();
    try {
        String sql = "SELECT kd_bk1, kd_bk2, kd_bk3, jumlah_pinjam " +
                     "FROM peminjaman WHERE kode_peminjaman = ?";
        pst = conn.prepareStatement(sql);
        pst.setString(1, kodePinjam);
        rs = pst.executeQuery();
        if (rs.next()) {
            String[] buku = {
                rs.getString("kd_bk1"),
                rs.getString("kd_bk2"),
                rs.getString("kd_bk3"),
                rs.getString("jumlah_pinjam")
            };
            listBuku.add(buku);
        }
    } catch (Exception e) {
        tampilkanError("Gagal ambil data buku", e);
        return;
    }

        cetakSlipPeminjamanFormat(kodePinjam, noAnggota, fullname, judulBuku, listBuku, tglPinjam, tglKembali);
    }

    // ──────────────────────────────────────────────────────
//  Slip peminjaman terformat (versi A6 dengan logo & TTD)
// ──────────────────────────────────────────────────────
private void cetakSlipPeminjamanFormat(
        String kode, String noAnggota, String nama, String judulBuku,
        List<String[]> daftarBuku,
        String tglPinjam, String tglKembali) {
    try {
        if (daftarBuku == null || daftarBuku.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data buku untuk dicetak!");
            return;
        }

        // Path default ke Documents/slip-peminjaman
        String documents = System.getProperty("user.home") + File.separator + "Documents";
        String slipDir   = documents + File.separator + "slip-peminjaman";
        File folder = new File(slipDir);
        if (!folder.exists()) folder.mkdirs();
        String path = slipDir + File.separator + "Slip_Peminjaman_" + kode + ".pdf";

        PdfWriter   writer = new PdfWriter(path);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf, PageSize.A6);
        doc.setMargins(10, 10, 10, 10);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Warna biru untuk peminjaman
        com.itextpdf.kernel.colors.Color BIRU =
            new com.itextpdf.kernel.colors.DeviceRgb(30, 100, 200);
        com.itextpdf.kernel.colors.Color BIRU_MUDA =
            new com.itextpdf.kernel.colors.DeviceRgb(210, 225, 245);

        // Header
        Table header = new Table(new float[]{1, 3, 1});
        header.setWidth(UnitValue.createPercentValue(100));
        header.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        Image logoKiri  = new Image(ImageDataFactory.create(
            getClass().getResource("/img/logo.png"))).setHeight(35);
        Image logoKanan = new Image(ImageDataFactory.create(
            getClass().getResource("/img/logo.png"))).setHeight(35);

        header.addCell(new Cell().add(logoKiri)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setVerticalAlignment(VerticalAlignment.MIDDLE));
        header.addCell(new Cell().add(
            new Paragraph()
                .add(new Text("SLIP PEMINJAMAN\n").setFont(bold).setFontSize(12))
                .add(new Text("PERPUSTAKAAN DIGITAL BOJONEGORO\n").setFont(bold).setFontSize(9))
                .add(new Text("Jl. Raya Surabaya–Bojonegoro, Kapas").setFont(font).setFontSize(8))
                .setTextAlignment(TextAlignment.CENTER))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setVerticalAlignment(VerticalAlignment.MIDDLE));
        header.addCell(new Cell().add(logoKanan)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setTextAlignment(TextAlignment.RIGHT)
            .setVerticalAlignment(VerticalAlignment.MIDDLE));

        doc.add(header);
        doc.add(new LineSeparator(new SolidLine(1f)));
        doc.add(new Paragraph(" "));

        // Info peminjam
        Table info = new Table(new float[]{1, 2});
        info.setWidth(UnitValue.createPercentValue(60));
        info.addCell(infoCell("Kode Peminjaman", bold));
        info.addCell(infoCell(": " + kode,        font));
        info.addCell(infoCell("No Anggota",       font));
        info.addCell(infoCell(": " + noAnggota,   font));
        info.addCell(infoCell("Nama",             font));
        info.addCell(infoCell(": " + nama,        font));
        info.addCell(infoCell("Judul Buku",       font));
        info.addCell(infoCell(": " + judulBuku,   font));
        info.addCell(infoCell("Tgl Pinjam",       font));
        info.addCell(infoCell(": " + tglPinjam,   font));
        info.addCell(infoCell("Tgl Kembali",      font));
        info.addCell(infoCell(": " + tglKembali,  font));
        doc.add(info);
        doc.add(new Paragraph(" "));

        // Tabel kode buku
        Table tabel = new Table(new float[]{2, 2, 2, 1});
        tabel.setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"Kode Buku 1", "Kode Buku 2", "Kode Buku 3", "Jml Pinjam"};
        for (String h : headers) {
            tabel.addHeaderCell(
                new Cell().add(new Paragraph(h).setFont(bold).setFontSize(7)
                    .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(BIRU)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5)
            );
        }

        for (int i = 0; i < daftarBuku.size(); i++) {
            String[] buku = daftarBuku.get(i);
            com.itextpdf.kernel.colors.Color bg =
                (i % 2 == 0) ? ColorConstants.WHITE : BIRU_MUDA;
            for (String col : buku) {
                tabel.addCell(
                    new Cell().add(new Paragraph(col == null ? "N/A" : col)
                        .setFont(font).setFontSize(7))
                    .setBackgroundColor(bg)
                    .setPadding(4)
                );
            }
        }

        doc.add(tabel);
        doc.add(new Paragraph(" ").setMarginBottom(15));

        doc.add(new Paragraph("\"Terima kasih telah meminjam.\nMohon dikembalikan tepat waktu.\"")
            .setFont(font).setFontSize(7).setItalic());
        doc.add(new Paragraph(" "));

        // TTD
        Image ttd = new Image(ImageDataFactory.create(
            getClass().getResource("/img/ttd.png")));
        ttd.setWidth(50).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        doc.add(ttd);
        doc.add(new Paragraph("Ayanagi Noshiro S.KOM")
            .setFont(bold).setFontSize(8)
            .setTextAlignment(TextAlignment.RIGHT));
        doc.add(new Paragraph("Admin Perpustakaan Digital Bojonegoro")
            .setFont(font).setFontSize(7).setItalic()
            .setTextAlignment(TextAlignment.RIGHT));

        doc.close();
        Desktop.getDesktop().open(new File(path));

    } catch (Exception e) {
        tampilkanError("Gagal cetak slip format", e);
    }
}

    // ──────────────────────────────────────────────────────
    //  Slip pengembalian
    // ──────────────────────────────────────────────────────
    private void cetakSlipPengembalian() {
    int row = jTable1.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!");
        return;
    }

    String status = model.getValueAt(row, 9).toString().trim().toLowerCase();
    if (!status.equals("selesai")) {
        JOptionPane.showMessageDialog(this,
            "Slip pengembalian hanya bisa dibuat jika status selesai.\nStatus saat ini: " + status.toUpperCase());
        return;
    }

    String kodePinjam = model.getValueAt(row, 1).toString();
    String noAnggota  = model.getValueAt(row, 2).toString();
    String fullname   = model.getValueAt(row, 3).toString();
    String judulBuku  = model.getValueAt(row, 4).toString();
    String tglPinjam  = model.getValueAt(row, 7).toString();
    String tglKembali = model.getValueAt(row, 8).toString();

    // Ambil denda dari DB
    String dendaStr = "Rp0";
    try {
    String sql = "SELECT denda FROM peminjaman WHERE kode_peminjaman = ? LIMIT 1";
        pst = conn.prepareStatement(sql);
        pst.setString(1, kodePinjam);
        rs = pst.executeQuery();
        if (rs.next()) {
            int denda = rs.getInt("denda");
            dendaStr = denda > 0 ? "Rp" + String.format("%,d", denda) : "Rp0 (Tidak ada keterlambatan)";
        }
    } catch (Exception e) {
        // abaikan, denda tetap Rp0
    }

    // Ambil kode buku
    List<String[]> listBuku = new ArrayList<>();
    try {
        String sql = "SELECT kd_bk1, kd_bk2, kd_bk3, jumlah_pinjam FROM peminjaman WHERE kode_peminjaman = ? LIMIT 1";
        pst = conn.prepareStatement(sql);
        pst.setString(1, kodePinjam);
        rs = pst.executeQuery();
        if (rs.next()) {
            String[] buku = {
                rs.getString("kd_bk1"),
                rs.getString("kd_bk2"),
                rs.getString("kd_bk3"),
                rs.getString("jumlah_pinjam")
            };
            listBuku.add(buku);
        }
    } catch (Exception e) {
        tampilkanError("Gagal ambil data buku", e);
        return;
    }

    cetakSlipPengembalianFormat(kodePinjam, noAnggota, fullname, judulBuku,
                                listBuku, tglPinjam, tglKembali, dendaStr);
}

    // ──────────────────────────────────────────────────────
    //  Perpanjangan
    // ──────────────────────────────────────────────────────
    private void ajukanPerpanjangan() {
        int row = jTable1.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!"); return; }

        String status = model.getValueAt(row, 9).toString().toLowerCase();
        if (!status.equals("dipinjam")) {
            JOptionPane.showMessageDialog(this, "❌ Tidak bisa diajukan perpanjangan!");
            return;
        }

        String kode     = model.getValueAt(row, 1).toString();
        String bukuId   = model.getValueAt(row, 2).toString();
        Date tglKembali = (Date) model.getValueAt(row, 8);

        JTextArea txtAlasan = new JTextArea(3, 20);
        if (JOptionPane.showConfirmDialog(this, new JScrollPane(txtAlasan),
                "Alasan Perpanjangan", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        String alasan = txtAlasan.getText().trim();
        if (alasan.isEmpty()) { JOptionPane.showMessageDialog(this, "Alasan wajib diisi!"); return; }

        Calendar cal = Calendar.getInstance();
        cal.setTime(tglKembali);
        cal.add(Calendar.DAY_OF_MONTH, 5);
        Date tglBaru = new Date(cal.getTimeInMillis());

        try (Connection c = koneksi.koneksi.koneksiDB();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE peminjaman SET status=?, catatan_pengajuan=?, tanggal_kembali=? " +
                 "WHERE kode_peminjaman=? AND buku_id=? AND tanggal_kembali=? AND status='dipinjam'")) {

            ps.setString(1, "diperpanjang");
            ps.setString(2, alasan);
            ps.setDate(3, tglBaru);
            ps.setString(4, kode);
            ps.setString(5, bukuId);
            ps.setDate(6, tglKembali);

            if (ps.executeUpdate() == 0) {
                JOptionPane.showMessageDialog(this, "⚠ Data tidak ditemukan atau sudah berubah.");
                return;
            }

            model.setValueAt("diperpanjang", row, 9);
            model.setValueAt(tglBaru, row, 8);
            updateBtnPerpanjang();
            JOptionPane.showMessageDialog(this, "📨 Perpanjangan berhasil. Tanggal kembali +5 hari.");

        } catch (Exception e) {
            tampilkanError("Gagal perpanjangan", e);
        }
    }

    // ──────────────────────────────────────────────────────
    //  Pindahkan ke riwayat
    // ──────────────────────────────────────────────────────
    private void pindahkanKeRiwayat() {
        int row = jTable1.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!"); return; }

        String status = model.getValueAt(row, 9).toString().trim().toLowerCase();
        if (!status.equals("selesai")) {
            JOptionPane.showMessageDialog(this,
                "Tombol hanya aktif jika status selesai.\nStatus saat ini: " + status.toUpperCase());
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Kirim ke Riwayat Transaksi?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        Connection  c         = null;
        ResultSet   rsData    = null;
        try {
            c = koneksi.koneksi.koneksiDB();
            c.setAutoCommit(false);

            String kodePeminjaman = model.getValueAt(row, 1).toString();
            PreparedStatement psSelect = c.prepareStatement(
                "SELECT * FROM peminjaman WHERE kode_peminjaman = ?");
            psSelect.setString(1, kodePeminjaman);
            rsData = psSelect.executeQuery();

            if (!rsData.next()) {
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan di database!");
                return;
            }

            // Salin ke riwayat
            PreparedStatement psInsert = c.prepareStatement(
                "INSERT INTO riwayat_peminjaman(peminjaman_id,kode_peminjaman,user_id,buku_id," +
                "kd_bk1,kd_bk2,kd_bk3,jumlah_pinjam,tanggal_pinjam,tanggal_kembali,status," +
                "denda,bayar,kembali,total,catatan,catatan_pengajuan,update_by,update_at," +
                "created_by,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            int    peminjaman_id  = rsData.getInt("peminjaman_id");
            String buku_id        = rsData.getString("buku_id");
            String kd_bk1         = rsData.getString("kd_bk1");
            String kd_bk2         = rsData.getString("kd_bk2");
            String kd_bk3         = rsData.getString("kd_bk3");
            int    jumlah_pinjam  = rsData.getInt("jumlah_pinjam");

            psInsert.setInt(1,       peminjaman_id);
            psInsert.setString(2,    kodePeminjaman);
            psInsert.setString(3,    rsData.getString("user_id"));
            psInsert.setString(4,    buku_id);
            psInsert.setString(5,    kd_bk1);
            psInsert.setString(6,    kd_bk2);
            psInsert.setString(7,    kd_bk3);
            psInsert.setInt(8,       jumlah_pinjam);
            psInsert.setDate(9,      rsData.getDate("tanggal_pinjam"));
            psInsert.setDate(10,     rsData.getDate("tanggal_kembali"));
            psInsert.setString(11,   rsData.getString("status"));
            psInsert.setInt(12,      rsData.getInt("denda"));
            psInsert.setInt(13,      rsData.getInt("bayar"));
            psInsert.setInt(14,      rsData.getInt("kembali"));
            psInsert.setInt(15,      rsData.getInt("total"));
            psInsert.setString(16,   rsData.getString("catatan"));
            psInsert.setString(17,   rsData.getString("catatan_pengajuan"));
            psInsert.setString(18,   rsData.getString("update_by"));
            psInsert.setTimestamp(19, rsData.getTimestamp("update_at"));
            psInsert.setString(20,   rsData.getString("created_by"));
            psInsert.setTimestamp(21, rsData.getTimestamp("created_at"));
            psInsert.executeUpdate();

            // Kembalikan item buku → tersedia
            updateStatusBanyakItem(c, new String[]{kd_bk1, kd_bk2, kd_bk3}, "tersedia");

            // Hapus dari peminjaman aktif & update stok
            PreparedStatement psDelete = c.prepareStatement(
                "DELETE FROM peminjaman WHERE peminjaman_id = ?");
            psDelete.setInt(1, peminjaman_id);
            psDelete.executeUpdate();

            kembalikanStok(c, buku_id, jumlah_pinjam);

            c.commit();
            model.removeRow(row);
            JOptionPane.showMessageDialog(this, "📚 Data dipindahkan ke Riwayat Transaksi.");

        } catch (Exception e) {
            try { if (c != null) c.rollback(); } catch (Exception ex) { /* abaikan */ }
            tampilkanError("Terjadi error", e);
        } finally {
            try { if (rsData != null) rsData.close(); } catch (Exception ex) { /* abaikan */ }
            try { if (c != null) c.close(); } catch (Exception ex) { /* abaikan */ }
        }
    }

    // ──────────────────────────────────────────────────────
//  Slip pengembalian terformat (versi A6 dengan logo & TTD)
// ──────────────────────────────────────────────────────
private void cetakSlipPengembalianFormat(
        String kode, String noAnggota, String nama, String judulBuku,
        List<String[]> daftarBuku,
        String tglPinjam, String tglKembali, String denda) {
    try {
        if (daftarBuku == null || daftarBuku.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data buku untuk dicetak!");
            return;
        }

        // Path default ke Documents/slip-pengembalian
        String documents = System.getProperty("user.home") + File.separator + "Documents";
        String slipDir   = documents + File.separator + "slip-pengembalian";
        File folder = new File(slipDir);
        if (!folder.exists()) folder.mkdirs();

        String path = slipDir + File.separator + "Slip_Pengembalian_" + kode + ".pdf";

        PdfWriter   writer = new PdfWriter(path);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf, PageSize.A6);
        doc.setMargins(10, 10, 10, 10);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // ── WARNA HIJAU untuk pengembalian ──
        com.itextpdf.kernel.colors.Color HIJAU = 
            new com.itextpdf.kernel.colors.DeviceRgb(39, 174, 96);
        com.itextpdf.kernel.colors.Color HIJAU_MUDA = 
            new com.itextpdf.kernel.colors.DeviceRgb(212, 237, 218);

        // Header
        Table header = new Table(new float[]{1, 3, 1});
        header.setWidth(UnitValue.createPercentValue(100));
        header.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        Image logoKiri  = new Image(ImageDataFactory.create(
            getClass().getResource("/img/logo.png"))).setHeight(35);
        Image logoKanan = new Image(ImageDataFactory.create(
            getClass().getResource("/img/logo.png"))).setHeight(35);

        header.addCell(new Cell().add(logoKiri)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setVerticalAlignment(VerticalAlignment.MIDDLE));

        header.addCell(new Cell().add(
            new Paragraph()
                .add(new Text("SLIP PENGEMBALIAN\n").setFont(bold).setFontSize(12))
                .add(new Text("PERPUSTAKAAN DIGITAL BOJONEGORO\n").setFont(bold).setFontSize(9))
                .add(new Text("Jl. Raya Surabaya–Bojonegoro, Kapas").setFont(font).setFontSize(8))
                .setTextAlignment(TextAlignment.CENTER))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setVerticalAlignment(VerticalAlignment.MIDDLE));

        header.addCell(new Cell().add(logoKanan)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setTextAlignment(TextAlignment.RIGHT)
            .setVerticalAlignment(VerticalAlignment.MIDDLE));

        doc.add(header);
        doc.add(new LineSeparator(new SolidLine(1f)));
        doc.add(new Paragraph(" "));

        // Info pengembalian
        Table info = new Table(new float[]{1, 2});
        info.setWidth(UnitValue.createPercentValue(60));
        info.addCell(infoCell("Kode Peminjaman",  bold)); 
        info.addCell(infoCell(": " + kode,         font));
        info.addCell(infoCell("No Anggota",        font)); 
        info.addCell(infoCell(": " + noAnggota,    font));
        info.addCell(infoCell("Nama",              font)); 
        info.addCell(infoCell(": " + nama,         font));
        info.addCell(infoCell("Judul Buku",        font)); 
        info.addCell(infoCell(": " + judulBuku,    font));
        info.addCell(infoCell("Tgl Pinjam",        font)); 
        info.addCell(infoCell(": " + tglPinjam,    font));
        info.addCell(infoCell("Tgl Kembali",       font)); 
        info.addCell(infoCell(": " + tglKembali,   font));
        info.addCell(infoCell("Tgl Dikembalikan",  font)); 
        info.addCell(infoCell(": " + LocalDate.now(), font));
        info.addCell(infoCell("Denda",             bold)); 
        info.addCell(infoCell(": " + denda,        font));
        info.addCell(infoCell("Status",            bold)); 
        info.addCell(infoCell(": SELESAI ✓",       font));
        doc.add(info);
        doc.add(new Paragraph(" "));

        // Tabel kode buku — header HIJAU
        Table tabel = new Table(new float[]{2, 2, 2, 1});
        tabel.setWidth(UnitValue.createPercentValue(100));

        // Header hijau
        String[] headers = {"Kode Buku 1", "Kode Buku 2", "Kode Buku 3", "Jml Pinjam"};
        for (String h : headers) {
            tabel.addHeaderCell(
                new Cell().add(new Paragraph(h).setFont(bold).setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE))
                .setBackgroundColor(HIJAU)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5)
            );
        }

        for (int i = 0; i < daftarBuku.size(); i++) {
            String[] buku = daftarBuku.get(i);
            com.itextpdf.kernel.colors.Color bg = 
                (i % 2 == 0) ? com.itextpdf.kernel.colors.ColorConstants.WHITE : HIJAU_MUDA;
            for (String col : buku) {
                tabel.addCell(
                    new Cell().add(new Paragraph(col == null ? "N/A" : col)
                        .setFont(font).setFontSize(7))
                    .setBackgroundColor(bg)
                    .setPadding(4)
                );
            }
        }

        doc.add(tabel);
        doc.add(new Paragraph(" ").setMarginBottom(15));

        doc.add(new Paragraph("\"Terima kasih telah mengembalikan buku\ntepat waktu. Sampai jumpa kembali!\"")
            .setFont(font).setFontSize(7).setItalic());
        doc.add(new Paragraph(" "));

        // TTD
        Image ttd = new Image(ImageDataFactory.create(
            getClass().getResource("/img/ttd.png")));
        ttd.setWidth(50).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        doc.add(ttd);
        doc.add(new Paragraph("Ayanagi Noshiro S.KOM")
            .setFont(bold).setFontSize(8)
            .setTextAlignment(TextAlignment.RIGHT));
        doc.add(new Paragraph("Admin Perpustakaan Digital Bojonegoro")
            .setFont(font).setFontSize(7).setItalic()
            .setTextAlignment(TextAlignment.RIGHT));

        doc.close();
        Desktop.getDesktop().open(new File(path));

    } catch (Exception e) {
        tampilkanError("Gagal cetak slip pengembalian format", e);
    }
}

    // ──────────────────────────────────────────────────────
    //  Helper PDF
    // ──────────────────────────────────────────────────────
    private Document buatDokumenPdf(String path) throws Exception {
        return new Document(new PdfDocument(new PdfWriter(path)));
    }

    private Table buatTabelBuku(String bukuId, String judul, String kategori, int jumlah, String tglKembali) {
        Table t = new Table(5);
        t.addHeaderCell("Buku ID");
        t.addHeaderCell("Judul");
        t.addHeaderCell("Kategori");
        t.addHeaderCell("Jumlah");
        t.addHeaderCell("Tanggal Kembali");
        t.addCell(bukuId);
        t.addCell(judul);
        t.addCell(kategori);
        t.addCell(String.valueOf(jumlah));
        t.addCell(tglKembali);
        return t;
    }

    private void tambahBarisTabel(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)));
        table.addCell(new Cell().add(new Paragraph(value)));
    }

    private Cell headerCell(String text, PdfFont bold) {
        try {
            return new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(7).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(ColorConstants.BLUE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        } catch (Exception e) { return new Cell().add(new Paragraph(text)); }
    }

    private Cell bodyCell(String text, PdfFont font) {
        try {
            return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(7))
                .setPadding(4);
        } catch (Exception e) { return new Cell().add(new Paragraph(text)); }
    }

    private Cell infoCell(String text, PdfFont font) {
        try {
            return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(7))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(3);
        } catch (Exception e) { return new Cell().add(new Paragraph(text)); }
    }

    // ──────────────────────────────────────────────────────
    //  Utility umum
    // ──────────────────────────────────────────────────────
    private String safe(String value) { return value == null ? "" : value; }

    private String formatRupiah(double angka) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(angka);
    }

    private void tampilkanError(String judul, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
            judul + ":\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ──────────────────────────────────────────────────────
    //  Event handlers (dipanggil dari initComponents)
    // ──────────────────────────────────────────────────────

    // Tombol Simpan
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        simpanTransaksi();
    }

    // Tombol Bayar Denda
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        bayarDenda();
    }

    // Tombol Slip Peminjaman
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        cetakSlipPeminjaman();
    }

    // Tombol Slip Pengembalian
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
        cetakSlipPengembalian();
    }

    // Tombol Refresh
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
        resetForm();
        getData();
    }

    // Tombol Batalkan Peminjaman
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {
        batalkanAtauUbahJumlah();
    }

    // Tombol Peminjaman Selesai
    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {
        pindahkanKeRiwayat();
    }

    // Tombol Ajukan Perpanjangan
    private void btnPerpanjangActionPerformed(java.awt.event.ActionEvent evt) {
        ajukanPerpanjangan();
    }

    // Tombol Cari
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        search();
    }

    // Klik baris tabel
    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {
        pilihData();
    }

    // Search realtime (filter combo)
    private void cmb_transaksiActionPerformed(java.awt.event.ActionEvent evt) {
        search();
    }

    private void txt_cariActionPerformed(java.awt.event.ActionEvent evt) {
        search();
    }

    private void txt_cariKeyReleased(java.awt.event.KeyEvent evt) {
        search();
    }

    private void kode_peminjamanActionPerformed(java.awt.event.ActionEvent evt) {
        // tidak digunakan
    }

    // ──────────────────────────────────────────────────────
    //  Generated GUI – JANGAN DIUBAH MANUAL
    // ──────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        txt_cari = new javax.swing.JTextField();
        cmb_transaksi = new javax.swing.JComboBox<String>();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        id_user = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        kode_peminjaman = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cmbstatus = new javax.swing.JComboBox<String>();
        jLabel6 = new javax.swing.JLabel();
        denda = new javax.swing.JTextField();
        cmb_kd1 = new javax.swing.JComboBox<String>();
        cmb_kd2 = new javax.swing.JComboBox<String>();
        cmb_kd3 = new javax.swing.JComboBox<String>();
        jButton7 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtcatatan = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        btnPerpanjang = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();

        jButton4.setText("jButton4");

        setBackground(new java.awt.Color(197, 216, 157));
        setMaximumSize(new java.awt.Dimension(1860, 1080));
        setMinimumSize(new java.awt.Dimension(1860, 1080));
        setPreferredSize(new java.awt.Dimension(1860, 1080));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

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
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 200, 1740, 470));

        txt_cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_cariActionPerformed(evt);
            }
        });
        txt_cari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_cariKeyReleased(evt);
            }
        });
        add(txt_cari, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 110, 600, 30));

        cmb_transaksi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "pending ", "dipinjam", "selesai", "ditolak ", "diterima" }));
        cmb_transaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmb_transaksiActionPerformed(evt);
            }
        });
        add(cmb_transaksi, new org.netbeans.lib.awtextra.AbsoluteConstraints(1170, 110, 160, 40));

        jButton1.setText(".");
        jButton1.setContentAreaFilled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1380, 110, 220, 40));

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("kode peminjaman");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 700, -1, -1));

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("kode Buku 1");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 790, -1, -1));

        jButton2.setText(".");
        jButton2.setContentAreaFilled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 1000, 140, 40));

        jButton3.setText(".");
        jButton3.setContentAreaFilled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 1000, 170, 40));

        jButton5.setText(".");
        jButton5.setContentAreaFilled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 1000, 200, 40));

        jButton6.setText(".");
        jButton6.setContentAreaFilled(false);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 990, 240, 40));

        id_user.setForeground(new java.awt.Color(255, 255, 255));
        id_user.setText("jLabel3");
        add(id_user, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 30, 220, -1));

        username.setForeground(new java.awt.Color(255, 255, 255));
        username.setText("jLabel3");
        add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 30, 220, -1));

        jLabel3.setText("kode Buku 2");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 790, -1, -1));

        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("kode Buku 3");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 890, -1, -1));

        kode_peminjaman.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kode_peminjamanActionPerformed(evt);
            }
        });
        add(kode_peminjaman, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 730, 290, 25));

        jLabel5.setText("Status");
        add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 700, -1, -1));

        cmbstatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "pending", "dipinjam", "selesai", "ditolak", "diterima" }));
        add(cmbstatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 730, 190, -1));

        jLabel6.setText("Denda");
        add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 790, -1, -1));

        denda.setText("1000");
        add(denda, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 820, 320, -1));

        add(cmb_kd1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 820, 200, -1));

        add(cmb_kd2, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 820, 220, -1));

        add(cmb_kd3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 920, 210, -1));

        jButton7.setText(".");
        jButton7.setContentAreaFilled(false);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(1670, 100, 124, 50));

        txtcatatan.setColumns(20);
        txtcatatan.setRows(5);
        jScrollPane2.setViewportView(txtcatatan);

        add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1220, 776, 330, 110));

        jLabel7.setText("Catatan");
        add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(1230, 740, -1, -1));

        jLabel8.setText("Name user :");
        add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(37, 12, -1, -1));

        jButton8.setText(".");
        jButton8.setContentAreaFilled(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 990, 250, 40));

        btnPerpanjang.setText(".");
        btnPerpanjang.setContentAreaFilled(false);
        btnPerpanjang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPerpanjangActionPerformed(evt);
            }
        });
        add(btnPerpanjang, new org.netbeans.lib.awtextra.AbsoluteConstraints(1330, 990, 250, 40));

        jButton9.setText(".");
        jButton9.setContentAreaFilled(false);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(1610, 990, 140, 40));

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/1860/Peminjaman & Denda.png"))); // NOI18N
        jLabel9.setText("jLabel9");
        jLabel9.setPreferredSize(new java.awt.Dimension(1920, 1080));
        add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPerpanjang;
    private javax.swing.JComboBox<String> cmb_kd1;
    private javax.swing.JComboBox<String> cmb_kd2;
    private javax.swing.JComboBox<String> cmb_kd3;
    private javax.swing.JComboBox<String> cmb_transaksi;
    private javax.swing.JComboBox<String> cmbstatus;
    private javax.swing.JTextField denda;
    private javax.swing.JLabel id_user;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField kode_peminjaman;
    private javax.swing.JTextField txt_cari;
    private javax.swing.JTextArea txtcatatan;
    private javax.swing.JLabel username;
    // End of variables declaration//GEN-END:variables
}
