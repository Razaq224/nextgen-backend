INSERT INTO patients (id, name, age, email) VALUES (1, 'John Doe', 30, 'john@example.com');
INSERT INTO doctors (id, name, specialization, email) VALUES (1, 'Dr. Smith', 'Cardiology', 'drsmith@example.com');

INSERT INTO appointments (id, patient_id, doctor_id, appointment_time, reason, status) VALUES
(1, 1, 1, '2025-10-10 14:30:00', 'Initial checkup', 'SCHEDULED');
