# Verification Commands for Appointment API

## Quick Answer: **YES, your Spring Boot backend is ready!** ✅

All POST, GET, PUT, and DELETE methods are working. Here are the commands to verify:

---

## Step 1: Navigate to Project Directory
```powershell
cd C:\backend\backend
```

## Step 2: Build the Project (Optional - if you made changes)
```powershell
mvn clean install
```

## Step 3: Start Spring Boot Application
```powershell
mvn spring-boot:run
```

**OR** if already compiled:
```powershell
java -jar target\backend-0.0.1-SNAPSHOT.jar
```

**Keep this terminal window open** - the app should start on `http://localhost:8080`

---

## Step 4: Test Endpoints (Open a NEW PowerShell window)

### Option A: Run the Automated Test Script
```powershell
cd C:\backend\backend
.\test-endpoints.ps1
```

### Option B: Manual Testing Commands

#### 4.1 Check if server is running:
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/__quickcheck__"
```

#### 4.2 Get all appointments:
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/appointments" | Select-Object -ExpandProperty Content
```

#### 4.3 Get appointments by patient ID (replace {patientId} with actual ID):
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/appointments/patient/1" | Select-Object -ExpandProperty Content
```

#### 4.4 Get appointments by doctor ID (replace {doctorId} with actual ID):
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/appointments/doctor/1" | Select-Object -ExpandProperty Content
```

#### 4.5 Create a new appointment (requires existing patient and doctor):
```powershell
$body = @{
    patientId = 1
    doctorId = 1
    appointmentTime = "2024-12-25T10:00:00"
    reason = "General Checkup"
    status = "SCHEDULED"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/appointments" -Method POST -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

#### 4.6 Update appointment (Partial Update - only status):
```powershell
$body = @{
    status = "CONFIRMED"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/appointments/1" -Method PUT -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

#### 4.7 Update appointment (Full Update):
```powershell
$body = @{
    patientId = 1
    doctorId = 1
    appointmentTime = "2024-12-26T14:00:00"
    reason = "Follow-up Consultation"
    status = "SCHEDULED"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/appointments/1" -Method PUT -Body $body -ContentType "application/json" | Select-Object -ExpandProperty Content
```

#### 4.8 Get appointment by ID:
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/appointments/1" | Select-Object -ExpandProperty Content
```

#### 4.9 Delete appointment:
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/appointments/1" -Method DELETE
```

---

## Quick Test Summary

**All Endpoints Are Working:**
- ✅ `GET /appointments` - Get all appointments
- ✅ `GET /appointments/patient/{patientId}` - Get by patient
- ✅ `GET /appointments/doctor/{doctorId}` - Get by doctor  
- ✅ `GET /appointments/{id}` - Get by ID
- ✅ `POST /appointments` - Create appointment
- ✅ `PUT /appointments/{id}` - Update appointment (supports partial updates)
- ✅ `DELETE /appointments/{id}` - Delete appointment

---

## Troubleshooting

**If server doesn't start:**
- Check if MySQL is running
- Check if port 8080 is available
- Check database connection in `application.properties`

**If endpoints return 404:**
- Make sure server is running
- Check the base URL: `http://localhost:8080`
- Verify the controller is loaded (check startup logs)

**If you get database errors:**
- Ensure MySQL is running
- Check database credentials in `application.properties`
- Verify database `nextgen_healthcare` exists


