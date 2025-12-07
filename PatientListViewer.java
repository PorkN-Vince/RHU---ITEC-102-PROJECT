package com.inventory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class PatientListViewer extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private Dashboard dashboard;

    private JButton btnReturn, btnEdit, btnDelete;

    public PatientListViewer(Dashboard dashboard) {
        this.dashboard = dashboard;

        try { UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf()); } catch(Exception e) {}

        setTitle("All Patient Forms");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        setContentPane(panel);

        model = new DefaultTableModel();
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(138,43,226));
        table.setSelectionForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        // ------------------- Buttons -------------------
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnEdit = createButton(" Edit", new Color(138, 43, 226));
        btnDelete = createButton(" Delete", new Color(128, 0, 128));
        btnReturn = createButton(" Return", new Color(255, 20, 147));

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnReturn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        btnReturn.addActionListener(e -> dispose());
        btnEdit.addActionListener(e -> editPatient());
        btnDelete.addActionListener(e -> deletePatient());

        loadPatients();
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(color.brighter()); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(color); }
        });
        return btn;
    }

    public void loadPatients() {
        model.setRowCount(0);
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Age", "Address", "Medicine", "Stocks Given"});

        try(Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM patients")) {

            while(rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getInt("age"));
                row.add(rs.getString("address"));
                row.add(rs.getString("medicine"));
                row.add(rs.getInt("stocks_given"));
                model.addRow(row);
            }

        } catch(Exception e) {
            e.printStackTrace();
            ActivityLog.log("admin", "Error loading patients: " + e.getMessage());
        }

        // Highlight low stock rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int stocks = (int) table.getValueAt(row, 5);
                if(!isSelected) {
                    c.setBackground(stocks <= 10 ? new Color(255,100,100) : Color.WHITE);
                    c.setForeground(stocks <= 10 ? Color.WHITE : Color.BLACK);
                }
                return c;
            }
        });
    }

    private void editPatient() {
        int selected = table.getSelectedRow();
        if(selected == -1) {
            JOptionPane.showMessageDialog(this,"Please select a patient to edit.");
            return;
        }

        int id = (int) model.getValueAt(selected,0);
        String name = (String) model.getValueAt(selected,1);
        int age = (int) model.getValueAt(selected,2);
        String address = (String) model.getValueAt(selected,3);
        String medicine = (String) model.getValueAt(selected,4);
        int stockGiven = (int) model.getValueAt(selected,5);

        PatientForm pf = new PatientForm(this);
        pf.setPatientData(id, name, age, address, medicine, stockGiven);
        pf.setVisible(true);
    }

    private void deletePatient() {
        int selected = table.getSelectedRow();
        if(selected == -1) {
            JOptionPane.showMessageDialog(this,"Please select a patient to delete.");
            return;
        }

        int id = (int) model.getValueAt(selected,0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this patient?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM patients WHERE id=?")) {
            ps.setInt(1,id);
            ps.executeUpdate();
            ActivityLog.log("admin", "Deleted patient ID: "+id);
            loadPatients();
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error deleting patient: "+e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PatientListViewer(null).setVisible(true));
    }
}
