package application;
	
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class Main extends Application {

	  private static final String DB_URL = "jdbc:mysql://localhost:3306/dummy";
	  private static final String DB_USER = "root";
	  private static final String DB_PASSWORD = "class123"; // Change this

	  private final TextField nameField = new TextField();
	  private final TextField ageField = new TextField();
	  private final ComboBox<String> genderBox = new ComboBox<>();
	  private final TextField emailField = new TextField();
	  private final ComboBox<String> programBox = new ComboBox<>();
	  private final Label statusLabel = new Label("Enter patient details.");

	  @Override

	  public void start(Stage stage) {
	    BorderPane root = new BorderPane();
	    root.setPadding(new Insets(20));

	    Label heading = new Label("Student Registration");

	    heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
	    Label subtitle = new Label("College Student Services");
	    VBox header = new VBox(5, heading, subtitle);
	    header.setPadding(new Insets(0, 0, 20, 0));
	    root.setTop(header);

	    GridPane form = new GridPane();
	    form.setHgap(12);
	    form.setVgap(14);
	    form.setPadding(new Insets(20));

	    nameField.setPromptText("Full name");
	    ageField.setPromptText("Age in years");
	    emailField.setPromptText("name@example.com");
	    genderBox.getItems().addAll("Female", "Male", "Non-binary", "Prefer not to say");
	    genderBox.setPromptText("Select gender");
	    programBox.getItems().addAll(
	        "Computer Programming", "Cybersecurity",
	        "Data Analytics", "Cloud Computing");
	    programBox.setPromptText("Select program");

	    addRow(form, 0, "Student name:", nameField);
	    addRow(form, 1, "Age:", ageField);
	    addRow(form, 2, "Gender:", genderBox);
	    addRow(form, 3, "Email:", emailField);
	    addRow(form, 4, "Program:", programBox);

	    ColumnConstraints labels = new ColumnConstraints(130);
	    ColumnConstraints fields = new ColumnConstraints();
	    fields.setHgrow(Priority.ALWAYS);
	    form.getColumnConstraints().addAll(labels, fields);

	    Button registerButton = new Button("Register");
	    registerButton.setDefaultButton(true);
	    registerButton.setOnAction(e -> registerStudent());

	    Button clearButton = new Button("Clear");
	    clearButton.setOnAction(e -> clearForm());

	    HBox buttons = new HBox(10, registerButton, clearButton);
	    buttons.setAlignment(Pos.CENTER_RIGHT);

	    VBox content = new VBox(12, form, buttons, statusLabel);
	    content.setMaxWidth(620);
	    root.setCenter(content);
	    root.setBottom(new Label("JavaFX • JDBC • MySQL"));

	    stage.setTitle("Student Registration System");
	    stage.setScene(new Scene(root, 700, 520));
	    stage.show();
	  }

	  private void addRow(GridPane grid, int row, String text, Control control) {
	    Label label = new Label(text);
	    label.setStyle("-fx-font-weight: bold;");
	    control.setMaxWidth(Double.MAX_VALUE);
	    grid.add(label, 0, row);
	    grid.add(control, 1, row);
	  }



	  private void registerStudent() {
	    String name = nameField.getText().trim();
	    String ageValue = ageField.getText().trim();
	    String gender = genderBox.getValue();
	    String email = emailField.getText().trim();
	    String program = programBox.getValue();

	    if (name.isEmpty() || ageValue.isEmpty() || gender == null
	        || email.isEmpty() || program == null) {
	      setStatus("Please complete all fields.", true);
	      return;
	    }

	    int age;
	    try {
	      age = Integer.parseInt(ageValue);
	    } catch (NumberFormatException ex) {
	      setStatus("Age must be a whole number.", true);
	      return;
	    }

	    if (age < 16 || age > 100) {
	      setStatus("Age must be between 16 and 100.", true);
	      return;
	    }
	    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
	      setStatus("Enter a valid email address.", true);
	      return;
	    }

	    String sql = "INSERT INTO student "
	        + "(student_name, age, gender, email, program) "
	        + "VALUES (?, ?, ?, ?, ?)";

	    try (Connection connection =
	           DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
	       PreparedStatement statement = connection.prepareStatement(sql)) {

	      statement.setString(1, name);
	      statement.setInt(2, age);
	      statement.setString(3, gender);
	      statement.setString(4, email);
	      statement.setString(5, program);

	      int count = statement.executeUpdate();
	      if (count == 1) {
	        setStatus("Patient added successfully!", false);
	        clearFields();
	      } else {
	        setStatus("No record was inserted.", true);
	      }
	    } catch (SQLException ex) {
	      setStatus("Database error: " + ex.getMessage(), true);
	      ex.printStackTrace();
	    }
	  }

	  private void clearForm() {
	    clearFields();
	    setStatus("Form cleared.", false);
	  }

	  private void clearFields() {
	    nameField.clear();
	    ageField.clear();
	    genderBox.getSelectionModel().clearSelection();
	    emailField.clear();
	    programBox.getSelectionModel().clearSelection();
	  }

	  private void setStatus(String message, boolean error) {
	    statusLabel.setText(message);
	    statusLabel.setStyle(error ? "-fx-text-fill: #b00020;"
	                  : "-fx-text-fill: #167545;");
	  }

	  public static void main(String[] args) {
	    launch(args);
	  }
	}