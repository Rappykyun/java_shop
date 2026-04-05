package model;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummary {
    private final BigDecimal todaySales;
    private final int todayTransactions;
    private final int lowStockCount;
    private final int activeCashiers;
    private final List<ActivityLog> recentActivities;

    public DashboardSummary(BigDecimal todaySales, int todayTransactions, int lowStockCount, int activeCashiers,
            List<ActivityLog> recentActivities) {
        this.todaySales = todaySales;
        this.todayTransactions = todayTransactions;
        this.lowStockCount = lowStockCount;
        this.activeCashiers = activeCashiers;
        this.recentActivities = recentActivities;
    }

    public BigDecimal getTodaySales() {
        return todaySales;
    }

    public int getTodayTransactions() {
        return todayTransactions;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public int getActiveCashiers() {
        return activeCashiers;
    }

    public List<ActivityLog> getRecentActivities() {
        return recentActivities;
    }
}
