package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

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
            seedSampleTransactions(connection);
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
        execute(connection, """
                CREATE TABLE IF NOT EXISTS cashier_sessions (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    cashier_id INT NOT NULL,
                    time_in TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    time_out TIMESTAMP NULL,
                    CONSTRAINT fk_session_cashier FOREIGN KEY (cashier_id) REFERENCES users(id)
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

    private void seedSampleTransactions(Connection connection) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM sales_transactions";
        try (PreparedStatement stmt = connection.prepareStatement(countSql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        }

        int cashierId1 = ensureSeedCashier(connection, "maria", "Maria Santos", "maria123");
        int cashierId2 = ensureSeedCashier(connection, "ralph", "Ralph Garcia", "ralph123");
        int[] cashierIds = {cashierId1, cashierId2};

        String productSql = "SELECT id, name, price FROM products WHERE active = TRUE ORDER BY id LIMIT 20";
        int[][] products;
        String[] productNames;
        BigDecimal[] productPrices;
        try (PreparedStatement stmt = connection.prepareStatement(productSql);
             ResultSet rs = stmt.executeQuery()) {
            java.util.List<int[]> idList = new java.util.ArrayList<>();
            java.util.List<String> nameList = new java.util.ArrayList<>();
            java.util.List<BigDecimal> priceList = new java.util.ArrayList<>();
            while (rs.next()) {
                idList.add(new int[]{rs.getInt("id")});
                nameList.add(rs.getString("name"));
                priceList.add(rs.getBigDecimal("price"));
            }
            products = idList.toArray(new int[0][]);
            productNames = nameList.toArray(new String[0]);
            productPrices = priceList.toArray(new BigDecimal[0]);
        }
        if (products.length == 0) return;

        Random rand = new Random(42);
        LocalDate today = LocalDate.now();

        LocalDate[] sampleDates = {
                today,
                today,
                today,
                today.minusDays(1),
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(3),
                today.minusDays(4),
                today.minusDays(5),
                today.minusDays(6),
                today.minusDays(8),
                today.minusDays(10),
                today.minusDays(12),
                today.minusDays(15),
                today.minusDays(18),
                today.minusDays(20),
                today.minusDays(22),
                today.minusDays(25),
                today.minusDays(30),
                today.minusDays(35),
                today.minusDays(40),
                today.minusDays(45),
                today.minusDays(50),
                today.minusDays(55),
                today.minusDays(60),
        };

        String[] orderTypes = {"DINE-IN", "TAKE-OUT"};

        for (int i = 0; i < sampleDates.length; i++) {
            int cashierId = cashierIds[i % cashierIds.length];
            LocalDateTime txnTime = LocalDateTime.of(sampleDates[i],
                    LocalTime.of(8 + rand.nextInt(12), rand.nextInt(60), rand.nextInt(60)));
            String serial = String.format("%04d%02d%02d%02d%02d%02d",
                    txnTime.getYear(), txnTime.getMonthValue(), txnTime.getDayOfMonth(),
                    txnTime.getHour(), txnTime.getMinute(), txnTime.getSecond());
            String txnNum = "TXN-" + serial;
            String rctNum = "RCT-" + serial;
            String orderType = orderTypes[rand.nextInt(2)];
            String buzzer = String.valueOf(100 + i);
            String custName = "Customer " + (i + 1);

            int itemsInOrder = 1 + rand.nextInt(4);
            BigDecimal subtotal = BigDecimal.ZERO;
            int totalQty = 0;

            int[][] orderItems = new int[itemsInOrder][];
            String[] orderItemNames = new String[itemsInOrder];
            BigDecimal[] orderItemPrices = new BigDecimal[itemsInOrder];
            int[] orderItemQtys = new int[itemsInOrder];

            for (int j = 0; j < itemsInOrder; j++) {
                int idx = rand.nextInt(products.length);
                int qty = 1 + rand.nextInt(3);
                orderItems[j] = products[idx];
                orderItemNames[j] = productNames[idx];
                orderItemPrices[j] = productPrices[idx];
                orderItemQtys[j] = qty;
                subtotal = subtotal.add(productPrices[idx].multiply(BigDecimal.valueOf(qty)));
                totalQty += qty;
            }

            BigDecimal payment = subtotal.add(BigDecimal.valueOf(rand.nextInt(100) + 1));
            BigDecimal change = payment.subtract(subtotal);

            String txnSql = """
                    INSERT INTO sales_transactions
                    (transaction_number, cashier_id, subtotal, total, payment_amount, change_amount,
                     item_count, order_type, buzzer_number, customer_name, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            int txnId;
            try (PreparedStatement stmt = connection.prepareStatement(txnSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, txnNum);
                stmt.setInt(2, cashierId);
                stmt.setBigDecimal(3, subtotal);
                stmt.setBigDecimal(4, subtotal);
                stmt.setBigDecimal(5, payment);
                stmt.setBigDecimal(6, change);
                stmt.setInt(7, totalQty);
                stmt.setString(8, orderType);
                stmt.setString(9, buzzer);
                stmt.setString(10, custName);
                stmt.setTimestamp(11, Timestamp.valueOf(txnTime));
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    keys.next();
                    txnId = keys.getInt(1);
                }
            }

            for (int j = 0; j < itemsInOrder; j++) {
                BigDecimal lineTotal = orderItemPrices[j].multiply(BigDecimal.valueOf(orderItemQtys[j]));
                String itemSql = """
                        INSERT INTO sale_items (transaction_id, product_id, product_name, quantity, unit_price, line_total)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement stmt = connection.prepareStatement(itemSql)) {
                    stmt.setInt(1, txnId);
                    stmt.setInt(2, orderItems[j][0]);
                    stmt.setString(3, orderItemNames[j]);
                    stmt.setInt(4, orderItemQtys[j]);
                    stmt.setBigDecimal(5, orderItemPrices[j]);
                    stmt.setBigDecimal(6, lineTotal);
                    stmt.executeUpdate();
                }
            }

            String rctSql = "INSERT INTO receipts (transaction_id, receipt_number, created_at) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(rctSql)) {
                stmt.setInt(1, txnId);
                stmt.setString(2, rctNum);
                stmt.setTimestamp(3, Timestamp.valueOf(txnTime));
                stmt.executeUpdate();
            }
        }

        seedSessions(connection, cashierIds, sampleDates, rand);
    }

    private void seedSessions(Connection connection, int[] cashierIds, LocalDate[] sampleDates, Random rand)
            throws SQLException {
        String countSql = "SELECT COUNT(*) FROM cashier_sessions";
        try (PreparedStatement stmt = connection.prepareStatement(countSql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }

        java.util.Set<String> seeded = new java.util.HashSet<>();

        for (int i = 0; i < sampleDates.length; i++) {
            int cashierId = cashierIds[i % cashierIds.length];
            String key = cashierId + "-" + sampleDates[i];
            if (seeded.contains(key)) continue;
            seeded.add(key);

            int startHour = 7 + rand.nextInt(3);
            int shiftHours = 4 + rand.nextInt(5);
            LocalDateTime timeIn = LocalDateTime.of(sampleDates[i], LocalTime.of(startHour, rand.nextInt(30)));
            LocalDateTime timeOut = timeIn.plusHours(shiftHours).plusMinutes(rand.nextInt(45));

            if (sampleDates[i].equals(LocalDate.now())) {
                timeOut = null;
            }

            String sql = "INSERT INTO cashier_sessions (cashier_id, time_in, time_out) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, cashierId);
                stmt.setTimestamp(2, Timestamp.valueOf(timeIn));
                if (timeOut != null) {
                    stmt.setTimestamp(3, Timestamp.valueOf(timeOut));
                } else {
                    stmt.setNull(3, java.sql.Types.TIMESTAMP);
                }
                stmt.executeUpdate();
            }
        }
    }

    private int ensureSeedCashier(Connection connection, String username, String fullName, String password)
            throws SQLException {
        String findSql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(findSql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        String salt = PasswordUtils.generateSalt();
        User cashier = new User();
        cashier.setUsername(username);
        cashier.setFullName(fullName);
        cashier.setPasswordSalt(salt);
        cashier.setPasswordHash(PasswordUtils.hashPassword(password, salt));
        cashier.setActive(true);
        Integer roleId = roleDao.findRoleId(connection, RoleType.CASHIER);
        return userDao.insert(connection, cashier, roleId);
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
