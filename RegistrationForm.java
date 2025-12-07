package com.inventory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import com.formdev.flatlaf.FlatLightLaf;

public class RegistrationForm extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField txtUsername, txtEmail;
    private JPasswordField txtPassword;
    private JButton btnRegister, btnBack;

    public RegistrationForm() {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) { e.printStackTrace(); }

        setTitle("Register");
        setSize(460, 470);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // ---------------- BACKGROUND PANEL WITH BLACK + TRANSPARENT VIOLET-PINK GRADIENT ----------------
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                Color violet = new Color(138, 43, 226, 80);
                Color pink   = new Color(255, 20, 147, 80);

                GradientPaint gp = new GradientPaint(
                        0, 0, violet,
                        0, getHeight(), pink
                );

                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setLayout(null);
        setContentPane(panel);

        // ---------------- TITLE ----------------
        JLabel lblTitle = new JLabel("Create New Account");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(20, 20, 300, 30);
        panel.add(lblTitle);

        // ---------------- GLASS CARD PANEL ----------------
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setOpaque(false);

        card.setBounds(20, 70, 420, 330);
        panel.add(card);

        // ---------------- Username ----------------
        JLabel lblUser = new JLabel("Username:");
        lblUser.setBounds(20, 5, 200, 20);
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(lblUser);

        txtUsername = createGlassField();
        txtUsername.setBounds(20, 30, 260, 35);
        card.add(txtUsername);

        // ---------------- Email ----------------
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setBounds(20, 75, 200, 20);
        lblEmail.setForeground(Color.WHITE);
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(lblEmail);

        txtEmail = createGlassField();
        txtEmail.setBounds(20, 100, 260, 35);
        card.add(txtEmail);

        // ---------------- Password ----------------
        JLabel lblPass = new JLabel("Password:");
        lblPass.setBounds(20, 145, 200, 20);
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(lblPass);

        txtPassword = new JPasswordField();
        styleGlassPassword(txtPassword);
        txtPassword.setBounds(20, 170, 260, 35);
        card.add(txtPassword);

        // ---------------- BUTTONS ----------------
        btnRegister = createStyledButton("Register");
        btnRegister.setBounds(300, 30, 110, 40);
        card.add(btnRegister);

        btnBack = createStyledButton("Back");
        btnBack.setBounds(300, 100, 110, 40);
        card.add(btnBack);

        btnBack.setBackground(new Color(200, 20, 100));  // PINKISH RED

        // ---------------- ACTIONS ----------------
        btnBack.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });

        btnRegister.addActionListener(e -> register());
    }

    // -------------------------------------------------------------------------
    // UI HELPERS (same style as LoginForm)
    // -------------------------------------------------------------------------

    private JTextField createGlassField() {
        JTextField tf = new JTextField();
        tf.setBackground(new Color(50, 50, 50, 180));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return tf;
    }

    private void styleGlassPassword(JPasswordField pf) {
        pf.setBackground(new Color(50, 50, 50, 180));
        pf.setForeground(Color.WHITE);
        pf.setCaretColor(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JButton b = (JButton) c;
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color c1 = new Color(138, 43, 226, 200);
                Color c2 = new Color(255, 20, 147, 200);

                if (b.getModel().isRollover()) {
                    c1 = c1.brighter();
                    c2 = c2.brighter();
                }

                if (b.getModel().isPressed()) {
                    c1 = c1.darker();
                    c2 = c2.darker();
                }

                GradientPaint gp = new GradientPaint(0, 0, c1, 0, b.getHeight(), c2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 12, 12);

                g2d.setColor(Color.WHITE);
                FontMetrics fm = g.getFontMetrics();
                int x = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                int y = (b.getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(b.getText(), x, y);

                g2d.dispose();
            }
        });

        return btn;
    }

    // -------------------------------------------------------------------------
    // REGISTRATION LOGIC
    // -------------------------------------------------------------------------

    private void register() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(username,email,password,otp_verified) VALUES(?,?,?,?)")) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, BCrypt.hashpw(password, BCrypt.gensalt()));
            ps.setBoolean(4, true);  // OTP skipped

            ps.executeUpdate();

            ActivityLog.log(username, "Registered new account");

            JOptionPane.showMessageDialog(this, "Registration successful!");

            new LoginForm().setVisible(true);
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Username or Email already exists.");
            ActivityLog.log(username, "Registration error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new RegistrationForm().setVisible(true);
    }
}
