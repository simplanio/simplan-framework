package com.intuit.data.simplan.logging.domain.v2.fiedsets;

import com.intuit.data.simplan.logging.domain.JacksonAnyProperty;

import javax.swing.text.html.Option;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 26-May-2022 at 4:50 PM
 */
public class ProcessOpsEvent extends JacksonAnyProperty {
    String name;
    Instant start;
    Instant end;
    Long duration;
    String status;

    public String getName() {
        return name;
    }

    public ProcessOpsEvent setName(String name) {
        this.name = name;
        getDuration();
        return this;
    }

    public Instant getStart() {
        return start;
    }

    public ProcessOpsEvent setStart(Instant start) {
        this.start = start;
        return this;
    }

    public Instant getEnd() {
        return end;
    }

    public ProcessOpsEvent setEnd(Instant end) {
        this.end = end;
        getDuration();
        return this;
    }

    public Long getDuration() {
        if (duration != null && start != null && end != null) this.duration = Duration.between(start, end).getSeconds();
        return duration;
    }

    public ProcessOpsEvent setDuration(Long duration) {
        this.duration = duration;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ProcessOpsEvent setStatus(String status) {
        this.status = status;
        return this;
    }
}
