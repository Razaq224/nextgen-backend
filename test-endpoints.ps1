# PowerShell Script to Test Appointment API Endpoints
# Make sure your Spring Boot app is running on http://localhost:8080

$baseUrl = "http://localhost:8080"
$appointmentsUrl = "$baseUrl/appointments"
$patientsUrl = "$baseUrl/patients"
$doctorsUrl = "$baseUrl/doctors"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Appointment API Endpoints" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Function to make HTTP requests
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [string]$Description
    )
    
    Write-Host "Testing: $Description" -ForegroundColor Yellow
    Write-Host "  $Method $Url" -ForegroundColor Gray
    
    try {
        $headers = @{
            "Content-Type" = "application/json"
        }
        
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json
            $response = Invoke-WebRequest -Method $Method -Uri $Url -Headers $headers -Body $jsonBody -UseBasicParsing
        } else {
            $response = Invoke-WebRequest -Method $Method -Uri $Url -Headers $headers -UseBasicParsing
        }
        
        Write-Host "  ✓ SUCCESS (Status: $($response.StatusCode))" -ForegroundColor Green
        if ($response.Content) {
            $jsonResponse = $response.Content | ConvertFrom-Json
            Write-Host "  Response: $($jsonResponse | ConvertTo-Json -Depth 3)" -ForegroundColor DarkGray
        }
        Write-Host ""
        return $response
    } catch {
        Write-Host "  ✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            Write-Host "  Status Code: $statusCode" -ForegroundColor Red
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "  Response: $responseBody" -ForegroundColor Red
        }
        Write-Host ""
        return $null
    }
}

# Step 1: Check if server is running
Write-Host "Step 1: Checking if server is running..." -ForegroundColor Cyan
try {
    $quickCheck = Invoke-WebRequest -Uri "$baseUrl/__quickcheck__" -UseBasicParsing -TimeoutSec 5
    Write-Host "✓ Server is running!" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Server is NOT running!" -ForegroundColor Red
    Write-Host "Please start your Spring Boot application first:" -ForegroundColor Yellow
    Write-Host "  cd backend" -ForegroundColor Yellow
    Write-Host "  mvn spring-boot:run" -ForegroundColor Yellow
    Write-Host ""
    exit
}

# Step 2: Get existing patients and doctors (we'll need their IDs)
Write-Host "Step 2: Checking existing Patients and Doctors..." -ForegroundColor Cyan
$patients = Test-Endpoint -Method "GET" -Url $patientsUrl -Description "GET all patients"
$doctors = Test-Endpoint -Method "GET" -Url $doctorsUrl -Description "GET all doctors"

# Extract IDs if available
$patientId = $null
$doctorId = $null

if ($patients -and $patients.Content) {
    $patientsList = $patients.Content | ConvertFrom-Json
    if ($patientsList.Count -gt 0) {
        $patientId = $patientsList[0].id
        Write-Host "  Using Patient ID: $patientId" -ForegroundColor Green
    }
}

if ($doctors -and $doctors.Content) {
    $doctorsList = $doctors.Content | ConvertFrom-Json
    if ($doctorsList.Count -gt 0) {
        $doctorId = $doctorsList[0].id
        Write-Host "  Using Doctor ID: $doctorId" -ForegroundColor Green
    }
}

# If no patients/doctors exist, create them
if (-not $patientId) {
    Write-Host "  Creating a test patient..." -ForegroundColor Yellow
    $newPatient = @{
        name = "John Doe"
        age = 30
        email = "john.doe@example.com"
    }
    $patientResponse = Test-Endpoint -Method "POST" -Url $patientsUrl -Body $newPatient -Description "CREATE patient"
    if ($patientResponse) {
        $patientObj = $patientResponse.Content | ConvertFrom-Json
        $patientId = $patientObj.id
        Write-Host "  ✓ Created Patient ID: $patientId" -ForegroundColor Green
    }
}

