package lumiere.server;

import com.google.gson.GsonBuilder;
import lumiere.model.Movie;
import lumiere.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JsonHandler {
    private static final String MOVIES_FILE = "res/movies.json";
    private static final String USERS_FILE = "res/users.json";

    public static List<Movie> loadMovies() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(MOVIES_FILE)));
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            List<Movie> movies = new Gson().fromJson(jsonObject.get("movies"), new TypeToken<List<Movie>>() {}.getType());

            // Debugging output
            System.out.println("Loaded movies: " + movies.size());
            for (Movie movie : movies) {
                System.out.println("Title: " + movie.getTitle());
            }

            return movies;
        } catch (Exception e) {
            e.printStackTrace();
            return new CopyOnWriteArrayList<>();
        }
    }

    public static void saveMovies(List<Movie> movies) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("movies", gson.toJsonTree(movies));
            String prettyJson = gson.toJson(jsonObject);
            Files.write(Paths.get(MOVIES_FILE), prettyJson.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<User> loadAdmins() {
        return loadUsers("admins");
    }

    public static List<User> loadCustomers() {
        return loadUsers("customers");
    }

    private static List<User> loadUsers(String key) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            return new Gson().fromJson(jsonObject.get(key), new TypeToken<List<User>>() {}.getType());
        } catch (Exception e) {
            return new CopyOnWriteArrayList<>();
        }
    }

    public static void saveCustomers(List<User> customers) {
        try {
            JsonObject jsonObject;
            if (Files.exists(Paths.get(USERS_FILE))) {
                String json = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
                jsonObject = JsonParser.parseString(json).getAsJsonObject();
            } else {
                jsonObject = new JsonObject();
                jsonObject.add("admins", new Gson().toJsonTree(new CopyOnWriteArrayList<User>()));
            }
            jsonObject.add("customers", new Gson().toJsonTree(customers));

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(jsonObject);
            Files.write(Paths.get(USERS_FILE), prettyJson.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
