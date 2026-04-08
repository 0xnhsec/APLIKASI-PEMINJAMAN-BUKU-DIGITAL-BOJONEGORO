/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pusdigg;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JFileChooser;
import java.io.File;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Statement;


/**
 *
 * @author dppra
 */
public class buku extends javax.swing.JPanel {
    

    /**
     * Creates new form buku
     */
    
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    private DefaultTableModel  model;
    private String sql;
    private final DateFormat dfi = new SimpleDateFormat ("dd/MM/yyyy");
    Calendar hariini ;
    String pathGambar;
   private int kategoriId;
   private int bukuId;




    String id = session.getU_id();
    String usernamee = session.getU_username();    
    
   public buku() {
    initComponents();
    id_user.setText("SELAMAT DATANG "+id);
    username.setText("ANDA LOGIN SEBAGAI "+usernamee);
    
    AutoCompleteDecorator.decorate(cmb_kategori);
    
    conn = koneksi.koneksi.koneksiDB();

    model = new DefaultTableModel() {
   @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 10) { 
            return ImageIcon.class;
        }
        return Object.class;
    }

    };

jTable1.setModel(model);
jTable1.setRowHeight(80);



 // 0

model.addColumn("Buku_Id");
model.addColumn("Kategori_ID");
model.addColumn("Kategori");
model.addColumn("Judul");
model.addColumn("Penulis");
model.addColumn("Penerbit");
model.addColumn("Tahun Terbit");
model.addColumn("Stok");
model.addColumn("Deskripsi");
model.addColumn("Rak Buku");
model.addColumn("Gambar");
model.addColumn("PathGambar");
model.addColumn("Aksi"); // ⬅ kolom button




    getData();
    
    // Buku_ID
jTable1.getColumnModel().getColumn(0).setMinWidth(0);
jTable1.getColumnModel().getColumn(0).setMaxWidth(0);

// Kategori_ID
jTable1.getColumnModel().getColumn(1).setMinWidth(0);
jTable1.getColumnModel().getColumn(1).setMaxWidth(0);

jTable1.getColumnModel().getColumn(11).setMinWidth(0);
jTable1.getColumnModel().getColumn(11).setMaxWidth(0);

loadKategori();


}

        
    void getData() {
    model.setRowCount(0); // bersihkan tabel

    try {
        String sql =
            "SELECT b.*, k.name_kategori " +
            "FROM buku b " +
            "JOIN kategori k ON b.kategori_id = k.kategori_id";

        pst = conn.prepareStatement(sql);
        rs = pst.executeQuery();

        while (rs.next()) {
            Object[] obj = new Object[13];

            obj[0]  = rs.getInt("buku_id");
            obj[1]  = rs.getInt("kategori_id");
            obj[2]  = rs.getString("name_kategori");
            obj[3]  = rs.getString("judul");
            obj[4]  = rs.getString("penulis");
            obj[5]  = rs.getString("penerbit");
            obj[6]  = rs.getDate("tahun_terbit");
            obj[7]  = rs.getInt("stok");
            obj[8]  = rs.getString("deskripsi");
            obj[9]  = rs.getString("rak_buku");

            // gambar
            String path = rs.getString("imgsampul");
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(60, 80, Image.SCALE_SMOOTH);
            obj[10] = new ImageIcon(img);
            
            // path gambar hidden
            obj[11] = path;
            obj[12] = "Detail";


            model.addRow(obj);
        }

        jTable1.setModel(model);
        jTable1.getColumnModel().getColumn(12)
        .setCellRenderer(new ButtonRenderer());

        jTable1.getColumnModel().getColumn(12)
                .setCellEditor(new ButtonEditor(new JCheckBox(), jTable1));

        jTable1.getColumnModel().getColumn(12).setPreferredWidth(80);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, e);
    }
}

