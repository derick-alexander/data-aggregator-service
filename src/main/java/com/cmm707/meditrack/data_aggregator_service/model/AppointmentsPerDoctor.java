package com.cmm707.meditrack.data_aggregator_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentsPerDoctor {
    private String doctorId;
    private String doctorName;
    private int appointmentCount;
}
