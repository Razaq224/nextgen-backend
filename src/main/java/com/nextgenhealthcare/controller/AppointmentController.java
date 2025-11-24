package com.nextgenhealthcare.controller;

import com.nextgenhealthcare.dto.AppointmentDTO;
import com.nextgenhealthcare.exception.ResourceNotFoundException;
import com.nextgenhealthcare.model.Appointment;
import com.nextgenhealthcare.model.Doctor;
import com.nextgenhealthcare.model.Patient;
import com.nextgenhealthcare.repository.AppointmentRepository;
import com.nextgenhealthcare.repository.DoctorRepository;
import com.nextgenhealthcare.repository.PatientRepository;
import com.nextgenhealthcare.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    @Autowired private AppointmentService appointmentService;
    @Autowired private AppointmentRepository appointmentRepo;
    @Autowired private DoctorRepository doctorRepo;
    @Autowired private PatientRepository patientRepo;

    @GetMapping
    public List<Appointment> getAll() { return appointmentService.getAllAppointments(); }

    @GetMapping("/patient/{patientId}")
    public List<Appointment> getByPatientId(@PathVariable Long patientId) {
        return appointmentService.getByPatientId(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> getByDoctorId(@PathVariable Long doctorId) {
        return appointmentService.getByDoctorId(doctorId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Appointment> create(@RequestBody AppointmentDTO dto) {
        Patient patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + dto.getPatientId()));
        Doctor doctor = doctorRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + dto.getDoctorId()));

        Appointment appointment = new Appointment(
                patient,
                doctor,
                dto.getAppointmentTime(),
                dto.getReason(),
                dto.getStatus() != null ? dto.getStatus() : "SCHEDULED"
        );

        Appointment created = appointmentService.createAppointment(appointment);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> update(@PathVariable Long id, @RequestBody AppointmentDTO dto) {
        // Get existing appointment
        Appointment existingAppointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        // Update patient if patientId is provided
        if (dto.getPatientId() != null) {
            Patient patient = patientRepo.findById(dto.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + dto.getPatientId()));
            existingAppointment.setPatient(patient);
        }

        // Update doctor if doctorId is provided
        if (dto.getDoctorId() != null) {
            Doctor doctor = doctorRepo.findById(dto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + dto.getDoctorId()));
            existingAppointment.setDoctor(doctor);
        }

        // Update other fields if provided
        if (dto.getAppointmentTime() != null) {
            existingAppointment.setAppointmentTime(dto.getAppointmentTime());
        }
        if (dto.getReason() != null) {
            existingAppointment.setReason(dto.getReason());
        }
        if (dto.getStatus() != null) {
            existingAppointment.setStatus(dto.getStatus());
        }

        // Save the updated appointment
        Appointment updated = appointmentRepo.save(existingAppointment);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
