package com.traccar.PositionGeofence.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;


public class Calendar extends ExtendedModel {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private byte[] data;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) throws IOException, ParserException {
        CalendarBuilder builder = new CalendarBuilder();
        calendar = builder.build(new ByteArrayInputStream(data));
        this.data = data;
    }

    private net.fortuna.ical4j.model.Calendar calendar;


    @JsonIgnore
    public net.fortuna.ical4j.model.Calendar getCalendar() {
        return calendar;
    }

    public Set<Period<Instant>> findPeriods(Date date) {
        if (calendar != null) {
            var period = new Period<>(date.toInstant(), Duration.ZERO);
            return calendar.<VEvent>getComponents(CalendarComponent.VEVENT).stream()
                    .flatMap(c -> c.calculateRecurrenceSet(period).stream())
                    .map(p -> new Period<>(temporalToInstant(p.getStart()), temporalToInstant(p.getEnd())))
                    .collect(Collectors.toUnmodifiableSet());
        } else {
            return Set.of();
        }
    }

    public boolean checkMoment(Date date) {
        return !findPeriods(date).isEmpty();
    }

    private static Instant temporalToInstant(Temporal temporal) {
        if (temporal instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporal).toInstant();
        } else if (temporal instanceof OffsetDateTime) {
            return ((OffsetDateTime) temporal).toInstant();
        } else if (temporal instanceof Instant) {
            return (Instant) temporal;
        } else {
            throw new IllegalArgumentException("Unsupported Temporal type");
        }
    }

}
