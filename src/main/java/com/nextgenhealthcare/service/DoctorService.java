package com.nextgenhealthcare.service;

import com.nextgenhealthcare.model.Doctor;
import com.nextgenhealthcare.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    @Autowired private DoctorRepository repo;

    public List<Doctor> getAll() { return repo.findAll(); }
    public Optional<Doctor> getById(Long id) { return repo.findById(id); }
    public Doctor create(Doctor d) { return repo.save(d); }
    public Doctor update(Long id, Doctor updated) {
        return repo.findById(id).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            if (updated.getSpecialization() != null) existing.setSpecialization(updated.getSpecialization());
            if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
            return repo.save(existing);
        }).orElseGet(() -> {
            updated.setId(id);
            return repo.save(updated);
        });
    }
    public void delete(Long id) { repo.deleteById(id); }
}
