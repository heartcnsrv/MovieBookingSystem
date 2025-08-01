package lumiere.server;

import lumiere.model.Booking;
import lumiere.model.Movie;
import lumiere.view.ClientBookingPanel;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {
    private transient ClientBookingPanel bookingPanel;

    public ClientCallbackImpl(ClientBookingPanel bookingPanel) throws RemoteException {
        super();
        this.bookingPanel = bookingPanel;
    }

    @Override
    public void updateBookings(List<Booking> bookings) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (bookingPanel != null) {
                bookingPanel.updateBookingList(bookings);
            }
        });
    }

    @Override
    public void updateMovies(List<Movie> movies) throws RemoteException {
        // Not needed for the booking panel
    }
}