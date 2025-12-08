package com.inventory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class PatientForm extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField txtName, txtAge, txtAddress, txtMedicine, txtQuantity;
    private JButton btnSave, btnCancel;
    private PatientListViewer viewer;
    private Integer patientId = null; // null = new patient, non-null = editing existing

    public PatientForm(PatientListViewer viewer) {
        this.viewer = viewer;

        try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); } catch(Exception e) {}

        setTitle("Patient Form");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ------------------- Main panel with gradient -------------------
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(138, 43, 226, 180);
                Color color2 = new Color(255, 20, 147, 180);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(null);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(panel);

        // ------------------- Labels -------------------
        JLabel lblTitle = new JLabel("Patient Information");
        lblTitle.setBounds(140, 10, 250, 30);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle);

        JLabel lblName = new JLabel("Name:"); lblName.setBounds(50, 60, 100, 25); lblName.setForeground(Color.WHITE); panel.add(lblName);
        JLabel lblAge = new JLabel("Age:"); lblAge.setBounds(50, 100, 100, 25); lblAge.setForeground(Color.WHITE); panel.add(lblAge);
        JLabel lblAddress = new JLabel("Address:"); lblAddress.setBounds(50, 140, 100, 25); lblAddress.setForeground(Color.WHITE); panel.add(lblAddress);
        JLabel lblMedicine = new JLabel("Medicine:"); lblMedicine.setBounds(50, 180, 100, 25); lblMedicine.setForeground(Color.WHITE); panel.add(lblMedicine);
        JLabel lblQuantity = new JLabel("Stocks Given:"); lblQuantity.setBounds(50, 220, 100, 25); lblQuantity.setForeground(Color.WHITE); panel.add(lblQuantity);

        // ------------------- Text Fields -------------------
        txtName = createTextField(150, 60, 250, 25); panel.add(txtName);
        txtAge = createTextField(150, 100, 100, 25); panel.add(txtAge);
        txtAddress = createTextField(150, 140, 250, 25); panel.add(txtAddress);
        txtMedicine = createTextField(150, 180, 250, 25); panel.add(txtMedicine);
        txtQuantity = createTextField(150, 220, 100, 25); panel.add(txtQuantity);

        // ------------------- Buttons -------------------
        btnSave = createButton(" Save", 100, 280, 120, 35, new Color(138, 43, 226));
        panel.add(btnSave);
        btnCancel = createButton(" Cancel", 250, 280, 120, 35, new Color(255, 20, 147));
        panel.add(btnCancel);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> savePatient());
    }

    private JTextField createTextField(int x, int y, int w, int h) {
        JTextField tf = new JTextField();
        tf.setBounds(x, y, w, h);
        tf.setBackground(new Color(50, 50, 50, 200));
        tf.setForeground(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        return tf;
    }

    private JButton createButton(String text, int x, int y, int w, int h, Color color) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, w, h);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(color.brighter()); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(color); }
        });
        return btn;
    }

    // ------------------- Set data for editing -------------------
    public void setPatientData(int id, String name, int age, String address, String medicine, int stock) {
        this.patientId = id;
        txtName.setText(name);
        txtAge.setText(String.valueOf(age));
        txtAddress.setText(address);
        txtMedicine.setText(medicine);
        txtQuantity.setText(String.valueOf(stock));
    }

    // ------------------- Save or Update patient -------------------
    private void savePatient() {
        String name = txtName.getText();
        int age = Integer.parseInt(txtAge.getText());
        String address = txtAddress.getText();
        String medicine = txtMedicine.getText();
        int stockGiven = Integer.parseInt(txtQuantity.getText());

        try(Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            if(patientId == null) {
                // New patient
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO patients(name, age, address, medicine, stocks_given) VALUES(?,?,?,?,?)")) {
                    ps.setString(1, name);
                    ps.setInt(2, age);
                    ps.setString(3, address);
                    ps.setString(4, medicine);
                    ps.setInt(5, stockGiven);
                    ps.executeUpdate();
                }
            } else {
                // Editing existing patient
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE patients SET name=?, age=?, address=?, medicine=?, stocks_given=? WHERE id=?")) {
                    ps.setString(1, name);
                    ps.setInt(2, age);
                    ps.setString(3, address);
                    ps.setString(4, medicine);
                    ps.setInt(5, stockGiven);
                    ps.setInt(6, patientId);
                    ps.executeUpdate();
                }
            }

            // Deduct stock
            try (PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE products SET stock = stock - ? WHERE product_name = ?")) {
                ps2.setInt(1, stockGiven);
                ps2.setString(2, medicine);
                int updated = ps2.executeUpdate();
                if(updated == 0) {
                    JOptionPane.showMessageDialog(this,"Medicine not found in inventory!");
                    conn.rollback();
                    return;
                }

                // --- Check new stock and send email if low ---
                try (PreparedStatement psCheck = conn.prepareStatement(
                        "SELECT stock FROM products WHERE product_name = ?")) {
                    psCheck.setString(1, medicine);
                    ResultSet rs = psCheck.executeQuery();
                    if(rs.next()) {
                        int newStock = rs.getInt("stock");
                        if(newStock <= 10) {
                            EmailHelper.sendLowStockAlert(medicine); // AUTO EMAIL
                        }
                    }
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this,"Patient saved and stock updated!");
            if(viewer != null) viewer.loadPatients();
            dispose();

        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error: "+e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PatientForm(null).setVisible(true));
    }
}
