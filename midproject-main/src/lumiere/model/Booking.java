package lumiere.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.List;

public class Booking implements Serializable {
    private String bookingId;
    private String movieTitle;
    private String showDate;
    private String showTime;
    private int numberOfTickets;
    private double totalPrice;
    private String username;
    private String name;
    private String cardNumber;
    private List<String> seatNumbers;

    public Booking(String username, String name, String movieTitle, String showDate, String showTime,
                   int numberOfTickets, double totalPrice, String cardNumber, List<String> seatNumbers) {
        this.bookingId = "B" + System.currentTimeMillis(); // Auto-generate ID
        this.movieTitle = movieTitle;
        this.showDate = showDate;
        this.showTime = showTime;
        this.numberOfTickets = numberOfTickets;
        this.totalPrice = totalPrice;
        this.username = (username != null) ? username : "Unknown"; // ✅ Fix: Prevents null username
        this.name = name;
        this.cardNumber = cardNumber;
        this.seatNumbers = seatNumbers;
    }


    // Getters
    public String getShowDate() { return showDate; }
    public String getBookingId() { return bookingId; }
    public String getMovieTitle() { return movieTitle; }
    public String getShowTime() { return showTime; }
    public int getNumberOfTickets() { return numberOfTickets; }
    public double getTotalPrice() { return totalPrice; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getCardNumber() { return cardNumber; }
    public List<String> getSeatNumbers() { return seatNumbers; }

    // Convert to JSON for saving to bookings.json
    public JsonObject toJSON() {
        Gson gson = new Gson();
        return gson.toJsonTree(this).getAsJsonObject();
    }
}
