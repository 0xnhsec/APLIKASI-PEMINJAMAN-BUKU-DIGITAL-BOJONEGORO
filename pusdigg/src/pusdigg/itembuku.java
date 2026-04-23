package pusdigg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.JFileChooser;
import java.io.File;

/**
 * Form Item Buku untuk Admin
 * FITUR: Edit langsung di tabel (Kode Buku &amp; Status) + Tombol SAVE
 * @author snxx - Ambatukam - Raja Ngawi - Singa Harimaw 
 * Kuntilanak Jago Ngoding - Sawit siapa ini
 */
public class itembuku extends javax.swing.JPanel {

    private Connection conn = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;

    private DefaultTableModel model;

    private String id = session.getU_id();
    private String usernamee = session.getU_username();

    public itembuku() {
        initComponents();

        // Set user info
        id_user.setText("SELAMAT DATANG USER KE " + id);
        username.setText("ANDA LOGIN SEBAGAI " + usernamee);

        // Koneksi database
        conn = koneksi.koneksi.koneksiDB();

        // Setup table model DENGAN EDITABLE CELLS
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Hanya kolom Kode Buku (index 3) dan Status (index 4) yang bisa diedit
                return column == 3 || column == 4;
            }
        };
        
        jTable1.setModel(model);

        // Tambahkan kolom tabel
        model.addColumn("bukuitem_id");
        model.addColumn("buku_id");
        model.addColumn("Judul Buku");
        model.addColumn("Kode Buku");
        model.addColumn("Status");

        // Sembunyikan kolom ID
        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(0).setWidth(0);

        jTable1.getColumnModel().getColumn(1).setMinWidth(0);
        jTable1.getColumnModel().getColumn(1).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(1).setWidth(0);

        // Load data awal
        getData();
        hitungStatistik();
        setupComboBox();
    }

    /**
     * Setup ComboBox untuk filter
     */
    private void setupComboBox() {
        // Filter by Status
        filterbystatus.removeAllItems();
        filterbystatus.addItem("Semua Status");
        filterbystatus.addItem("tersedia");
        filterbystatus.addItem("dipinjam");
        filterbystatus.addItem("rusak");
        filterbystatus.addItem("hilang");

        // Filter by Name and Kode Buku
        filterbynameandkodebuku.removeAllItems();
        filterbynameandkodebuku.addItem("Urutkan Berdasarkan");
        filterbynameandkodebuku.addItem("Judul Buku (A-Z)");
        filterbynameandkodebuku.addItem("Kode Buku (0-9)");
    }

    /**
     * Ambil semua data buku item
     */
    private void getData() {
        model.setRowCount(0);

        try {
            String sql =
                "SELECT bi.bukuitem_id, bi.buku_id, b.Judul, bi.kode_buku, bi.status " +
                "FROM buku_item bi " +
                "JOIN buku b ON bi.buku_id = b.Buku_id " +
                "ORDER BY bi.bukuitem_id DESC";

            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();

            while (rs.next()) {
                Object[] obj = new Object[5];
                obj[0] = rs.getInt("bukuitem_id");
                obj[1] = rs.getInt("buku_id");
                obj[2] = rs.getString("Judul");
                obj[3] = rs.getString("kode_buku");
                obj[4] = rs.getString("status");
                model.addRow(obj);
            }

            rs.close();
            pst.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error getData: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    void pilihData() {
        int i = jTable1.getSelectedRow();
        jTextField1.setText(model.getValueAt(i, 2).toString());
        jTextField2.setText(model.getValueAt(i, 3).toString());
        String kategoriDariTabel = model.getValueAt(i, 4).toString();
        jComboBox1.setSelectedItem(kategoriDariTabel);
    }

    
         void bersih() {
            jTextField1.setText("");
            jTextField2.setText("");
            jumlah_buku_tersedia.setText("");
            jumlah_buku_terpinjam.setText("");
            jumlah_buku_rusak.setText("");
           jComboBox1.setSelectedItem(null);
            
        }
    /**
     * FITUR SEARCH - Cari berdasarkan Judul atau Kode Buku
     */
    private void cariData() {
        model.setRowCount(0);
        String keyword = searchstatus.getText().trim();
        String filterStatus = filterbystatus.getSelectedItem().toString();
        String sortBy = filterbynameandkodebuku.getSelectedItem().toString();

        try {
            StringBuilder query = new StringBuilder(
                "SELECT bi.bukuitem_id, bi.buku_id, b.Judul, bi.kode_buku, bi.status " +
                "FROM buku_item bi " +
                "JOIN buku b ON bi.buku_id = b.Buku_id WHERE 1=1"
            );

            // Filter berdasarkan keyword (Judul atau Kode Buku)
            if (!keyword.isEmpty()) {
                query.append(" AND (b.Judul LIKE ? OR bi.kode_buku LIKE ?)");
            }

            // Filter berdasarkan Status
            if (!filterStatus.equals("Semua Status")) {
                query.append(" AND bi.status = ?");
            }

            // Sorting (Urutkan)
            if (sortBy.equals("Judul Buku (A-Z)")) {
                query.append(" ORDER BY b.Judul ASC");
            } else if (sortBy.equals("Kode Buku (0-9)")) {
                query.append(" ORDER BY bi.kode_buku ASC");
            } else {
                query.append(" ORDER BY bi.bukuitem_id DESC");
            }

            pst = conn.prepareStatement(query.toString());

            int paramIndex = 1;

            // Set parameter untuk keyword
            if (!keyword.isEmpty()) {
                pst.setString(paramIndex++, "%" + keyword + "%");
                pst.setString(paramIndex++, "%" + keyword + "%");
            }

            // Set parameter untuk status
            if (!filterStatus.equals("Semua Status")) {
                pst.setString(paramIndex++, filterStatus);
            }

            rs = pst.executeQuery();

            while (rs.next()) {
                Object[] obj = new Object[5];
                obj[0] = rs.getInt("bukuitem_id");
                obj[1] = rs.getInt("buku_id");
                obj[2] = rs.getString("Judul");
                obj[3] = rs.getString("kode_buku");
                obj[4] = rs.getString("status");
                model.addRow(obj);
            }

            rs.close();
            pst.close();

            // Update statistik setelah filter
            hitungStatistik();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cariData: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * SIMPAN SEMUA PERUBAHAN DI TABEL KE DATABASE
     * Ini dipanggil ketika tombol SAVE diklik
     */
private void saveAllChanges() {
    try {
        boolean hasError = false;
        int savedCount = 0;
        StringBuilder errorDetails = new StringBuilder();

        for (int i = 0; i < jTable1.getRowCount(); i++) {
            int bukuItemId = (int) jTable1.getValueAt(i, 0);
            String kodeBuku = jTable1.getValueAt(i, 3).toString().trim();

            Object statusObj = jTable1.getValueAt(i, 4);
            String status = (statusObj != null)
                    ? statusObj.toString().trim().toLowerCase()
                    : "";

            // ===== VALIDASI =====
            if (kodeBuku.isEmpty()) {
                errorDetails.append("❌ Baris ").append(i + 1)
                        .append(": Kode buku kosong\n");
                hasError = true;
                continue;
            }

            // hanya boleh: tersedia, hilang, rusak
            if (!(status.equals("tersedia")
                || status.equals("hilang")
                || status.equals("rusak"))) {

                String tampil = status.isEmpty() ? "[KOSONG]" : status;

                errorDetails.append("❌ Baris ").append(i + 1)
                        .append(": Status tidak valid (")
                        .append(tampil).append(")\n")
                        .append("   Status yang diperbolehkan: hilang dan rusak\n\n");
                hasError = true;
                continue;
            }

            // ===== UPDATE =====
            String sql = "UPDATE buku_item SET kode_buku=?, status=? WHERE bukuitem_id=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, kodeBuku);
            pst.setString(2, status);
            pst.setInt(3, bukuItemId);

            if (pst.executeUpdate() > 0) savedCount++;
            pst.close();
        }

        // ===== HASIL =====
        if (hasError) {
            JOptionPane.showMessageDialog(null,
                "⚠️ Sebagian data gagal disimpan!\n\n" +
                errorDetails +
                "\n✅ Berhasil disimpan: " + savedCount + " baris\n" +
                "Perbaiki data yang error lalu SAVE lagi.",
                "Peringatan",
                JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                "✅ Semua perubahan berhasil disimpan!\nTotal: " + savedCount + " baris",
                "Sukses",
                JOptionPane.INFORMATION_MESSAGE);
        }

        cariData();
        hitungStatistik();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null,
            "Error saat menyimpan: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

private boolean isValidStatus(String status) {
    return status.equals("tersedia") || status.equals("dipinjam") 
        || status.equals("rusak") || status.equals("hilang");
}

    /**
     * HITUNG STATISTIK - Jumlah buku per status
     */
    private void hitungStatistik() {
        try {
            // Hitung buku tersedia
            String sqlTersedia = "SELECT COUNT(*) AS total FROM buku_item WHERE status = 'tersedia'";
            pst = conn.prepareStatement(sqlTersedia);
            rs = pst.executeQuery();
            if (rs.next()) {
                jumlah_buku_tersedia.setText("Tersedia: " + rs.getInt("total"));
            }
            rs.close();
            pst.close();

            // Hitung buku dipinjam
            String sqlDipinjam = "SELECT COUNT(*) AS total FROM buku_item WHERE status = 'dipinjam'";
            pst = conn.prepareStatement(sqlDipinjam);
            rs = pst.executeQuery();
            if (rs.next()) {
                jumlah_buku_terpinjam.setText("Dipinjam: " + rs.getInt("total"));
            }
            rs.close();
            pst.close();

            // Hitung buku rusak
            String sqlRusak = "SELECT COUNT(*) AS total FROM buku_item WHERE status = 'rusak'";
            pst = conn.prepareStatement(sqlRusak);
            rs = pst.executeQuery();
            if (rs.next()) {
                jumlah_buku_rusak.setText("Rusak: " + rs.getInt("total"));
            }
            rs.close();
            pst.close();

            // Hitung buku hilang
            String sqlHilang = "SELECT COUNT(*) AS total FROM buku_item WHERE status = 'hilang'";
            pst = conn.prepareStatement(sqlHilang);
            rs = pst.executeQuery();
            if (rs.next()) {
                jumlah_buku_hilang.setText("Hilang: " + rs.getInt("total"));
            }
            rs.close();
            pst.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error hitungStatistik: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * EXPORT KE EXCEL
     */
private void exportToExcel() {
    System.out.println("EXPORT DIPANGGIL");

    try {
        // 1. Cek data tabel
        int rowCount = jTable1.getRowCount();
        System.out.println("Jumlah baris: " + rowCount);

        if (rowCount == 0) {
            JOptionPane.showMessageDialog(null,
                    "Tidak ada data untuk di-export!",
                    "Info",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Tentukan folder default
        String userHome = System.getProperty("user.home");
        File folder = new File(userHome + File.separator + "Documents");
        if (!folder.exists()) {
            folder = new File(userHome + File.separator + "Desktop");
        }

        // 3. File chooser
        JFileChooser chooser = new JFileChooser(folder);
        chooser.setDialogTitle("Simpan File Excel");

        String time = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new java.util.Date());
        chooser.setSelectedFile(new File("Data_Item_Buku_" + time + ".xlsx"));

        int choose = chooser.showSaveDialog(this);
        if (choose != JFileChooser.APPROVE_OPTION) {
            System.out.println("Export dibatalkan");
            return;
        }

        File file = chooser.getSelectedFile();
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".xlsx")) {
            path += ".xlsx";
        }

        System.out.println("Saving to: " + path);

        // 4. Buat workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data Item Buku");

        // 5. Header
        Row header = sheet.createRow(0);
        String[] kolom = {"Judul Buku", "Kode Buku", "Status"};

        for (int i = 0; i < kolom.length; i++) {
            header.createCell(i).setCellValue(kolom[i]);
        }

        // 6. Isi data
        for (int i = 0; i < rowCount; i++) {
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(
                    jTable1.getValueAt(i, 2) != null
                            ? jTable1.getValueAt(i, 2).toString()
                            : ""
            );

            row.createCell(1).setCellValue(
                    jTable1.getValueAt(i, 3) != null
                            ? jTable1.getValueAt(i, 3).toString()
                            : ""
            );

            row.createCell(2).setCellValue(
                    jTable1.getValueAt(i, 4) != null
                            ? jTable1.getValueAt(i, 4).toString()
                            : ""
            );
        }

        // 7. Autosize
        for (int i = 0; i < kolom.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 8. Simpan file
        FileOutputStream out = new FileOutputStream(path);
        System.out.println("Stream OK");

        workbook.write(out);
        System.out.println("WRITE OK");

        out.close();
        workbook.close();

        // 9. Konfirmasi
        File saved = new File(path);
        if (saved.exists()) {
            JOptionPane.showMessageDialog(null,
                    "✅ Export BERHASIL!\n\n"
                    + "Lokasi:\n" + path + "\n"
                    + "Total Data: " + rowCount,
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

            // buka folder
            try {
                java.awt.Desktop.getDesktop().open(saved.getParentFile());
            } catch (Exception ex) {
                System.out.println("Tidak bisa buka folder");
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "⚠ File tidak ditemukan setelah export!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null,
                "❌ Export GAGAL:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

/**
 * Generate buku_item otomatis berdasarkan stok di tabel buku
 * Hanya generate untuk buku yang belum punya buku_item
 */
private void generateBukuItem() {
    int confirm = JOptionPane.showConfirmDialog(null,
        "Generate item buku otomatis berdasarkan stok?\n" +
        "Hanya buku yang belum punya item yang akan di-generate.",
        "Generate Buku Item", JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) return;

    try {
        // Ambil semua buku yang belum punya buku_item
        String sqlBuku = 
            "SELECT b.Buku_id, b.Judul, b.stok " +
            "FROM buku b " +
            "WHERE b.stok > 0 " +
            "AND (SELECT COUNT(*) FROM buku_item bi WHERE bi.buku_id = b.Buku_id) = 0";

        pst = conn.prepareStatement(sqlBuku);
        rs = pst.executeQuery();

        int totalGenerated = 0;

        while (rs.next()) {
            int bukuId = rs.getInt("Buku_id");
            String judul = rs.getString("Judul");
            int stok = rs.getInt("stok");

            // Generate kode unik dan insert per item
            for (int i = 1; i <= stok; i++) {
                // Ambil kode terakhir
                String sqlKode = "SELECT MAX(CAST(SUBSTRING(kode_buku, 4) AS UNSIGNED)) AS maxKode FROM buku_item";
                PreparedStatement pstKode = conn.prepareStatement(sqlKode);
                ResultSet rsKode = pstKode.executeQuery();
                int lastKode = 0;
                if (rsKode.next()) lastKode = rsKode.getInt("maxKode");
                rsKode.close();
                pstKode.close();

                String kode = String.format("BK-%04d", lastKode + 1);

                String sqlInsert = "INSERT INTO buku_item (buku_id, kode_buku, status) VALUES (?, ?, 'tersedia')";
                PreparedStatement pstInsert = conn.prepareStatement(sqlInsert);
                pstInsert.setInt(1, bukuId);
                pstInsert.setString(2, kode);
                pstInsert.executeUpdate();
                pstInsert.close();

                totalGenerated++;
            }
        }

        rs.close();
        pst.close();

        JOptionPane.showMessageDialog(null,
            "✅ Berhasil generate " + totalGenerated + " item buku baru!",
            "Sukses", JOptionPane.INFORMATION_MESSAGE);

        getData();
        hitungStatistik();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null,
            "Gagal generate: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<String>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        searchstatus = new javax.swing.JTextField();
        filterbynameandkodebuku = new javax.swing.JComboBox<String>();
        cari = new javax.swing.JButton();
        id_user = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        filterbystatus = new javax.swing.JComboBox<String>();
        button_excel = new javax.swing.JButton();
        save = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jumlah_buku_tersedia = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jumlah_buku_hilang = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jumlah_buku_rusak = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jumlah_buku_terpinjam = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<String>();
        jLabel5 = new javax.swing.JLabel();

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jList1);

        setBackground(new java.awt.Color(197, 216, 157));
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

        add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 240, 1780, 430));

        jLabel1.setText("Menu Kode Buku");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 0, -1, -1));

        searchstatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchstatusActionPerformed(evt);
            }
        });
        add(searchstatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 170, 438, 30));

        filterbynameandkodebuku.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "filter by name and kode buku", " " }));
        add(filterbynameandkodebuku, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 170, 210, 30));

        cari.setText(".");
        cari.setContentAreaFilled(false);
        cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cariActionPerformed(evt);
            }
        });
        add(cari, new org.netbeans.lib.awtextra.AbsoluteConstraints(1070, 165, 210, 40));

        id_user.setText("id_user");
        add(id_user, new org.netbeans.lib.awtextra.AbsoluteConstraints(1220, 60, -1, -1));

        username.setText("username");
        add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(1220, 110, -1, -1));

        filterbystatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Filter by status", " " }));
        add(filterbystatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 170, 160, 40));

        button_excel.setText(".");
        button_excel.setContentAreaFilled(false);
        button_excel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_excelActionPerformed(evt);
            }
        });
        add(button_excel, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 950, 210, 30));

        save.setText(".");
        save.setContentAreaFilled(false);
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });
        add(save, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 940, 150, 40));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setOpaque(false);

        jumlah_buku_tersedia.setText("buku yg tersedia");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(66, Short.MAX_VALUE)
                .addComponent(jumlah_buku_tersedia)
                .addGap(60, 60, 60))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jumlah_buku_tersedia)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(78, 702, 220, 50));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setOpaque(false);

        jumlah_buku_hilang.setText("Hilang");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jumlah_buku_hilang)
                .addContainerGap(43, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jumlah_buku_hilang)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 702, -1, 50));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setOpaque(false);

        jumlah_buku_rusak.setText("Rusak");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(86, 86, 86)
                .addComponent(jumlah_buku_rusak)
                .addContainerGap(100, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jumlah_buku_rusak)
                .addGap(25, 25, 25))
        );

        add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(688, 700, 220, 50));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setOpaque(false);

        jumlah_buku_terpinjam.setText("Terpinjam");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jumlah_buku_terpinjam)
                .addContainerGap(141, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(31, Short.MAX_VALUE)
                .addComponent(jumlah_buku_terpinjam)
                .addGap(25, 25, 25))
        );

        add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(992, 680, 220, 72));

        jButton1.setText(".");
        jButton1.setContentAreaFilled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1360, 160, 340, 60));

        jLabel2.setText("name buku:");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 750, -1, -1));

        jLabel3.setText("Kode Buku:");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 830, -1, -1));

        jLabel4.setText("Status");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 830, -1, -1));
        add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 780, 270, 30));
        add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 850, 270, 40));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tersedia", "Rusak", "Hilang", " " }));
        jComboBox1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jComboBox1MouseClicked(evt);
            }
        });
        add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 780, 230, -1));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/1860/status & inventaris buku.png"))); // NOI18N
        jLabel5.setText("jLabel5");
        jLabel5.setPreferredSize(new java.awt.Dimension(1860, 1080));
        add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1860, 1080));
    }// </editor-fold>//GEN-END:initComponents

    private void searchstatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchstatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchstatusActionPerformed

    private void cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cariActionPerformed
        cariData();
    }//GEN-LAST:event_cariActionPerformed

    private void button_excelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_excelActionPerformed
        exportToExcel();
    }//GEN-LAST:event_button_excelActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
