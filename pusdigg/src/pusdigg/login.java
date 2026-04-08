package pusdigg;

import java.sql.*;
import javax.swing.*;
import org.jdesktop.swingx.prompt.PromptSupport;

/**
 * Form Login untuk Admin dan User
 * @author dppra
 */
public class login extends javax.swing.JFrame {

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    
    public login() {
        initComponents();
        conn = koneksi.koneksi.koneksiDB();
        this.setLocationRelativeTo(null);
        
        // PLACEHOLDER
        PromptSupport.setPrompt("Masukkan NISN/NIP/NIK", jTextField1);
        PromptSupport.setPrompt("Masukkan Password", jPasswordField1);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1920, 1080));
        setSize(new java.awt.Dimension(1920, 1080));

        jPanel1.setBackground(new java.awt.Color(156, 171, 132));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jPanel1.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 510, 460, 60));
        jPanel1.add(jPasswordField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 650, 470, 70));

        jButton1.setBackground(new java.awt.Color(246, 240, 215));
        jButton1.setText(".");
        jButton1.setContentAreaFilled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 830, 200, -1));

        jButton2.setText(".");
        jButton2.setContentAreaFilled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 920, 150, -1));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/1920/Login.png"))); // NOI18N
        jLabel4.setText("aaaaaaaaaaaaaa");
        jLabel4.setMinimumSize(new java.awt.Dimension(1920, 1080));
        jLabel4.setPreferredSize(new java.awt.Dimension(1920, 1080));
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1920, 1080));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String id;
        String nomor;
        String username;
        String role;
        String status;

        String sql = "SELECT * FROM user WHERE nomor=? AND password=?";

        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, jTextField1.getText());
            pst.setString(2, jPasswordField1.getText());

            rs = pst.executeQuery();

            if (rs.next()) {
                id = rs.getString("user_id");
                nomor = jTextField1.getText(); // Ambil dari textfield
                username = rs.getString("fullname");
                role = rs.getString("role");
                status = rs.getString("status");

                // Set session
                session.setU_id(id);
                session.setU_nomor(nomor);
                session.setU_username(username);
                session.setU_status(status);

                // Redirect berdasarkan role
                if (role.equalsIgnoreCase("admin")) {
                    sidebar_admin adm = new sidebar_admin();
                    adm.setVisible(true);
                    adm.setExtendedState(JFrame.MAXIMIZED_BOTH);
                } else if (role.equalsIgnoreCase("user")) {
                    U_peminjaman user = new U_peminjaman();
                    user.setVisible(true);
                    user.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }

                this.dispose();
            } else {
    JOptionPane.showMessageDialog(null, 
        "Nomor atau Password salah!", 
        "Login Gagal", 
        JOptionPane.ERROR_MESSAGE);
}
            
            rs.close();
            pst.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error login: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        registrasi registrasi = new registrasi();
        registrasi.setVisible(true);
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new login().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
