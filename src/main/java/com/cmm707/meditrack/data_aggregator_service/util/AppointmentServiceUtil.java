package com.cmm707.meditrack.data_aggregator_service.util;

import com.cmm707.meditrack.data_aggregator_service.model.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentServiceUtil {
    private final RestTemplate restTemplate;
    private static final String APPOINTMENTS_API_URL = "http://35.244.210.229/api/appointment-management-service/appointments/all";

    /**
     * Fetch all appointments from the Appointment Management Service.
     *
     * @return List of Appointment objects.
     */
    public List<Appointment> getAllAppointments() {
        log.info("Fetching all appointments from {}", APPOINTMENTS_API_URL);

        try {
            // Make GET request and map response to Appointment[]
            Appointment[] appointmentsArray = restTemplate.getForObject(APPOINTMENTS_API_URL, Appointment[].class);

            if (appointmentsArray != null) {
                log.info("Successfully fetched {} appointments.", appointmentsArray.length);
                return Arrays.asList(appointmentsArray);
            } else {
                log.warn("No appointments found.");
                return List.of();
            }
        } catch (Exception ex) {
            log.error("Error fetching appointments from Appointment Management Service: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch appointments", ex);
        }
    }
}
