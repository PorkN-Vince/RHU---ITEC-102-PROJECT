package com.inventory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProductForm extends JFrame {
    private static final long serialVersionUID = 1L;

    private JTextField txtName, txtCategory, txtPrice, txtStock;
    private JButton btnSave, btnCancel;
    private int productId = -1;
    private Dashboard dashboard;

    public ProductForm(Dashboard dashboard) {
        this.dashboard = dashboard;
        setupUI("Add Product");
    }

    public ProductForm(Dashboard dashboard, int productId, String name, String category, double price, int stock) {
        this.dashboard = dashboard;
        this.productId = productId;
        setupUI("Edit Product");

        txtName.setText(name);
        txtCategory.setText(category);
        txtPrice.setText(String.valueOf(price));
        txtStock.setText(String.valueOf(stock));
    }

    private void setupUI(String title) {

        try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); } catch(Exception e){}

        setTitle(title);
        setSize(380, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        // ================================
        //  MODERN GRADIENT BACKGROUND
        // ================================
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                Color violet = new Color(138, 43, 226);
                Color pink = new Color(255, 20, 147);

                GradientPaint gp = new GradientPaint(0, 0, violet, getWidth(), getHeight(), pink);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setLayout(null);
        setContentPane(panel);

        // LABEL FONT
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        // ================================
        //  INPUT LABELS & FIELDS
        // ================================
        JLabel lblName = new JLabel("Product Name");
        lblName.setBounds(30, 30, 150, 25);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(labelFont);
        panel.add(lblName);

        txtName = createField();
        txtName.setBounds(30, 58, 300, 32);
        panel.add(txtName);

        JLabel lblCat = new JLabel("Category");
        lblCat.setBounds(30, 98, 150, 25);
        lblCat.setForeground(Color.WHITE);
        lblCat.setFont(labelFont);
        panel.add(lblCat);

        txtCategory = createField();
        txtCategory.setBounds(30, 126, 300, 32);
        panel.add(txtCategory);

        JLabel lblPrice = new JLabel("Price");
        lblPrice.setBounds(30, 166, 150, 25);
        lblPrice.setForeground(Color.WHITE);
        lblPrice.setFont(labelFont);
        panel.add(lblPrice);

        txtPrice = createField();
        txtPrice.setBounds(30, 194, 300, 32);
        panel.add(txtPrice);

        JLabel lblStock = new JLabel("Stock");
        lblStock.setBounds(30, 234, 150, 25);
        lblStock.setForeground(Color.WHITE);
        lblStock.setFont(labelFont);
        panel.add(lblStock);

        txtStock = createField();
        txtStock.setBounds(30, 262, 300, 32);
        panel.add(txtStock);

        // ================================
        //  MODERN BUTTONS (NO EMOJIS)
        // ================================
        btnSave = createGradientButton("Save");
        btnSave.setBounds(40, 315, 130, 38);
        panel.add(btnSave);

        btnCancel = createGradientButton("Cancel");
        btnCancel.setBounds(200, 315, 130, 38);
        panel.add(btnCancel);

        // Events
        btnSave.addActionListener(e -> saveProduct());
        btnCancel.addActionListener(e -> dispose());
    }

    // =======================================
    //  MODERN TEXTFIELD STYLE
    // =======================================
    private JTextField createField() {
        JTextField tf = new JTextField();
        tf.setBackground(new Color(255, 255, 255, 210));
        tf.setForeground(Color.BLACK);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        return tf;
    }

    // =======================================
    //  MODERN BUTTON STYLE (VIOLET–PINK)
    // =======================================
    private JButton createGradientButton(String text) {

        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {

                JButton b = (JButton) c;
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color violet = new Color(138, 43, 226);
                Color pink = new Color(255, 20, 147);

                // Hover
                if (b.getModel().isRollover()) {
                    violet = violet.brighter();
                    pink = pink.brighter();
                }

                // Press
                if (b.getModel().isPressed()) {
                    violet = violet.darker();
                    pink = pink.darker();
                }

                GradientPaint gp = new GradientPaint(0, 0, violet, b.getWidth(), b.getHeight(), pink);
                g2.setPaint(gp);

                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 18, 18);

                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int textX = (b.getWidth() - fm.stringWidth(b.getText())) / 2;
                int textY = (b.getHeight() + fm.getAscent()) / 2 - 3;

                g2.drawString(b.getText(), textX, textY);
                g2.dispose();
            }
        });

        return btn;
    }

    // =======================================
    //  SAVE PRODUCT LOGIC
    // =======================================
    private void saveProduct() {
        String name = txtName.getText();
        String category = txtCategory.getText();
        double price;
        int stock;

        try {
            price = Double.parseDouble(txtPrice.getText());
            stock = Integer.parseInt(txtStock.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid price or stock");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            if (productId == -1) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO products(product_name, category, price, stock) VALUES(?,?,?,?)"
                );
                ps.setString(1, name);
                ps.setString(2, category);
                ps.setDouble(3, price);
                ps.setInt(4, stock);
                ps.executeUpdate();

                ActivityLog.log("admin", "Added product: " + name);

            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE products SET product_name=?, category=?, price=?, stock=? WHERE product_id=?"
                );
                ps.setString(1, name);
                ps.setString(2, category);
                ps.setDouble(3, price);
                ps.setInt(4, stock);
                ps.setInt(5, productId);
                ps.executeUpdate();

                ActivityLog.log("admin", "Updated product: " + name);
            }

            if (stock <= 10) {
                EmailHelper.sendLowStockAlert(name);
            }

            dashboard.loadProducts();
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            ActivityLog.log("admin", "Error saving product: " + e.getMessage());
        }
    }
}
