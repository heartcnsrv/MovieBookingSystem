package lumiere.view;

import lumiere.model.Booking;
import lumiere.model.Movie;
import lumiere.controller.MovieController;
import lumiere.server.BookingService;
import lumiere.server.MovieService;
import lumiere.server.ServerClient;


import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

public class AdminMain extends JFrame {
    private JTabbedPane tabbedPane;
    private JButton logoutButton;
    private static AdminMoviePanel adminMoviePanel;
    private AdminBookingPanel adminBookingPanel;
    private String serverIp;


    public AdminMain(String serverIp) {
        this.serverIp = serverIp;
        setTitle("Lumiere Admin Panel");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            adminMoviePanel = new AdminMoviePanel(ServerClient.getInstance(serverIp).getMovieService());
            MovieService movieService = ServerClient.getInstance(serverIp).getMovieService();
            new MovieController(movieService, adminMoviePanel);


            BookingService bookingService = ServerClient.getInstance(serverIp).getBookingService();
            adminBookingPanel = new AdminBookingPanel(bookingService);

            tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Movies", adminMoviePanel);
            tabbedPane.addTab("Bookings", adminBookingPanel);

        } catch (Exception e) {
            e.printStackTrace();
        }

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane);
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                Login.main(null);
            });
        }
    }

    public static void updateMovieList(List<Movie> movies) {
        if (adminMoviePanel != null) {
            adminMoviePanel.updateMovieTable(movies);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminMain("localhost").setVisible(true));
    }
}
