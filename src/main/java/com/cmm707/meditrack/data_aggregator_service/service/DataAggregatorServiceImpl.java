package com.cmm707.meditrack.data_aggregator_service.service;

import com.cmm707.meditrack.data_aggregator_service.model.*;
import com.cmm707.meditrack.data_aggregator_service.util.AppointmentServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for aggregating data from Appointment and Patient entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataAggregatorServiceImpl implements DataAggregatorService {

    private final AppointmentServiceUtil appointmentServiceUtil;
    private final com.cmm707.meditrack.data_aggregator_service.service.PatientServiceUtil patientServiceUtil;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void aggregateData() {
        log.info("Starting data aggregation...");

        List<Appointment> appointments = appointmentServiceUtil.getAllAppointments();
        List<Patient> patients = patientServiceUtil.getAllPatients();
        // Step 1: Aggregate Appointments Per Doctor
        List<AppointmentsPerDoctor> appointmentsPerDoctor = aggregateAppointmentsPerDoctor(appointments);
        pushAppointmentsPerDoctorToRedshift(appointmentsPerDoctor);

        // Step 2: Aggregate Appointments Over Time
        List<AppointmentsOverTime> appointmentsOverTime = aggregateAppointmentsOverTime(appointments);
        pushAppointmentsOverTimeToRedshift(appointmentsOverTime);

        // Step 3: Aggregate Symptoms by Specialty
        List<SymptomsBySpecialty> symptomsBySpecialty = aggregateSymptomsBySpecialty(patients);
        pushSymptomsBySpecialtyToRedshift(symptomsBySpecialty);

        log.info("Data aggregation completed.");
    }

    /**
     * Aggregates the number of appointments per doctor.
     */
    private List<AppointmentsPerDoctor> aggregateAppointmentsPerDoctor(List<Appointment> appointments) {
        log.info("Aggregating appointments per doctor...");

        Map<String, Long> doctorAppointments = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getDoctorId, Collectors.counting()));

        List<AppointmentsPerDoctor> results = doctorAppointments.entrySet().stream()
                .map(entry -> {
                    AppointmentsPerDoctor record = new AppointmentsPerDoctor();
                    record.setDoctorId(entry.getKey());
                    record.setDoctorName("Doctor_" + entry.getKey()); // Replace with actual doctor name if available
                    record.setAppointmentCount(entry.getValue().intValue());
                    return record;
                }).collect(Collectors.toList());

        log.info("Aggregated {} records for appointments per doctor.", results.size());
        return results;
    }

    private void pushAppointmentsPerDoctorToRedshift(List<AppointmentsPerDoctor> data) {
        log.info("Pushing appointments per doctor to Redshift...");

        String sql = "INSERT INTO appointments_per_doctor (doctor_id, doctor_name, appointment_count, current_date_time) VALUES (?, ?, ?, ?)";
        data.forEach(record -> jdbcTemplate.update(sql, record.getDoctorId(), record.getDoctorName(), record.getAppointmentCount(), LocalDateTime.now()));

        log.info("Pushed {} records to Redshift.", data.size());
    }

    /**
     * Aggregates the frequency of appointments over time.
     */
    private List<AppointmentsOverTime> aggregateAppointmentsOverTime(List<Appointment> appointments) {
        log.info("Aggregating appointments over time...");

        Map<String, Long> appointmentsByDate = appointments.stream()
                .collect(Collectors.groupingBy(a -> a.getAppointmentDate().toLocalDate().toString(), Collectors.counting()));

        List<AppointmentsOverTime> results = appointmentsByDate.entrySet().stream()
                .map(entry -> {
                    AppointmentsOverTime record = new AppointmentsOverTime();
                    record.setPeriod(entry.getKey());
                    record.setAppointmentCount(entry.getValue().intValue());
                    return record;
                }).collect(Collectors.toList());

        log.info("Aggregated {} records for appointments over time.", results.size());
        return results;
    }

    private void pushAppointmentsOverTimeToRedshift(List<AppointmentsOverTime> data) {
        log.info("Pushing appointments over time to Redshift...");

        String sql = "INSERT INTO appointments_over_time (period, appointment_count, current_date_time) VALUES (?, ?, ?)";
        data.forEach(record -> jdbcTemplate.update(sql, record.getPeriod(), record.getAppointmentCount(), LocalDateTime.now()));

        log.info("Pushed {} records to Redshift.", data.size());
    }

    /**
     * Aggregates the frequency of symptoms by specialty.
     */
    private List<SymptomsBySpecialty> aggregateSymptomsBySpecialty(List<Patient> patients) {
        log.info("Aggregating symptoms by specialty...");

        Map<String, Map<String, Long>> specialtySymptoms = patients.stream()
                .flatMap(patient -> patient.getMedicalHistory().stream().map(symptom -> Map.entry(patient.getGender(), symptom)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.groupingBy(Map.Entry::getValue, Collectors.counting())));

        List<SymptomsBySpecialty> results = specialtySymptoms.entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                        .map(innerEntry -> {
                            SymptomsBySpecialty record = new SymptomsBySpecialty();
                            record.setSpecialty(entry.getKey());
                            record.setSymptom(innerEntry.getKey());
                            record.setOccurrenceCount(innerEntry.getValue().intValue());
                            return record;
                        })).collect(Collectors.toList());

        log.info("Aggregated {} records for symptoms by specialty.", results.size());
        return results;
    }

    private void pushSymptomsBySpecialtyToRedshift(List<SymptomsBySpecialty> data) {
        log.info("Pushing symptoms by specialty to Redshift...");

        String sql = "INSERT INTO symptoms_by_specialty (specialty, symptom, occurrence_count, current_date_time) VALUES (?, ?, ?, ?)";
        data.forEach(record -> jdbcTemplate.update(sql, record.getSpecialty(), record.getSymptom(), record.getOccurrenceCount(), LocalDateTime.now()));

        log.info("Pushed {} records to Redshift.", data.size());
    }
}
