package ui.screens.admin;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import config.Theme;
import service.UserService;
import ui.components.RoundedButton;
import ui.components.UIFactory;

public class SessionsPanel extends JPanel {
    private final UserService userService;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public SessionsPanel(UserService userService) {
        this.userService = userService;
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Cashier", "Time In", "Time Out", "Duration"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.removeColumn(table.getColumnModel().getColumn(0));

        add(buildToolbar(), BorderLayout.NORTH);
        add(UIFactory.createTableScrollPane(table), BorderLayout.CENTER);
    }

    public void refreshData() throws SQLException {
        tableModel.setRowCount(0);
        List<Object[]> sessions = userService.listSessions();
        for (Object[] row : sessions) {
            tableModel.addRow(row);
        }
    }

    private JPanel buildToolbar() {
        JPanel panel = UIFactory.createCard();
        panel.setLayout(new BorderLayout());
        panel.add(UIFactory.createSectionTitle("Cashier Time In / Out"), BorderLayout.WEST);

        JPanel actions = UIFactory.createFlowPanel(java.awt.FlowLayout.RIGHT);
        RoundedButton refreshButton = new RoundedButton("Refresh", Theme.ESPRESSO, java.awt.Color.WHITE);
        refreshButton.addActionListener(event -> {
            try {
                refreshData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        actions.add(refreshButton);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }
}
