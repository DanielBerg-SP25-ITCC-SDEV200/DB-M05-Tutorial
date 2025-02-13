package com.example.javafxdemo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

/**
 * A simple JavaFX application for data entry, saving to an SQLite database,
 * and viewing the data.
 */
public class DataEntryApp extends Application {

    // UI elements
    private TextField nameField;
    private TextField ageField;
    private TextArea displayArea;

    // Database connection
    private Connection connection;

    /**
     * Main method to launch the application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start method to set up the UI and database connection.
     * @param primaryStage The primary stage of the application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simple Data Entry");

        // Initialize UI elements
        nameField = new TextField();
        ageField = new TextField();
        displayArea = new TextArea();
        displayArea.setEditable(false); // Make the display area read-only

        // Create buttons
        Button saveButton = new Button("Save");
        Button viewButton = new Button("View Data");
        Button clearButton = new Button("Clear");

        // Set up event handlers for buttons
        saveButton.setOnAction(e -> saveData());
        viewButton.setOnAction(e -> viewData());
        clearButton.setOnAction(e -> clearFields());


        // Create layout using GridPane for form elements
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10)); // Set padding around the grid
        grid.setVgap(8); // Set vertical gap between elements
        grid.setHgap(5); // Set horizontal gap between elements

        // Add labels and input fields to the grid
        grid.add(new Label("Name:"), 0, 0); // Column 0, Row 0
        grid.add(nameField, 1, 0); // Column 1, Row 0
        grid.add(new Label("Age:"), 0, 1); // Column 0, Row 1
        grid.add(ageField, 1, 1); // Column 1, Row 1
        grid.add(saveButton, 0, 2); // Column 0, Row 2
        grid.add(viewButton, 1, 2); // Column 1, Row 2
        grid.add(clearButton, 0, 3);

        // Create a VBox to hold the grid and the display area vertically
        VBox vbox = new VBox(10, grid, displayArea); // Spacing of 10 between elements
        vbox.setAlignment(Pos.CENTER); // Center the content
        vbox.setPadding(new Insets(10)); // Set padding around the VBox

        // Create the scene and set it to the primary stage
        Scene scene = new Scene(vbox, 400, 300); // Increased height for displayArea
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize database connection
        try {
            // 1. Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // 2. Establish a connection to the database (creates the file if it doesn't exist)
            connection = DriverManager.getConnection("jdbc:sqlite:data.db");

            // 3. Create the table if it doesn't already exist
            createTable();
        } catch (Exception e) {
            e.printStackTrace(); // Print the error for debugging
            showAlert("Database Error", "Error connecting to database: " + e.getMessage());
        }
    }

    /**
     * Creates the "people" table in the database if it doesn't exist.
     */
    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS people (name TEXT, age INTEGER)");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error creating table: " + e.getMessage());
        }
    }


    /**
     * Saves the data entered by the user to the database.
     */
    private void saveData() {
        String name = nameField.getText();
        String ageStr = ageField.getText();

        try {
            int age = Integer.parseInt(ageStr); // Convert age to integer

            // Use PreparedStatement to prevent SQL injection
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO people (name, age) VALUES (?, ?)")) {
                statement.setString(1, name);
                statement.setInt(2, age);
                statement.executeUpdate(); // Execute the insert statement

                clearFields(); // Clear input fields after saving
                showAlert("Success", "Data saved successfully.");
            }
        } catch (NumberFormatException ex) {
            showAlert("Input Error", "Please enter a valid age (integer).");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error saving data: " + e.getMessage());
        }
    }

    /**
     * Retrieves and displays the data from the database in the display area.
     */
    private void viewData() {
        displayArea.clear(); // Clear previous data

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT name, age FROM people")) {

            StringBuilder sb = new StringBuilder(); // Efficient string building
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                sb.append(name).append(", ").append(age).append("\n");
            }
            displayArea.setText(sb.toString()); // Set text in display area
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error retrieving data: " + e.getMessage());
        }
    }

    /**
     * Clears the input fields.
     */
    private void clearFields() {
        nameField.clear();
        ageField.clear();
    }

    /**
     * Displays an alert dialog with the given title and message.
     * @param title The title of the alert.
     * @param message The message to display.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Closes the database connection when the application stops.
     * @throws Exception If an error occurs while closing the connection.
     */
    @Override
    public void stop() throws Exception {
        if (connection != null) {
            connection.close();
        }
        super.stop();
    }
}