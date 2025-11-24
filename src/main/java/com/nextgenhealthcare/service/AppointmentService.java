package com.nextgenhealthcare.service;

import com.nextgenhealthcare.model.Appointment;
import com.nextgenhealthcare.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    @Autowired private AppointmentRepository repo;

    public List<Appointment> getAllAppointments() { return repo.findAll(); }
    public Optional<Appointment> getAppointmentById(Long id) { return repo.findById(id); }

    public Appointment createAppointment(Appointment appointment) {
        if (appointment.getStatus() == null) appointment.setStatus("SCHEDULED");
        return repo.save(appointment);
    }

    public Appointment updateAppointment(Long id, Appointment updated) {
        return repo.findById(id).map(existing -> {
            if (updated.getAppointmentTime() != null) existing.setAppointmentTime(updated.getAppointmentTime());
            if (updated.getReason() != null) existing.setReason(updated.getReason());
            if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
            if (updated.getDoctor() != null) existing.setDoctor(updated.getDoctor());
            if (updated.getPatient() != null) existing.setPatient(updated.getPatient());
            return repo.save(existing);
        }).orElseGet(() -> {
            updated.setId(id);
            return repo.save(updated);
        });
    }

    public void deleteAppointment(Long id) { repo.deleteById(id); }

    public List<Appointment> getByPatientId(Long patientId) { return repo.findByPatientId(patientId); }
    public List<Appointment> getByDoctorId(Long doctorId) { return repo.findByDoctorId(doctorId); }
}
