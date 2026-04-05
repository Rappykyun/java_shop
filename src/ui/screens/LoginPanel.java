package ui.screens;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import config.AppConfig;
import config.Theme;
import model.User;
import service.AuthService;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.UIFactory;
import util.ImageUtils;

public class LoginPanel extends JPanel {
    private final AuthService authService;
    private final Consumer<User> loginSuccessHandler;

    private final javax.swing.JTextField usernameField = UIFactory.createTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginPanel(AuthService authService, Consumer<User> loginSuccessHandler) {
        this.authService = authService;
        this.loginSuccessHandler = loginSuccessHandler;

        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        RoundedPanel panel = new RoundedPanel(Theme.LATTE, 30);
        panel.setLayout(new BorderLayout(24, 24));
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        panel.add(buildBrandColumn(), BorderLayout.WEST);
        panel.add(buildFormColumn(), BorderLayout.CENTER);

        wrapper.add(panel);
        return wrapper;
    }

    private JPanel buildBrandColumn() {
        RoundedPanel brandPanel = new RoundedPanel(Theme.ESPRESSO, 26);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        brandPanel.setPreferredSize(new Dimension(460, 600));

        RoundedPanel logoFrame = new RoundedPanel(new Color(151, 99, 58), 24);
        logoFrame.setLayout(new GridBagLayout());
        logoFrame.setOpaque(false);
        logoFrame.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        logoFrame.setMaximumSize(new Dimension(380, 380));
        logoFrame.setAlignmentX(CENTER_ALIGNMENT);

        ImageIcon logoIcon = ImageUtils.loadScaled(AppConfig.IMAGE_LOGO, 320, 320);
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);
        logoFrame.add(logoLabel);

        JLabel titleLabel = new JLabel("Cuddle Cup");
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setFont(new java.awt.Font("Serif", java.awt.Font.BOLD, 32));
        titleLabel.setForeground(java.awt.Color.WHITE);

        JLabel subtitleLabel = new JLabel("More Espresso, Less Depresso");
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);
        subtitleLabel.setFont(Theme.BODY_FONT);
        subtitleLabel.setForeground(Theme.LATTE);

        JLabel addressLabel = new JLabel(AppConfig.STORE_ADDRESS);
        addressLabel.setAlignmentX(CENTER_ALIGNMENT);
        addressLabel.setFont(Theme.SMALL_FONT);
        addressLabel.setForeground(new Color(232, 216, 201));

        brandPanel.add(Box.createVerticalStrut(10));
        if (logoIcon != null) {
            brandPanel.add(logoFrame);
            brandPanel.add(Box.createVerticalStrut(22));
        }
        brandPanel.add(titleLabel);
        brandPanel.add(Box.createVerticalStrut(8));
        brandPanel.add(subtitleLabel);
        brandPanel.add(Box.createVerticalStrut(10));
        brandPanel.add(addressLabel);
        brandPanel.add(Box.createVerticalGlue());
        return brandPanel;
    }

    private JPanel buildFormColumn() {
        RoundedPanel formCard = new RoundedPanel(java.awt.Color.WHITE, 26);
        formCard.setLayout(new BorderLayout(16, 16));
        formCard.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        formCard.setPreferredSize(new Dimension(480, 0));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("SECURE LOGIN");
        badge.setOpaque(true);
        badge.setBackground(Theme.LATTE);
        badge.setForeground(Theme.ESPRESSO);
        badge.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
        badge.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        badge.setAlignmentX(LEFT_ALIGNMENT);
        heading.add(badge);
        heading.add(Box.createVerticalStrut(18));
        heading.add(UIFactory.createTitle("Welcome back"));
        heading.add(Box.createVerticalStrut(8));
        heading.add(UIFactory.createMutedLabel("Login as Admin or Cashier to continue."));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        form.add(UIFactory.createSectionTitle("Username"), gbc);
        gbc.gridy++;
        usernameField.setPreferredSize(new Dimension(380, 48));
        usernameField.setMinimumSize(new Dimension(380, 48));
        form.add(usernameField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(14, 0, 8, 0);
        form.add(UIFactory.createSectionTitle("Password"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 8, 0);
        passwordField.setFont(Theme.BODY_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(11, 12, 11, 12)));
        passwordField.setPreferredSize(new Dimension(380, 48));
        passwordField.setMinimumSize(new Dimension(380, 48));
        form.add(passwordField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(22, 0, 8, 0);
        RoundedButton loginButton = new RoundedButton("Login", Theme.ESPRESSO, java.awt.Color.WHITE);
        loginButton.setPreferredSize(new Dimension(380, 50));
        loginButton.setMinimumSize(new Dimension(380, 50));
        loginButton.addActionListener(event -> attemptLogin());
        form.add(loginButton, gbc);

        RoundedPanel footer = new RoundedPanel(Theme.CREAM, 20);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        formCard.add(heading, BorderLayout.NORTH);
        formCard.add(form, BorderLayout.CENTER);
        formCard.add(footer, BorderLayout.SOUTH);
        return formCard;
    }

    private void attemptLogin() {
        try {
            User user = authService.login(usernameField.getText().trim(), new String(passwordField.getPassword()));
            passwordField.setText("");
            loginSuccessHandler.accept(user);
        } catch (IllegalArgumentException | SQLException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
