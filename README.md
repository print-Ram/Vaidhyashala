# 🏥 Vaidhyashala — Wellness Consultation Platform

> A full-stack appointment booking platform that connects patients (customers) with wellness providers, featuring Google Calendar integration, Google Meet video call link generation, and automated email notifications.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Complete Booking Flow](#complete-booking-flow)
- [Email Notification Flow](#email-notification-flow)
- [Role-Based Access Control](#role-based-access-control)
- [Environment Variables](#environment-variables)
- [Running Locally](#running-locally)
- [Running on GCP](#running-on-gcp)
- [Postman Testing Guide](#postman-testing-guide)

---

## Overview

**Vaidhyashala** is a telemedicine / wellness consultation platform where:

- **Customers** register, browse available time slots, and book appointments with wellness providers
- **Providers** manage their schedule and view all customer bookings
- On booking, a **Google Calendar event** is automatically created with a **Google Meet video call link**
- A **confirmation email** with a clickable "Join Google Meet" button is sent to the customer instantly
- A **24-hour reminder email** with the Meet link is scheduled and dispatched automatically

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Framework** | Spring Boot 4.1.0 |
| **Language** | Java 17 |
| **Security** | Spring Security + JWT (JJWT 0.11.5) |
| **Database (Prod)** | PostgreSQL (via Google Cloud SQL) |
| **Database (Local)** | H2 File-Based Database |
| **ORM** | Spring Data JPA + Hibernate 7 |
| **Email** | Spring Mail (Gmail SMTP) |
| **Calendar** | Google Calendar API v3 (Service Account) |
| **Video Calls** | Google Meet (auto-generated via Calendar API) |
| **Payments** | Custom Billing, Checkout & Verification Pipeline |
| **Secrets** | Google Cloud Secret Manager |
| **Build Tool** | Apache Maven |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Utilities** | Lombok |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Client (Postman / Frontend)           │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP REST
┌──────────────────────────▼──────────────────────────────────┐
│                    Spring Boot Backend                        │
│                                                              │
│  ┌─────────────┐   ┌──────────────┐   ┌──────────────────┐  │
│  │AuthController│  │CustomerCtrl  │   │AppointmentCtrl   │  │
│  └──────┬──────┘   └──────┬───────┘   └────────┬─────────┘  │
│         │                 │                    │             │
│  ┌──────▼─────────────────▼────────────────────▼──────────┐ │
│  │              Service Layer                               │ │
│  │  AuthService │ CustomerService │ AppointmentService      │ │
│  │  EmailService │ GoogleCalendarService │ ReminderScheduler│ │
│  └──────┬──────────────────────────────────────────────────┘ │
│         │                                                    │
│  ┌──────▼──────────────────────────────────────────────────┐ │
│  │          Repository Layer (Spring Data JPA)              │ │
│  │  UserRepo │ CustomerProfileRepo │ AppointmentRepo        │ │
│  │  EmailNotificationRepo │ AddressRepo                     │ │
│  └──────┬──────────────────────────────────────────────────┘ │
└─────────┼────────────────────────────────────────────────────┘
          │
    ┌─────┴──────────────────────────────────────┐
    │              External Services              │
    │                                             │
    │  ┌─────────────┐   ┌─────────────────────┐ │
    │  │  H2 / PgSQL │   │  Google Calendar API │ │
    │  │  Database   │   │  (Service Account)   │ │
    │  └─────────────┘   └──────────┬──────────┘ │
    │                               │ Meet Link   │
    │  ┌──────────────┐   ┌─────────▼──────────┐ │
    │  │  Gmail SMTP  │   │  Google Meet Video  │ │
    │  │  (Email)     │   │  (auto-generated)   │ │
    │  └──────────────┘   └────────────────────┘ │
    └────────────────────────────────────────────┘
```

---

## Project Structure

```
Vaidhyashala/
├── Backend/
│   ├── src/main/java/com/version1/backend/
│   │   ├── BackendApplication.java          # Entry point + Provider seeder
│   │   ├── config/
│   │   │   ├── GoogleCalendarConfig.java    # Service Account auth setup
│   │   │   └── SecurityConfig.java          # JWT filter chain, CORS, role rules
│   │   ├── controller/
│   │   │   ├── AuthController.java          # /api/v1/auth (register, login)
│   │   │   ├── CustomerController.java      # /api/v1/customers (profile CRUD)
│   │   │   ├── DoctorController.java        # /api/v1/doctors (profile settings, upload pics, sync slots)
│   │   │   ├── AppointmentController.java   # /api/v1/appointments (book, list)
│   │   │   └── PaymentController.java       # /api/v1/payments (checkout, verify)
│   │   ├── dto/
│   │   │   ├── UserRegistrationDto.java     # Register request body
│   │   │   ├── LoginRequestDto.java         # Login request body
│   │   │   ├── TokenResponseDto.java        # Login response (JWT token)
│   │   │   ├── CustomerProfileDto.java      # Customer profile view/update
│   │   │   ├── DoctorProfileDto.java        # Doctor profile view/update
│   │   │   ├── DoctorConfigurationRequestDto.java # Combined settings payload
│   │   │   ├── AppointmentCreateDto.java    # Book appointment request body
│   │   │   ├── AppointmentResponseDto.java  # Appointment API response (role-filtered)
│   │   │   ├── PaymentCheckoutDto.java      # Initiate payment session
│   │   │   ├── PaymentVerificationDto.java  # Confirm transaction completion
│   │   │   └── CalendarEventResult.java     # Internal DTO: eventId + meetLink
│   │   ├── pojo/ (JPA Entities)
│   │   │   ├── User.java                    # Auth user (email, password, role)
│   │   │   ├── CustomerProfile.java         # Extended customer data (name, DOB, etc)
│   │   │   ├── DoctorProfile.java           # Extended doctor data (name, designation, regNo)
│   │   │   ├── Address.java                 # Customer address details
│   │   │   ├── Appointment.java             # Booking record (times, status, meetLink)
│   │   │   ├── Payment.java                 # Billing record (amount, status, gateway transaction ID)
│   │   │   ├── EmailNotification.java       # Scheduled notification record
│   │   │   ├── Role.java                    # Enum: CUSTOMER, PROVIDER, DOCTOR
│   │   │   ├── AppointmentStatus.java       # Enum: PENDING, CONFIRMED, CANCELLED, COMPLETED
│   │   │   ├── PaymentStatus.java           # Enum: PENDING, COMPLETED, FAILED, REFUNDED
│   │   │   ├── NotificationType.java        # Enum: REMINDER, CONFIRMATION
│   │   │   ├── NotificationStatus.java      # Enum: PENDING, SENT, FAILED
│   │   │   └── UserStatus.java              # Enum: ACTIVE, INACTIVE
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── CustomerProfileRepository.java
│   │   │   ├── DoctorProfileRepository.java
│   │   │   ├── AppointmentRepository.java
│   │   │   ├── PaymentRepository.java
│   │   │   ├── EmailNotificationRepository.java
│   │   │   └── AddressRepository.java
│   │   ├── security/
│   │   │   ├── JwtAuthFilter.java           # JWT validation filter
│   │   │   ├── JwtTokenProvider.java        # JWT generation + parsing
│   │   │   ├── UserPrincipal.java           # Spring Security user details
│   │   │   └── CustomUserDetailsService.java
│   │   ├── service/
│   │   │   ├── AuthService / Impl           # Register + Login logic
│   │   │   ├── CustomerService / Impl       # Profile management
│   │   │   ├── DoctorService / Impl         # Doctor dashboard / configuration
│   │   │   ├── AppointmentService / Impl    # Booking orchestration
│   │   │   ├── PaymentService / Impl        # Checkout + verification logic
│   │   │   ├── GoogleCalendarService / Impl # Calendar event + Meet link creation
│   │   │   ├── EmailService / Impl          # HTML email dispatch
│   │   │   └── ReminderScheduler.java       # Cron: sends 24h reminders
│   │   └── exception/
│   │       ├── CustomException.java
│   │       ├── ResourceNotFoundException.java
│   │       └── GlobalExceptionHandler.java
│   ├── src/main/resources/
│   │   └── application.yml                  # Multi-profile config (default/local/gcp)
│   └── pom.xml
├── Frontend/                                # (Planned)
└── README.md
```

---

## Database Schema

```
users
├── id (UUID, PK)
├── email (UNIQUE)
├── password_hash
├── role (CUSTOMER | PROVIDER)
├── status (ACTIVE | INACTIVE)
├── created_at
└── updated_at

customer_profiles
├── id (UUID, PK)
├── user_id (FK → users.id)
├── first_name
├── last_name
├── phone
├── date_of_birth
├── gender
├── created_at
└── updated_at

doctor_profiles
├── id (UUID, PK)
├── user_id (FK → users.id)
├── first_name
├── last_name
├── phone_number
├── speciality (TEXT, list)
├── department
├── expert_in (TEXT, list)
├── education_details (TEXT, JSON list)
├── certifications (TEXT, JSON list)
├── about (TEXT)
├── resume_url
├── status (PENDING_APPROVAL | ACTIVE | SUSPENDED | OPTED_OUT)
├── designation
├── reg_no
├── profile_image_url
├── created_at
└── updated_at

addresses
├── id (UUID, PK)
├── customer_id (FK → customer_profiles.id)
├── street, city, state, postal_code, country

appointments
├── id (UUID, PK)
├── customer_id (FK → customer_profiles.id)
├── provider_id (FK → users.id)
├── start_time
├── end_time
├── status (PENDING | CONFIRMED | CANCELLED)
├── description
├── google_calendar_event_id       ← Calendar event reference
├── meet_link                      ← Google Meet URL for joining
├── created_at
└── updated_at

email_notifications
├── id (UUID, PK)
├── appointment_id (FK → appointments.id)
├── type (REMINDER | CONFIRMATION)
├── recipient_email
├── status (PENDING | SENT | FAILED)
├── scheduled_send_time            ← Triggered 24h before appointment
└── sent_at

payments
├── id (UUID, PK)
├── appointment_id (FK → appointments.id)
├── amount
├── status (PENDING | COMPLETED | FAILED | REFUNDED)
├── transaction_id
├── created_at
└── updated_at

special_slot_requests
├── id (UUID, PK)
├── customer_id (FK → customer_profiles.id)
├── doctor_id (FK → doctor_profiles.id)
├── requested_date
├── start_time
├── end_time
├── notes
├── status (PENDING | APPROVED | REJECTED)
├── created_at
└── updated_at
```

---

## API Endpoints

### 🔐 Auth — `/api/v1/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/auth/register` | ❌ Public | Register a new user (CUSTOMER or PROVIDER) |
| `POST` | `/api/v1/auth/login` | ❌ Public | Login and receive a JWT token |

**Register Request:**
```json
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "email": "rahul@gmail.com",
  "password": "SecurePass@123",
  "role": "CUSTOMER",
  "phone": "9876543210",
  "dateOfBirth": "1995-06-15",
  "gender": "MALE"
}
```

**Login Request:**
```json
{
  "email": "rahul@gmail.com",
  "password": "SecurePass@123"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "rahul@gmail.com",
  "role": "CUSTOMER"
}
```

> ⚠️ Each email can only be registered once — duplicate emails are rejected with `409 Conflict`.

---

### 👤 Customer Profile — `/api/v1/customers`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/v1/customers/me/profile` | ✅ CUSTOMER | Get your own profile |
| `PUT` | `/api/v1/customers/me/profile` | ✅ CUSTOMER | Update your own profile |

**Profile Update Request:**
```json
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "phone": "9876543210",
  "dateOfBirth": "1995-06-15",
  "gender": "MALE"
}
```

---

### 📅 Appointments — `/api/v1/appointments`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/appointments` | ✅ CUSTOMER | Book a new appointment |
| `GET` | `/api/v1/appointments` | ✅ CUSTOMER / PROVIDER | Provider → all bookings; Customer → own only |
| `GET` | `/api/v1/appointments/me` | ✅ CUSTOMER / PROVIDER | Provider → their schedule; Customer → own appointments |

**Book Appointment Request** (`CUSTOMER` only):
```json
{
  "providerId": "8c30d954-dd77-4af9-8056-c8e1fa0e0eb5",
  "startTime": "2026-07-05T10:00:00",
  "endTime": "2026-07-05T10:30:00",
  "description": "Initial consultation for knee pain"
}
```

**Book Appointment Response** (`201 Created`):
```json
{
  "id": "a1b2c3d4-...",
  "startTime": "2026-07-05T10:00:00",
  "endTime": "2026-07-05T10:30:00",
  "status": "PENDING",
  "description": "Initial consultation for knee pain",
  "meetLink": null,
  "googleCalendarEventId": null,
  "createdAt": "2026-06-28T16:30:00",
  "provider": {
    "id": "8c30d954-...",
    "name": "provider@vaidhyashala.com",
    "email": "provider@vaidhyashala.com"
  },
  "customer": null
}
```

> 📌 `customer` field is `null` for customer responses. Only PROVIDER role sees full customer details.

---

### 🩺 Doctor Profile & Dashboard — `/api/v1/doctors`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET`  | `/api/v1/doctors/me/profile` | ✅ DOCTOR | Get own doctor profile |
| `PUT`  | `/api/v1/doctors/me/configure` | ✅ DOCTOR | Unified configuration (profile fields, upload profile pic, sync slot list) |
| `POST` | `/api/v1/doctors/me/resume` | ✅ DOCTOR | Upload and save resume PDF path |
| `POST` | `/api/v1/doctors/me/opt-out` | ✅ DOCTOR | Voluntarily opt-out |
| `GET`  | `/api/v1/doctors/me/availability` | ✅ DOCTOR | Get own slot availabilities |

**Unified Configure Dashboard Settings Request** (`multipart/form-data`):
* `settings`: JSON string (includes profile info & availability slot configurations)
* `profileImage`: Image file (optional)

```json
{
  "profile": {
    "firstName": "Rahul",
    "lastName": "Sharma",
    "phoneNumber": "9876543210",
    "designation": "Senior Consultant",
    "regNo": "REG-12345",
    "about": "Expert cardiologist with 15+ years experience"
  },
  "availabilities": [
    {
      "availabilityDate": "2026-07-05",
      "startTime": "09:00:00",
      "endTime": "13:00:00",
      "slotDurationMinutes": 30,
      "consultationFee": 800.00,
      "offerPercent": 10,
      "offerLabel": "Weekend Discount"
    }
  ]
}
```

---

### 💳 Payments — `/api/v1/payments`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/payments/checkout` | ✅ CUSTOMER | Initiate a pending billing session for an appointment |
| `POST` | `/api/v1/payments/verify` | ✅ CUSTOMER | Confirm mock payment completion (triggers calendar events & emails) |

**Checkout Request:**
```json
{
  "appointmentId": "a1b2c3d4-..."
}
```

**Verify Payment Request:**
```json
{
  "paymentId": "p5q6r7s8-...",
  "transactionId": "tx-razorpay-998877"
}
```

---

### 📩 Special Slot Requests — `/api/v1/special-requests`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/v1/special-requests` | ✅ CUSTOMER | Customers request a custom slot for a doctor |
| `GET`  | `/api/v1/special-requests/customer` | ✅ CUSTOMER | Customers view their own custom requests |
| `GET`  | `/api/v1/special-requests/doctor` | ✅ DOCTOR | Doctors view slot requests sent to them |
| `PUT`  | `/api/v1/special-requests/{id}/status` | ✅ DOCTOR | Approve or reject a slot request |

**Create Special Slot Request:**
```json
{
  "doctorId": "d3c4b5a6-...",
  "requestedDate": "2026-07-20",
  "startTime": "16:00:00",
  "endTime": "16:45:00",
  "notes": "Emergency checkup"
}
```

**Doctor Update Request Status (Approve/Reject):**
```json
{
  "status": "APPROVED"
}
```

---

## Complete Booking Flow

```
Customer → POST /api/v1/appointments
               │
               ▼
    1. Validate customer profile exists
    2. Validate provider exists and has PROVIDER or DOCTOR role
    3. Check for overlapping appointments (time conflict with CONFIRMED slots → 409 Conflict)
    4. Save appointment record (status = PENDING)
               │
               ▼
Customer → POST /api/v1/payments/checkout
               │
               ▼
    5. Retrieve base fee and apply discount (e.g. 50% off for first consultation)
    6. Save Payment record (status = PENDING)
               │
               ▼
Customer → POST /api/v1/payments/verify
               │
               ▼
    7. Update Payment status = COMPLETED, store gateway transaction ID
    8. Update Appointment status = CONFIRMED
    9. Call Google Calendar API (Service Account)
       ├─ Create event with attendee invite
       ├─ Request Google Meet conference link (conferenceDataVersion=1)
       ├─ Extract hangoutLink from response
       └─ Fallback: retry without Meet if account doesn't support it
   10. Update Appointment record:
       ├─ google_calendar_event_id = event ID from Google
       └─ meet_link = https://meet.google.com/xxx-yyy-zzz
   11. Send HTML Confirmation Email (async)
   12. Schedule 24h Reminder entry in email_notifications
```

---

## Email Notification Flow

### Confirmation Email (Immediate — async)
Sent instantly after booking is confirmed:
- ✅ Blue gradient header: "Appointment Confirmed"
- 📅 Appointment date/time card
- 📹 **"Join Google Meet"** button (if Meet link is available)
- Fallback message if Meet link is unavailable (personal Google account)

### Reminder Email (Scheduled — 24h before)
Triggered by the `ReminderScheduler` cron job (runs every 15 minutes):
- ⏰ Orange gradient header: "Appointment Reminder"
- 📅 Appointment date/time card
- 📹 **"Join Google Meet"** button with the same Meet link stored in DB
- Automatically marks notification as `SENT` or `FAILED` in DB

```
ReminderScheduler (cron: every 15 min)
    │
    ▼
  Find all EmailNotifications WHERE:
    status = PENDING AND scheduled_send_time <= NOW()
    │
    ▼
  For each notification:
    ├─ Fetch appointment.meetLink from DB
    ├─ Send reminder email with Meet button
    ├─ Mark notification status = SENT
    └─ On failure → status = FAILED
```

---

## Role-Based Access Control

| Feature | CUSTOMER | PROVIDER |
|---------|----------|----------|
| Register / Login | ✅ | ✅ |
| View own profile | ✅ | ❌ |
| Update own profile | ✅ | ❌ |
| Book appointment | ✅ | ❌ |
| View own appointments (with Meet link) | ✅ | ❌ |
| View ALL customer appointments | ❌ | ✅ |
| View customer name & email in appointments | ❌ | ✅ |
| View their own schedule (`/appointments/me`) | ❌ | ✅ |

---

## Environment Variables

### Required for All Profiles

| Variable | Description | Example |
|----------|-------------|---------|
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | Gmail address | `yourapp@gmail.com` |
| `MAIL_PASSWORD` | Gmail App Password (not your login password) | `xxxx xxxx xxxx xxxx` |
| `JWT_SECRET` | Base64-encoded secret for signing JWTs | `dGhpcy...` |
| `GOOGLE_CREDENTIALS_JSON` | Path or content of GCP Service Account JSON | `/path/to/key.json` |
| `GOOGLE_CALENDAR_ID` | Target calendar ID for events | `yourname@gmail.com` |

### Required for GCP Profile Only

| Variable | Description |
|----------|-------------|
| `DB_HOST` | PostgreSQL host (Cloud SQL) |
| `DB_PORT` | PostgreSQL port (default: 5432) |
| `DB_NAME` | Database name |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |

### Optional

| Variable | Description | Default |
|----------|-------------|---------|
| `GEMINI_API_KEY` | Google Gemini AI API key | _(empty)_ |
| `JWT_EXPIRATION_MS` | Token expiry in milliseconds | `900000` (15 min) |

---

## Running Locally

### Prerequisites

- Java 17+
- Apache Maven 3.8+
- Gmail account with **App Password** enabled (2FA required)
- Google Cloud Service Account JSON key file

### Setup Steps

**1. Clone the repository:**
```bash
git clone https://github.com/your-org/vaidhyashala.git
cd Vaidhyashala/Backend
```

**2. Place your Service Account key:**
```
Backend/vaidhyashala-key.json
```

**3. Set environment variables (PowerShell):**
```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = "d:\Vaidhyashala\Backend\vaidhyashala-key.json"
$env:SPRING_PROFILES_ACTIVE = "local"
$env:MAIL_USERNAME = "yourapp@gmail.com"
$env:MAIL_PASSWORD = "xxxx xxxx xxxx xxxx"
```

**4. Run the application:**
```powershell
& "C:\Program Files\Apache\Maven\bin\mvn.cmd" spring-boot:run
```

**5. Verify startup:**
```
Started BackendApplication in ~20 seconds
Tomcat started on port 8080
SEED: Found existing provider user with ID: 8c30d954-dd77-4af9-8056-c8e1fa0e0eb5
```

### Local Endpoints

| Tool | URL |
|------|-----|
| **API Base** | `http://localhost:8080/api/v1` |
| **Swagger UI** | `http://localhost:8080/swagger-ui/index.html` |
| **H2 Console** | `http://localhost:8080/h2-console` |

### H2 Console Connection Settings
```
JDBC URL:  jdbc:h2:file:./data/vaidhyashala
Username:  sa
Password:  (leave empty)
```

---

## Running on GCP

**1. Activate GCP profile:**
```bash
export SPRING_PROFILES_ACTIVE=gcp
```

**2. Set required environment variables** (DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, MAIL_*, GOOGLE_* etc.)

**3. Run:**
```bash
mvn spring-boot:run
```

> For Cloud Run or GKE, set environment variables via the GCP Console or your deployment manifest.

---

## Postman Testing Guide

### Step 1 — Register a Customer
`POST http://localhost:8080/api/v1/auth/register`
```json
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "email": "rahul@gmail.com",
  "password": "SecurePass@123",
  "role": "CUSTOMER",
  "phone": "9876543210",
  "dateOfBirth": "1995-06-15",
  "gender": "MALE"
}
```

### Step 2 — Login as Customer
`POST http://localhost:8080/api/v1/auth/login`
```json
{
  "email": "rahul@gmail.com",
  "password": "SecurePass@123"
}
```
> Copy the `token` from the response.

### Step 3 — Book an Appointment
`POST http://localhost:8080/api/v1/appointments`  
Header: `Authorization: Bearer <your_token>`
```json
{
  "providerId": "8c30d954-dd77-4af9-8056-c8e1fa0e0eb5",
  "startTime": "2026-07-05T10:00:00",
  "endTime": "2026-07-05T10:30:00",
  "description": "Initial wellness consultation"
}
```
> 📌 The initial appointment response returns status `PENDING` with `meetLink` and `googleCalendarEventId` set to `null`. Proceed to checkout and payment verification to confirm the booking.

### Step 4 — Checkout Appointment Payment
`POST http://localhost:8080/api/v1/payments/checkout`
Header: `Authorization: Bearer <customer_token>`
```json
{
  "appointmentId": "<appointment_id>"
}
```

### Step 5 — Verify Payment
`POST http://localhost:8080/api/v1/payments/verify`
Header: `Authorization: Bearer <customer_token>`
```json
{
  "paymentId": "<payment_id>",
  "transactionId": "mock-tx-123"
}
```
> ✅ On verification success, the appointment status changes to `CONFIRMED`, generating the Google Calendar event and dispatching confirmation emails.

### Step 6 — View Your Appointments (Customer)
`GET http://localhost:8080/api/v1/appointments/me`  
Header: `Authorization: Bearer <customer_token>`

### Step 7 — View All Bookings (Provider)
`GET http://localhost:8080/api/v1/appointments`  
Header: `Authorization: Bearer <provider_token>`

---

## Pre-Seeded Provider

On every startup, the application checks for and seeds a default provider user:

| Field | Value |
|-------|-------|
| **Email** | `mssreeram65@gmail.com` |
| **Role** | `PROVIDER` |
| **ID** | `8c30d954-dd77-4af9-8056-c8e1fa0e0eb5` |
| **Status** | `ACTIVE` |

Use this `providerId` directly in the `Book Appointment` request body without needing to register a provider manually.

---

## Key Design Decisions

- **Duplicate email prevention**: Registration rejects any email already in the `users` table with `409 Conflict`
- **Slot conflict detection**: Overlapping appointment times for the same provider are blocked at booking
- **Deferred booking activation via payment**: Appointments are created as `PENDING` without triggering GCal integrations, Meet link creation, or email dispatches. These integrations are asynchronously executed upon successful payment verification.
- **Unified doctor configuration**: Updating profile attributes, uploading a profile picture, and synchronizing slot configurations (including deactivating slots missing from the request) are handled in a single PUT request.
- **Meet link graceful fallback**: If Google Meet generation fails (e.g., personal account restrictions), the event is still created without the Meet link — emails gracefully show a "check your calendar" message instead
- **Role-filtered responses**: `AppointmentResponseDto` uses `@JsonInclude(NON_NULL)` — the `customer` field is `null` for customer responses and populated only for provider responses
- **Async emails**: Confirmation emails use `@Async` so they never block the payment verification API response
- **Persistent local DB**: Uses H2 file-based database (`./data/vaidhyashala`) instead of in-memory, so data survives server restarts during development

