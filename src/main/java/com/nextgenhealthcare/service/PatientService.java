package com.nextgenhealthcare.service;

import com.nextgenhealthcare.model.Patient;
import com.nextgenhealthcare.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
    @Autowired private PatientRepository repo;

    public List<Patient> getAll() { return repo.findAll(); }
    public Optional<Patient> getById(Long id) { return repo.findById(id); }
    public Patient create(Patient p) { return repo.save(p); }

    public Patient update(Long id, Patient updated) {
        return repo.findById(id).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            // check for null — age is Integer now
            if (updated.getAge() != null) existing.setAge(updated.getAge());
            if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
            return repo.save(existing);
        }).orElseGet(() -> {
            updated.setId(id);
            return repo.save(updated);
        });
    }

    public void delete(Long id) { repo.deleteById(id); }
}
