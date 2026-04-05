package ui.screens.admin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import model.User;
import service.UserService;
import ui.components.RoundedButton;
import ui.components.UIFactory;
import util.DateTimeUtils;

public class UsersPanel extends JPanel {
    private final UserService userService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<User> cashiers = new ArrayList<>();
    private User currentUser;

    public UsersPanel(UserService userService) {
        this.userService = userService;
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);

        tableModel = new DefaultTableModel(new Object[] { "ID", "Username", "Full Name", "Status", "Created" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.removeColumn(table.getColumnModel().getColumn(0));

        add(buildToolbar(), BorderLayout.NORTH);
        add(UIFactory.createTableScrollPane(table), BorderLayout.CENTER);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void refreshData() throws SQLException {
        cashiers.clear();
        cashiers.addAll(userService.listCashiers());
        tableModel.setRowCount(0);
        for (User cashier : cashiers) {
            tableModel.addRow(new Object[] {
                    cashier.getId(),
                    cashier.getUsername(),
                    cashier.getFullName(),
                    cashier.isActive() ? "Active" : "Inactive",
                    DateTimeUtils.format(cashier.getCreatedAt())
            });
        }
    }

    private JPanel buildToolbar() {
        JPanel panel = UIFactory.createCard();
        panel.setLayout(new BorderLayout());
        panel.add(UIFactory.createSectionTitle("Cashier Accounts"), BorderLayout.WEST);

        JPanel actions = UIFactory.createFlowPanel(java.awt.FlowLayout.RIGHT);
        RoundedButton addButton = new RoundedButton("Create Cashier", config.Theme.SUCCESS, java.awt.Color.WHITE);
        RoundedButton editButton = new RoundedButton("Edit", config.Theme.GOLD, config.Theme.ESPRESSO_DARK);
        RoundedButton toggleButton = new RoundedButton("Activate / Deactivate", config.Theme.ESPRESSO,
                java.awt.Color.WHITE);

        addButton.addActionListener(event -> openCashierDialog(null));
        editButton.addActionListener(event -> openCashierDialog(getSelectedCashier()));
        toggleButton.addActionListener(event -> toggleCashier());

        actions.add(addButton);
        actions.add(editButton);
        actions.add(toggleButton);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private User getSelectedCashier() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a cashier first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return cashiers.get(table.convertRowIndexToModel(row));
    }

    private void openCashierDialog(User source) {
        if (source == null && currentUser == null) {
            return;
        }

        javax.swing.JTextField usernameField = UIFactory.createTextField();
        javax.swing.JTextField fullNameField = UIFactory.createTextField();
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(config.Theme.BODY_FONT);
        JCheckBox activeBox = new JCheckBox("Active");
        activeBox.setOpaque(false);

        if (source != null) {
            usernameField.setText(source.getUsername());
            fullNameField.setText(source.getFullName());
            activeBox.setSelected(source.isActive());
        } else {
            activeBox.setSelected(true);
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.add(new JLabel("Username"));
        form.add(usernameField);
        form.add(new JLabel("Full Name"));
        form.add(fullNameField);
        form.add(new JLabel(source == null ? "Password" : "New Password (optional)"));
        form.add(passwordField);
        form.add(activeBox);

        int result = JOptionPane.showConfirmDialog(this, form,
                source == null ? "Create Cashier" : "Edit Cashier",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            User cashier = source == null ? new User() : source;
            cashier.setUsername(usernameField.getText().trim());
            cashier.setFullName(fullNameField.getText().trim());
            cashier.setActive(activeBox.isSelected());
            userService.saveCashier(cashier, new String(passwordField.getPassword()).trim(), currentUser);
            refreshData();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Cashiers", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleCashier() {
        User cashier = getSelectedCashier();
        if (cashier == null) {
            return;
        }
        try {
            userService.setCashierActive(cashier.getId(), !cashier.isActive(), currentUser);
            refreshData();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Cashiers", JOptionPane.ERROR_MESSAGE);
        }
    }
}
