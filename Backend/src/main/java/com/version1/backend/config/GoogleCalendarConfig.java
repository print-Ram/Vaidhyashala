package com.version1.backend.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    private static final String APPLICATION_NAME = "Vaidhyashala-Backend";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${app.google.credentials.json:#{null}}")
    private String credentialsJson;

    @Bean
    public Calendar googleCalendarService() throws IOException, GeneralSecurityException {
        final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credentials = null;

        try {
            if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
                // Load from raw JSON string (e.g. injected via GCP Secret Manager or configuration properties)
                credentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))
                ).createScoped(Collections.singleton(CalendarScopes.CALENDAR));
            } else {
                // Fall back to Application Default Credentials (ADC) e.g. GOOGLE_APPLICATION_CREDENTIALS environment variable
                credentials = GoogleCredentials.getApplicationDefault()
                        .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
            }
        } catch (Throwable t) {
            System.err.println("WARNING: Google Credentials could not be loaded. Google Calendar integration is running in mock/unauthenticated mode: " + t.getMessage());
        }

        HttpRequestInitializer requestInitializer;
        if (credentials != null) {
            requestInitializer = new HttpCredentialsAdapter(credentials);
        } else {
            requestInitializer = request -> {}; // Fallback dummy initializer for local dev/testing
        }

        return new Calendar.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
