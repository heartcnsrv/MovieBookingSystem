package lumiere.server;

import lumiere.model.Booking;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BookingService extends Remote {
    boolean addBooking(Booking booking) throws RemoteException;
    boolean deleteBooking(String bookingId) throws RemoteException; // Keep only one
    List<Booking> getBookings() throws RemoteException;
    void registerClient(ClientCallback client) throws RemoteException;
    List<Booking> getUserBookings(String username) throws RemoteException;
}