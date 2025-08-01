package lumiere.view;

import lumiere.controller.ReceiptController;
import lumiere.model.Booking;
import lumiere.server.BookingService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

public class AdminBookingPanel extends JPanel {
    private JTable bookingTable;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private JButton searchButton, clearSearchButton, receiptButton;
    private static DefaultTableModel tableModel;
    private BookingService bookingService;
    private ReceiptController receiptController;

    public AdminBookingPanel(BookingService bookingService) {
        this.bookingService = bookingService;
        this.receiptController = new ReceiptController(bookingService);

        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));

        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        clearSearchButton = new JButton("Clear Search");
        receiptButton = new JButton("See Receipt");

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);
        searchPanel.add(receiptButton);  // Add receipt button to panel

        String[] columnNames = {
                "Booking ID", "Username", "Client Name",
                "Movie Title", "Show Date", "Show Time",
                "Tickets", "Seats", "Delete"
        };
        tableModel = new DefaultTableModel(columnNames, 0);
        bookingTable = new JTable(tableModel);

        // Set Delete column to use buttons
        bookingTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        bookingTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox()));

        scrollPane = new JScrollPane(bookingTable);
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadBookings();

        searchButton.addActionListener(e -> searchBookings());
        clearSearchButton.addActionListener(e -> loadBookings());
        receiptButton.addActionListener(e -> showReceipt());
    }

    private void loadBookings() {
        try {
            List<Booking> bookings = bookingService.getBookings();
            updateBookingList(bookings);
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load bookings.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void updateBookingList(List<Booking> bookings) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0); // Clear previous rows
            for (Booking booking : bookings) {
                tableModel.addRow(new Object[]{
                        booking.getBookingId(),
                        booking.getUsername(),
                        booking.getName(),
                        booking.getMovieTitle(),
                        booking.getShowDate(),
                        booking.getShowTime(),
                        booking.getNumberOfTickets(),
                        booking.getSeatNumbers() != null
                                ? String.join(", ", booking.getSeatNumbers())
                                : "-",
                        "Delete"
                });
            }
        });
    }

    /**
     * Displays the receipt image in a dialog with a "Download" button.
     */
    private void showReceipt() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a booking first.",
                    "No Booking Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Get the booking ID
        String bookingId = (String) tableModel.getValueAt(selectedRow, 0);
        Booking booking = null;
        try {
            List<Booking> bookings = bookingService.getBookings();
            booking = bookings.stream()
                    .filter(b -> bookingId.equals(b.getBookingId()))
                    .findFirst()
                    .orElse(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (booking == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Booking not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        BufferedImage receiptImage = receiptController.autoSaveReceipt(booking);
        showReceiptInDialog(receiptImage, booking.getBookingId());
    }

    /**
     * Creates a dialog to display the receipt image, with a button to download it.
     */
    private void showReceiptInDialog(BufferedImage receiptImage, String bookingId) {
        ImageIcon icon = new ImageIcon(receiptImage);
        JLabel imageLabel = new JLabel(icon);

        JScrollPane imageScroll = new JScrollPane(imageLabel);

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
        dialog.setSize(400, 500); // Or dialog.pack()
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void searchBookings() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadBookings();
            return;
        }

        try {
            List<Booking> bookings = bookingService.getBookings();
            List<Booking> filtered = bookings.stream()
                    .filter(b -> b.getBookingId().toLowerCase().contains(query.toLowerCase()) ||
                            b.getName().toLowerCase().contains(query.toLowerCase()) ||
                            b.getMovieTitle().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            updateBookingList(filtered);
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to search bookings.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteBooking(int row) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this booking?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String bookingId = (String) tableModel.getValueAt(row, 0);
                bookingService.deleteBooking(bookingId);
                tableModel.removeRow(row);
                JOptionPane.showMessageDialog(this, "Booking deleted successfully.");
            } catch (RemoteException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to delete booking.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("Delete");
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Delete");
            button.addActionListener(e -> deleteBooking(row));
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column
        ) {
            this.row = row;
            return button;
        }
    }
}