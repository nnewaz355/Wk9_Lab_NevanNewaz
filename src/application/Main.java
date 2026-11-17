package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.time.*;

public class Main extends Application {

	private static final String DB_URL = "jdbc:mysql://localhost:3306/hospital_db";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "class123"; // Change this

	private final TextField nameField = new TextField();
	private final TextField ageField = new TextField();
	private final ComboBox<String> genderBox = new ComboBox<>();
	private final TextField phoneField = new TextField();
	private final TextField diagnosisField = new TextField();
	private final DatePicker dateField = new DatePicker();
	private final Label statusLabel = new Label("Enter patient details.");
	private final TableView<Patient> patientsTable = new TableView<>();
	private final ObservableList<Patient> patientList = FXCollections.observableArrayList();
	private final TextField patientIdField = new TextField();
	private final TextField searchField = new TextField();

	@Override
	public void start(Stage stage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(20));

		Label heading = new Label("Hospital Management System");

		heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
		Label subtitle = new Label("College Student Services");
		VBox header = new VBox(5, heading, subtitle);
		header.setPadding(new Insets(0, 0, 20, 0));
		root.setTop(header);

		GridPane form = new GridPane();
		form.setHgap(12);
		form.setVgap(14);
		form.setPadding(new Insets(20));
		
		patientIdField.setEditable(false);
		nameField.setPromptText("Full name");
		ageField.setPromptText("Age in years");
		phoneField.setPromptText("555-555-5555");
		genderBox.getItems().addAll("Female", "Male", "Non-binary", "Prefer not to say");
		genderBox.setPromptText("Select gender");
		diagnosisField.setPromptText("Enter reason for visit");

		addRow(form, 0, "Patient ID:", patientIdField);
		addRow(form, 1, "Full name:", nameField);
		addRow(form, 2, "Age:", ageField);
		addRow(form, 3, "Gender:", genderBox);
		addRow(form, 4, "Phone:", phoneField);
		addRow(form, 5, "Reason for visit:", diagnosisField);
		addRow(form, 6, "Admission date:", dateField);

		ColumnConstraints labels = new ColumnConstraints(130);
		ColumnConstraints fields = new ColumnConstraints();
		fields.setHgrow(Priority.ALWAYS);
		form.getColumnConstraints().addAll(labels, fields);

		Button addPatientButton = new Button("Add Patient");
		addPatientButton.setDefaultButton(true);
		addPatientButton.setOnAction(e -> addPatient());

		Button updateButton = new Button("Update");
		updateButton.setOnAction(e -> updatePatient());

		Button deleteButton = new Button("Delete");
		deleteButton.setOnAction(e -> deletePatient());

		patientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedPatient) -> {
			if (selectedPatient != null) {
				patientIdField.setText(String.valueOf(selectedPatient.getPatientId()));
				nameField.setText(selectedPatient.getName());
				ageField.setText(String.valueOf(selectedPatient.getAge()));
				genderBox.setValue(selectedPatient.getGender());
				phoneField.setText(selectedPatient.getPhone());
				diagnosisField.setText(selectedPatient.getDiagnosis());
				dateField.setValue(selectedPatient.getAdmissionDate());
				setStatus("Selected Patient ID: " + selectedPatient.getPatientId(), false);
			}
		});

		Button clearButton = new Button("Clear");
		clearButton.setOnAction(e -> clearForm());

		HBox formButtons = new HBox(10, addPatientButton, updateButton, deleteButton, clearButton);
		formButtons.setAlignment(Pos.CENTER_RIGHT);

		VBox formPane = new VBox(15, form, formButtons);
		formPane.setPrefWidth(380);
		formPane.setPadding(new Insets(15));

		searchField.setPromptText("Search by name or phone...");
		HBox.setHgrow(searchField, Priority.ALWAYS);

		Button searchButton = new Button("Search");
		searchButton.setOnAction(e -> searchPatients());

		Button showAllButton = new Button("Show All");
		showAllButton.setOnAction(e -> showAllPatients());

		HBox searchBar = new HBox(8, searchField, searchButton, showAllButton);
		searchBar.setAlignment(Pos.CENTER_LEFT);

		VBox tablePane = new VBox(15, patientsTable, searchBar);

		TableColumn<Patient, Integer> patientIdCol = new TableColumn<>("Patient ID");
		patientIdCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));
		TableColumn<Patient, String> nameCol = new TableColumn<>("Full Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<Patient, Integer> ageCol = new TableColumn<>("Age");
		ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
		TableColumn<Patient, String> genderCol = new TableColumn<>("Gender");
		genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
		TableColumn<Patient, String> phoneCol = new TableColumn<>("Phone");
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
		TableColumn<Patient, String> diagnosisCol = new TableColumn<>("Diagnosis");
		diagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
		TableColumn<Patient, LocalDate> dateCol = new TableColumn<>("Admission Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));
		patientsTable.getColumns().addAll(patientIdCol, nameCol, ageCol, genderCol, phoneCol, diagnosisCol, dateCol);
		patientsTable.setItems(patientList);
		
		showAllPatients();

		HBox center = new HBox(10, formPane, tablePane);

		VBox content = new VBox(12, /* form */center);
		content.setMaxWidth(1240);
		root.setCenter(content);
		root.setBottom(statusLabel);

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

	private void addPatient() {
		/*
		String name = nameField.getText().trim();
		String ageValue = ageField.getText().trim();
		String gender = genderBox.getValue();
		String phone = phoneField.getText().trim();
		String diagnosis = diagnosisField.getText().trim();
		LocalDate admissionDate = dateField.getValue();

		if (name.isEmpty() || ageValue.isEmpty() || gender == null || phone.isEmpty() || diagnosis.isEmpty()
				|| admissionDate == null) {
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

		if (age < 0 || age > 125) {
			setStatus("Age must be between 0 and 125.", true);
			return;
		}
		if (!phone.matches("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$")) {
			setStatus("Enter a valid phone number.", true);
			return;
		}
		if (admissionDate.isAfter(LocalDate.now())) {
			setStatus("Admission date cannot be in the future.", true);
			return;
		}
		*/
		
		String name = nameField.getText().trim();
		String ageValue = ageField.getText().trim();
		String gender = genderBox.getValue();
		String phone = phoneField.getText().trim();
		String diagnosis = diagnosisField.getText().trim();
		LocalDate admissionDate = dateField.getValue();
		
		if (!validateForm()) {
			return;
		}
		int age = Integer.parseInt(ageValue);

		String sql = "INSERT INTO patients " + "(name, age, gender, phone, diagnosis, admission_date) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, name);
			statement.setInt(2, age);
			statement.setString(3, gender);
			statement.setString(4, phone);
			statement.setString(5, diagnosis);
			statement.setObject(6, admissionDate);

			int count = statement.executeUpdate();
			if (count == 1) {
				setStatus("Patient added successfully!", false);
				showAllPatients();
				clearFields();
			} else {
				setStatus("No record was inserted.", true);
			}
		} catch (SQLException ex) {
			setStatus("Database error: " + ex.getMessage(), true);
			ex.printStackTrace();
		}
	}

	private void updatePatient() {
		Patient selected = patientsTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			setStatus("Select a patient in the table before updating.", true);
			return;
		}

		String name = nameField.getText().trim();
		String ageValue = ageField.getText().trim();
		String gender = genderBox.getValue();
		String phone = phoneField.getText().trim();
		String diagnosis = diagnosisField.getText().trim();
		LocalDate admissionDate = dateField.getValue();

		if (!validateForm()) {
			return;
		}
		int age = Integer.parseInt(ageValue);

		String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, phone = ?, "
				+ "diagnosis = ?, admission_date = ? WHERE patient_id = ?";

		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, name);
			statement.setInt(2, age);
			statement.setString(3, gender);
			statement.setString(4, phone);
			statement.setString(5, diagnosis);
			statement.setObject(6, admissionDate);
			statement.setInt(7, selected.getPatientId());

			int count = statement.executeUpdate();
			if (count == 1) {
				setStatus("Patient updated successfully!", false);
				showAllPatients();
			} else {
				setStatus("No record was updated.", true);
			}
		} catch (SQLException ex) {
			setStatus("Database error: " + ex.getMessage(), true);
			ex.printStackTrace();
		}
	}

	private void deletePatient() {
		String idText = patientIdField.getText().trim();

		if (idText.isBlank()) {
			setStatus("Please select a patient record from the table to delete.", true);
			return;
		}
		
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Confirm deletion");
		confirm.setHeaderText("Delete patient record?");
		confirm.setContentText("This action cannot be undone.");
		if(confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

		int patientId = Integer.parseInt(idText);

		String sql = "DELETE FROM patients WHERE patient_id = ?";

		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, patientId);

			int count = statement.executeUpdate();
			if (count == 1) {
				showAllPatients(); // Refresh TableView
				clearFields(); // Reset form and table selection
				setStatus("Patient ID deleted successfully!", false);
			} else {
				setStatus("No record was deleted.", true);
			}
		} catch (SQLException ex) {
			setStatus("Database error: " + ex.getMessage(), true);
			ex.printStackTrace();
		}
	}

	private void searchPatients() {
		String query = searchField.getText().trim();
		if (query.isEmpty()) {
			showAllPatients();
			setStatus("Showing all patients.", false);
			return;
		}

		String sql = "SELECT patient_id, name, age, gender, phone, diagnosis, admission_date FROM patients WHERE name LIKE ? OR phone LIKE ? ORDER BY patient_id";

		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, "%" + query + "%");
			statement.setString(2, "%" + query + "%");

			try (ResultSet resultSet = statement.executeQuery()) {
				patientList.clear();
				while (resultSet.next()) {
					patientList.add(new Patient(resultSet.getInt("patient_id"), resultSet.getString("name"),
							resultSet.getInt("age"), resultSet.getString("gender"), resultSet.getString("phone"),
							resultSet.getString("diagnosis"), resultSet.getDate("admission_date").toLocalDate()));
				}
			}
			setStatus(patientList.size() + " record(s) matched \"" + query + "\".", false);
		} catch (SQLException ex) {
			setStatus("Database error: " + ex.getMessage(), true);
			ex.printStackTrace();
		}
	}

	private void showAllPatients() {
		patientList.clear();
		String sql = "SELECT patient_id, name, age, gender, phone, diagnosis, admission_date FROM patients";

		try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				patientList.add(new Patient(rs.getInt("patient_id"), rs.getString("name"), rs.getInt("age"),
						rs.getString("gender"), rs.getString("phone"), rs.getString("diagnosis"),
						rs.getDate("admission_date").toLocalDate()));
			}
		} catch (Exception e) {
			setStatus("Database error: " + e.getMessage(), true);
			e.printStackTrace();
		}
	}
	
	private boolean validateForm() {
		String name = nameField.getText().trim();
		String ageValue = ageField.getText().trim();
		String gender = genderBox.getValue();
		String phone = phoneField.getText().trim();
		String diagnosis = diagnosisField.getText().trim();
		LocalDate admissionDate = dateField.getValue();

		if (name.isEmpty() || ageValue.isEmpty() || gender == null || phone.isEmpty() || diagnosis.isEmpty()
				|| admissionDate == null) {
			setStatus("Please complete all fields.", true);
			return false;
		}

		int age;
		try {
			age = Integer.parseInt(ageValue);
		} catch (NumberFormatException ex) {
			setStatus("Age must be a whole number.", true);
			return false;
		}

		if (age < 0 || age > 125) {
			setStatus("Age must be between 0 and 125.", true);
			return false;
		}
		if (!phone.matches("^\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*$")) {
			setStatus("Enter a valid phone number.", true);
			return false;
		}
		if (admissionDate.isAfter(LocalDate.now())) {
			setStatus("Admission date cannot be in the future.", true);
			return false;
		}
		return true;
	}

	private void clearForm() {
		clearFields();
		setStatus("Form cleared.", false);
	}

	private void clearFields() {
		patientIdField.clear();
		nameField.clear();
		ageField.clear();
		genderBox.getSelectionModel().clearSelection();
		phoneField.clear();
		diagnosisField.clear();
		dateField.setValue(null);
		dateField.getEditor().clear();
		patientsTable.getSelectionModel().clearSelection();
	}

	private void setStatus(String message, boolean error) {
		statusLabel.setText(message);
		statusLabel.setStyle(error ? "-fx-text-fill: #b00020;" : "-fx-text-fill: #167545;");
	}

	public static void main(String[] args) {
		launch(args);
	}
}