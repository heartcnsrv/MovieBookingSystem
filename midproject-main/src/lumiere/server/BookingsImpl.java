package lumiere.server;

import lumiere.model.Booking;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class BookingsImpl extends UnicastRemoteObject implements BookingService {
    private static final String BOOKINGS_FILE = "res/bookings.json";
    private List<Booking> bookings;
    private List<ClientCallback> clients = new CopyOnWriteArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public BookingsImpl() throws RemoteException {
        super();
        this.bookings = loadBookings();
    }

    private synchronized List<Booking> loadBookings() {
        try {
            if (!Files.exists(Paths.get(BOOKINGS_FILE))) {
                return new CopyOnWriteArrayList<>();
            }
            String json = new String(Files.readAllBytes(Paths.get(BOOKINGS_FILE)));
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            return new Gson().fromJson(jsonObject.get("bookings"), new TypeToken<List<Booking>>() {}.getType());
        } catch (Exception e) {
            return new CopyOnWriteArrayList<>();
        }
    }

    private synchronized void saveBookings() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("bookings", gson.toJsonTree(bookings));
            String prettyJson = gson.toJson(jsonObject);
            Files.write(Paths.get(BOOKINGS_FILE), prettyJson.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addBooking(Booking booking) throws RemoteException {
        lock.writeLock().lock();
        try {
            // PREVENT DUPLICATE SEATS ON SAME MOVIE AND DATE
            for (Booking existingBooking : bookings) {
                if (existingBooking.getMovieTitle().equals(booking.getMovieTitle()) &&
                        existingBooking.getShowDate().equals(booking.getShowDate())) {
                    for (String seat : booking.getSeatNumbers()) {
                        if (existingBooking.getSeatNumbers().contains(seat)) {
                            throw new RemoteException("Seats already booked. Please try again.");
                        }
                    }
                }
            }
            bookings.add(booking);
            saveBookings();
            notifyClients();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public synchronized List<Booking> getBookings() throws RemoteException {
        return bookings;
    }

    @Override
    public synchronized boolean deleteBooking(String bookingId) throws RemoteException {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equals(bookingId)) {
                bookings.remove(booking);
                saveBookings();
                notifyClients();
                return true;
            }
        }
        return false; // Booking not found
    }

    @Override
    public void registerClient(ClientCallback client) throws RemoteException {
        clients.add(client);
    }

    private void notifyClients() throws RemoteException {
        for (ClientCallback client : clients) {
            client.updateBookings(bookings);
        }
    }

    @Override
    public synchronized List<Booking> getUserBookings(String username) throws RemoteException {
        return bookings.stream()
                .filter(b -> b.getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }
}