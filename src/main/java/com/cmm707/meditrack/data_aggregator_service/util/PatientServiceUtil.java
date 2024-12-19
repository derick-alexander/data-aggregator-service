package com.cmm707.meditrack.data_aggregator_service.service;

import com.cmm707.meditrack.data_aggregator_service.model.Patient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Service to fetch patients from Patient Management Service.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceUtil {

    private final RestTemplate restTemplate;

    private static final String PATIENTS_API_URL = "http://35.244.210.229/api/patient-management-service/patients";

    /**
     * Fetch all patients from the Patient Management Service.
     * @return List of Patient objects.
     */
    public List<Patient> getAllPatients() {
        log.info("Fetching all patients from {}", PATIENTS_API_URL);

        try {
            // Make GET request and map response to Patient[]
            Patient[] patientsArray = restTemplate.getForObject(PATIENTS_API_URL, Patient[].class);

            if (patientsArray != null) {
                log.info("Successfully fetched {} patients.", patientsArray.length);
                return Arrays.asList(patientsArray);
            } else {
                log.warn("No patients found.");
                return List.of();
            }
        } catch (Exception ex) {
            log.error("Error fetching patients from Patient Management Service: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch patients", ex);
        }
    }
}
