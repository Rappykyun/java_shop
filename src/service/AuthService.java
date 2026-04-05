package service;

import java.sql.SQLException;

import dao.UserDao;
import model.User;
import util.PasswordUtils;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public User login(String username, String password) throws SQLException {
        User user = userDao.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("This account is inactive.");
        }
        boolean valid = PasswordUtils.verifyPassword(password, user.getPasswordSalt(), user.getPasswordHash());
        if (!valid) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return user;
    }
}
