package lumiere.view;

import lumiere.model.Booking;
import lumiere.model.Movie;
import lumiere.server.BookingService;
import lumiere.server.ClientCallback;
import lumiere.server.ClientCallbackImpl;
import lumiere.server.MovieService;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ClientMain extends UnicastRemoteObject implements ClientCallback {
    private static ClientMain instance;  // static reference to the one ClientMain

    private MovieService movieService;
    private BookingService bookingService;

    // The main JFrame
    private JFrame mainFrame;

    // CardLayout container
    private JPanel cardPanel;
    private static final String MOVIES_CARD = "MOVIES_CARD";
    private static final String BOOKINGS_CARD = "BOOKINGS_CARD";
    private ClientMoviePanel clientMoviePanel;
    private ClientBookingPanel clientBookingPanel;
    private String loggedInUsername;

    public ClientMain(String serverIp, int port, String loggedInUsername) throws RemoteException {
        super();
        instance = this;
        this.loggedInUsername = loggedInUsername;  // store the logged-in username
        try {
            movieService = (MovieService) Naming.lookup("rmi://" + serverIp + ":" + port + "/MovieService");
            bookingService = (BookingService) Naming.lookup("rmi://" + serverIp + ":" + port + "/BookingService");

            movieService.registerClient(this);
            setupGUI();

            ClientCallback callback = new ClientCallbackImpl(clientBookingPanel);
            bookingService.registerClient(callback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getter for the logged-in username.
    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    private void setupGUI() {
        mainFrame = new JFrame("Lumière - Movies");
        mainFrame.setSize(1000, 700);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        // Container that flips between movie panel & booking panel
        cardPanel = new JPanel(new CardLayout());

        try {
            // Fetch initial data
            List<Movie> movies = movieService.getMovies();
            List<Booking> bookings = bookingService.getBookings();

            // Create panels
            clientMoviePanel = new ClientMoviePanel(movies, this);
            clientBookingPanel = new ClientBookingPanel(this, bookingService, bookings);

            // Add panels to cardPanel
            cardPanel.add(clientMoviePanel, MOVIES_CARD);
            cardPanel.add(clientBookingPanel, BOOKINGS_CARD);

        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame,
                    "Failed to fetch movies or bookings from the server.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        mainFrame.add(cardPanel);
        mainFrame.setVisible(true);
    }

    @Override
    public void updateMovies(List<Movie> movies) throws RemoteException {
        if (clientMoviePanel != null) {
            SwingUtilities.invokeLater(() -> clientMoviePanel.updateMovieList(movies));
        }
    }

    @Override
    public void updateBookings(List<Booking> bookings) throws RemoteException {
        // Optionally, you can also update the booking panel here if desired.
        if (clientBookingPanel != null) {
            SwingUtilities.invokeLater(() -> clientBookingPanel.updateBookingList(bookings));
        }
    }

    public static void updateMovieList(List<Movie> movies) {
        if (instance != null && instance.clientMoviePanel != null) {
            SwingUtilities.invokeLater(() -> instance.clientMoviePanel.updateMovieList(movies));
        }
    }

    public static void updateBookingList(List<Booking> bookings) {
        if (instance != null && instance.clientBookingPanel != null) {
            SwingUtilities.invokeLater(() -> instance.clientBookingPanel.updateBookingList(bookings));
        }
    }

    public void showMoviesPanel() {
        SwingUtilities.invokeLater(() -> {
            CardLayout cl = (CardLayout) cardPanel.getLayout();
            cl.show(cardPanel, MOVIES_CARD);
        });
    }

    public void showClientBookingPanel() {
        SwingUtilities.invokeLater(() -> {
            CardLayout cl = (CardLayout) cardPanel.getLayout();
            cl.show(cardPanel, BOOKINGS_CARD);
        });
    }
}