package com.wems.employee.service;

import com.wems.employee.domain.Employee;
import com.wems.employee.domain.EmploymentStatus;
import com.wems.employee.domain.Role;
import com.wems.employee.dto.EmployeeDto;
import com.wems.employee.repository.EmployeeRepository;
import com.wems.common.exception.ResourceNotFoundException;
import com.wems.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for EmployeeService
 * Tests cover all CRUD operations