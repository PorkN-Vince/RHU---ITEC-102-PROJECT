package com.inventory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class Dashboard extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private String username;

    // Sidebar buttons
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnViewLogs, btnPatientForm, btnViewAllPatients, btnLogout;
    private PatientListViewer patientViewer;

    public Dashboard(String username) {
        this.username = username;
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch(Exception e) { e.printStackTrace(); }

        setTitle("Inventory Dashboard - " + username);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // ------------------- Main Panel with Gradient -------------------
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(138, 43, 226, 150); // Violet
                Color color2 = new Color(255, 20, 147, 150); // Pink
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(null);
        setContentPane(panel);

        // ------------------- IMAGE INSERTION -------------------
        JLabel lblImage = new JLabel();
        java.net.URL imgUrl = getClass().getResource("/icons/Logos1.png");
        if(imgUrl != null) {
            ImageIcon icon = new ImageIcon(imgUrl);
            Image scaled = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            lblImage.setIcon(new ImageIcon(scaled));
        }
        lblImage.setBounds(55, 10, 60, 60);
        panel.add(lblImage);

        // ------------------- Welcome Message -------------------
        JLabel lblWelcome = new JLabel("Welcome " + username + "!");
        lblWelcome.setBounds(800, 15, 250, 30);
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblWelcome);

        // ------------------- Sidebar -------------------
        JPanel sidebar = new JPanel();
        sidebar.setBounds(10, 80, 150, 470);
        sidebar.setLayout(new GridLayout(9, 1, 5, 5));
        sidebar.setBackground(new Color(0, 0, 0, 180));
        panel.add(sidebar);

        // Colors
        Color violet = new Color(138, 43, 226);
        Color purple = new Color(128, 0, 128);
        Color pink = new Color(255, 20, 147);

        btnAdd = new JButton("➕ Add"); btnAdd.setBackground(violet); btnAdd.setForeground(Color.WHITE); btnAdd.setFocusPainted(false); sidebar.add(btnAdd);
        btnEdit = new JButton("✏️ Edit"); btnEdit.setBackground(purple); btnEdit.setForeground(Color.WHITE); btnEdit.setFocusPainted(false); sidebar.add(btnEdit);
        btnDelete = new JButton("🗑️ Delete"); btnDelete.setBackground(pink); btnDelete.setForeground(Color.WHITE); btnDelete.setFocusPainted(false); sidebar.add(btnDelete);
        btnRefresh = new JButton("🔄 Refresh"); btnRefresh.setBackground(violet); btnRefresh.setForeground(Color.WHITE); btnRefresh.setFocusPainted(false); sidebar.add(btnRefresh);
        btnViewLogs = new JButton("📄 Logs"); btnViewLogs.setBackground(purple); btnViewLogs.setForeground(Color.WHITE); btnViewLogs.setFocusPainted(false); sidebar.add(btnViewLogs);
        btnPatientForm = new JButton("👤 Patient"); btnPatientForm.setBackground(pink); btnPatientForm.setForeground(Color.WHITE); btnPatientForm.setFocusPainted(false); sidebar.add(btnPatientForm);
        btnViewAllPatients = new JButton("👥 All Patients"); btnViewAllPatients.setBackground(pink); btnViewAllPatients.setForeground(Color.WHITE); btnViewAllPatients.setFocusPainted(false); sidebar.add(btnViewAllPatients);
        btnLogout = new JButton("🚪 Logout"); btnLogout.setBackground(violet); btnLogout.setForeground(Color.WHITE); btnLogout.setFocusPainted(false); sidebar.add(btnLogout);

        // ------------------- Search Bar -------------------
        txtSearch = new JTextField();
        txtSearch.setBounds(180, 10, 400, 30);
        txtSearch.setBackground(new Color(255,255,255,200));
        panel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(590, 10, 100, 30);
        btnSearch.setBackground(violet);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        panel.add(btnSearch);

        // ------------------- Product Table -------------------
        model = new DefaultTableModel(new String[]{"ID","Name","Category","Price","Stock"},0);
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                int stock = Integer.parseInt(String.valueOf(getValueAt(row,4)));
                c.setBackground(stock<=10 ? Color.RED : Color.BLACK);
                c.setForeground(Color.WHITE);
                return c;
            }
        };
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(180, 50, 780, 500);
        panel.add(scroll);

        // ------------------- Button Actions -------------------
        btnAdd.addActionListener(e -> new ProductForm(this).setVisible(true));
        btnEdit.addActionListener(e -> editProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnRefresh.addActionListener(e -> loadProducts());
        btnViewLogs.addActionListener(e -> new ActivityLogViewer().setVisible(true));

        btnPatientForm.addActionListener(e -> {
        	PatientForm pf = new PatientForm(null); // pass null if no viewer reference
        	pf.setVisible(true);
        });

        btnViewAllPatients.addActionListener(e -> {
            patientViewer = new PatientListViewer(this);
            patientViewer.setVisible(true);
        });

        btnLogout.addActionListener(e -> {
            ActivityLog.log(username, "Logged out");
            new LoginForm().setVisible(true);
            dispose();
        });

        btnSearch.addActionListener(e -> searchProducts());

        loadProducts();
    }

    public void loadProducts() {
        model.setRowCount(0);
        try(Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM products")) {
            while(rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("product_id"));
                row.add(rs.getString("product_name"));
                row.add(rs.getString("category"));
                row.add(rs.getDouble("price"));
                row.add(rs.getInt("stock"));
                model.addRow(row);
            }
        } catch(Exception e) {
            e.printStackTrace();
            ActivityLog.log(username,"Error loading products: "+e.getMessage());
        }
    }

    private void searchProducts() {
        String keyword = txtSearch.getText();
        model.setRowCount(0);
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM products WHERE product_name LIKE ? OR category LIKE ?")) {
            ps.setString(1, "%"+keyword+"%");
            ps.setString(2, "%"+keyword+"%");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("product_id"));
                row.add(rs.getString("product_name"));
                row.add(rs.getString("category"));
                row.add(rs.getDouble("price"));
                row.add(rs.getInt("stock"));
                model.addRow(row);
            }
        } catch(Exception e) {
            e.printStackTrace();
            ActivityLog.log(username,"Error searching products: "+e.getMessage());
        }
    }

    private void editProduct() {
        int selected = table.getSelectedRow();
        if(selected==-1) return;
        int id = (int) model.getValueAt(selected,0);
        String name = (String) model.getValueAt(selected,1);
        String cat = (String) model.getValueAt(selected,2);
        double price = (double) model.getValueAt(selected,3);
        int stock = (int) model.getValueAt(selected,4);
        new ProductForm(this,id,name,cat,price,stock).setVisible(true);
    }

    private void deleteProduct() {
        int selected = table.getSelectedRow();
        if(selected==-1) return;
        int id = (int) model.getValueAt(selected,0);
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE product_id=?")) {
            ps.setInt(1,id);
            ps.executeUpdate();
            ActivityLog.log(username,"Deleted product ID: "+id);
            loadProducts();
        } catch(Exception e) {
            e.printStackTrace();
            ActivityLog.log(username,"Error deleting product: "+e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard("admin").setVisible(true));
    }
}
