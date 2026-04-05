package service;

import java.sql.SQLException;

import dao.DashboardDao;
import dao.ProductDao;
import dao.UserDao;
import model.DashboardSummary;

public class DashboardService {
    private final DashboardDao dashboardDao = new DashboardDao();
    private final ProductDao productDao = new ProductDao();
    private final UserDao userDao = new UserDao();

    public DashboardSummary loadSummary() throws SQLException {
        return dashboardDao.loadSummary(productDao.countLowStock(), userDao.countActiveCashiers());
    }
}
