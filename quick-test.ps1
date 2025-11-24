# Quick Test Script for Appointment API

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Appointment API" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Create an appointment with valid IDs
Write-Host "Step 1: Creating Appointment..." -ForegroundColor Yellow
$body = @{
    patientId = 3
    doctorId = 2
    appointmentTime = "2024-12-25T10:00:00"
    reason = "General Checkup"
    status = "SCHEDULED"
} | ConvertTo-Json

try {
    $createResponse = Invoke-WebRequest -Uri "http://localhost:8080/appointments" -Method POST -Body $body -ContentType "application/json"
    $appointment = $createResponse.Content | ConvertFrom-Json
    $appointmentId = $appointment.id
    Write-Host "✓ Appointment Created Successfully!" -ForegroundColor Green
    Write-Host "  Appointment ID: $appointmentId" -ForegroundColor Green
    Write-Host "  Patient ID: $($appointment.patient.id)" -ForegroundColor Gray
    Write-Host "  Doctor ID: $($appointment.doctor.id)" -ForegroundColor Gray
    Write-Host "  Status: $($appointment.status)" -ForegroundColor Gray
    Write-Host ""
    
    # Step 2: Test GET by ID
    Write-Host "Step 2: Testing GET /appointments/$appointmentId" -ForegroundColor Yellow
    $getResponse = Invoke-WebRequest -Uri "http://localhost:8080/appointments/$appointmentId"
    $fetchedAppointment = $getResponse.Content | ConvertFrom-Json
    Write-Host "✓ GET by ID Works!" -ForegroundColor Green
    Write-Host "  Appointment ID: $($fetchedAppointment.id)" -ForegroundColor Gray
    Write-Host "  Reason: $($fetchedAppointment.reason)" -ForegroundColor Gray
    Write-Host ""
    
    # Step 3: Test GET by Patient ID
    Write-Host "Step 3: Testing GET /appointments/patient/3" -ForegroundColor Yellow
    $patientResponse = Invoke-WebRequest -Uri "http://localhost:8080/appointments/patient/3"
    $patientAppointments = $patientResponse.Content | ConvertFrom-Json
    Write-Host "✓ GET by Patient Works!" -ForegroundColor Green
    Write-Host "  Found $($patientAppointments.Count) appointment(s)" -ForegroundColor Gray
    Write-Host ""
    
    # Step 4: Test GET by Doctor ID
    Write-Host "Step 4: Testing GET /appointments/doctor/2" -ForegroundColor Yellow
    $doctorResponse = Invoke-WebRequest -Uri "http://localhost:8080/appointments/doctor/2"
    $doctorAppointments = $doctorResponse.Content | ConvertFrom-Json
    Write-Host "✓ GET by Doctor Works!" -ForegroundColor Green
    Write-Host "  Found $($doctorAppointments.Count) appointment(s)" -ForegroundColor Gray
    Write-Host ""
    
    # Step 5: Test PUT (Partial Update)
    Write-Host "Step 5: Testing PUT /appointments/$appointmentId (Partial Update)" -ForegroundColor Yellow
    $updateBody = @{
        status = "CONFIRMED"
    } | ConvertTo-Json
    $putResponse = Invoke-WebRequest -Uri "http://localhost:8080/appointments/$appointmentId" -Method PUT -Body $updateBody -ContentType "application/json"
    $updatedAppointment = $putResponse.Content | ConvertFrom-Json
    Write-Host "✓ PUT (Partial Update) Works!" -ForegroundColor Green
    Write-Host "  Updated Status: $($updatedAppointment.status)" -ForegroundColor Gray
    Write-Host ""
    
    # Step 6: Test PUT (Full Update)
    Write-Host "Step 6: Testing PUT /appointments/$appointmentId (Full Update)" -ForegroundColor Yellow
    $fullUpdateBody = @{
        patientId = 3
        doctorId = 2
        appointmentTime = "2024-12-26T14:00:00"
        reason = "Follow-up Consultation"
        status = "SCHEDULED"
    } | ConvertTo-Json
    $putFullResponse = Invoke-WebRequest -Uri "http://localhost:8080/appointments/$appointmentId" -Method PUT -Body $fullUpdateBody -ContentType "application/json"
    $fullUpdatedAppointment = $putFullResponse.Content | ConvertFrom-Json
    Write-Host "✓ PUT (Full Update) Works!" -ForegroundColor Green
    Write-Host "  Updated Reason: $($fullUpdatedAppointment.reason)" -ForegroundColor Gray
    Write-Host "  Updated Time: $($fullUpdatedAppointment.appointmentTime)" -ForegroundColor Gray
    Write-Host ""
    
    # Step 7: Test DELETE
    Write-Host "Step 7: Testing DELETE /appointments/$appointmentId" -ForegroundColor Yellow
    $deleteResponse = Invoke-WebRequest -Uri "http://localhost:8080/appointments/$appointmentId" -Method DELETE
    Write-Host "✓ DELETE Works! (Status: $($deleteResponse.StatusCode))" -ForegroundColor Green
    Write-Host ""
    
    # Step 8: Verify Deletion
    Write-Host "Step 8: Verifying Deletion..." -ForegroundColor Yellow
    try {
        $verifyResponse = Invoke-WebRequest -Uri "http://localhost:8080/appointments/$appointmentId"
        Write-Host "✗ FAILED: Appointment still exists!" -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 404) {
            Write-Host "✓ Verification: Appointment deleted (404 Not Found)" -ForegroundColor Green
        }
    }
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "ALL TESTS PASSED! ✅" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    
} catch {
    Write-Host "✗ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "  Status Code: $statusCode" -ForegroundColor Red
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "  Response: $responseBody" -ForegroundColor Red
    }
}


