package lumiere.server;

import lumiere.model.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class MovieImpl extends UnicastRemoteObject implements MovieService {
    private List<Movie> movies;
    private List<ClientCallback> clients;
    private JsonHandler jsonHandler;

    public MovieImpl() throws RemoteException {
        super();
        this.jsonHandler = new JsonHandler();
        this.movies = jsonHandler.loadMovies();
        this.clients = new ArrayList<>();
    }

    @Override
    public List<Movie> getMovies() throws RemoteException {
        movies = JsonHandler.loadMovies();
        return movies;
    }

    @Override
    public boolean addMovie(Movie movie) throws RemoteException {
        System.out.println("Sending admin action: ADDED_MOVIE "+ movie.getTitle());
        ServerView.logAdminAction("ADDED_MOVIE", movie.getTitle());
        if (movies.add(movie)) {
            jsonHandler.saveMovies(movies);
            notifyClients();
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteMovie(String delete) throws RemoteException {
        System.out.println("Sending admin action: DELETED_MOVIE "+ delete);
        ServerView.logAdminAction("DELETED_MOVIE", delete);
        if (movies.removeIf(m -> m.getTitle().equals(delete))) {
            jsonHandler.saveMovies(movies);
            notifyClients();
            return true;
        }
        return false;
    }

    @Override
    public boolean editMovie(Movie updatedMovie) throws RemoteException {
        System.out.println("Sending admin action: UPDATED_MOVIE "+ updatedMovie.getTitle());
        ServerView.logAdminAction("UPDATED_MOVIE", updatedMovie.getTitle());
        for (int i = 0; i < movies.size(); i++) {
            Movie m = movies.get(i);
            if (m.getTitle().equals(updatedMovie.getTitle())) {
                movies.set(i, updatedMovie);
                jsonHandler.saveMovies(movies);
                notifyClients();
                return true;
            }
        }
        return false;
    }

    @Override
    public void registerClient(ClientCallback client) throws RemoteException {
        clients.add(client);
    }

    private void notifyClients() throws RemoteException {
        for (ClientCallback client : clients) {
            client.updateMovies(movies);
        }
    }
}