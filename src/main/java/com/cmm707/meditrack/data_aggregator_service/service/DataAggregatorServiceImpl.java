package com.cmm707.meditrack.data_aggregator_service.service;

import com.cmm707.meditrack.data_aggregator_service.model.AppointmentsOverTime;
import com.cmm707.meditrack.data_aggregator_service.model.AppointmentsPerDoctor;
import com.cmm707.meditrack.data_aggregator_service.model.SymptomsBySpecialty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataAggregatorServiceImpl implements DataAggregatorService {

    private final MongoTemplate mongoTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void aggregateData() {
        log.info("Starting data aggregation...");

        // Step 1: Aggregate Appointments Per Doctor
        List<AppointmentsPerDoctor> appointmentsPerDoctor = aggregateAppointmentsPerDoctor();
        pushAppointmentsPerDoctorToRedshift(appointmentsPerDoctor);

        // Step 2: Aggregate Appointments Over Time
        List<AppointmentsOverTime> appointmentsOverTime = aggregateAppointmentsOverTime();
        pushAppointmentsOverTimeToRedshift(appointmentsOverTime);

        // Step 3: Aggregate Symptoms by Specialty
        List<SymptomsBySpecialty> symptomsBySpecialty = aggregateSymptomsBySpecialty();
        pushSymptomsBySpecialtyToRedshift(symptomsBySpecialty);

        log.info("Data aggregation completed.");
    }

    private List<AppointmentsPerDoctor> aggregateAppointmentsPerDoctor() {
        log.info("Aggregating appointments per doctor...");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("doctorId")
                        .count().as("appointmentCount")
                        .first("doctorName").as("doctorName")
        );

        List<AppointmentsPerDoctor> results = mongoTemplate.aggregate(aggregation, "appointments", AppointmentsPerDoctor.class).getMappedResults();
        log.info("Aggregated {} records for appointments per doctor.", results.size());

        return results;
    }

    private void pushAppointmentsPerDoctorToRedshift(List<AppointmentsPerDoctor> data) {
        log.info("Pushing appointments per doctor to Redshift...");

        String sql = "INSERT INTO appointments_per_doctor (doctor_id, doctor_name, appointment_count) VALUES (?, ?, ?)";
        for (AppointmentsPerDoctor record : data) {
            jdbcTemplate.update(sql, record.getDoctorId(), record.getDoctorName(), record.getAppointmentCount());
        }

        log.info("Pushed {} records to Redshift.", data.size());
    }

    private List<AppointmentsOverTime> aggregateAppointmentsOverTime() {
        log.info("Aggregating appointments over time...");

        AggregationOperation projectDateToString = context ->
                context.getMappedObject(
                        org.bson.Document.parse("{ $project: { period: { $dateToString: { format: '%Y-%m-%d', date: '$appointmentDate' } } } }")
                );

        Aggregation aggregation = Aggregation.newAggregation(
                projectDateToString,
                Aggregation.group("period")
                        .count().as("appointmentCount")
        );

        List<AppointmentsOverTime> results = mongoTemplate.aggregate(aggregation, "appointments", AppointmentsOverTime.class).getMappedResults();
        log.info("Aggregated {} records for appointments over time.", results.size());

        return results;
    }

    private void pushAppointmentsOverTimeToRedshift(List<AppointmentsOverTime> data) {
        log.info("Pushing appointments over time to Redshift...");

        String sql = "INSERT INTO appointments_over_time (period, appointment_count) VALUES (?, ?)";
        for (AppointmentsOverTime record : data) {
            jdbcTemplate.update(sql, record.getPeriod(), record.getAppointmentCount());
        }

        log.info("Pushed {} records to Redshift.", data.size());
    }

    private List<SymptomsBySpecialty> aggregateSymptomsBySpecialty() {
        log.info("Aggregating symptoms by specialty...");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("symptoms"),
                Aggregation.group("specialty", "symptoms")
                        .count().as("occurrenceCount")
        );

        List<SymptomsBySpecialty> results = mongoTemplate.aggregate(aggregation, "patients", SymptomsBySpecialty.class).getMappedResults();
        log.info("Aggregated {} records for symptoms by specialty.", results.size());

        return results;
    }

    private void pushSymptomsBySpecialtyToRedshift(List<SymptomsBySpecialty> data) {
        log.info("Pushing symptoms by specialty to Redshift...");

        String sql = "INSERT INTO symptoms_by_specialty (specialty, symptom, occurrence_count) VALUES (?, ?, ?)";
        for (SymptomsBySpecialty record : data) {
            jdbcTemplate.update(sql, record.getSpecialty(), record.getSymptom(), record.getOccurrenceCount());
        }

        log.info("Pushed {} records to Redshift.", data.size());
    }
}
