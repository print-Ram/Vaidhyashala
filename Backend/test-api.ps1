# Vaidhyashala Backend - E2E API Verification Script
# This script automates registering a unique customer, logging in to get a JWT token,
# and booking an appointment with the seeded provider.

$baseUrl = "http://localhost:8080"
$providerId = "8c30d954-dd77-4af9-8056-c8e1fa0e0eb5"
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

Write-Host "3. Booking appointment with provider ID: $providerId..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
}

$randomOffset = Get-Random -Minimum 1 -Maximum 10000
$bookingBody = @{
    providerId = $providerId
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

Write-Host "5. Logging in as seeded PROVIDER (provider@vaidhyashala.com)..." -ForegroundColor Yellow
$providerLoginBody = @{
    email = "provider@vaidhyashala.com"
    password = "ProviderPass123"
} | ConvertTo-Json

try {
    $provLoginResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $providerLoginBody -ContentType "application/json"
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

