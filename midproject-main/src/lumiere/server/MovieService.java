package lumiere.server;

import lumiere.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MovieService extends Remote {
    List<Movie> getMovies() throws RemoteException;
    boolean addMovie(Movie movie) throws RemoteException;
    boolean deleteMovie(String title) throws RemoteException;
    boolean editMovie(Movie edit) throws RemoteException;
    void registerClient(ClientCallback client) throws RemoteException;
}
