package com.cmm707.meditrack.data_aggregator_service.service;

/**
 * Interface for Data Aggregator Service.
 * This service handles data aggregation from multiple sources and stores metrics in Redshift.
 */
public interface DataAggregatorService {

    /**
     * Method to aggregate data from various sources, process it,
     * and store the aggregated results into Redshift.
     */
    void aggregateData();
}
