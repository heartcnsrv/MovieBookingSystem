package lumiere.server;

import com.google.gson.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.swing.*;

public class ServerView extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JTextArea loginLogArea;
    private JTextArea adminActionLogArea;
    private JButton startButton, stopButton;
    private Registry registry;
    private boolean serverRunning = false;
    private static ServerView instance;
    private static final String LOGIN_HISTORY_FILE = "res/login_history.json";

    public ServerView() {
        instance = this;

        setTitle("Lumiere Cinema Server");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // If the server is running, stop it first
                if (serverRunning) {
                    stopServer();
                }
                // Then close the window and exit
                dispose();
                System.exit(0);
            }
        });

        setLocationRelativeTo(null);

        // Main panel with CardLayout for two tabs
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        // Navigation panel with two buttons (tabs)
        JPanel navPanel = new JPanel();
        JButton loginButton = new JButton("Login Tracking");
        JButton adminButton = new JButton("Admin Actions");
        loginButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        adminButton.addActionListener(e -> cardLayout.show(mainPanel, "admin"));
        navPanel.add(loginButton);
        navPanel.add(adminButton);

        // Create the two panels (cards)
        JPanel loginTrackingPanel = createLoginTrackingPanel();
        JPanel adminActionsPanel = createAdminActionsPanel();

        mainPanel.add(loginTrackingPanel, "login");
        mainPanel.add(adminActionsPanel, "admin");
        cardLayout.show(mainPanel, "login");

        setLayout(new BorderLayout());
        add(navPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Panel for Login Tracking (server status, logs, and Start/Stop buttons)
     */
    private JPanel createLoginTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Server status label at the top
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Server Status: STOPPED");
        statusLabel.setForeground(Color.RED);
        statusPanel.add(statusLabel);
        panel.add(statusPanel, BorderLayout.NORTH);

        // Text area for server log messages
        loginLogArea = new JTextArea();
        loginLogArea.setEditable(false);
        panel.add(new JScrollPane(loginLogArea), BorderLayout.CENTER);

        // Control panel for Start/Stop
        JPanel controlPanel = new JPanel();
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Panel for Admin Actions (admin logs, Clear button)
     */
    private JPanel createAdminActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Title at the top
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Admin Actions Log");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);

        // Text area for admin actions
        adminActionLogArea = new JTextArea();
        adminActionLogArea.setEditable(false);
        adminActionLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(adminActionLogArea), BorderLayout.CENTER);

        // Control panel with a Clear button
        JPanel controlPanel = new JPanel();
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> adminActionLogArea.setText(""));
        controlPanel.add(clearButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        // Initialize with a timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        adminActionLogArea.append("Admin action panel initialized at " + timestamp + "\n");
        return panel;
    }

    public static void logAdminAction(String actionType, String details) {
        if (instance == null || instance.adminActionLogArea == null) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logLine = String.format(
                "%s - Attempting to send admin action to server: %s %s",
                timestamp, actionType, details
        );
        instance.adminActionLogArea.append(logLine + "\n");

        // Simulate server response logging (can be expanded based on actual logic)
        instance.adminActionLogArea.append(timestamp + " - Admin action successfully sent to server\n");
        instance.adminActionLogArea.append(timestamp + " - Server response: SUCCESS\n");
    }

    /**
     * Creates the RMI registry on port 1099, binds MovieImpl and UserManager,
     * and updates the GUI/log to show the server is running.
     */
    private void startServer() {
        try {
            if (!serverRunning) {
                registry = LocateRegistry.createRegistry(1099);
                logServerMessage("RMI Registry started on port 1099.");

                MovieImpl movieService = new MovieImpl();  // uses JSON for movies
                registry.rebind("MovieService", movieService);
                logServerMessage("MovieService bound successfully.");

                UserManager userManager = new UserManager(); // uses JSON for users
                registry.rebind("UserService", userManager);
                logServerMessage("UserService bound successfully.");

                BookingsImpl bookingService = new BookingsImpl();
                registry.rebind("BookingService", bookingService);
                logServerMessage("BookingService bound successfully.");

                serverRunning = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                statusLabel.setText("Server Status: RUNNING");
                statusLabel.setForeground(Color.GREEN);

                String ip = InetAddress.getLocalHost().getHostAddress();
                logServerMessage("Server IP address: " + ip);
                logServerMessage("IMPORTANT: Ensure port 1099 is open in your firewall.");
            } else {
                logServerMessage("Server is already running.");
            }
        } catch (Exception ex) {
            logServerMessage("Error starting server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Unbinds the remote objects, unexports the registry, and updates the GUI/log to show the server is stopped.
     */
    private void stopServer() {
        try {
            if (serverRunning && registry != null) {
                Naming.unbind("MovieService");
                Naming.unbind("UserService");
                Naming.unbind("BookingService");
                logServerMessage("Remote objects unbound.");

                UnicastRemoteObject.unexportObject(registry, true);
                logServerMessage("Registry unexported. Server fully stopped.");

                serverRunning = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                statusLabel.setText("Server Status: STOPPED");
                statusLabel.setForeground(Color.RED);
            } else {
                logServerMessage("Server is not running or registry is null.");
            }
        } catch (Exception ex) {
            logServerMessage("Error stopping server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Appends a message to the login tracking log area.
     * (We could also store these server-level logs in a JSON file if desired.)
     */
    private void logServerMessage(String message) {
        loginLogArea.append(message + "\n");
    }

    /**
     * Logs a user connection event (login success, fail, logout, etc.) with IP, username, role, timestamp.
     * Also writes to login_history.json for permanent record.
     *
     * @param clientIp  The IP address of the client
     * @param username  The user's username
     * @param role      e.g., \"admin\" or \"customer\"
     * @param action    e.g., \"login success\", \"login fail\", \"logout\"
     */
    public static void logUserConnection(String clientIp, String username, String role, String action) {
        if (instance == null || instance.loginLogArea == null) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logLine = String.format(
                "%s | IP: %s | Role: %s | User: %s | Action: %s",
                timestamp, clientIp, role, username, action
        );
        instance.loginLogArea.append(logLine + "\n");

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonArray logArray = new JsonArray();

            StringBuilder fileContent = new StringBuilder();
            try (Scanner scanner = new Scanner(new FileReader(LOGIN_HISTORY_FILE))) {
                while (scanner.hasNextLine()) {
                    fileContent.append(scanner.nextLine());
                }
            }

            if (fileContent.length() > 0) {
                JsonElement element = gson.fromJson(fileContent.toString(), JsonElement.class);
                if (element.isJsonArray()) {
                    logArray = element.getAsJsonArray();
                } else {
                    instance.logServerMessage("Warning: login_history.json wasn't an array. Starting fresh.");
                    logArray = new JsonArray();
                }
            }

            JsonObject logEntry = new JsonObject();
            logEntry.addProperty("timestamp", timestamp);
            logEntry.addProperty("ip", clientIp);
            logEntry.addProperty("role", role);
            logEntry.addProperty("username", username);
            logEntry.addProperty("action", action);

            logArray.add(logEntry);

            try (FileWriter writer = new FileWriter(LOGIN_HISTORY_FILE)) {
                gson.toJson(logArray, writer);
                instance.logServerMessage("Login log successfully saved to file.");
            }
        } catch (Exception e) {
            instance.logServerMessage("Error saving login log: " + e.getMessage());
            e.printStackTrace();
        }
    }
}