# Vaidhyashala Backend - E2E API Verification Script
# This script automates registering a unique customer, logging in to get a JWT token,
# and booking an appointment with the seeded provider.

$baseUrl = if ($env:API_BASE_URL) { $env:API_BASE_URL } else { "http://localhost:8080" }
$uniqueId = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$customerEmail = "customer-$uniqueId`@example.com"
$password = "CustomerPass123"

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Starting Vaidhyashala E2E Booking Verification" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "1. Registering unique customer: $customerEmail" -ForegroundColor Yellow

$regBody = @{
    email = $customerEmail
    password = $password
    firstName = "John"
    lastName = "Doe"
    streetAddress = "123 Test Lane"
    city = "Dallas"
    state = "TX"
    postalCode = "75201"
    country = "USA"
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/register" -Method Post -Body $regBody -ContentType "application/json"
    Write-Host "✅ Customer registration successful!" -ForegroundColor Green
} catch {
    Write-Error "❌ Registration failed: $_"
    exit
}

Write-Host "2. Logging in..." -ForegroundColor Yellow
$loginBody = @{
    email = $customerEmail
    password = $password
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.accessToken
    Write-Host "✅ Login successful! JWT Token acquired." -ForegroundColor Green
} catch {
    Write-Error "❌ Login failed: $_"
    exit
}

Write-Host "2b. Fetching available doctor dynamically..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
}
try {
    $doctors = Invoke-RestMethod -Uri "$baseUrl/api/v1/appointments/doctors" -Method Get -Headers $headers
    if ($doctors.Count -eq 0) {
        throw "No doctors found in the database"
    }
    $doctor = $doctors[0]
    $doctorId = $doctor.id
    $doctorEmail = $doctor.email
    Write-Host "✅ Found doctor: $doctorEmail (ID: $doctorId)" -ForegroundColor Green
} catch {
    Write-Error "❌ Failed to fetch doctor: $_"
    exit
}

Write-Host "3. Booking appointment with doctor ID: $doctorId..." -ForegroundColor Yellow

$randomOffset = Get-Random -Minimum 1 -Maximum 10000
$bookingBody = @{
    doctorId = $doctorId
    startTime = [DateTime]::Now.AddDays(5).AddMinutes($randomOffset).ToString("yyyy-MM-ddTHH:mm:ss")
    endTime = [DateTime]::Now.AddDays(5).AddMinutes($randomOffset).AddMinutes(45).ToString("yyyy-MM-ddTHH:mm:ss")
    description = "Wellness therapy schedule consultation"
} | ConvertTo-Json

try {
    $bookingResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/appointments" -Method Post -Headers $headers -Body $bookingBody -ContentType "application/json"
    Write-Host "=============================================" -ForegroundColor Green
    Write-Host "🎉 SUCCESS! Appointment Booked Successfully!" -ForegroundColor Green
    Write-Host "Appointment ID: $($bookingResponse.id)" -ForegroundColor Green
    Write-Host "Google Event ID: $($bookingResponse.googleCalendarEventId)" -ForegroundColor Green
    Write-Host "Status: $($bookingResponse.status)" -ForegroundColor Green
    Write-Host "=============================================" -ForegroundColor Green
} catch {
    Write-Error "❌ Booking failed: $_"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errBody = $reader.ReadToEnd()
        Write-Host "Error Details: $errBody" -ForegroundColor Red
    }
    exit
}

Write-Host "4. Verifying GET /api/v1/appointments as CUSTOMER..." -ForegroundColor Yellow
try {
    $customerAppointments = Invoke-RestMethod -Uri "$baseUrl/api/v1/appointments" -Method Get -Headers $headers
    Write-Host "✅ Succeeded! Customer fetched $($customerAppointments.Count) appointments." -ForegroundColor Green
    foreach ($app in $customerAppointments) {
        Write-Host "   - Appt ID: $($app.id) | Status: $($app.status) | Customer Email: $($app.customer.user.email)" -ForegroundColor Gray
    }
} catch {
    Write-Error "❌ Customer GET failed: $_"
}

$doctorPassword = if ($env:SEED_PROVIDER_PASSWORD) { $env:SEED_PROVIDER_PASSWORD } else { "AdminProvider123" }
Write-Host "5. Logging in as seeded PROVIDER ($doctorEmail)..." -ForegroundColor Yellow
$doctorLoginBody = @{
    email = $doctorEmail
    password = $doctorPassword
} | ConvertTo-Json

try {
    $provLoginResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $doctorLoginBody -ContentType "application/json"
    $provToken = $provLoginResponse.accessToken
    Write-Host "✅ Provider Login successful!" -ForegroundColor Green
} catch {
    Write-Error "❌ Provider Login failed: $_"
    exit
}

Write-Host "6. Verifying GET /api/v1/appointments as PROVIDER (Should see all events)..." -ForegroundColor Yellow
$provHeaders = @{
    Authorization = "Bearer $provToken"
}

try {
    $providerAppointments = Invoke-RestMethod -Uri "$baseUrl/api/v1/appointments" -Method Get -Headers $provHeaders
    Write-Host "=============================================" -ForegroundColor Green
    Write-Host "🎉 SUCCESS! Provider fetched ALL appointments ($($providerAppointments.Count) events total):" -ForegroundColor Green
    foreach ($app in $providerAppointments) {
        Write-Host "   - Appt ID: $($app.id) | Status: $($app.status) | Customer: $($app.customer.firstName) $($app.customer.lastName) ($($app.customer.user.email))" -ForegroundColor Gray
    }
    Write-Host "=============================================" -ForegroundColor Green
} catch {
    Write-Error "❌ Provider GET failed: $_"
}

