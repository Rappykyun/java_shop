package ui.screens.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import config.AppConfig;
import config.Theme;
import model.User;
import service.DashboardService;
import service.InventoryService;
import service.ProductService;
import service.SalesService;
import service.UserService;
import ui.components.RoundedButton;
import ui.components.UIFactory;
import util.ImageUtils;

public class AdminPanel extends JPanel {
    private final DashboardPanel dashboardPanel;
    private final ProductsPanel productsPanel;
    private final InventoryPanel inventoryPanel;
    private final UsersPanel usersPanel;
    private final TransactionsPanel transactionsPanel;
    private final SessionsPanel sessionsPanel;

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);
    private final JLabel userLabel = new JLabel("", SwingConstants.RIGHT);
    private User currentUser;

    public AdminPanel(DashboardService dashboardService, ProductService productService, InventoryService inventoryService,
            UserService userService, SalesService salesService, Runnable logoutHandler) {
        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);

        dashboardPanel = new DashboardPanel(dashboardService);
        productsPanel = new ProductsPanel(productService);
        inventoryPanel = new InventoryPanel(inventoryService);
        usersPanel = new UsersPanel(userService);
        transactionsPanel = new TransactionsPanel(salesService);
        sessionsPanel = new SessionsPanel(userService);

        contentPanel.setOpaque(false);
        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(productsPanel, "products");
        contentPanel.add(inventoryPanel, "inventory");
        contentPanel.add(usersPanel, "users");
        contentPanel.add(transactionsPanel, "transactions");
        contentPanel.add(sessionsPanel, "sessions");

        add(buildSidebar(logoutHandler), BorderLayout.WEST);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        this.userLabel.setText(currentUser.getFullName() + "  ");
        productsPanel.setCurrentUser(currentUser);
        inventoryPanel.setCurrentUser(currentUser);
        usersPanel.setCurrentUser(currentUser);
    }

    public void refreshAll() throws SQLException {
        dashboardPanel.refreshData();
        productsPanel.refreshData();
        inventoryPanel.refreshData();
        usersPanel.refreshData();
        transactionsPanel.refreshData();
        sessionsPanel.refreshData();
        showSection("dashboard");
    }

    private JPanel buildSidebar(Runnable logoutHandler) {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.ESPRESSO);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(32, 18, 24, 18));

        javax.swing.ImageIcon logoIcon = ImageUtils.loadScaled(AppConfig.IMAGE_LOGO, 70, 70);
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setAlignmentX(LEFT_ALIGNMENT);
            sidebar.add(logoLabel);
            sidebar.add(Box.createVerticalStrut(12));
        }

        JLabel brandLabel = new JLabel("Cuddle Cup");
        brandLabel.setFont(new java.awt.Font("Serif", java.awt.Font.BOLD, 30));
        brandLabel.setForeground(java.awt.Color.WHITE);
        sidebar.add(brandLabel);
        sidebar.add(Box.createVerticalStrut(8));

        JLabel moduleLabel = new JLabel("Admin Workspace");
        moduleLabel.setFont(Theme.BODY_BOLD_FONT);
        moduleLabel.setForeground(Theme.LATTE);
        sidebar.add(moduleLabel);
        sidebar.add(Box.createVerticalStrut(28));

        sidebar.add(createMenuButton("Dashboard", "dashboard"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createMenuButton("Products", "products"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createMenuButton("Inventory", "inventory"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createMenuButton("Cashiers", "users"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createMenuButton("Transactions", "transactions"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createMenuButton("Time In / Out", "sessions"));

        sidebar.add(Box.createVerticalGlue());
        RoundedButton logoutButton = new RoundedButton("Logout", Theme.GOLD, Theme.ESPRESSO_DARK);
        logoutButton.setAlignmentX(LEFT_ALIGNMENT);
        logoutButton.addActionListener(event -> logoutHandler.run());
        sidebar.add(logoutButton);
        return sidebar;
    }

    private RoundedButton createMenuButton(String label, String card) {
        RoundedButton button = new RoundedButton(label, Theme.ESPRESSO_DARK, java.awt.Color.WHITE);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        button.addActionListener(event -> {
            try {
                refreshVisiblePanel(card);
                showSection(card);
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(this, exception.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return button;
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(26, 22, 22, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel headingCopy = new JPanel();
        headingCopy.setOpaque(false);
        headingCopy.setLayout(new BoxLayout(headingCopy, BoxLayout.Y_AXIS));
        JLabel title = UIFactory.createPageTitle("Store Oversight");
        JLabel subtitle = UIFactory.createMutedLabel("Monitor sales, stock, team activity, and receipts.");
        headingCopy.add(title);
        headingCopy.add(Box.createVerticalStrut(6));
        headingCopy.add(subtitle);

        userLabel.setFont(Theme.BODY_BOLD_FONT);
        userLabel.setForeground(Theme.ESPRESSO);
        userLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        userLabel.setOpaque(true);
        userLabel.setBackground(java.awt.Color.WHITE);

        header.add(headingCopy, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);
        main.add(contentPanel, BorderLayout.CENTER);
        return main;
    }

    private void showSection(String card) {
        contentLayout.show(contentPanel, card);
    }

    private void refreshVisiblePanel(String card) throws SQLException {
        switch (card) {
            case "dashboard" -> dashboardPanel.refreshData();
            case "products" -> productsPanel.refreshData();
            case "inventory" -> inventoryPanel.refreshData();
            case "users" -> usersPanel.refreshData();
            case "transactions" -> transactionsPanel.refreshData();
            case "sessions" -> sessionsPanel.refreshData();
            default -> {
            }
        }
    }
}
