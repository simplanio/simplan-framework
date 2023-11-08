package com.intuit.data.simplan.logging.metrics;

import com.intuit.data.simplan.logging.metrics.config.MetricsConfig;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;

public class MetricsUtil {
    private static transient MeterRegHolder registry;
    private static Duration reportInterval = Duration.ofSeconds(10);

    public static void init(MetricsConfig metricsConfig) {
        if (registry == null) {
            reportInterval = Duration.ofSeconds(metricsConfig.resolvedReportingInterval());
            System.out.println("Creating MeterRegHolder from with Interval +" + DurationFormatUtils.formatDurationWords(reportInterval.toMillis(), true, false));
            registry = new MeterRegHolder(reportInterval);
        }
    }

    public static synchronized MeterRegHolder getRegistry() {
        if (registry == null) {
            registry = new MeterRegHolder(reportInterval);
        }
        return registry;

    }
}