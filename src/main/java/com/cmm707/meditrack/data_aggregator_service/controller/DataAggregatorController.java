package com.cmm707.meditrack.data_aggregator_service.controller;

import com.cmm707.meditrack.data_aggregator_service.service.DataAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing Data Aggregator Service.
 */
@RestController
@RequestMapping("/aggregate")
@RequiredArgsConstructor
public class DataAggregatorController {

    private final DataAggregatorService dataAggregatorService;

    /**
     * Endpoint to manually trigger the data aggregation process.
     */
    @GetMapping("/process")
    public ResponseEntity<String> runAggregation() {
        dataAggregatorService.aggregateData();
        return ResponseEntity.ok("Data aggregation process initiated.");
    }
}
