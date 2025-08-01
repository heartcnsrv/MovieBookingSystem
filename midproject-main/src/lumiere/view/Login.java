package lumiere.view;

import lumiere.server.UserService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Login extends JFrame {
    private static String loggedInUsername;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Panels for the login flow
    private JPanel choicePanel;
    private JPanel loginPanel;
    private JPanel signUpPanel;

    // Components for loginPanel
    private JTextField loginServerIPField;
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JButton signUpButton;

    // Components for signUpPanel
    private JTextField signUpServerIPField;
    private JTextField signUpUsernameField;
    private JPasswordField signUpPasswordField;
    private JPasswordField signUpConfirmPasswordField;
    private String currentRole = "customer";

    public Login() {
        setTitle("Lumiere Cinema - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        createChoicePanel();
        createLoginPanel();
        createSignUpPanel();

        mainPanel.add(choicePanel, "choice");
        mainPanel.add(loginPanel, "login");
        mainPanel.add(signUpPanel, "signup");

        add(mainPanel);
        cardLayout.show(mainPanel, "choice");
    }

    public Login(String defaultIp) {
        this();
        loginServerIPField.setText(defaultIp);
        signUpServerIPField.setText(defaultIp);
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    private void createChoicePanel() {
        choicePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Welcome to Lumiere Cinema");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        choicePanel.add(titleLabel, gbc);

        JButton adminButton = new JButton("Admin Login");
        JButton clientButton = new JButton("Client Login");
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        choicePanel.add(adminButton, gbc);
        gbc.gridx = 1;
        choicePanel.add(clientButton, gbc);

        adminButton.addActionListener(e -> {
            currentRole = "admin";
            signUpButton.setVisible(false);
            cardLayout.show(mainPanel, "login");
        });

        clientButton.addActionListener(e -> {
            currentRole = "customer";
            signUpButton.setVisible(true);
            cardLayout.show(mainPanel, "login");
        });
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        loginPanel.add(new JLabel("Server IP:"), gbc);
        loginServerIPField = new JTextField("localhost");
        gbc.gridx = 1;
        loginPanel.add(loginServerIPField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        loginUsernameField = new JTextField();
        gbc.gridx = 1;
        loginPanel.add(loginUsernameField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        loginPanel.add(new JLabel("Password:"), gbc);
        loginPasswordField = new JPasswordField();
        gbc.gridx = 1;
        loginPanel.add(loginPasswordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");
        signUpButton = new JButton("Sign Up");
        signUpButton.setVisible(false);

        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        loginPanel.add(buttonPanel, gbc);

        loginButton.addActionListener(e -> authenticateLogin());
        signUpButton.addActionListener(e -> cardLayout.show(mainPanel, "signup"));
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "choice"));
    }

    private void createSignUpPanel() {
        signUpPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Sign Up");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        signUpPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        signUpPanel.add(new JLabel("Server IP:"), gbc);
        signUpServerIPField = new JTextField("localhost");
        gbc.gridx = 1;
        signUpPanel.add(signUpServerIPField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        signUpPanel.add(new JLabel("Username:"), gbc);
        signUpUsernameField = new JTextField();
        gbc.gridx = 1;
        signUpPanel.add(signUpUsernameField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        signUpPanel.add(new JLabel("Password:"), gbc);
        signUpPasswordField = new JPasswordField();
        gbc.gridx = 1;
        signUpPanel.add(signUpPasswordField, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        signUpPanel.add(new JLabel("Confirm Password:"), gbc);
        signUpConfirmPasswordField = new JPasswordField();
        gbc.gridx = 1;
        signUpPanel.add(signUpConfirmPasswordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton signUpButton = new JButton("Sign Up");
        JButton backButton = new JButton("Back");
        buttonPanel.add(signUpButton);
        buttonPanel.add(backButton);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        signUpPanel.add(buttonPanel, gbc);

        signUpButton.addActionListener(e -> handleSignUp());
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
    }

    private void authenticateLogin() {
        String ip = loginServerIPField.getText().trim();
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UserService userService = (UserService) Naming.lookup("rmi://" + ip + ":1099/UserService");
            boolean isAuthenticated = userService.authenticate(username, password, currentRole);
            if (isAuthenticated) {
                loggedInUsername = username; // Save username
                dispose();
                if ("admin".equalsIgnoreCase(currentRole)) {
                    SwingUtilities.invokeLater(() -> new AdminMain(ip).setVisible(true));
                } else {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            // Pass the logged-in username to the ClientMain constructor.
                            new ClientMain(ip, 1099, username);
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Connection error: Could not connect to server.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Connection error: Could not reach server.", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void handleSignUp() {
        String ip = signUpServerIPField.getText().trim();
        String username = signUpUsernameField.getText().trim();
        String password = new String(signUpPasswordField.getPassword());
        String confirmPassword = new String(signUpConfirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Sign Up Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Sign Up Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UserService userService = (UserService) Naming.lookup("rmi://" + ip + ":1099/UserService");
            boolean success = userService.registerCustomer(username, password);
            if (success) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainPanel, "login");
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists or registration failed.", "Sign Up Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Server error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Connection error: Could not reach server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}