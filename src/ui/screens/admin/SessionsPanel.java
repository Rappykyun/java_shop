package ui.screens.admin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import config.Theme;
import model.RoleType;
import service.UserService;
import ui.components.RoundedButton;
import ui.components.UIFactory;

public class SessionsPanel extends JPanel {
    private final UserService userService;
    private final DefaultTableModel adminTableModel;
    private final DefaultTableModel cashierTableModel;

    public SessionsPanel(UserService userService) {
        this.userService = userService;
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);

        adminTableModel = createSessionModel("Admin");
        cashierTableModel = createSessionModel("Cashier");

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTables(), BorderLayout.CENTER);
    }

    public void refreshData() throws SQLException {
        loadSessions(adminTableModel, userService.listSessions(RoleType.ADMIN));
        loadSessions(cashierTableModel, userService.listSessions(RoleType.CASHIER));
    }

    private DefaultTableModel createSessionModel(String userColumnName) {
        return new DefaultTableModel(
                new Object[]{"ID", userColumnName, "Time In", "Time Out", "Duration"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel buildTables() {
        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 16));
        tables.setOpaque(false);
        tables.add(buildTableCard("Admin Time In / Out", adminTableModel));
        tables.add(buildTableCard("Cashier Time In / Out", cashierTableModel));
        return tables;
    }

    private JPanel buildTableCard(String title, DefaultTableModel model) {
        JTable table = new JTable(model);
        table.removeColumn(table.getColumnModel().getColumn(0));

        JPanel card = UIFactory.createCard();
        card.add(UIFactory.createSectionTitle(title), BorderLayout.NORTH);
        card.add(UIFactory.createTableScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void loadSessions(DefaultTableModel model, List<Object[]> sessions) {
        model.setRowCount(0);
        for (Object[] row : sessions) {
            model.addRow(row);
        }
    }

    private JPanel buildToolbar() {
        JPanel panel = UIFactory.createCard();
        panel.setLayout(new BorderLayout());
        panel.add(UIFactory.createSectionTitle("Time In / Out"), BorderLayout.WEST);

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