int i = jTable1.getSelectedRow();
if (i == -1) {
    JOptionPane.showMessageDialog(null, "Pilih data terlebih dahulu!");
    return;
}

int id = Integer.parseInt(
    jTable1.getValueAt(i, 0).toString()
);

String status = jComboBox1.getSelectedItem().toString();

try {
    String sql = "UPDATE buku_item SET status=? WHERE bukuitem_id=?";
    pst = conn.prepareStatement(sql);
    pst.setString(1, status);
    pst.setInt(2, id);

    int hasil = pst.executeUpdate();

    if (hasil > 0) {
        JOptionPane.showMessageDialog(null, "Status berhasil disimpan");
    } else {
        JOptionPane.showMessageDialog(null, "Data tidak berubah");
    }

} catch (Exception e) {
    JOptionPane.showMessageDialog(null, e.getMessage());
}

getData();
bersih();
    }//GEN-LAST:event_saveActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        pilihData();
    }//GEN-LAST:event_jTable1MouseClicked

    private void jComboBox1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jComboBox1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        getData();        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_excel;
    private javax.swing.JButton cari;
    private javax.swing.JComboBox<String> filterbynameandkodebuku;
    private javax.swing.JComboBox<String> filterbystatus;
    private javax.swing.JLabel id_user;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JLabel jumlah_buku_hilang;
    private javax.swing.JLabel jumlah_buku_rusak;
    private javax.swing.JLabel jumlah_buku_terpinjam;
    private javax.swing.JLabel jumlah_buku_tersedia;
    private javax.swing.JButton save;
    private javax.swing.JTextField searchstatus;
    private javax.swing.JLabel username;
    // End of variables declaration//GEN-END:variables
}
