package lumiere.controller;

import lumiere.model.Booking;
import lumiere.server.BookingService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

public class ReceiptController {

    private final BookingService bookingService;
    public ReceiptController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Fetch all bookings
    public List<Booking> getAllBookings() {
        try {
            return bookingService.getBookings();
        } catch (RemoteException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // Generate the receipt text
    public String generateReceipt(Booking booking) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("======= Lumiere Cinema Receipt =======\n")
                .append("Booking ID: ").append(booking.getBookingId()).append("\n")
                .append("Name: ").append(booking.getName()).append("\n")
                .append("Movie: ").append(booking.getMovieTitle()).append("\n")
                .append("Date: ").append(booking.getShowDate()).append("\n")
                .append("Time: ").append(booking.getShowTime()).append("\n")
                .append("Seats: ").append(String.join(", ", booking.getSeatNumbers())).append("\n")
                .append("Tickets: ").append(booking.getNumberOfTickets()).append("\n")
                .append("Total Price: ₱").append(booking.getTotalPrice()).append("\n")
                .append("====================================");
        return receipt.toString();
    }

    // Create an image from the receipt text
    public BufferedImage createReceiptImage(String receiptText) {
        // Choose font and calculate image dimensions
        Font font = new Font("Monospaced", Font.PLAIN, 12);
        FontMetrics metrics = new Canvas().getFontMetrics(font);
        String[] lines = receiptText.split("\n");
        int lineHeight = metrics.getHeight();
        int maxWidth = 0;
        for (String line : lines) {
            int width = metrics.stringWidth(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        int padding = 20;
        int imageWidth = maxWidth + padding * 2;
        int imageHeight = lineHeight * lines.length + padding * 2;

        // Create the image and draw the text
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);  // Background color
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        g2d.setColor(Color.BLACK);  // Text color

        int y = padding + metrics.getAscent();
        for (String line : lines) {
            g2d.drawString(line, padding, y);
            y += lineHeight;
        }
        g2d.dispose();
        return image;
    }

    // Opens the receipt image and saves it in the res/receipts folder automatically
    public void downloadReceipt(String bookingId) {
        List<Booking> bookings = getAllBookings();
        Booking selectedBooking = bookings.stream()
                .filter(booking -> booking.getBookingId().equals(bookingId))
                .findFirst()
                .orElse(null);

        if (selectedBooking != null) {
            String receiptText = generateReceipt(selectedBooking);
            BufferedImage receiptImage = createReceiptImage(receiptText);

            // Define the target directory and ensure it exists
            File receiptsDir = new File("res/GeneratedReceipts");
            if (!receiptsDir.exists()) {
                receiptsDir.mkdirs();
            }

            // Create the file within the specified directory
            File fileToSave = new File(receiptsDir, "receipt_" + bookingId + ".png");

            try {
                ImageIO.write(receiptImage, "png", fileToSave);
                JOptionPane.showMessageDialog(null, "Receipt saved successfully:\n" + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to save receipt image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Booking not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public BufferedImage autoSaveReceipt(Booking booking) {
        String receiptText = generateReceipt(booking);
        BufferedImage receiptImage = createReceiptImage(receiptText);
        File receiptsDir = new File("res/GeneratedReceipts");
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs();
        }
        File fileToSave = new File(receiptsDir, "receipt_" + booking.getBookingId() + ".png");

        try {
            ImageIO.write(receiptImage, "png", fileToSave);
            System.out.println("Receipt auto-saved to: " + fileToSave.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receiptImage;
    }
}