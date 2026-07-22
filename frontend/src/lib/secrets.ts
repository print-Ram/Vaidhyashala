const API_ENDPOINT = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
const API_VERSION = `${API_ENDPOINT}/api/v1`;

const AUTH_ENDPOINT = `${API_VERSION}/auth`;

export const REGISTRATION_ENDPOINT = `${AUTH_ENDPOINT}/register`;
export const LOGIN_ENDPOINT = `${AUTH_ENDPOINT}/login`;
export const GOOGLE_LOGIN_ENDPOINT = `${AUTH_ENDPOINT}/google`;

export const CUSTOMER_ENDPOINT = `${API_VERSION}/customers/me/profile`;

export const APPOINTMENT_ENDPOINT = `${API_VERSION}/appointments`;
export const MY_APPOINTMENT_ENDPOINT = `${API_VERSION}/appointments/me`;
export const PROVIDERS_APPOINTMENT_ENDPOINT = `${API_VERSION}/appointments/providers`;

export const DOCTORS_ENDPOINT = `${API_VERSION}/doctors`;
export const DOCTOR_ENDPOINT = (id: string) => {
  return `${API_VERSION}/doctors/${id}`;
};

export const DOCTOR_PROFILE_ENDPOINT = `${API_VERSION}/doctors/me/profile`;
