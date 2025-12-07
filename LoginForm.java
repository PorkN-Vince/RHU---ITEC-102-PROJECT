package com.inventory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import com.formdev.flatlaf.FlatLightLaf;

public class LoginForm extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister, btnForgot;

    public LoginForm() {
        try { 
            UIManager.setLookAndFeel(new FlatLightLaf()); 
        } catch(Exception e) {
            e.printStackTrace();
        }

        setTitle("Login");
        setSize(400, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // ------------------- BLACK BACKGROUND WITH TRANSPARENT GRADIENT OVERLAY -------------------
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Fill background with solid black
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                // Transparent gradient overlay
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(138, 43, 226, 80);  // Violet, transparent
                Color color2 = new Color(255, 20, 147, 80);  // Pink, transparent
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight()/2, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(null);
        setContentPane(panel);

        // ------------------- IMAGE INSERTION: Add login logo here -------------------
         ImageIcon loginIcon = new ImageIcon(getClass().getResource("/icons/Logos.png"));
         JLabel lblImage = new JLabel(loginIcon);
         lblImage.setBounds(25, 200, 332, 177);
         panel.add(lblImage);

        // ------------------- Labels with white text -------------------
        JLabel lblUser = new JLabel("Username:");
        lblUser.setBounds(30, 30, 80, 25);
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setBounds(120, 30, 180, 25);
        txtUsername.setBackground(new Color(50, 50, 50, 200)); // semi-transparent dark gray
        txtUsername.setForeground(Color.WHITE);
        panel.add(txtUsername);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setBounds(30, 70, 80, 25);
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(120, 70, 180, 25);
        txtPassword.setBackground(new Color(50, 50, 50, 200)); // semi-transparent dark gray
        txtPassword.setForeground(Color.WHITE);
        panel.add(txtPassword);

        // ------------------- Buttons with gradient, hover, and press effect -------------------
        btnLogin = new JButton("Login");
        styleInteractiveButton(btnLogin);
        btnLogin.setBounds(30, 120, 100, 35);
        panel.add(btnLogin);

        btnRegister = new JButton("Register");
        styleInteractiveButton(btnRegister);
        btnRegister.setBounds(140, 120, 100, 35);
        panel.add(btnRegister);

        btnForgot = new JButton("Forgot Password");
        styleInteractiveButton(btnForgot);
        btnForgot.setBounds(250, 120, 130, 35);
        panel.add(btnForgot);

        // ------------------- Button actions -------------------
        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> {
            new RegistrationForm().setVisible(true);
            this.dispose();
        });
        btnForgot.addActionListener(e -> {
            new ForgotPasswordForm().setVisible(true);
            this.dispose();
        });
    }

    // ------------------- Helper method to style buttons with gradient, hover, and press effect -------------------
    private void styleInteractiveButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color color1 = new Color(138, 43, 226, 200); // violet
                Color color2 = new Color(255, 20, 147, 200); // pink

                // Hover effect
                if (button.getModel().isRollover()) {
                    color1 = new Color(168, 73, 246, 220);
                    color2 = new Color(255, 50, 177, 220);
                }

                // Press effect
                if (button.getModel().isPressed()) {
                    color1 = color1.darker();
                    color2 = color2.darker();
                }

                GradientPaint gp = new GradientPaint(0, 0, color1, 0, button.getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 10, 10);

                g2d.setColor(button.getForeground());
                FontMetrics fm = g.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(button.getText(), g2d).getBounds();
                int textX = (button.getWidth() - stringBounds.width) / 2;
                int textY = (button.getHeight() - stringBounds.height) / 2 + fm.getAscent();
                g2d.drawString(button.getText(), textX, textY);

                g2d.dispose();
            }
        });
    }

    // ------------------- LOGIN METHOD -------------------
    private void login() {
        String username = txtUsername.getText();
        String password = String.valueOf(txtPassword.getPassword());

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=?")) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashed = rs.getString("password");
                if (BCrypt.checkpw(password, hashed)) {
                    ActivityLog.log(username, "Logged in");
                    new Dashboard(username).setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect password");
                }
            } else {
                JOptionPane.showMessageDialog(this, "User not found");
            }

        } catch(Exception e) {
            e.printStackTrace();
            ActivityLog.log(username, "Error during login: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new LoginForm().setVisible(true);
    }
}

