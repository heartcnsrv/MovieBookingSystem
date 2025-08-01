package lumiere.view;

import lumiere.model.Movie;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

public class ClientMoviePanel extends JPanel {
    private ClientMain clientMain;
    private JPanel movieGridPanel;
    private JScrollPane scrollPane;
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private List<Movie> allMovies;


    public ClientMoviePanel(List<Movie> movies, ClientMain clientMain) {
        this.clientMain = clientMain;
        this.allMovies = movies;

        setLayout(new BorderLayout());

        // Navigation panel
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(Color.decode("#1a1a1a"));
        navPanel.setPreferredSize(new Dimension(800, 90));


        ImageIcon logoIcon = loadLogoImage("res/Logo/logo.png");
        JLabel logoLabel = new JLabel(logoIcon);


        JLabel titleLabel = new JLabel("Lumiere");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JButton moviesButton = new JButton("Movies");
        JButton bookingsButton = new JButton("Bookings");
        JButton logoutButton = new JButton("Logout");

        moviesButton.setBackground(Color.decode("#1a1a1a"));
        bookingsButton.setBackground(Color.decode("#1a1a1a"));
        logoutButton.setBackground(Color.decode("#1a1a1a"));

        logoutButton.setForeground(Color.WHITE);
        moviesButton.setForeground(Color.WHITE);
        bookingsButton.setForeground(Color.WHITE);

        Dimension buttonSize = new Dimension(120, 40);
        moviesButton.setPreferredSize(buttonSize);
        bookingsButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(buttonSize);

        buttonPanel.add(moviesButton);
        buttonPanel.add(bookingsButton);
        buttonPanel.add(logoutButton);

        moviesButton.addActionListener(e -> clientMain.showMoviesPanel());
        bookingsButton.addActionListener(e -> clientMain.showClientBookingPanel());

        logoutButton.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topFrame != null) {
                topFrame.dispose();
            }
            new Login().setVisible(true);
        });

        JPanel logoTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoTitlePanel.setOpaque(false);
        logoTitlePanel.add(logoLabel);
        logoTitlePanel.add(titleLabel);
        navPanel.add(logoTitlePanel, BorderLayout.WEST);
        navPanel.add(buttonPanel, BorderLayout.EAST);

        add(navPanel, BorderLayout.NORTH);

        // Search Panel (Lowered Below Navigation Bar)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12)); // Bold label

        searchField = new JTextField(30);
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");

        // Add components in order
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        // Ensure spacing & margin for proper positioning
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Move search bar lower

        // Create a separate panel to place BELOW navigation
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.add(searchPanel, BorderLayout.WEST); // Align left

        add(searchContainer, BorderLayout.AFTER_LINE_ENDS);

        // Add search functionality
        searchButton.addActionListener(e -> performSearch());
        clearButton.addActionListener(e -> clearSearch());

        // Add search panel to the top navigation bar
        add(navPanel, BorderLayout.NORTH);
        add(searchContainer, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Stacks components vertically
        topPanel.add(navPanel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(searchContainer);
        add(topPanel, BorderLayout.NORTH);

        // Movie Grid Panel
        movieGridPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        movieGridPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        updateMovieList(movies); // Populate the initial movie list

        // Scroll
        updateMovieList(movies);

        scrollPane = new JScrollPane(movieGridPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Optimized Search using Streams
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            updateMovieList(allMovies); // Show all movies if search is empty
            return;
        }

        List<Movie> filteredMovies = allMovies.stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(query) ||
                        (movie.getGenre() != null && String.join(", ", movie.getGenre()).toLowerCase().contains(query)))
                .collect(Collectors.toList());

        updateMovieList(filteredMovies);
    }

    // Clear search and show all movies
    private void clearSearch() {
        searchField.setText("");
        updateMovieList(allMovies);
    }

    // Update movie list
    public void updateMovieList(List<Movie> movies) {
        movieGridPanel.removeAll();
        for (Movie movie : movies) {
            movieGridPanel.add(createMoviePanel(movie));
        }
        movieGridPanel.revalidate();
        movieGridPanel.repaint();
    }

    private JPanel createMoviePanel(Movie movie) {
        JPanel moviePanel = new JPanel(new BorderLayout());
        moviePanel.setPreferredSize(new Dimension(160, 350));
        moviePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel moviePoster = new JLabel(loadPosterImage(movie.getPoster()));
        moviePanel.add(moviePoster, BorderLayout.CENTER);

        JLabel movieTitle = new JLabel(movie.getTitle(), SwingConstants.CENTER);
        movieTitle.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel moviePrice = new JLabel(movie.getPrice(), SwingConstants.CENTER);
        moviePrice.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton selectButton = new JButton("Select");
        selectButton.setBackground(Color.decode("#1a1a1a"));
        selectButton.setForeground(Color.WHITE);

        selectButton.addActionListener(e -> new ClientMovieDetails(movie));

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.add(movieTitle);
        infoPanel.add(moviePrice);
        infoPanel.add(selectButton);

        moviePanel.add(infoPanel, BorderLayout.SOUTH);
        return moviePanel;
    }

    // FOR LOGO
    private ImageIcon loadLogoImage(String path) {
        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            System.err.println("Logo not found: " + path);
            return new ImageIcon(); // Return empty icon if not found
        }

        ImageIcon originalIcon = new ImageIcon(path);
        Image scaledImage = originalIcon.getImage().getScaledInstance(50, 80, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    // FOR POSTERS
    private ImageIcon loadPosterImage(String posterPath) {
        if (posterPath == null || posterPath.isEmpty()) {
            return new ImageIcon(new BufferedImage(180, 220, BufferedImage.TYPE_INT_ARGB)); // Placeholder
        }

        try {
            ImageIcon originalIcon = new ImageIcon(posterPath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(180, 220, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageIcon(new BufferedImage(180, 220, BufferedImage.TYPE_INT_ARGB)); // Return empty image if error
        }
    }
}