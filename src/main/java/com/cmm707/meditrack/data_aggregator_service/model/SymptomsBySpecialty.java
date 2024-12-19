package com.cmm707.meditrack.data_aggregator_service.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomsBySpecialty {
    private String specialty;
    private String symptom;
    private int occurrenceCount;
}