private int getLastKodeBuku(Connection conn) throws SQLException {
    int last = 0;

    String sql = "SELECT MAX(CAST(SUBSTRING(kode_buku, 4) AS UNSIGNED)) FROM buku_item";
    PreparedStatement ps = conn.prepareStatement(sql);
    ResultSet rs = ps.executeQuery();

    if (rs.next()) {
        last = rs.getInt(1); // kalau NULL -> 0
    }

    rs.close();
    ps.close();

    return last;
}




    
    public class ButtonRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        JButton btn = new JButton("Detail");
        btn.setFocusPainted(false);
        return btn;
    }
}
 
public class ButtonEditor extends DefaultCellEditor {

    private JButton button;
    private int row;
    private JTable table;

    public ButtonEditor(JCheckBox checkBox, JTable table) {
        super(checkBox);
        this.table = table;

        button = new JButton("Detail");
        button.setFocusPainted(false);

        button.addActionListener(e -> showDetail());
    }

    private void showDetail() {
        int i = table.getSelectedRow();
        if (i == -1) return;

        // ambil data dari tabel
        String judul = table.getValueAt(i, 3).toString();
        String penulis = table.getValueAt(i, 4).toString();
        String penerbit = table.getValueAt(i, 5).toString();
        String kategori = table.getValueAt(i, 2).toString();
        String stok = table.getValueAt(i, 7).toString();
        String deskripsi = table.getValueAt(i, 8).toString();
        String rak = table.getValueAt(i, 9).toString();
        String path = table.getValueAt(i, 11).toString();

        // gambar
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(180, 240, Image.SCALE_SMOOTH);
        JLabel lblImg = new JLabel(new ImageIcon(img));

        JTextArea txt = new JTextArea(
                "Judul     : " + judul +
                "\nPenulis  : " + penulis +
                "\nPenerbit : " + penerbit +
                "\nKategori : " + kategori +
                "\nStok     : " + stok +
                "\nRak      : " + rak +
                "\n\nDeskripsi:\n" + deskripsi
        );
        txt.setEditable(false);
        txt.setWrapStyleWord(true);
        txt.setLineWrap(true);

        JScrollPane sp = new JScrollPane(txt);
        sp.setPreferredSize(new Dimension(300, 200));

        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.add(lblImg, BorderLayout.WEST);
        panel.add(sp, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                null,
                panel,
                "Detail Buku",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        this.row = row;
        return button;
    }
}
    
void loadKategori() {
    try {
        cmb_kategori.removeAllItems();

        cmb_kategori.addItem("Semua Kategori");

        String sql = "SELECT name_kategori FROM kategori ORDER BY name_kategori ASC";
        pst = conn.prepareStatement(sql);
        rs = pst.executeQuery();

        while (rs.next()) {
            cmb_kategori.addItem(rs.getString("name_kategori"));
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null,
            "Load kategori gagal: " + e.getMessage());
    }
}








      
//     void pilihData() {
//    int i = jTable1.getSelectedRow();
//    if (i == -1) return;
//
//    txt_kategori.setText(model.getValueAt(i, 1).toString());
//    judul.setText(model.getValueAt(i, 2).toString());
//    penulis.setText(model.getValueAt(i, 3).toString());
//    penerbit.setText(model.getValueAt(i, 4).toString());
//
//    // ⬇ JDateChooser
//    java.util.Date tanggal = (java.util.Date) model.getValueAt(i, 5);
//    tgl.setDate(tanggal);
//
//    stok.setText(model.getValueAt(i, 6).toString());
//    deskripsi.setText(model.getValueAt(i, 8).toString());
//    upload.setText(model.getValueAt(i, 9).toString());
//    
//}
   void pilihData() {
    int i = jTable1.getSelectedRow();
    if (i == -1) return;

    // PK & FK
    bukuId = Integer.parseInt(model.getValueAt(i, 0).toString());
    kategoriId = Integer.parseInt(model.getValueAt(i, 1).toString());

    // text field
    txt_kategori.setText(model.getValueAt(i, 2).toString());
    judul.setText(model.getValueAt(i, 3).toString());
    penulis.setText(model.getValueAt(i, 4).toString());
    penerbit.setText(model.getValueAt(i, 5).toString());

    // tanggal
    java.util.Date tanggal = (java.util.Date) model.getValueAt(i, 6);
    tgl.setDate(tanggal);

    stok.setText(model.getValueAt(i, 7).toString());
    deskripsi.setText(model.getValueAt(i, 8).toString()); // ✅ benar
    rak.setText(model.getValueAt(i, 9).toString());       // ✅ benar

    // path gambar (hidden column)
    pathGambar = model.getValueAt(i, 11).toString();
    upload.setText(pathGambar);
}




    
    