if (-not $doctorId) {
    Write-Host "  Creating a test doctor..." -ForegroundColor Yellow
    $newDoctor = @{
        name = "Dr. Smith"
        specialization = "Cardiology"
        email = "dr.smith@example.com"
    }
    $doctorResponse = Test-Endpoint -Method "POST" -Url $doctorsUrl -Body $newDoctor -Description "CREATE doctor"
    if ($doctorResponse) {
        $doctorObj = $doctorResponse.Content | ConvertFrom-Json
        $doctorId = $doctorObj.id
        Write-Host "  ✓ Created Doctor ID: $doctorId" -ForegroundColor Green
    }
}

if (-not $patientId -or -not $doctorId) {
    Write-Host "✗ Cannot proceed without Patient and Doctor IDs" -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Appointment Endpoints" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 3: Test Appointment Endpoints
Write-Host "3.1 Testing GET all appointments" -ForegroundColor Cyan
Test-Endpoint -Method "GET" -Url $appointmentsUrl -Description "GET /appointments"

Write-Host "3.2 Testing POST (Create Appointment)" -ForegroundColor Cyan
$appointmentTime = (Get-Date).AddDays(7).ToString("yyyy-MM-ddTHH:mm:ss")
$newAppointment = @{
    patientId = $patientId
    doctorId = $doctorId
    appointmentTime = $appointmentTime
    reason = "General Checkup"
    status = "SCHEDULED"
}
$createResponse = Test-Endpoint -Method "POST" -Url $appointmentsUrl -Body $newAppointment -Description "POST /appointments (CREATE)"

# Get the created appointment ID
$appointmentId = $null
if ($createResponse -and $createResponse.Content) {
    $appointmentObj = $createResponse.Content | ConvertFrom-Json
    $appointmentId = $appointmentObj.id
    Write-Host "  Created Appointment ID: $appointmentId" -ForegroundColor Green
    Write-Host ""
}

if ($appointmentId) {
    Write-Host "3.3 Testing GET appointment by ID" -ForegroundColor Cyan
    Test-Endpoint -Method "GET" -Url "$appointmentsUrl/$appointmentId" -Description "GET /appointments/{id}"

    Write-Host "3.4 Testing GET appointments by Patient ID" -ForegroundColor Cyan
    Test-Endpoint -Method "GET" -Url "$appointmentsUrl/patient/$patientId" -Description "GET /appointments/patient/{patientId}"

    Write-Host "3.5 Testing GET appointments by Doctor ID" -ForegroundColor Cyan
    Test-Endpoint -Method "GET" -Url "$appointmentsUrl/doctor/$doctorId" -Description "GET /appointments/doctor/{doctorId}"

    Write-Host "3.6 Testing PUT (Update Appointment - Partial Update)" -ForegroundColor Cyan
    $updateAppointment = @{
        status = "CONFIRMED"
        reason = "Updated: Annual Checkup"
    }
    Test-Endpoint -Method "PUT" -Url "$appointmentsUrl/$appointmentId" -Body $updateAppointment -Description "PUT /appointments/{id} (Partial Update)"

    Write-Host "3.7 Testing PUT (Update Appointment - Full Update)" -ForegroundColor Cyan
    $fullUpdateAppointment = @{
        patientId = $patientId
        doctorId = $doctorId
        appointmentTime = (Get-Date).AddDays(14).ToString("yyyy-MM-ddTHH:mm:ss")
        reason = "Follow-up Consultation"
        status = "SCHEDULED"
    }
    Test-Endpoint -Method "PUT" -Url "$appointmentsUrl/$appointmentId" -Body $fullUpdateAppointment -Description "PUT /appointments/{id} (Full Update)"

    Write-Host "3.8 Testing DELETE appointment" -ForegroundColor Cyan
    Test-Endpoint -Method "DELETE" -Url "$appointmentsUrl/$appointmentId" -Description "DELETE /appointments/{id}"

    Write-Host "3.9 Verify deletion (should return 404)" -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Method "GET" -Uri "$appointmentsUrl/$appointmentId" -UseBasicParsing
        Write-Host "  ✗ FAILED: Appointment still exists!" -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 404) {
            Write-Host "  ✓ SUCCESS: Appointment deleted (404 Not Found)" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "All Tests Completed!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan


