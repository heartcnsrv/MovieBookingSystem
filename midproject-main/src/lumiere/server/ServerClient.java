package lumiere.server;

import lumiere.model.Booking;
import lumiere.model.Movie;
import lumiere.view.AdminBookingPanel;
import lumiere.view.AdminMain;
import lumiere.view.ClientMain;
import lumiere.view.Login;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ServerClient extends UnicastRemoteObject implements ClientCallback {
    private static ServerClient instance;
    private final MovieService movieService;
    private static UserService userService;
    public static BookingService bookingService;
    private String serverIP;

    private ServerClient(String serverIP) throws RemoteException {
        super();
        this.serverIP = serverIP;
        try {
            movieService = (MovieService) Naming.lookup("rmi://" + serverIP + "/MovieService");
            userService = (UserService) Naming.lookup("rmi://" + serverIP + "/UserService");
            bookingService = (BookingService) Naming.lookup("rmi://" + serverIP + "/BookingService");

            movieService.registerClient(this);
            bookingService.registerClient(this);
        } catch (Exception e) {
            throw new RemoteException("Failed to connect to RMI server", e);
        }
    }

    public static ServerClient getInstance(String serverIP) throws RemoteException {
        if (instance == null) {
            instance = new ServerClient(serverIP);
        }
        return instance;
    }

    public MovieService getMovieService() {
        return movieService;
    }

    public static UserService getUserService() {
        return userService;
    }

    public BookingService getBookingService() {
        return bookingService;
    }
    public String getServerIP() {
        return serverIP;
    }

    @Override
    public void updateMovies(List<Movie> updatedMovies) throws RemoteException {
        ClientMain.updateMovieList(updatedMovies);
        AdminMain.updateMovieList(updatedMovies);
    }

    @Override
    public void updateBookings(List<Booking> updatedBookings) throws RemoteException {
        ClientMain.updateBookingList(updatedBookings);
        AdminBookingPanel.updateBookingList(updatedBookings); // uncomment when AdminBookingPanel exists
    }
}