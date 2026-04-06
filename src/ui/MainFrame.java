package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import config.AppConfig;
import config.Theme;
import dao.DatabaseInitializer;
import model.RoleType;
import model.User;
import service.AuthService;
import service.DashboardService;
import service.InventoryService;
import service.ProductService;
import service.SalesService;
import service.UserService;
import ui.screens.LoginPanel;
import ui.screens.admin.AdminPanel;
import ui.screens.cashier.CashierPanel;

public class MainFrame extends JFrame {
    private static final String LOGIN_CARD = "login";
    private static final String ADMIN_CARD = "admin";
    private static final String CASHIER_CARD = "cashier";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final AuthService authService = new AuthService();
    private final ProductService productService = new ProductService();
    private final InventoryService inventoryService = new InventoryService();
    private final UserService userService = new UserService();
    private final SalesService salesService = new SalesService();
    private final DashboardService dashboardService = new DashboardService();

    private final LoginPanel loginPanel;
    private final AdminPanel adminPanel;
    private final CashierPanel cashierPanel;

    public MainFrame() throws SQLException {
        super(AppConfig.APP_NAME);
        initializeDatabase();

        getContentPane().setBackground(Theme.CREAM);
        setLayout(new BorderLayout());
        setSize(1440, 860);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        loginPanel = new LoginPanel(authService, this::handleLogin);
        adminPanel = new AdminPanel(dashboardService, productService, inventoryService, userService, salesService,
                this::logout);
        cashierPanel = new CashierPanel(productService, salesService, userService, this::logout);

        contentPanel.add(loginPanel, LOGIN_CARD);
        contentPanel.add(adminPanel, ADMIN_CARD);
        contentPanel.add(cashierPanel, CASHIER_CARD);
        add(contentPanel, BorderLayout.CENTER);

        showLogin();
    }

    private void initializeDatabase() throws SQLException {
        new DatabaseInitializer().initialize();
    }

    private void handleLogin(User user) {
        try {
            if (user.getRole() == RoleType.ADMIN) {
                adminPanel.setCurrentUser(user);
                adminPanel.refreshAll();
                cardLayout.show(contentPanel, ADMIN_CARD);
            } else {
                cashierPanel.setCurrentUser(user);
                cashierPanel.refreshProducts();
                cardLayout.show(contentPanel, CASHIER_CARD);
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        cashierPanel.clockOut();
        showLogin();
    }

    private void showLogin() {
        cardLayout.show(contentPanel, LOGIN_CARD);
    }
}
