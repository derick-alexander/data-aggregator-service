package com.cmm707.meditrack.data_aggregator_service.cron;

import com.cmm707.meditrack.data_aggregator_service.service.DataAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cron job for triggering the data aggregation process.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorCron {

    private final DataAggregatorService dataAggregatorService;

    /**
     * Scheduled job to run the data aggregation process every day at midnight (12:00 AM).
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void runAggregationJob() {
        log.info("Scheduled Data Aggregation Job started.");
        try {
            dataAggregatorService.aggregateData();
            log.info("Scheduled Data Aggregation Job completed successfully.");
        } catch (Exception e) {
            log.error("Error occurred during the Scheduled Data Aggregation Job: {}", e.getMessage(), e);
        }
    }
}
