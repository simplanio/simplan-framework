package com.intuit.data.simplan.logging.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.kafka.KafkaConsumerMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;

public class SystemMetricsForwarder {
    private final MeterRegistry registry;

    public SystemMetricsForwarder(MeterRegistry registry) {
        this.registry = registry;
    }

    public void bindSystemMetricsToRegistry() {
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new KafkaConsumerMetrics().bindTo(registry);
    }

    public void enableMeterHistogram() {

        registry.config().meterFilter(new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                if (id.getType().equals(Meter.Type.TIMER)) {
                    return DistributionStatisticConfig.builder().percentiles(0.01, 0.05, 0.25, 0.5, 0.75, 0.95, 0.99).build().merge(config);
                }
                return config;
            }
        });
    }
}
