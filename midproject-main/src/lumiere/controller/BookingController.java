package lumiere.controller;

import lumiere.model.Booking;
import lumiere.model.Movie;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static lumiere.server.ServerClient.bookingService;

public class BookingController {

    public static List<String> getAvailableDates(Movie movie) {
        List<String> availableDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");

        if (isTodayBookable(movie)) {
            availableDates.add(today.format(formatter));
        }

        // 3 DAYS FROM NOW
        for (int i = 1; i <= 3; i++) {
            availableDates.add(today.plusDays(i).format(formatter));
        }

        return availableDates;
    }

    // IF TIMESLOT IS STILL BOOKABLE
    public static boolean isTodayBookable(Movie movie) {
        LocalTime now = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        try {
            LocalTime movieTime = LocalTime.parse(movie.getTimeSlot().get(0), timeFormatter);
            return movieTime.isAfter(now);
        } catch (Exception e) {
            System.err.println("Invalid time format: " + movie.getTimeSlot().get(0));
            return false;
        }
    }

    public boolean createBooking(String username, String name, String cardNumber,
                                 String movieTitle, String showDate, String showTime,
                                 List<String> seats, int numTickets, double totalPrice) {
        try {
            Booking booking = new Booking(username, name, movieTitle, showDate, showTime,
                    numTickets, totalPrice, cardNumber, seats);
            return bookingService.addBooking(booking);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
