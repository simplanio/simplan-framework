package com.intuit.data.simplan.logging.metrics;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;

public class MeterRegHolder implements Serializable {
    private static final long serialVersionUID = -7294179636551134519L;

    private static final Logger LOG = LoggerFactory.getLogger(MeterRegHolder.class);
    private static final MeterRegHolderInt delegate = new MeterRegHolderInt();
    private final Duration reportingInterval;

    public MeterRegHolder() {
        this(Duration.ofSeconds(5));
    }

    public MeterRegHolder(Duration reportingInterval) {
        this.reportingInterval = reportingInterval;
        // delegate.configure(proxyUri, reportingInterval, clock);
    }

    /**
     * Return the Meter Registry instance that this system is wrapping. This instance is equivalent
     * to an otherwise available global registry.
     *
     * @return the meter registry.
     */
    public MeterRegistry registry() {
        delegate.configure(reportingInterval, Clock.SYSTEM);
        return delegate.reg;
    }

    private static class MeterRegHolderInt implements Serializable {
        private static final long serialVersionUID = -1236019577013438679L;

        private transient volatile MeterRegistry reg;
        private Duration reportingInterval;
        private transient boolean configured;

        public MeterRegHolderInt() {
        }

        public synchronized void configure(Duration reportingInterval, Clock clock) {
            if (!configured) {
                LOG.info(
                        "Configuring MeterRegHolderInt with reportingInterval={}",
                        reportingInterval);
                this.reportingInterval = reportingInterval;
                makeWave(clock);
                SystemMetricsForwarder systemMetricsForwarder = new SystemMetricsForwarder(reg);
                systemMetricsForwarder.enableMeterHistogram();
                systemMetricsForwarder.bindSystemMetricsToRegistry();
                LOG.info("Configuring MeterRegHolderInt has been completed");
                configured = true;
            }
        }

        // This currently pickles a copy of RegHolder in it as a refrenrece,
        private void makeWave(Clock clock) {
            reg = new StatsdMeterRegistry(new StepStatsdConf(reportingInterval), clock);
        }
    }

    /**
     * To-do use proxyuri from MetricsConf The problem right now is sdk versions <= 1.0.3 need tcp
     * uri and >= 1.0.4 require statsd UDP uri Ignoring the proxyuri for now. Default is localhost,
     * which works for now.
     */
    private static class StepStatsdConf implements StatsdConfig {
        private final Duration reportingInterval;

        public StepStatsdConf(Duration reportingInterval) {
            LOG.info("Configuring statsd reporter with report interval: {}", reportingInterval);
            this.reportingInterval = reportingInterval;
        }

        @Override
        public String get(String key) {
            return null;
        }

        @Override
        public StatsdFlavor flavor() {
            return StatsdFlavor.TELEGRAF;
        }

        @Override
        public String prefix() {
            return null;
        }

        @Override
        public Duration step() {
            return reportingInterval;
        }

        @Override
        public Duration pollingFrequency() {
            return reportingInterval;
        }
    }

    private static class StepLoggingConf implements LoggingRegistryConfig {
        private final Duration reportingInterval;

        public StepLoggingConf(Duration reportingInterval) {
            LOG.info("Configuring logging reporter with report interval: {}", reportingInterval);
            this.reportingInterval = reportingInterval;
        }

        @Override
        public String get(String s) {
            return null;
        }

        @Override
        public Duration step() {
            return reportingInterval;
        }
    }
}
