package lumiere.view;

import lumiere.model.Movie;
import lumiere.server.BookingService;
import lumiere.controller.BookingController;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;
import java.util.List;

public class ClientMovieDetails extends JFrame {
    public ClientMovieDetails(Movie movie) {
        setTitle(movie.getTitle());
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.decode("#f5f5f5"));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.decode("#f5f5f5"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Movie Poster
        ImageIcon posterIcon = new ImageIcon(movie.getPoster());
        Image scaledImage = posterIcon.getImage().getScaledInstance(250, 350, Image.SCALE_SMOOTH);
        JLabel posterLabel = new JLabel(new ImageIcon(scaledImage));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(posterLabel, gbc);

        // Movie Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.decode("#f5f5f5"));

        JLabel titleLabel = new JLabel("<html><b>" + movie.getTitle() + " (" + movie.getYear() + ")</b></html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel directorLabel = new JLabel("Director: " + movie.getDirector());
        directorLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel priceLabel = new JLabel("<html><b>" + movie.getPrice() + "</b></html>");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel timeLabel = new JLabel("<html><b>" + String.join(", ", movie.getTimeSlot()) + "</b></html>");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel synopsisLabel = new JLabel("<html><b>Synopsis:</b> " + movie.getSynopsis() + "</html>");
        synopsisLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        infoPanel.add(titleLabel);
        infoPanel.add(directorLabel);
        infoPanel.add(priceLabel);
        infoPanel.add(timeLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(synopsisLabel);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        contentPanel.add(infoPanel, gbc);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBackground(Color.decode("#f5f5f5"));

        JLabel dateLabel = new JLabel("Choose a Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));

        List<String> availableDates = BookingController.getAvailableDates(movie);
        JComboBox<String> dateComboBox = new JComboBox<>(availableDates.toArray(new String[0]));
        dateComboBox.setPreferredSize(new Dimension(150, 30));

        datePanel.add(dateLabel);
        datePanel.add(dateComboBox);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(datePanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.decode("#f5f5f5"));

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(Color.BLACK);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(150, 50));
        backButton.addActionListener(e -> dispose());

        JButton buyTicketButton = new JButton("Buy Tickets");
        buyTicketButton.setFont(new Font("Arial", Font.BOLD, 16));
        buyTicketButton.setBackground(Color.decode("#f4d03f"));
        buyTicketButton.setForeground(Color.BLACK);
        buyTicketButton.setFocusPainted(false);
        buyTicketButton.setPreferredSize(new Dimension(200, 50));

        buttonPanel.add(backButton);
        buttonPanel.add(buyTicketButton);

        buyTicketButton.addActionListener(e -> {
            try {
                BookingService bookingService = (BookingService) Naming.lookup("rmi://localhost:1099/BookingService");

                String selectedDate = (String) dateComboBox.getSelectedItem();

                // ✅ Fetch the logged-in username
                String loggedInUsername = Login.getLoggedInUsername();

                dispose(); // Close current window
                new ClientBookingInfo(bookingService, movie, selectedDate, (JFrame) this, loggedInUsername); // Pass username

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Booking Service Unavailable", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}
