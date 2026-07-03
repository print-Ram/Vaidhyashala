import dotenv from "dotenv";

dotenv.config();

const API_ENDPOINT = process.env.API_ENDPOINT;

const VERSION = process.env.VERSION;

const API_VERSION = `${API_ENDPOINT}/${VERSION}`;

const AUTH_ENDPOINT = `${API_VERSION}/auth`;

export const REGISTRATION_ENDPOINT = `${AUTH_ENDPOINT}/register`;
export const LOGIN_ENDPOINT = `${AUTH_ENDPOINT}/login`;

const CUSTOMER_ENDPOINT = `${API_VERSION}/customers/me/profile`;

const APPOINTMENT_ENDPOINT = `${API_VERSION}/appointments`;
const MY_APPOINTMENT_ENDPOINT = `${API_VERSION}/appointments/me`;
const PROVIDERS_APPOINTMENT_ENDPOINT = `${API_VERSION}/appointments/providers`;
