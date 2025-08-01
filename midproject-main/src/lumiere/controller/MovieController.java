package lumiere.controller;

import lumiere.server.MovieService;
import lumiere.view.AdminMoviePanel;
import lumiere.model.Movie;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;

public class MovieController {
    private final MovieService service;
    private final AdminMoviePanel panel;

    public MovieController(MovieService service, AdminMoviePanel panel) {
        this.service = service;
        this.panel = panel;
        fetchMovies();
    }

    public void fetchMovies() {
        try {
            List<Movie> movies = service.getMovies();
            panel.updateMovieTable(movies);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void addMovie(Movie movie) {
        try{
            service.addMovie(movie);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void deleteMovie(String title) {
        try {
            service.deleteMovie(title);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void editMovie(Movie editMovie) {
        try {
            service.editMovie(editMovie);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
