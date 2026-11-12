package application;

import java.time.LocalDate;

public class Patient {
	private int patientId;
	private String name;
	private int age;
	private String gender;
	private String phone;
	private String diagnosis;
	private LocalDate admissionDate;
	
	public Patient(int patientId, String name, int age, String gender, String phone, String diagnosis,
			LocalDate admissionDate) {
		this.patientId = patientId;
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.phone = phone;
		this.diagnosis = diagnosis;
		this.admissionDate = admissionDate;
	}

	public int getPatientId() {
		return this.patientId;
	}

	public String getName() {
		return this.name;
	}

	public int getAge() {
		return this.age;
	}

	public String getGender() {
		return this.gender;
	}

	public String getPhone() {
		return this.phone;
	}

	public String getDiagnosis() {
		return this.diagnosis;
	}

	public LocalDate getAdmissionDate() {
		return this.admissionDate;
	}
	
	
}
