package com.nextgenhealthcare.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;              // <-- Integer (nullable)
    private String email;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "patient-appointments")
    private List<Appointment> appointments = new ArrayList<>();

    public Patient() {}

    // use Integer here too
    public Patient(String name, Integer age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    // getters & setters (all consistent)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }           // Integer return
    public void setAge(Integer age) { this.age = age; } // Integer param

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
}
