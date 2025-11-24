package com.nextgenhealthcare.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many appointments can belong to one patient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference(value = "patient-appointments")
    private Patient patient;

    // Many appointments can belong to one doctor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    @JsonBackReference(value = "doctor-appointments")
    private Doctor doctor;

    @Column(name = "appointment_time")
    private LocalDateTime appointmentTime;

    @Column(length = 100)
    private String reason;

    @Column(length = 50)
    private String status = "SCHEDULED";

    public Appointment() {}

    // Constructor without explicit status (status will default to "SCHEDULED")
    public Appointment(Patient patient, Doctor doctor, LocalDateTime appointmentTime, String reason) {
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
    }

    // Constructor with explicit status (matches your controller usage)
    public Appointment(Patient patient, Doctor doctor, LocalDateTime appointmentTime, String reason, String status) {
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        if (status != null) {
            this.status = status;
        }
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        if (status != null) this.status = status;
    }
}