        void bersih() {
            judul.setText("");
            penulis.setText("");
            penerbit.setText("");
            tgl.setDate(null);
            stok.setText("");
            rak.setText("");
            txt_kategori.setText("");
            deskripsi.setText("");
            upload.setText("");
            txt_cari.setText("");
           cmb_kategori.setSelectedItem(null);
            
        }
   
    public void setKategori(int id, String nama) {
    this.kategoriId = id;      // untuk DB
    txt_kategori.setText(nama); // untuk tampilan
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
        judul = new javax.swing.JTextField();
        penulis = new javax.swing.JTextField();
        upload = new javax.swing.JTextField();
        tgl = new com.toedter.calendar.JDateChooser();
        penerbit = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        deskripsi = new javax.swing.JTextArea();
        stok = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        txt_cari = new javax.swing.JTextField();
        cmb_cari = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txt_kategori = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        rak = new javax.swing.JTextField();
        id_user = new javax.swing.JLabel();
        username = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        cmb_kategori = new javax.swing.JComboBox<String>();
        jLabel11 = new javax.swing.JLabel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(197, 216, 157));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        judul.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                judulActionPerformed(evt);
            }
        });
        jPanel1.add(judul, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 290, 440, 50));
        jPanel1.add(penulis, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 390, 440, 50));
        jPanel1.add(upload, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 900, 201, 28));
        jPanel1.add(tgl, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 590, 460, 50));

        penerbit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                penerbitActionPerformed(evt);
            }
        });
        jPanel1.add(penerbit, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 490, 440, 50));

        deskripsi.setColumns(20);
        deskripsi.setRows(5);
        jScrollPane1.setViewportView(deskripsi);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 890, 460, 50));
        jPanel1.add(stok, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 700, 450, 40));

        jButton2.setText(".");
        jButton2.setContentAreaFilled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1480, 890, 220, 50));

        jButton3.setText(".");
        jButton3.setContentAreaFilled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 800, 140, 30));

        jButton4.setText(".");
        jButton4.setContentAreaFilled(false);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 800, 140, 40));

        jButton5.setText(".");
        jButton5.setContentAreaFilled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1310, 800, 140, 30));

        jButton6.setText(".");
        jButton6.setContentAreaFilled(false);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1510, 790, 140, 50));
        jPanel1.add(txt_cari, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 190, 250, 33));

        cmb_cari.setText(".");
        cmb_cari.setContentAreaFilled(false);
        cmb_cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmb_cariActionPerformed(evt);
            }
        });
        jPanel1.add(cmb_cari, new org.netbeans.lib.awtextra.AbsoluteConstraints(1580, 180, 150, 40));

        jLabel1.setText("Judul:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 160, -1, -1));

        jLabel9.setText("Kategori:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 160, -1, -1));

        txt_kategori.setOpaque(false);
        txt_kategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_kategoriActionPerformed(evt);
            }
        });
        jPanel1.add(txt_kategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 180, 440, 50));

        jButton1.setText(".");
        jButton1.setContentAreaFilled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 180, 150, 50));
        jPanel1.add(rak, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 800, 430, 40));

        id_user.setText("jLabel11");
        jPanel1.add(id_user, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 40, 200, -1));

        username.setText("jLabel11");
        jPanel1.add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 90, 230, -1));

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
        jScrollPane3.setViewportView(jTable1);

        jPanel1.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 270, 910, 470));

        cmb_kategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmb_kategoriActionPerformed(evt);
            }
        });
        jPanel1.add(cmb_kategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(1156, 190, 390, 30));

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/1860/Data buku.png"))); // NOI18N
        jLabel11.setText("jLabel11");
        jLabel11.setMaximumSize(new java.awt.Dimension(1860, 1080));
        jLabel11.setMinimumSize(new java.awt.Dimension(1860, 1080));
        jLabel11.setPreferredSize(new java.awt.Dimension(1860, 1080));
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void judulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_judulActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_judulActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    // =======================
    // VALIDASI INPUT
    // =======================
    if (judul.getText().trim().isEmpty()
        || penulis.getText().trim().isEmpty()
        || penerbit.getText().trim().isEmpty()
        || stok.getText().trim().isEmpty()
        || tgl.getDate() == null
        || kategoriId == 0) {

        JOptionPane.showMessageDialog(null, "Lengkapi semua data terlebih dahulu!");
        return;
    }

    int jumlahStok;
    try {
        jumlahStok = Integer.parseInt(stok.getText().trim());
        if (jumlahStok <= 0) {
            JOptionPane.showMessageDialog(null, "Stok harus lebih dari 0!");
            return;
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(null, "Stok harus berupa angka!");
        return;
    }

    // =======================
    // FORMAT TANGGAL
    // =======================
    String format = "yyyy-MM-dd";
    SimpleDateFormat fm = new SimpleDateFormat(format);
    String Tanggal = fm.format(tgl.getDate());

    Connection conn = null;
    PreparedStatement pstBuku = null;
    PreparedStatement pstEksemplar = null;
    ResultSet rs = null;

    try {
        conn = koneksi.koneksi.koneksiDB();
        conn.setAutoCommit(false); // TRANSACTION START

        // =======================
        // 1. INSERT BUKU
        // =======================
        String sqlBuku = "INSERT INTO buku "
                + "(judul, penulis, penerbit, tahun_terbit, stok, kategori_id, rak_buku, deskripsi, imgsampul, created_by, update_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        pstBuku = conn.prepareStatement(sqlBuku, Statement.RETURN_GENERATED_KEYS);

        pstBuku.setString(1, judul.getText());
        pstBuku.setString(2, penulis.getText());
        pstBuku.setString(3, penerbit.getText());
        pstBuku.setString(4, Tanggal);
        pstBuku.setInt(5, jumlahStok);
        pstBuku.setInt(6, kategoriId);
        pstBuku.setString(7, rak.getText());
        pstBuku.setString(8, deskripsi.getText());
        pstBuku.setString(9, pathGambar);
        pstBuku.setString(10, session.getU_id());
        pstBuku.setString(11, session.getU_id());

        pstBuku.executeUpdate();

        // =======================
        // 2. AMBIL ID BUKU
        // =======================
        rs = pstBuku.getGeneratedKeys();
        int bukuId = 0;
        if (rs.next()) {
            bukuId = rs.getInt(1);
        }

        // =======================
        // 3. INSERT BUKU_ITEM
        // =======================
        String sqlEksemplar = "INSERT INTO buku_item "
                + "(buku_id, kode_buku, status, created_at) "
                + "VALUES (?, ?, ?, ?)";

        pstEksemplar = conn.prepareStatement(sqlEksemplar);

        int lastKode = getLastKodeBuku(conn);

        for (int i = 1; i <= jumlahStok; i++) {
            int nomor = lastKode + i;
            String kodeBuku = String.format("BK-%04d", nomor);

            pstEksemplar.setInt(1, bukuId);
            pstEksemplar.setString(2, kodeBuku);
            pstEksemplar.setString(3, "tersedia");
            pstEksemplar.setString(4, Tanggal);
            pstEksemplar.executeUpdate();
        }


        conn.commit(); // TRANSACTION SUCCESS

        JOptionPane.showMessageDialog(null, "Data buku & eksemplar berhasil disimpan!");

        // =======================
        // REFRESH & RESET
        // =======================
        getData();
        bersih();

    } catch (Exception e) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException ex) {}

        JOptionPane.showMessageDialog(null, "Gagal menyimpan data:\n" + e.getMessage());

    } finally {
        try {
            if (rs != null) rs.close();
            if (pstBuku != null) pstBuku.close();
            if (pstEksemplar != null) pstEksemplar.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {}
    }
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void penerbitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_penerbitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_penerbitActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
  try {
    String format = "yyyy-MM-dd";
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    String tahunTerbit = sdf.format(tgl.getDate());

    conn.setAutoCommit(false);

    // =====================
    // ambil stok lama
    // =====================
    int stokLama = 0;
    PreparedStatement psStok = conn.prepareStatement(
        "SELECT stok FROM buku WHERE buku_id=?"
    );
    psStok.setInt(1, bukuId);
    ResultSet rs = psStok.executeQuery();
    if (rs.next()) stokLama = rs.getInt(1);
    rs.close();
    psStok.close();

    int stokBaru = Integer.parseInt(stok.getText());

    // =====================
    // update buku
    // =====================
    String sql = "UPDATE buku SET " +
            "judul=?, penulis=?, penerbit=?, tahun_terbit=?, " +
            "stok=?, kategori_id=?, rak_buku=?, deskripsi=?, imgsampul=?, update_by=? " +
            "WHERE buku_id=?";

    pst = conn.prepareStatement(sql);
    pst.setString(1, judul.getText());
    pst.setString(2, penulis.getText());
    pst.setString(3, penerbit.getText());
    pst.setString(4, tahunTerbit);
    pst.setInt(5, stokBaru);
    pst.setInt(6, kategoriId);
    pst.setString(7, rak.getText());
    pst.setString(8, deskripsi.getText());
    pst.setString(9, pathGambar);
    pst.setString(10, session.getU_id());
    pst.setInt(11, bukuId);
    pst.executeUpdate();
    pst.close();

    int selisih = stokBaru - stokLama;

    // =====================
    // stok bertambah
    // =====================
    if (selisih > 0) {
        int last = getLastKodeBuku(conn);
        PreparedStatement ins = conn.prepareStatement(
            "INSERT INTO buku_item (buku_id, kode_buku, status) VALUES (?, ?, 'tersedia')"
        );

        for (int i = 1; i <= selisih; i++) {
            ins.setInt(1, bukuId);
            ins.setString(2, String.format("BK-%04d", last + i));
            ins.executeUpdate();
        }
        ins.close();
    }

    // =====================
    // stok berkurang
    // =====================
    if (selisih < 0) {
        PreparedStatement del = conn.prepareStatement(
            "DELETE FROM buku_item WHERE buku_id=? AND status='tersedia' LIMIT ?"
        );
        del.setInt(1, bukuId);
        del.setInt(2, Math.abs(selisih));
        del.executeUpdate();
        del.close();
    }

    conn.commit();

    JOptionPane.showMessageDialog(null, "Data berhasil diupdate");

} catch (Exception e) {
    try { conn.rollback(); } catch (Exception ex) {}
    JOptionPane.showMessageDialog(null, "Gagal update: " + e.getMessage());
}

getData();
bersih();

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        bersih();
        getData();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

    int i = jTable1.getSelectedRow();
    int id = Integer.parseInt(model.getValueAt(i, 0).toString());

    int a = JOptionPane.showConfirmDialog(null,
            "Yakin ingin menghapus?",
            "Delete",
            JOptionPane.YES_NO_OPTION);

    if (a == JOptionPane.YES_OPTION) {
        try {
            String sql = "DELETE FROM buku WHERE Buku_id=?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Deleted");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    getData();
    bersih();

    
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
                                         
    JFileChooser fc = new JFileChooser();
fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
fc.setAcceptAllFileFilterUsed(false);
fc.addChoosableFileFilter(
    new javax.swing.filechooser.FileNameExtensionFilter(
        "Image Files", "jpg", "jpeg", "png"
    )
);

int result = fc.showOpenDialog(this);

if (result == JFileChooser.APPROVE_OPTION) {
    File file = fc.getSelectedFile();

    // cek ukuran file (maksimal 5 MB)
    long maxSize = 5 * 1024 * 1024; // 5 MB dalam byte

    if (file.length() > maxSize) {
        JOptionPane.showMessageDialog(
            this,
            "Ukuran file terlalu besar! Maksimal 5 MB.",
            "Error Upload",
            JOptionPane.ERROR_MESSAGE
        );
        return; // hentikan proses upload
    }

    // tampilkan nama file
    upload.setText(file.getName());

    // simpan path gambar
    pathGambar = file.getAbsolutePath();
}


        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        pilihData();
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable1MouseClicked

    private void cmb_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmb_cariActionPerformed
    model.setRowCount(0);

    try {
        String judulCari = txt_cari.getText().trim();
        String kategori = cmb_kategori.getSelectedItem().toString();

        String sql;
        pst = null;

        if (kategori.equals("Semua Kategori")) {
            sql =
                "SELECT b.*, k.name_kategori " +
                "FROM buku b " +
                "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                "WHERE b.judul LIKE ?";

            pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + judulCari + "%");

        } else {
            sql =
                "SELECT b.*, k.name_kategori " +
                "FROM buku b " +
                "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                "WHERE k.name_kategori = ? " +
                "AND b.judul LIKE ?";

            pst = conn.prepareStatement(sql);
            pst.setString(1, kategori);
            pst.setString(2, "%" + judulCari + "%");
        }

        rs = pst.executeQuery();

        while (rs.next()) {
            Object[] obj = new Object[12];

            obj[0] = rs.getInt("buku_id");
            obj[1] = rs.getInt("kategori_id");
            obj[2] = rs.getString("name_kategori");
            obj[3] = rs.getString("judul");
            obj[4] = rs.getString("penulis");
            obj[5] = rs.getString("penerbit");
            obj[6] = rs.getDate("tahun_terbit");
            obj[7] = rs.getInt("stok");
            obj[8] = rs.getString("deskripsi");
            obj[9] = rs.getString("rak_buku");

            String path = rs.getString("imgsampul");
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(60, 80, Image.SCALE_SMOOTH);
            obj[10] = new ImageIcon(img);
            obj[11] = path;

            model.addRow(obj);
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
    }//GEN-LAST:event_cmb_cariActionPerformed

    private void txt_kategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_kategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_kategoriActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    JDialog dialog = new JDialog((java.awt.Frame) null, true);
    daftar_kategori panel = new daftar_kategori(this, dialog);

    dialog.setContentPane(panel);
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);

        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void cmb_kategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmb_kategoriActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmb_kategoriActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmb_cari;
    private javax.swing.JComboBox<String> cmb_kategori;
    private javax.swing.JTextArea deskripsi;
    private javax.swing.JLabel id_user;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField judul;
    private javax.swing.JTextField penerbit;
    private javax.swing.JTextField penulis;
    private javax.swing.JTextField rak;
    private javax.swing.JTextField stok;
    private com.toedter.calendar.JDateChooser tgl;
    private javax.swing.JTextField txt_cari;
    private javax.swing.JTextField txt_kategori;
    private javax.swing.JTextField upload;
    private javax.swing.JLabel username;
    // End of variables declaration//GEN-END:variables

}
