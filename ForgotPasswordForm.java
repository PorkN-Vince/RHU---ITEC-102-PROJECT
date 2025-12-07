package com.inventory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.security.SecureRandom;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import com.formdev.flatlaf.FlatDarkLaf;

public class ForgotPasswordForm extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField txtEmail, txtOTP;
    private JPasswordField txtNewPassword;
    private JButton btnSendOTP, btnReset, btnCancel;
    private String sentOTP;
    private SecureRandom random = new SecureRandom();
    private JLabel lblStatus;

    public ForgotPasswordForm() {

        // use the same FlatLaf theme as RegistrationForm
        try { UIManager.setLookAndFeel(new FlatDarkLaf()); } catch (Exception e) { /* ignore */ }

        setTitle("Forgot Password");
        setSize(480, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ============================
        // BACKGROUND: BLACK + GRADIENT (violet -> pink)
        // ============================
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Solid black base
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                // Gradient overlay (violet -> pink, semi-transparent)
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color c1 = new Color(70, 0, 120, 80);   // deep violet (transparent)
                Color c2 = new Color(110, 0, 90, 80);   // darker violet
                Color c3 = new Color(255, 20, 147, 60); // pink (transparent)
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth()/2, getHeight()/2, c2, true);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // subtle pink overlay from bottom-right
                GradientPaint gp2 = new GradientPaint(getWidth(), getHeight(), c3, getWidth()/2, getHeight()/2, new Color(0,0,0,0), true);
                g2.setPaint(gp2);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(null);
        setContentPane(bg);

        // ============================
        // TITLE + STATUS
        // ============================
        JLabel lblTitle = new JLabel("Forgot Password");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(25, 20, 300, 25);
        bg.add(lblTitle);

        lblStatus = new JLabel("Enter your registered email to receive an OTP.");
        lblStatus.setForeground(new Color(220, 220, 220));
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setBounds(25, 45, 420, 20);
        bg.add(lblStatus);

        // ============================
        // GLASS PANEL CONTAINER
        // ============================
        JPanel card = new JPanel();
        card.setBounds(25, 80, 430, 250);
        card.setLayout(null);
        card.setOpaque(false);
        card.setBorder(new LineBorder(new Color(255, 255, 255, 40), 1, true));
        bg.add(card);

        // ============================
        // EMAIL FIELD
        // ============================
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setForeground(Color.WHITE);
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEmail.setBounds(20, 15, 200, 20);
        card.add(lblEmail);

        txtEmail = createGlassField();
        txtEmail.setBounds(20, 40, 280, 32);
        card.add(txtEmail);

        btnSendOTP = createModernButton("Send OTP");
        btnSendOTP.setBounds(310, 40, 100, 32);
        card.add(btnSendOTP);

        // ============================
        // OTP FIELD
        // ============================
        JLabel lblOTP = new JLabel("OTP:");
        lblOTP.setForeground(Color.WHITE);
        lblOTP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOTP.setBounds(20, 85, 200, 20);
        card.add(lblOTP);

        txtOTP = createGlassField();
        txtOTP.setBounds(20, 110, 140, 32);
        card.add(txtOTP);

        // ============================
        // PASSWORD FIELD
        // ============================
        JLabel lblPass = new JLabel("New Password:");
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setBounds(180, 85, 200, 20);
        card.add(lblPass);

        txtNewPassword = new JPasswordField();
        styleGlassPassword(txtNewPassword);
        txtNewPassword.setBounds(180, 110, 230, 32);
        card.add(txtNewPassword);

        // ============================
        // BUTTONS (matching RegistrationForm theme)
        // ============================
        btnReset = createModernButton("Reset");
        btnReset.setBounds(80, 165, 120, 40);
        btnReset.setEnabled(false); // enable after OTP sent
        card.add(btnReset);

        btnCancel = createModernButton("Cancel");
        btnCancel.setBackground(new Color(180, 0, 80)); // pink-ish cancel color
        btnCancel.setBounds(220, 165, 120, 40);
        card.add(btnCancel);

        // ============================
        // EVENT LISTENERS
        // ============================
        btnSendOTP.addActionListener(e -> sendOTP());
        btnReset.addActionListener(e -> resetPassword());
        btnCancel.addActionListener(e -> dispose());

        txtEmail.addActionListener(e -> sendOTP());
        txtOTP.addActionListener(e -> attemptEnableReset());
        txtNewPassword.addActionListener(e -> resetPassword());
    }

    // ============================================================
    // UI HELPERS (same theme as RegistrationForm)
    // ============================================================
    private JTextField createGlassField() {
        JTextField tf = new JTextField();
        tf.setBackground(new Color(255, 255, 255, 220)); // glass white
        tf.setForeground(Color.BLACK);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 255, 255, 50), 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    private void styleGlassPassword(JPasswordField pf) {
        pf.setBackground(new Color(255, 255, 255, 220));
        pf.setForeground(Color.BLACK);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 255, 255, 50), 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private JButton createModernButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(128, 0, 128)); // violet
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new LineBorder(new Color(255, 255, 255, 40), 1, true));

        // keep content area transparent so custom UI paints gradient
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setUI(new ModernButtonUI());

        return btn;
    }

    // ============================================================
    // OTP + RESET LOGIC (unchanged behavior)
    // ============================================================
    private void sendOTP() {
        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email cannot be empty.");
            return;
        }

        // generate 6-digit OTP
        sentOTP = String.valueOf(100000 + random.nextInt(900000));

        try {
            // send OTP using EmailHelper (make sure EmailHelper works/configured)
            EmailHelper.sendEmail(email, "Password Reset OTP", "Your OTP: " + sentOTP);

            lblStatus.setText("OTP sent successfully to " + email);
            ActivityLog.log(email, "OTP sent for password reset");
            btnReset.setEnabled(true);
            btnSendOTP.setEnabled(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to send OTP. Check Email settings.");
            ActivityLog.log(email, "Failed to send OTP: " + e.getMessage());
        }
    }

    private void attemptEnableReset() {
        // called when user types OTP: enable reset button if it matches
        if (txtOTP.getText().trim().equals(sentOTP) && txtNewPassword.getPassword().length >= 6) {
            btnReset.setEnabled(true);
        }
    }

    private void resetPassword() {
        String email = txtEmail.getText().trim();
        String enteredOTP = txtOTP.getText().trim();
        String newPass = new String(txtNewPassword.getPassword());

        if (sentOTP == null || !enteredOTP.equals(sentOTP)) {
            JOptionPane.showMessageDialog(this, "Incorrect or missing OTP.");
            return;
        }

        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET password=? WHERE email=?")) {

            ps.setString(1, BCrypt.hashpw(newPass, BCrypt.gensalt()));
            ps.setString(2, email);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Password reset successfully!");
                ActivityLog.log(email, "Password reset via OTP");
                new LoginForm().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Email not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating password.");
            ActivityLog.log(email, "Password reset error: " + e.getMessage());
        }
    }

    // ============================================================
    // CUSTOM BUTTON UI (same as RegistrationForm)
    // ============================================================
    class ModernButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            JButton b = (JButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // base colors (violet -> pink gradient)
            Color color1 = new Color(138, 43, 226);
            Color color2 = new Color(255, 20, 147);

            // hover/pressed adjustments
            if (b.getModel().isPressed()) {
                color1 = color1.darker();
                color2 = color2.darker();
            } else if (b.getModel().isRollover()) {
                color1 = color1.brighter();
                color2 = color2.brighter();
            }

            GradientPaint gp = new GradientPaint(0, 0, color1, 0, b.getHeight(), color2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 20, 20);

            // draw the label text centered
            g2.setColor(b.getForeground());
            FontMetrics fm = g2.getFontMetrics();
            Rectangle stringBounds = fm.getStringBounds(b.getText(), g2).getBounds();
            int textX = (b.getWidth() - stringBounds.width) / 2;
            int textY = (b.getHeight() - stringBounds.height) / 2 + fm.getAscent();
            g2.drawString(b.getText(), textX, textY);

            g2.dispose();
        }

        @Override
        protected void installDefaults(AbstractButton b) {
            super.installDefaults(b);
            b.setOpaque(false);
            b.setBorderPainted(false);
        }
    }

    // ============================================================
    // ENTRY POINT (for testing)
    // ============================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ForgotPasswordForm().setVisible(true));
    }
}

