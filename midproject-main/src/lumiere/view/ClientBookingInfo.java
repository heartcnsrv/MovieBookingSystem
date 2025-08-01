package lumiere.view;

import lumiere.model.Booking;
import lumiere.model.Movie;
import lumiere.server.BookingService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ClientBookingInfo extends JFrame {
    private JTextField nameField, cardNumberField;
    private String username;
    private JLabel totalAmountLabel, selectedDateLabel;
    private int ticketCount = 1;
    private double ticketPrice;
    private BookingService bookingService;
    private Movie movie;
    private List<String> selectedSeats = new ArrayList<>();
    private JLabel selectedSeatsLabel;
    private JFrame parentFrame;
    private String selectedDate;
    String loggedInUsername = Login.getLoggedInUsername();

    public ClientBookingInfo(BookingService bookingService, Movie movie, String selectedDate, JFrame parentFrame, String username) {
        this.username = username; // ✅ Store username correctly
        System.out.println("User " + username + " is booking " + movie.getTitle());
        this.bookingService = bookingService;
        this.movie = movie;
        this.selectedDate = selectedDate;
        this.ticketPrice = Double.parseDouble(movie.getPrice().replace("₱", ""));
        this.parentFrame = parentFrame;

        setTitle("Book Tickets - " + movie.getTitle());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        // Movie Poster
        JLabel posterLabel = new JLabel(new ImageIcon(new ImageIcon(movie.getPoster()).getImage().getScaledInstance(100, 150, Image.SCALE_SMOOTH)));
        posterLabel.setBounds(40, 30, 100, 150);
        add(posterLabel);

        // Movie Info
        JLabel movieInfo = new JLabel("<html><div style='width:300px;'><b>" + movie.getTitle() +
                " (" + movie.getYear() + ")</b><br>" + movie.getDirector() + "<br><b>" + movie.getPrice() +
                "</b><br>" + movie.getTimeSlot().get(0) + "<br>" + movie.getCinema() + "</div></html>");
        movieInfo.setFont(new Font("Arial", Font.BOLD, 16));
        movieInfo.setBounds(160, 30, 300, 100);
        add(movieInfo);

        // Display Selected Date
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateLabel.setBounds(160, 140, 100, 25);
        add(dateLabel);

        selectedDateLabel = new JLabel(selectedDate);
        selectedDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        selectedDateLabel.setBounds(210, 140, 150, 25);
        add(selectedDateLabel);

        // Input Fields
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setBounds(40, 200, 100, 25);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(40, 225, 250, 30);
        add(nameField);

        JLabel cardLabel = new JLabel("Card Number");
        cardLabel.setBounds(40, 265, 100, 25);
        add(cardLabel);

        cardNumberField = new JTextField();
        cardNumberField.setBounds(40, 290, 250, 30);
        add(cardNumberField);

        // Number of Tickets
        JLabel ticketLabel = new JLabel("Number of Ticket/s:");
        ticketLabel.setBounds(40, 330, 150, 25);
        add(ticketLabel);

        JButton minusButton = new JButton("-");
        minusButton.setBounds(40, 360, 50, 30);
        minusButton.setBackground(Color.YELLOW);
        add(minusButton);

        JLabel ticketCountLabel = new JLabel("1", SwingConstants.CENTER);
        ticketCountLabel.setBounds(85, 360, 50, 30);
        ticketCountLabel.setOpaque(true);
        ticketCountLabel.setBackground(Color.white);
        ticketCountLabel.setForeground(Color.black);
        add(ticketCountLabel);

        JButton plusButton = new JButton("+");
        plusButton.setBounds(130, 360, 50, 30);
        plusButton.setBackground(Color.YELLOW);
        add(plusButton);

        plusButton.addActionListener(e -> {
            if (ticketCount < 12) {  // ✅ Prevent ticket count from exceeding 12
                ticketCount++;
                ticketCountLabel.setText(String.valueOf(ticketCount));
                updateTotalAmount();
            } else {
                JOptionPane.showMessageDialog(this, "You can only book a maximum of 12 tickets.", "Limit Reached", JOptionPane.WARNING_MESSAGE);
            }
        });

        minusButton.addActionListener(e -> {
            if (ticketCount > 1) {
                ticketCount--;
                ticketCountLabel.setText(String.valueOf(ticketCount));
                updateTotalAmount();
            }
        });

        // Total Amount
        JLabel totalLabel = new JLabel("Total Amount:");
        totalLabel.setBounds(40, 400, 100, 25);
        add(totalLabel);

        totalAmountLabel = new JLabel("₱" + ticketPrice);
        totalAmountLabel.setBounds(150, 400, 100, 25);
        add(totalAmountLabel);

        // Seat Selection
        JLabel selectSeatLabel = new JLabel("Select Seat/s:");
        selectSeatLabel.setFont(new Font("Arial", Font.BOLD, 16));
        selectSeatLabel.setBounds(450, 180, 150, 25);
        add(selectSeatLabel);

        JPanel screenPanel = new JPanel();
        screenPanel.setBounds(450, 210, 250, 30);
        screenPanel.setBackground(Color.BLACK);
        JLabel screenLabel = new JLabel("Screen");
        screenLabel.setForeground(Color.YELLOW);
        screenPanel.add(screenLabel);
        add(screenPanel);

        JPanel seatPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        seatPanel.setBounds(450, 250, 250, 120);
        String[] seats = {"A1", "A2", "A3", "A4", "B1", "B2", "B3", "B4", "C1", "C2", "C3", "C4"};

        for (String seat : seats) {
            JButton seatButton = new JButton(seat);
            seatButton.setBackground(Color.BLACK);
            seatButton.setForeground(Color.YELLOW);
            seatButton.addActionListener(e -> toggleSeatSelection(seatButton, seat));
            seatPanel.add(seatButton);
        }
        add(seatPanel);

        selectedSeatsLabel = new JLabel("Selected: ");
        selectedSeatsLabel.setBounds(450, 380, 200, 25);
        add(selectedSeatsLabel);

        // Buttons
        JButton backButton = new JButton("Back");
        backButton.setBounds(600, 450, 100, 40);
        backButton.setBackground(Color.BLACK);
        backButton.setForeground(Color.WHITE);
        add(backButton);
        backButton.addActionListener(e -> dispose());

        JButton buyButton = new JButton("Buy Tickets");
        buyButton.setBounds(710, 450, 140, 40);
        buyButton.setBackground(Color.YELLOW);
        add(buyButton);

        buyButton.addActionListener(e -> processBooking());
        setVisible(true);
    }

    private void updateTotalAmount() {
        totalAmountLabel.setText("₱" + (ticketPrice * ticketCount));
    }

    private void toggleSeatSelection(JButton button, String seat) {
        if (selectedSeats.contains(seat)) {
            selectedSeats.remove(seat);
            button.setBackground(Color.BLACK);
            button.setForeground(Color.YELLOW);
        } else if (selectedSeats.size() < ticketCount) {
            selectedSeats.add(seat);
            button.setBackground(Color.YELLOW);
            button.setForeground(Color.BLACK);
        }
        selectedSeatsLabel.setText("Selected: " + String.join(", ", selectedSeats));
    }

    private void processBooking() {
        String username = this.username;
        String fullName = nameField.getText().trim();
        String cardNumber = cardNumberField.getText().trim();

        if (fullName.isEmpty() || cardNumber.isEmpty() || selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields and select seats.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // CARD NUMBER CHECKER
        if (!cardNumber.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(this, "Invalid card number! It should be exactly 16 digits.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // NUM OF TICKETS CHECKER
        if (ticketCount > 12) {
            JOptionPane.showMessageDialog(this, "You can only book up to 12 tickets per transaction.", "Limit Reached", JOptionPane.ERROR_MESSAGE);
            return;
        }


        Booking newBooking = new Booking(
                username,
                fullName,
                movie.getTitle(),
                selectedDate,
                movie.getTimeSlot().get(0),
                ticketCount,
                ticketPrice * ticketCount,
                cardNumber,
                selectedSeats
        );

        try {
            boolean success = bookingService.addBooking(newBooking);
            if (success) {
                JOptionPane.showMessageDialog(this, "Booking successful! Thank you!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (RemoteException e) {
            if (e.getMessage().contains("Seats already booked")) {
                JOptionPane.showMessageDialog(this, "Seats already booked. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Server connection error.", "Error", JOptionPane.ERROR_MESSAGE);

            }
        }
    }

}
