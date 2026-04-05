package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import config.AppConfig;
import config.DBConnection;
import model.RoleType;
import model.User;
import util.PasswordUtils;

public class DatabaseInitializer {
    private final RoleDao roleDao = new RoleDao();
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public void initialize() throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            createTables(connection);
            seedRoles(connection);
            seedAdmin(connection);
            seedProducts(connection);
        }
    }

    private void createTables(Connection connection) throws SQLException {
        execute(connection, """
                CREATE TABLE IF NOT EXISTS roles (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(50) NOT NULL UNIQUE
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    full_name VARCHAR(150) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    password_salt VARCHAR(255) NOT NULL,
                    role_id INT NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS products (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    sku VARCHAR(50) NOT NULL UNIQUE,
                    name VARCHAR(150) NOT NULL,
                    category VARCHAR(100) NOT NULL,
                    description VARCHAR(255),
                    price DECIMAL(10, 2) NOT NULL,
                    stock_quantity INT NOT NULL DEFAULT 0,
                    low_stock_threshold INT NOT NULL DEFAULT 10,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS inventory_movements (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    product_id INT NOT NULL,
                    movement_type VARCHAR(50) NOT NULL,
                    quantity_change INT NOT NULL,
                    previous_quantity INT NOT NULL,
                    new_quantity INT NOT NULL,
                    note VARCHAR(255),
                    performed_by INT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id),
                    CONSTRAINT fk_inventory_user FOREIGN KEY (performed_by) REFERENCES users(id)
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS sales_transactions (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    transaction_number VARCHAR(50) NOT NULL UNIQUE,
                    cashier_id INT NOT NULL,
                    subtotal DECIMAL(10, 2) NOT NULL,
                    total DECIMAL(10, 2) NOT NULL,
                    payment_amount DECIMAL(10, 2) NOT NULL,
                    change_amount DECIMAL(10, 2) NOT NULL,
                    item_count INT NOT NULL,
                    order_type VARCHAR(20),
                    buzzer_number VARCHAR(20),
                    customer_name VARCHAR(150),
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_sales_cashier FOREIGN KEY (cashier_id) REFERENCES users(id)
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS sale_items (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    transaction_id INT NOT NULL,
                    product_id INT NOT NULL,
                    product_name VARCHAR(150) NOT NULL,
                    quantity INT NOT NULL,
                    unit_price DECIMAL(10, 2) NOT NULL,
                    line_total DECIMAL(10, 2) NOT NULL,
                    CONSTRAINT fk_sale_items_transaction FOREIGN KEY (transaction_id) REFERENCES sales_transactions(id),
                    CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES products(id)
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS receipts (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    transaction_id INT NOT NULL UNIQUE,
                    receipt_number VARCHAR(50) NOT NULL UNIQUE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_receipts_transaction FOREIGN KEY (transaction_id) REFERENCES sales_transactions(id)
                )
                """);
        execute(connection, """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    actor_user_id INT,
                    action_type VARCHAR(50) NOT NULL,
                    entity_type VARCHAR(50) NOT NULL,
                    entity_id INT,
                    description VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_activity_actor FOREIGN KEY (actor_user_id) REFERENCES users(id)
                )
                """);
    }

    private void seedRoles(Connection connection) throws SQLException {
        insertRoleIfMissing(connection, RoleType.ADMIN);
        insertRoleIfMissing(connection, RoleType.CASHIER);
    }

    private void seedAdmin(Connection connection) throws SQLException {
        if (userDao.hasAnyAdmin(connection)) {
            return;
        }
        String salt = PasswordUtils.generateSalt();
        User user = new User();
        user.setUsername(AppConfig.DEFAULT_ADMIN_USERNAME);
        user.setFullName(AppConfig.DEFAULT_ADMIN_NAME);
        user.setPasswordSalt(salt);
        user.setPasswordHash(PasswordUtils.hashPassword(AppConfig.DEFAULT_ADMIN_PASSWORD, salt));
        user.setActive(true);

        Integer roleId = roleDao.findRoleId(connection, RoleType.ADMIN);
        int adminId = userDao.insert(connection, user, roleId);
        activityLogDao.log(connection, adminId, "BOOTSTRAP", "USER", adminId,
                "Default admin account created.");
    }

    private void seedProducts(Connection connection) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM products";
        try (PreparedStatement stmt = connection.prepareStatement(countSql);
             java.sql.ResultSet rs = stmt.executeQuery()) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        }

        // Iced Coffee
        insertProduct(connection, "IC-AMR-16", "Americano 16oz", "Iced Coffee", 55);
        insertProduct(connection, "IC-AMR-22", "Americano 22oz", "Iced Coffee", 75);
        insertProduct(connection, "IC-CFL-16", "Cafe Latte 16oz", "Iced Coffee", 65);
        insertProduct(connection, "IC-CFL-22", "Cafe Latte 22oz", "Iced Coffee", 85);
        insertProduct(connection, "IC-SPL-16", "Spanish Latte 16oz", "Iced Coffee", 79);
        insertProduct(connection, "IC-SPL-22", "Spanish Latte 22oz", "Iced Coffee", 89);
        insertProduct(connection, "IC-CRM-16", "Caramel Macchiato 16oz", "Iced Coffee", 79);
        insertProduct(connection, "IC-CRM-22", "Caramel Macchiato 22oz", "Iced Coffee", 89);
        insertProduct(connection, "IC-DCL-16", "Dark Choco Cafe Latte 16oz", "Iced Coffee", 69);
        insertProduct(connection, "IC-DCL-22", "Dark Choco Cafe Latte 22oz", "Iced Coffee", 79);
        insertProduct(connection, "IC-ORL-16", "Oreo Latte 16oz", "Iced Coffee", 69);
        insertProduct(connection, "IC-ORL-22", "Oreo Latte 22oz", "Iced Coffee", 79);
        insertProduct(connection, "IC-FRV-16", "French Vanilla 16oz", "Iced Coffee", 69);
        insertProduct(connection, "IC-FRV-22", "French Vanilla 22oz", "Iced Coffee", 79);
        insertProduct(connection, "IC-SCL-16", "Strawberry Cafe Latte 16oz", "Iced Coffee", 69);
        insertProduct(connection, "IC-SCL-22", "Strawberry Cafe Latte 22oz", "Iced Coffee", 79);

        // Milky Series
        insertProduct(connection, "MK-BRM-16", "Berry Mocha 16oz", "Milky Series", 69);
        insertProduct(connection, "MK-BRM-22", "Berry Mocha 22oz", "Milky Series", 79);
        insertProduct(connection, "MK-MCH-16", "Macha 16oz", "Milky Series", 69);
        insertProduct(connection, "MK-MCH-22", "Macha 22oz", "Milky Series", 79);
        insertProduct(connection, "MK-STR-16", "Strawberry 16oz", "Milky Series", 69);
        insertProduct(connection, "MK-STR-22", "Strawberry 22oz", "Milky Series", 79);

        // Milktea Series
        insertProduct(connection, "MT-WTR-16", "Wintermelon 16oz", "Milktea Series", 39);
        insertProduct(connection, "MT-WTR-22", "Wintermelon 22oz", "Milktea Series", 49);
        insertProduct(connection, "MT-DKC-16", "Dark Chocolate 16oz", "Milktea Series", 39);
        insertProduct(connection, "MT-DKC-22", "Dark Chocolate 22oz", "Milktea Series", 49);
        insertProduct(connection, "MT-SLC-16", "Salted Caramel 16oz", "Milktea Series", 39);
        insertProduct(connection, "MT-SLC-22", "Salted Caramel 22oz", "Milktea Series", 49);
        insertProduct(connection, "MT-CNC-16", "Cookies & Cream 16oz", "Milktea Series", 39);
        insertProduct(connection, "MT-CNC-22", "Cookies & Cream 22oz", "Milktea Series", 49);
        insertProduct(connection, "MT-STB-16", "Strawberry 16oz", "Milktea Series", 39);
        insertProduct(connection, "MT-STB-22", "Strawberry 22oz", "Milktea Series", 49);
        insertProduct(connection, "MT-MAT-16", "Matcha 16oz", "Milktea Series", 39);
        insertProduct(connection, "MT-MAT-22", "Matcha 22oz", "Milktea Series", 49);
        insertProduct(connection, "MT-TAR-16", "Taro 16oz", "Milktea Series", 39);
        insertProduct(connection, "MT-TAR-22", "Taro 22oz", "Milktea Series", 49);

        // Hot Coffee
        insertProduct(connection, "HC-AMR-12", "Cafe Americano 12oz", "Hot Coffee", 35);
        insertProduct(connection, "HC-CFL-12", "Cafe Latte 12oz", "Hot Coffee", 45);
        insertProduct(connection, "HC-SPL-12", "Spanish Latte 12oz", "Hot Coffee", 55);
        insertProduct(connection, "HC-FRV-12", "French Vanilla 12oz", "Hot Coffee", 55);
        insertProduct(connection, "HC-CHO-12", "Chocolate 12oz", "Hot Coffee", 65);

        // Frappe
        insertProduct(connection, "FR-CRM-16", "Caramel Macchiato 16oz", "Frappe", 60);
        insertProduct(connection, "FR-CRM-22", "Caramel Macchiato 22oz", "Frappe", 70);
        insertProduct(connection, "FR-MNG-16", "Mango Graham 16oz", "Frappe", 50);
        insertProduct(connection, "FR-MNG-22", "Mango Graham 22oz", "Frappe", 55);
        insertProduct(connection, "FR-STR-16", "Strawberry 16oz", "Frappe", 50);
        insertProduct(connection, "FR-STR-22", "Strawberry 22oz", "Frappe", 55);
        insertProduct(connection, "FR-CHO-16", "Chocolate 16oz", "Frappe", 50);
        insertProduct(connection, "FR-CHO-22", "Chocolate 22oz", "Frappe", 55);
        insertProduct(connection, "FR-JVC-16", "Java Chip 16oz", "Frappe", 50);
        insertProduct(connection, "FR-JVC-22", "Java Chip 22oz", "Frappe", 55);
    }

    private void insertProduct(Connection connection, String sku, String name, String category, int price)
            throws SQLException {
        String sql = """
                INSERT INTO products (sku, name, category, description, price, stock_quantity, low_stock_threshold, active)
                VALUES (?, ?, ?, ?, ?, 100, 10, TRUE)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sku);
            stmt.setString(2, name);
            stmt.setString(3, category);
            stmt.setString(4, name);
            stmt.setInt(5, price);
            stmt.executeUpdate();
        }
    }

    private void insertRoleIfMissing(Connection connection, RoleType roleType) throws SQLException {
        if (roleDao.findRoleId(connection, roleType) != null) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO roles (name) VALUES (?)")) {
            statement.setString(1, roleType.name());
            statement.executeUpdate();
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }
}
