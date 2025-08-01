package lumiere.view;

import lumiere.controller.ReceiptController;
import lumiere.model.Booking;
import lumiere.server.BookingService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientBookingPanel extends JPanel {
    private ClientMain clientMain;
    private BookingService bookingService;
    private List<Booking> allBookings;       // current list (may be filtered)
    private List<Booking> originalBookings;  // complete list of bookings
    private JPanel cardsContainer;           // container panel holding each booking "card"
    private JScrollPane scrollPane;
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private ReceiptController receiptController;

    public ClientBookingPanel(ClientMain clientMain, BookingService bookingService, List<Booking> bookings)
            throws RemoteException {
        this.clientMain = clientMain;
        this.bookingService = bookingService;
        this.allBookings = (bookings != null) ? bookings : new ArrayList<>();
        this.originalBookings = new ArrayList<>(this.allBookings);
        this.receiptController = new ReceiptController(bookingService);

        setLayout(new BorderLayout());
        setupNavigationBar();
        setupCardsContainer();

        // Populate with initial bookings
        updateBookingList(this.allBookings);
    }

    private void setupNavigationBar() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(Color.decode("#1a1a1a"));
        navPanel.setPreferredSize(new Dimension(800, 90));

        // Load the logo
        ImageIcon logoIcon = loadLogoImage("res/Logo/logo.png");
        JLabel logoLabel = new JLabel(logoIcon);

        // Title
        JLabel titleLabel = new JLabel("Lumiere");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Buttons on the right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JButton moviesButton = new JButton("Movies");
        JButton bookingsButton = new JButton("Bookings");
        JButton logoutButton = new JButton("Logout");

        moviesButton.setBackground(Color.decode("#1a1a1a"));
        bookingsButton.setBackground(Color.decode("#1a1a1a"));
        logoutButton.setBackground(Color.decode("#1a1a1a"));

        moviesButton.setForeground(Color.WHITE);
        bookingsButton.setForeground(Color.WHITE);
        logoutButton.setForeground(Color.WHITE);

        Dimension buttonSize = new Dimension(120, 40);
        moviesButton.setPreferredSize(buttonSize);
        bookingsButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(buttonSize);

        buttonPanel.add(moviesButton);
        buttonPanel.add(bookingsButton);
        buttonPanel.add(logoutButton);

        // Switch to Movies panel
        moviesButton.addActionListener(e -> clientMain.showMoviesPanel());

        // Stay on Bookings panel
        bookingsButton.addActionListener(e -> clientMain.showClientBookingPanel());

        // Logout action
        logoutButton.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topFrame != null) {
                topFrame.dispose();
            }
            new Login().setVisible(true);
        });

        // Logo + Title on the left
        JPanel logoTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoTitlePanel.setOpaque(false);
        logoTitlePanel.add(logoLabel);
        logoTitlePanel.add(Box.createHorizontalStrut(10));
        logoTitlePanel.add(titleLabel);

        navPanel.add(logoTitlePanel, BorderLayout.WEST);
        navPanel.add(buttonPanel, BorderLayout.EAST);

        // --- Search Panel below nav bar ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));

        searchField = new JTextField(30);
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add action listeners for search
        searchButton.addActionListener(e -> performSearch());
        clearButton.addActionListener(e -> clearSearch());

        // Container to place search panel below nav
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.add(searchPanel, BorderLayout.WEST);

        // Combine navPanel + searchContainer in top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(navPanel);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(searchContainer);

        add(topPanel, BorderLayout.NORTH);
    }

    private ImageIcon loadLogoImage(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("Logo not found: " + path);
            return new ImageIcon();
        }
        ImageIcon originalIcon = new ImageIcon(path);
        Image scaledImage = originalIcon.getImage().getScaledInstance(50, 80, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private void setupCardsContainer() {
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(
                cardsContainer,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Create a "card" panel for each booking.
     */
    private JPanel createBookingCard(Booking booking) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBorder(new LineBorder(Color.YELLOW, 1));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        // Left side: info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel dateLabel = new JLabel("Date: " + booking.getShowDate());
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dateLabel.setForeground(Color.BLACK);

        JLabel movieLabel = new JLabel("Movie: " + booking.getMovieTitle());
        movieLabel.setFont(new Font("Arial", Font.BOLD, 16));
        movieLabel.setForeground(Color.BLACK);

        JLabel idLabel = new JLabel("Booking ID: " + booking.getBookingId());
        idLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        idLabel.setForeground(Color.BLACK);

        String seats = (booking.getSeatNumbers() != null)
                ? String.join(", ", booking.getSeatNumbers())
                : "-";
        JLabel seatsLabel = new JLabel("Seats: " + seats);
        seatsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        seatsLabel.setForeground(Color.BLACK);

        JLabel ticketsLabel = new JLabel("Tickets: " + booking.getNumberOfTickets());
        ticketsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        ticketsLabel.setForeground(Color.BLACK);

        JLabel totalLabel = new JLabel("Total: ₱" + booking.getTotalPrice());
        totalLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalLabel.setForeground(Color.BLACK);

        infoPanel.add(dateLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(movieLabel);
        infoPanel.add(idLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(seatsLabel);
        infoPanel.add(ticketsLabel);
        infoPanel.add(totalLabel);

        // Right side: Buttons stacked vertically
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 20));

        // "View Receipt" button
        JButton receiptButton = new JButton("View Receipt");
        receiptButton.setBackground(Color.decode("#1a1a1a"));
        receiptButton.setForeground(Color.WHITE);
        receiptButton.setPreferredSize(new Dimension(120, 30));
        receiptButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Show the receipt image in a dialog
        receiptButton.addActionListener(e -> showReceiptDialog(booking));

        // "Cancel Booking" button
        JButton cancelButton = new JButton("Cancel Booking");
        cancelButton.setBackground(Color.YELLOW);
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setPreferredSize(new Dimension(170, 30));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        cancelButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to cancel this booking?",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = bookingService.deleteBooking(booking.getBookingId());
                    if (!success) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Failed to cancel booking. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                    // The server's notifyClients() will update the UI (both client & admin)
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to cancel booking. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // Stack the buttons with a bit of space
        buttonPanel.add(receiptButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(cancelButton);

        cardPanel.add(infoPanel, BorderLayout.CENTER);
        cardPanel.add(buttonPanel, BorderLayout.EAST);

        return cardPanel;
    }

    private void showReceiptDialog(Booking booking) {
        BufferedImage receiptImage = receiptController.autoSaveReceipt(booking);
        showReceiptInDialog(receiptImage, booking.getBookingId());
    }

    private void showReceiptInDialog(BufferedImage receiptImage, String bookingId) {
        // Convert the BufferedImage to an ImageIcon
        ImageIcon icon = new ImageIcon(receiptImage);

        JLabel imageLabel = new JLabel(icon);
        JScrollPane imageScroll = new JScrollPane(imageLabel);

        // "Download Image" button
        JButton downloadButton = new JButton("Download Image");
        downloadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Receipt as PNG");
            fileChooser.setSelectedFile(new File("receipt_" + bookingId + ".png"));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    ImageIO.write(receiptImage, "png", fileToSave);
                    JOptionPane.showMessageDialog(
                            this,
                            "Receipt saved successfully:\n" + fileToSave.getAbsolutePath(),
                            "Saved",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to save receipt image.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // Panel layout for the dialog
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(imageScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(downloadButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Show in a modal dialog
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Receipt for Booking ID: " + bookingId,
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.getContentPane().add(mainPanel);
        dialog.setSize(400, 500); // or dialog.pack()
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Search by movie title, booking ID, or date
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            updateBookingList(originalBookings);
            return;
        }
        List<Booking> filteredBookings = originalBookings.stream()
                .filter(b ->
                        (b.getMovieTitle() != null && b.getMovieTitle().toLowerCase().contains(query)) ||
                                (b.getBookingId() != null && b.getBookingId().toLowerCase().contains(query)) ||
                                (b.getShowDate() != null && b.getShowDate().toLowerCase().contains(query))
                )
                .collect(Collectors.toList());
        updateBookingList(filteredBookings);
    }

    private void clearSearch() {
        searchField.setText("");
        updateBookingList(originalBookings);
    }

    public void updateBookingList(List<Booking> bookings) {
        SwingUtilities.invokeLater(() -> {
            // Filter bookings to only those matching the logged-in user
            String currentUser = clientMain.getLoggedInUsername();
            List<Booking> userBookings = bookings.stream()
                    .filter(b -> b.getUsername().equalsIgnoreCase(currentUser))
                    .collect(Collectors.toList());
            allBookings = userBookings;
            cardsContainer.removeAll();

            for (Booking b : allBookings) {
                cardsContainer.add(createBookingCard(b));
                cardsContainer.add(Box.createVerticalStrut(10)); // space between cards
            }
            cardsContainer.revalidate();
            cardsContainer.repaint();
        });
    }
}