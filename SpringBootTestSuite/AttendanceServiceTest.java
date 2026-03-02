package com.wems.attendance.service;

import com.wems.attendance.domain.AttendanceEvent;
import com.wems.attendance.domain.EventType;
import com.wems.attendance.domain.EventStatus;
import com.wems.attendance.dto.ClockEventDto;
import com.wems.attendance.repository.AttendanceEventRepository;
import com.wems.employee.domain.Employee;
import com.wems.employee.repository.EmployeeRepository;
import com.wems.scheduling.domain.Schedule;
import com.wems.scheduling.service.ScheduleService;
import com.wems.common.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for AttendanceService
 * Tests cover clock in/out operations