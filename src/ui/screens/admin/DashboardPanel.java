package ui.screens.admin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;

import javax.swing.JPanel;

import model.DashboardSummary;
import service.DashboardService;
import ui.components.UIFactory;
import util.CurrencyUtils;

public class DashboardPanel extends JPanel {
    private final DashboardService dashboardService;
    private final JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 16, 16));

    public DashboardPanel(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        metricsPanel.setOpaque(false);
        add(metricsPanel, BorderLayout.NORTH);
    }

    public void refreshData() throws SQLException {
        DashboardSummary summary = dashboardService.loadSummary();

        metricsPanel.removeAll();
        metricsPanel.add(UIFactory.createMetricCard("Today's Sales", CurrencyUtils.format(summary.getTodaySales())));
        metricsPanel.add(UIFactory.createMetricCard("Transactions", String.valueOf(summary.getTodayTransactions())));
        metricsPanel.add(UIFactory.createMetricCard("Active Cashiers", String.valueOf(summary.getActiveCashiers())));
        metricsPanel.revalidate();
        metricsPanel.repaint();
    }
}
