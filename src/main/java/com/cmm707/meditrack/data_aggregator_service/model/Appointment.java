package com.cmm707.meditrack.data_aggregator_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing appointment details stored in the database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Appointment {

    private String id;

    private String patientId;

    private String doctorId;

    private LocalDateTime appointmentDate;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
