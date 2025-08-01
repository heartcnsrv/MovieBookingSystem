package lumiere.view;

import lumiere.model.Movie;
import lumiere.server.MovieService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class AdminMoviePanel extends JPanel {
    private JTable movieTable;
    private DefaultTableModel tableModel;
    private String[] columnNames = {"Title", "Year", "Price", "Time Slots", "Cinema", "Edit", "Delete"};
    private MovieService movieService;
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JComboBox<String> searchCriteriaComboBox;

    public AdminMoviePanel(MovieService movieService) {
        this.movieService = movieService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize the panel and GridBagConstraints
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by:"));
        String[] searchOptions = {"All", "Title", "Year", "Cinema", "Price", "Time Slot"};
        searchCriteriaComboBox = new JComboBox<>(searchOptions);
        searchPanel.add(searchCriteriaComboBox);
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear Search");
        searchButton.addActionListener(e -> searchMovies());
        clearButton.addActionListener(e -> clearSearch());

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addMovieButton = new JButton("Add Movie");
        addMovieButton.addActionListener(e -> openAddDialog());
        addPanel.add(addMovieButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(searchPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(addPanel, gbc);

        add(panel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6;
            }
        };

        movieTable = new JTable(tableModel);
        movieTable.setRowHeight(40);
        movieTable.getColumn("Edit").setCellRenderer(new ButtonRenderer("Edit"));
        movieTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), true));
        movieTable.getColumn("Delete").setCellRenderer(new ButtonRenderer("Delete"));
        movieTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), false));

        add(new JScrollPane(movieTable), BorderLayout.CENTER);
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Movie", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Title:"), gbc);
        JTextField titleField = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(titleField, gbc);

        // Year
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Year:"), gbc);
        JTextField yearField = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(yearField, gbc);

        // Duration
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Duration:"), gbc);
        JTextField durationField = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(durationField, gbc);

        // Director
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Director:"), gbc);
        JTextField directorField = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(directorField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Price:"), gbc);
        JTextField priceField = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(priceField, gbc);

        // Cinema Dropdown
        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Cinema:"), gbc);
        String[] cinemas = {"Cinema 1", "Cinema 2", "Cinema 3", "Cinema 4"};
        JComboBox<String> cinemaDropdown = new JComboBox<>(cinemas);
        gbc.gridx = 1;
        dialog.add(cinemaDropdown, gbc);

        // Genre (CheckBoxes)
        gbc.gridx = 0; gbc.gridy = 6;
        dialog.add(new JLabel("Genre:"), gbc);
        JPanel genrePanel = new JPanel(new GridLayout(1, 4));
        JCheckBox action = new JCheckBox("Action");
        JCheckBox comedy = new JCheckBox("Comedy");
        JCheckBox drama = new JCheckBox("Drama");
        JCheckBox horror = new JCheckBox("Horror");
        genrePanel.add(action);
        genrePanel.add(comedy);
        genrePanel.add(drama);
        genrePanel.add(horror);
        gbc.gridx = 1;
        dialog.add(genrePanel, gbc);

        // Synopsis (Scrollable)
        gbc.gridx = 0; gbc.gridy = 7;
        dialog.add(new JLabel("Synopsis:"), gbc);
        JTextArea synopsisArea = new JTextArea(5, 20);
        JScrollPane synopsisScroll = new JScrollPane(synopsisArea);
        synopsisScroll.setPreferredSize(new Dimension(250, 80));
        gbc.gridx = 1;
        dialog.add(synopsisScroll, gbc);

        // Poster Panel Setup
        JPanel posterPanel = new JPanel(new BorderLayout(5, 5));
        JLabel posterPreview = new JLabel();
        posterPreview.setPreferredSize(new Dimension(150, 200));
        posterPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        posterPreview.setHorizontalAlignment(JLabel.CENTER);

        JButton choosePosterButton = new JButton("Choose Poster");
        final String[] selectedPosterPath = { null };

        choosePosterButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png"));
            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {

                    File posterDir = new File("res/MoviePosters");
                    if (!posterDir.exists()) {
                        posterDir.mkdirs();
                    }

                    String newFileName = "res/MoviePosters/" + selectedFile.getName();

                    Files.copy(selectedFile.toPath(), new File(newFileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    selectedPosterPath[0] = newFileName;

                    ImageIcon imageIcon = new ImageIcon(newFileName);
                    Image image = imageIcon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
                    posterPreview.setIcon(new ImageIcon(image));

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error copying poster file: " + ex.getMessage());
                }
            }
        });

        posterPanel.add(posterPreview, BorderLayout.CENTER);
        posterPanel.add(choosePosterButton, BorderLayout.SOUTH);

        gbc.gridx = 1;
        gbc.gridy = 8;
        dialog.add(posterPanel, gbc);

        // Time Slot Management (JList with Add/Remove)
        gbc.gridx = 0; gbc.gridy = 9;
        dialog.add(new JLabel("Time Slots:"), gbc);
        DefaultListModel<String> timeSlotModel = new DefaultListModel<>();
        JList<String> timeSlotList = new JList<>(timeSlotModel);
        JScrollPane timeSlotScroll = new JScrollPane(timeSlotList);
        timeSlotScroll.setPreferredSize(new Dimension(100, 80));

        JButton addTimeButton = new JButton("Add Time");
        JButton removeTimeButton = new JButton("Remove Time");

        addTimeButton.addActionListener(e -> {
            String newTime = JOptionPane.showInputDialog(dialog, "Enter new time slot:");
            if (newTime != null && !newTime.trim().isEmpty() && !timeSlotModel.contains(newTime)) {
                timeSlotModel.addElement(newTime);
            }
        });

        removeTimeButton.addActionListener(e -> {
            int selectedIndex = timeSlotList.getSelectedIndex();
            if (selectedIndex != -1) {
                timeSlotModel.remove(selectedIndex);
            }
        });

        JPanel timeSlotPanel = new JPanel();
        timeSlotPanel.add(timeSlotScroll);
        timeSlotPanel.add(addTimeButton);
        timeSlotPanel.add(removeTimeButton);
        gbc.gridx = 1;
        dialog.add(timeSlotPanel, gbc);

        // Save & Cancel Buttons
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            Movie newMovie = new Movie(
                    titleField.getText(),
                    yearField.getText(),
                    durationField.getText(),
                    synopsisArea.getText(),
                    directorField.getText(),
                    List.of(
                            action.isSelected() ? "Action" : "",
                            comedy.isSelected() ? "Comedy" : "",
                            drama.isSelected() ? "Drama" : "",
                            horror.isSelected() ? "Horror" : ""
                    ).stream().filter(g -> !g.isEmpty()).collect(Collectors.toList()), // Filters empty genres
                    selectedPosterPath[0], // Adjust as needed
                    priceField.getText(),
                    cinemaDropdown.getSelectedItem().toString(),
                    Collections.list(timeSlotModel.elements()) // Converts DefaultListModel to List<String>
            );


            // Set genres based on checked boxes
            newMovie.setGenre(List.of(
                    action.isSelected() ? "Action" : "",
                    comedy.isSelected() ? "Comedy" : "",
                    drama.isSelected() ? "Drama" : "",
                    horror.isSelected() ? "Horror" : ""
            ).stream().filter(g -> !g.isEmpty()).collect(Collectors.toList()));

            // Set time slots from the list
            List<String> timeSlots = new ArrayList<>();
            Enumeration<String> elements = timeSlotModel.elements();
            while (elements.hasMoreElements()) {
                timeSlots.add(elements.nextElement());
            }
            newMovie.setTimeSlot(timeSlots);

            addMovie(newMovie);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openEditDialog(Movie edit) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Movie", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title (Not Editable)
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Title:"), gbc);
        JTextField titleField = new JTextField(edit.getTitle(), 20);
        titleField.setEditable(false);
        gbc.gridx = 1;
        dialog.add(titleField, gbc);

        // Year
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Year:"), gbc);
        JTextField yearField = new JTextField(edit.getYear(), 20);
        gbc.gridx = 1;
        dialog.add(yearField, gbc);

        // Duration
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Duration:"), gbc);
        JTextField durationField = new JTextField(edit.getDuration(), 20);
        gbc.gridx = 1;
        dialog.add(durationField, gbc);

        // Director
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Director:"), gbc);
        JTextField directorField = new JTextField(edit.getDirector(), 20);
        gbc.gridx = 1;
        dialog.add(directorField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Price:"), gbc);
        JTextField priceField = new JTextField(edit.getPrice(), 20);
        gbc.gridx = 1;
        dialog.add(priceField, gbc);

        // Cinema Dropdown
        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Cinema:"), gbc);
        String[] cinemas = {"Cinema 1", "Cinema 2", "Cinema 3", "Cinema 4"};
        JComboBox<String> cinemaDropdown = new JComboBox<>(cinemas);
        cinemaDropdown.setSelectedItem(edit.getCinema());
        gbc.gridx = 1;
        dialog.add(cinemaDropdown, gbc);

        // Genre (CheckBoxes)
        gbc.gridx = 0; gbc.gridy = 6;
        dialog.add(new JLabel("Genre:"), gbc);
        JPanel genrePanel = new JPanel(new GridLayout(1, 4));
        JCheckBox action = new JCheckBox("Action");
        JCheckBox comedy = new JCheckBox("Comedy");
        JCheckBox drama = new JCheckBox("Drama");
        JCheckBox horror = new JCheckBox("Horror");
        genrePanel.add(action);
        genrePanel.add(comedy);
        genrePanel.add(drama);
        genrePanel.add(horror);
        gbc.gridx = 1;
        dialog.add(genrePanel, gbc);

// Synopsis (Scrollable)
        gbc.gridx = 0; gbc.gridy = 7;
        dialog.add(new JLabel("Synopsis:"), gbc);
        JTextArea synopsisArea = new JTextArea(edit.getSynopsis(), 5, 20);
        JScrollPane synopsisScroll = new JScrollPane(synopsisArea);
        synopsisScroll.setPreferredSize(new Dimension(250, 80));
        gbc.gridx = 1;
        dialog.add(synopsisScroll, gbc);

// Poster label and button
        gbc.gridx = 0; gbc.gridy = 8;
        JLabel posterLabel = new JLabel("Poster: ");
        dialog.add(posterLabel, gbc);

        JLabel posterImageLabel = new JLabel();
        if (edit.getPoster() != null) {
            String posterPath = edit.getPoster();
            File file = new File(posterPath);

            if (file.exists()) {
                ImageIcon imageIcon = new ImageIcon(file.getAbsolutePath());
                Image image = imageIcon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
                posterImageLabel.setIcon(new ImageIcon(image));
            } else {
                JOptionPane.showMessageDialog(dialog, "Poster file does not exist at: " + file.getAbsolutePath());
            }
        }

        posterImageLabel.setPreferredSize(new Dimension(150, 200));  // Fixed size for the image label
        posterImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Adding border
        posterImageLabel.setHorizontalAlignment(JLabel.CENTER); // Centering the image


        gbc.gridx = 1;
        gbc.gridy = 8;
        dialog.add(posterImageLabel, gbc);

        JButton changePosterButton = new JButton("Change Poster");
        changePosterButton.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));
            int returnValue = fileChooser.showOpenDialog(dialog);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {

                    String newPosterPath = selectedFile.getAbsolutePath();

                    edit.setPoster(newPosterPath);

                    ImageIcon imageIcon = new ImageIcon(newPosterPath);
                    Image image = imageIcon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
                    posterImageLabel.setIcon(new ImageIcon(image));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error loading image: " + ex.getMessage());
                }
            }
        });


        gbc.gridx = 2;
        gbc.gridy = 8;
        dialog.add(changePosterButton, gbc);

        // Time Slot Management (JList with Add/Remove)
        gbc.gridx = 0; gbc.gridy = 9;
        dialog.add(new JLabel("Time Slots:"), gbc);
        DefaultListModel<String> timeSlotModel = new DefaultListModel<>();
        edit.getTimeSlot().forEach(timeSlotModel::addElement);
        JList<String> timeSlotList = new JList<>(timeSlotModel);
        JScrollPane timeSlotScroll = new JScrollPane(timeSlotList);
        timeSlotScroll.setPreferredSize(new Dimension(100, 80));

        JButton addTimeButton = new JButton("Add Time");
        JButton removeTimeButton = new JButton("Remove Time");

        addTimeButton.addActionListener(e -> {
            String newTime = JOptionPane.showInputDialog(dialog, "Enter new time slot:");
            if (newTime != null && !newTime.trim().isEmpty() && !timeSlotModel.contains(newTime)) {
                timeSlotModel.addElement(newTime);
            }
        });

        removeTimeButton.addActionListener(e -> {
            int selectedIndex = timeSlotList.getSelectedIndex();
            if (selectedIndex != -1) {
                timeSlotModel.remove(selectedIndex);
            }
        });

        JPanel timeSlotPanel = new JPanel();
        timeSlotPanel.add(timeSlotScroll);
        timeSlotPanel.add(addTimeButton);
        timeSlotPanel.add(removeTimeButton);
        gbc.gridx = 1;
        dialog.add(timeSlotPanel, gbc);

        // Save & Cancel Buttons
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            edit.setYear(yearField.getText());
            edit.setDuration(durationField.getText());
            edit.setDirector(directorField.getText());
            edit.setPrice(priceField.getText());
            edit.setCinema(cinemaDropdown.getSelectedItem().toString());
            edit.setSynopsis(synopsisArea.getText());

            // Set genres based on checked boxes
            edit.setGenre(List.of(
                    action.isSelected() ? "Action" : "",
                    comedy.isSelected() ? "Comedy" : "",
                    drama.isSelected() ? "Drama" : "",
                    horror.isSelected() ? "Horror" : ""
            ).stream().filter(g -> !g.isEmpty()).collect(Collectors.toList()));

            // Set time slots from the list
            List<String> timeSlots = new ArrayList<>();
            Enumeration<String> elements = timeSlotModel.elements();
            while (elements.hasMoreElements()) {
                timeSlots.add(elements.nextElement());
            }
            edit.setTimeSlot(timeSlots);

            editMovie(edit);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void updateMovieTable(List<Movie> movies) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Movie movie : movies) {
                tableModel.addRow(new Object[]{
                        movie.getTitle(),
                        movie.getYear(),
                        movie.getPrice(),
                        String.join(", ", movie.getTimeSlot()),
                        movie.getCinema(),
                        "Edit",
                        "Delete"
                });
            }
        });
    }

    private void searchMovies() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedCriteria = (String) searchCriteriaComboBox.getSelectedItem();

        try {
            List<Movie> movies = movieService.getMovies();
            List<Movie> filteredMovies = movies.stream()
                    .filter(movie -> {
                        switch (selectedCriteria) {
                            case "Title":
                                return movie.getTitle().toLowerCase().contains(query);
                            case "Year":
                                return movie.getYear().toLowerCase().contains(query);
                            case "Cinema":
                                return movie.getCinema().toLowerCase().contains(query);
                            case "Price":
                                return movie.getPrice().toLowerCase().contains(query);
                            case "Time Slot":
                                return movie.getTimeSlot().stream().anyMatch(slot -> slot.toLowerCase().contains(query));
                            default:
                                return movie.getTitle().toLowerCase().contains(query) ||
                                        movie.getYear().toLowerCase().contains(query) ||
                                        movie.getCinema().toLowerCase().contains(query) ||
                                        movie.getPrice().toLowerCase().contains(query) ||
                                        movie.getTimeSlot().stream().anyMatch(slot -> slot.toLowerCase().contains(query));
                        }
                    })
                    .collect(Collectors.toList());
            updateMovieTable(filteredMovies);
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching movies: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearSearch() {
        searchField.setText("");
        try {
            List<Movie> movies = movieService.getMovies();
            updateMovieTable(movies);
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching movies: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMovie(Movie movie) {
        try {
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to add this movie?",
                    "Confirm Add", JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (movieService.addMovie(movie)) {
                    JOptionPane.showMessageDialog(this, "Movie added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh the table after deletion
                    List<Movie> movies = movieService.getMovies();
                    updateMovieTable(movies);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add movie.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding the movie: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMovie(String title) {
        try {
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to delete this movie?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (movieService.deleteMovie(title)) {
                    JOptionPane.showMessageDialog(this, "Movie deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh the table after deletion
                    List<Movie> movies = movieService.getMovies();
                    updateMovieTable(movies);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete movie.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting movie: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editMovie(Movie edit) {
        try {
            if (movieService.editMovie(edit)) {
                JOptionPane.showMessageDialog(this, "Movie updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update movie.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating movie: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer(String text) {
            setText(text);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String title;
        private boolean isEdit;

        public ButtonEditor(JCheckBox checkBox, boolean isEdit) {
            super(checkBox);
            this.isEdit = isEdit;
            button = new JButton(isEdit ? "Edit" : "Delete");

            button.addActionListener((ActionEvent e) -> {
                int row = movieTable.getSelectedRow();
                if (row == -1) return;  // Prevent errors if no row is selected

                title = (String) movieTable.getValueAt(row, 0);
                fireEditingStopped(); // Ensures the table updates and stops editing

                if (isEdit) {
                    Movie selectedMovie = getMovieByTitle(title);
                    if (selectedMovie != null) {
                        openEditDialog(selectedMovie);
                    }
                } else {
                    deleteMovie(title);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.setText(isEdit ? "Edit" : "Delete");
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }

    private Movie getMovieByTitle(String title) {
        try {
            List<Movie> movies = movieService.getMovies();
            for (Movie movie : movies) {
                if (movie.getTitle().equals(title)) {
                    return movie;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}