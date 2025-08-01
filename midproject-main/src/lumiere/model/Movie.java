package lumiere.model;

import java.io.Serializable;
import java.util.List;

public class Movie implements Serializable {
    private String title, year, duration, synopsis, director, poster, price, cinema;
    private List<String> genre, timeSlot;

    public Movie(String title, String year, String duration, String synopsis, String director, List<String> genre, String poster, String price, String cinema, List<String> timeSlot) {
        this.title = title;
        this.year = year;
        this.duration = duration;
        this.synopsis = synopsis;
        this.director = director;
        this.genre = genre;
        this.poster = poster;
        this.price = price;
        this.cinema = cinema;
        this.timeSlot = timeSlot;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public List<String> getGenre() { return genre; }
    public void setGenre(List<String> genre) { this.genre = genre; }
    public String getCinema() { return cinema; }
    public void setCinema(String cinema) { this.cinema = cinema; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public List<String> getTimeSlot() { return timeSlot; }
    public void setTimeSlot(List<String> timeSlot) { this.timeSlot = timeSlot; }
}
