package com.inventory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ActivityLogViewer extends JFrame {
    private static final long serialVersionUID = 1L;

    public ActivityLogViewer() {
        try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); } catch(Exception e) {}

        setTitle("Activity Logs");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ------------------- Main Panel with Gradient -------------------
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(138, 43, 226, 150);
                Color color2 = new Color(255, 20, 147, 150);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(null);

        // ------------------- Table -------------------
        JTable table = new JTable();
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setBackground(Color.BLACK);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(128, 0, 128));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(20, 20, 550, 380);
        scroll.getViewport().setBackground(Color.BLACK);
        panel.add(scroll);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(138, 43, 226));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 14));

        // ------------------- Load logs -------------------
        loadLogs(table);

        // ------------------- Buttons -------------------
        Color violet = new Color(138, 43, 226);
        Color purple = new Color(128, 0, 128);
        Color pink = new Color(255, 20, 147);

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setBounds(80, 410, 120, 35);
        btnRefresh.setBackground(violet);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        panel.add(btnRefresh);

        JButton btnClear = new JButton("🗑️ Clear Logs");
        btnClear.setBounds(220, 410, 150, 35);
        btnClear.setBackground(purple);
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        panel.add(btnClear);

        JButton btnBack = new JButton("⬅️ Back");
        btnBack.setBounds(400, 410, 120, 35);
        btnBack.setBackground(pink);
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        panel.add(btnBack);

        btnBack.addActionListener(e -> this.dispose());

        btnRefresh.addActionListener(e -> loadLogs(table));

        btnClear.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all logs?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM activity_logs")) {
                    ps.executeUpdate();
                    loadLogs(table);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        setContentPane(panel);
    }

    private void loadLogs(JTable table) {
        Vector<String> columns = new Vector<>();
        columns.add("Username");
        columns.add("Action");
        columns.add("Timestamp");

        Vector<Vector<Object>> data = new Vector<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username, action, timestamp FROM activity_logs ORDER BY timestamp DESC")) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("username"));
                row.add(rs.getString("action"));
                row.add(rs.getTimestamp("timestamp"));
                data.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        table.setModel(new javax.swing.table.DefaultTableModel(data, columns));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String action = (String) table.getValueAt(row, 1);
                if (action != null && (action.contains("Error") || action.contains("Failed"))) {
                    c.setBackground(Color.RED);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.BLACK);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });
    }

    public static void main(String[] args) {
        new ActivityLogViewer().setVisible(true);
    }
}
