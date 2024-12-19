package com.cmm707.meditrack.data_aggregator_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentsOverTime {
    private String period;
    private int appointmentCount;
}
