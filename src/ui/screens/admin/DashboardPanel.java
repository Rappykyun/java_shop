package ui.screens.admin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import model.ActivityLog;
import model.DashboardSummary;
import service.DashboardService;
import ui.components.UIFactory;
import util.CurrencyUtils;
import util.DateTimeUtils;

public class DashboardPanel extends JPanel {
    private final DashboardService dashboardService;
    private final JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 16, 16));
    private final DefaultTableModel activityTableModel = new DefaultTableModel(
            new Object[] { "Date / Time", "Actor", "Action", "Area", "Details" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable activityTable = new JTable(activityTableModel);
    private final JLabel summaryLabel = UIFactory.createMutedLabel("Today's live activity across the store.");

    public DashboardPanel(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        metricsPanel.setOpaque(false);
        add(metricsPanel, BorderLayout.NORTH);

        JPanel activityCard = UIFactory.createCard();
        JPanel heading = new JPanel(new BorderLayout(8, 8));
        heading.setOpaque(false);
        heading.add(UIFactory.createSectionTitle("Recent Activity"), BorderLayout.NORTH);
        heading.add(summaryLabel, BorderLayout.SOUTH);
        activityCard.add(heading, BorderLayout.NORTH);

        activityTable.getColumnModel().getColumn(0).setPreferredWidth(170);
        activityTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        activityTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        activityTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        activityTable.getColumnModel().getColumn(4).setPreferredWidth(520);
        activityCard.add(UIFactory.createTableScrollPane(activityTable), BorderLayout.CENTER);
        add(activityCard, BorderLayout.CENTER);
    }

    public void refreshData() throws SQLException {
        DashboardSummary summary = dashboardService.loadSummary();

        metricsPanel.removeAll();
        metricsPanel.add(UIFactory.createMetricCard("Today's Sales", CurrencyUtils.format(summary.getTodaySales())));
        metricsPanel.add(UIFactory.createMetricCard("Transactions", String.valueOf(summary.getTodayTransactions())));
        metricsPanel.add(UIFactory.createMetricCard("Low Stock", String.valueOf(summary.getLowStockCount())));
        metricsPanel.add(UIFactory.createMetricCard("Active Cashiers", String.valueOf(summary.getActiveCashiers())));
        metricsPanel.revalidate();
        metricsPanel.repaint();

        activityTableModel.setRowCount(0);
        if (summary.getRecentActivities().isEmpty()) {
            summaryLabel.setText("No recent activity recorded yet.");
        } else {
            summaryLabel.setText("Latest system events from admin actions and completed sales.");
            for (ActivityLog log : summary.getRecentActivities()) {
                activityTableModel.addRow(new Object[] {
                        DateTimeUtils.format(log.getCreatedAt()),
                        log.getActorName(),
                        log.getActionType(),
                        log.getEntityType(),
                        log.getDescription()
                });
            }
        }
    }
}
