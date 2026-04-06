package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import config.DBConnection;
import dao.ActivityLogDao;
import dao.RoleDao;
import dao.SessionDao;
import dao.UserDao;
import model.RoleType;
import model.User;
import util.PasswordUtils;

public class UserService {
    private final UserDao userDao = new UserDao();
    private final RoleDao roleDao = new RoleDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final SessionDao sessionDao = new SessionDao();

    public List<User> listCashiers() throws SQLException {
        return userDao.listCashiers();
    }

    public void saveCashier(User cashier, String plainPassword, User actor) throws SQLException {
        if (cashier.getUsername() == null || cashier.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (cashier.getFullName() == null || cashier.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        boolean newCashier = cashier.getId() == 0;
        boolean updatePassword = plainPassword != null && !plainPassword.isBlank();
        if (newCashier && !updatePassword) {
            throw new IllegalArgumentException("Password is required for a new cashier.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (newCashier) {
                    String salt = PasswordUtils.generateSalt();
                    cashier.setPasswordSalt(salt);
                    cashier.setPasswordHash(PasswordUtils.hashPassword(plainPassword, salt));
                    cashier.setActive(true);
                    int roleId = roleDao.findRoleId(connection, RoleType.CASHIER);
                    int userId = userDao.insert(connection, cashier, roleId);
                    activityLogDao.log(connection, actor.getId(), "CREATE", "USER", userId,
                            "Created cashier account " + cashier.getUsername());
                } else {
                    if (updatePassword) {
                        String salt = PasswordUtils.generateSalt();
                        cashier.setPasswordSalt(salt);
                        cashier.setPasswordHash(PasswordUtils.hashPassword(plainPassword, salt));
                    }
                    userDao.updateCashier(connection, cashier, updatePassword);
                    activityLogDao.log(connection, actor.getId(), "UPDATE", "USER", cashier.getId(),
                            "Updated cashier account " + cashier.getUsername());
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public int clockIn(int cashierId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            sessionDao.clockOutOpenSessions(connection, cashierId);
            return sessionDao.clockIn(connection, cashierId);
        }
    }

    public void clockOut(int sessionId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            sessionDao.clockOut(connection, sessionId);
        }
    }

    public java.util.List<Object[]> listSessions() throws SQLException {
        return sessionDao.listSessions();
    }

    public void setCashierActive(int cashierId, boolean active, User actor) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                User user = userDao.findById(connection, cashierId);
                if (user == null || user.getRole() != RoleType.CASHIER) {
                    throw new IllegalArgumentException("Cashier not found.");
                }
                user.setActive(active);
                userDao.updateCashier(connection, user, false);
                activityLogDao.log(connection, actor.getId(), active ? "ACTIVATE" : "DEACTIVATE", "USER", cashierId,
                        (active ? "Activated " : "Deactivated ") + user.getUsername());
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
