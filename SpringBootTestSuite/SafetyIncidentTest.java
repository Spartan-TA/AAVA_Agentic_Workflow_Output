package com.warehouse.employee;

import com.warehouse.employee.model.SafetyIncident;
import com.warehouse.employee.model.Employee;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class SafetyIncidentTest {
    @Test
    void testConstructorAndGetters() {
        Employee reporter = new Employee();
        HashSet<Employee> involved = new HashSet<>();
        involved.add(reporter);
        LocalDateTime incidentDate = LocalDateTime.of(2024, 6, 1, 10, 0);
        LocalDateTime reportedAt = LocalDateTime.of(2024, 6, 1, 11, 0);
        LocalDateTime resolvedAt = LocalDateTime.of(2024, 6, 2, 12, 0);
        SafetyIncident incident = new SafetyIncident(1L, reporter, incidentDate, "HIGH", "Warehouse A", "Forklift accident", involved, "CLOSED", reportedAt, resolvedAt);
        assertEquals(1L, incident.getId());
        assertEquals(reporter, incident.getReportedBy());
        assertEquals(incidentDate, incident.getIncidentDate());
        assertEquals("HIGH", incident.getSeverity());
        assertEquals("Warehouse A", incident.getLocation());
        assertEquals("Forklift accident", incident.getDescription());
        assertEquals(involved, incident.getInvolvedEmployees());
        assertEquals("CLOSED", incident.getStatus());
        assertEquals(reportedAt, incident.getReportedAt());
        assertEquals(resolvedAt, incident.getResolvedAt());
    }

    @Test
    void testDefaultValues() {
        SafetyIncident incident = new SafetyIncident();
        assertNull(incident.getId());
        assertNull(incident.getReportedBy());
        assertNull(incident.getIncidentDate());
        assertNull(incident.getSeverity());
        assertNull(incident.getLocation());
        assertNull(incident.getDescription());
        assertNull(incident.getInvolvedEmployees());
        assertEquals("OPEN", incident.getStatus());
        assertNotNull(incident.getReportedAt());
        assertNull(incident.getResolvedAt());
    }

    @Test
    void testSetters() {
        SafetyIncident incident = new SafetyIncident();
        Employee reporter = new Employee();
        HashSet<Employee> involved = new HashSet<>();
        involved.add(reporter);
        LocalDateTime incidentDate = LocalDateTime.of(2024, 7, 1, 10, 0);
        LocalDateTime reportedAt = LocalDateTime.of(2024, 7, 1, 11, 0);
        LocalDateTime resolvedAt = LocalDateTime.of(2024, 7, 2, 12, 0);
        incident.setId(2L);
        incident.setReportedBy(reporter);
        incident.setIncidentDate(incidentDate);
        incident.setSeverity("LOW");
        incident.setLocation("Warehouse B");
        incident.setDescription("Minor spill");
        incident.setInvolvedEmployees(involved);
        incident.setStatus("RESOLVED");
        incident.setReportedAt(reportedAt);
        incident.setResolvedAt(resolvedAt);
        assertEquals(2L, incident.getId());
        assertEquals(reporter, incident.getReportedBy());
        assertEquals(incidentDate, incident.getIncidentDate());
        assertEquals("LOW", incident.getSeverity());
        assertEquals("Warehouse B", incident.getLocation());
        assertEquals("Minor spill", incident.getDescription());
        assertEquals(involved, incident.getInvolvedEmployees());
        assertEquals("RESOLVED", incident.getStatus());
        assertEquals(reportedAt, incident.getReportedAt());
        assertEquals(resolvedAt, incident.getResolvedAt());
    }

    @Test
    void testEdgeCases() {
        SafetyIncident incident = new SafetyIncident();
        incident.setStatus("");
        incident.setSeverity(null);
        incident.setLocation("");
        incident.setDescription("");
        incident.setInvolvedEmployees(new HashSet<>());
        assertEquals("", incident.getStatus());
        assertNull(incident.getSeverity());
        assertEquals("", incident.getLocation());
        assertEquals("", incident.getDescription());
        assertTrue(incident.getInvolvedEmployees().isEmpty());
    }
}
