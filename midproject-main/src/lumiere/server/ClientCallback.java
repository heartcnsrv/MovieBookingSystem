package lumiere.server;

import lumiere.model.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientCallback extends Remote {
    void updateMovies(List<Movie> updatedMovies) throws RemoteException;
    void updateBookings(List<Booking> bookings) throws RemoteException;
}