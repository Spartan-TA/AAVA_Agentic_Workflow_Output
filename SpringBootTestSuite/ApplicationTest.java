package com.warehouse.employee_mgmt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for Application.java
 * Tests Spring Boot application initialization and context loading
 * 
 * @author Automation Test Engineer
 * @version 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Main Class Tests")
public class ApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Test Case 1: Verify Spring Boot application context loads successfully
     * Normal Case: Application context should be initialized without errors
     */
    @Test
    @DisplayName("Test application context loads successfully")
    public void testApplicationContextLoads() {
        // Assert
        assertNotNull(applicationContext, "Application context should not be null");
        assertTrue(applicationContext.containsBean("application"), 
                   "Application bean should be present in context");
    }

    /**
     * Test Case 2: Verify main method executes without exceptions
     * Normal Case: Main method should start application successfully
     */
    @Test
    @DisplayName("Test main method executes without exceptions")
    public void testMainMethodExecution() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            Application.main(new String[]{});
        }, "Main method should execute without throwing exceptions");
    }

    /**
     * Test Case 3: Verify main method with null arguments
     * Edge Case: Main method should handle null arguments gracefully
     */
    @Test
    @DisplayName("Test main method with null arguments")
    public void testMainMethodWithNullArguments() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            Application.main(null);
        }, "Main method should handle null arguments without throwing exceptions");
    }

    /**
     * Test Case 4: Verify main method with empty arguments array
     * Boundary Case: Main method should handle empty arguments
     */
    @Test
    @DisplayName("Test main method with empty arguments")
    public void testMainMethodWithEmptyArguments() {
        // Arrange
        String[] emptyArgs = new String[0];
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            Application.main(emptyArgs);
        }, "Main method should handle empty arguments without throwing exceptions");
    }

    /**
     * Test Case 5: Verify main method with invalid arguments
     * Edge Case: Main method should handle invalid arguments gracefully
     */
    @Test
    @DisplayName("Test main method with invalid arguments")
    public void testMainMethodWithInvalidArguments() {
        // Arrange
        String[] invalidArgs = new String[]{"--invalid.property=value", "--another.invalid=test"};
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            Application.main(invalidArgs);
        }, "Main method should handle invalid arguments without throwing exceptions");
    }

    /**
     * Test Case 6: Verify all required beans are loaded in context
     * Normal Case: Essential beans should be present
     */
    @Test
    @DisplayName("Test essential beans are loaded")
    public void testEssentialBeansLoaded() {
        // Assert
        assertTrue(applicationContext.containsBean("employeeService"), 
                   "EmployeeService bean should be present");
        assertTrue(applicationContext.containsBean("employeeRepository"), 
                   "EmployeeRepository bean should be present");
        assertTrue(applicationContext.containsBean("employeeController"), 
                   "EmployeeController bean should be present");
    }

    /**
     * Test Case 7: Verify application context is active
     * Normal Case: Application context should be in active state
     */
    @Test
    @DisplayName("Test application context is active")
    public void testApplicationContextIsActive() {
        // Assert
        assertTrue(applicationContext.isActive(), 
                   "Application context should be active");
    }

    /**
     * Test Case 8: Verify application context contains expected bean count
     * Normal Case: Context should have minimum expected beans
     */
    @Test
    @DisplayName("Test application context bean count")
    public void testApplicationContextBeanCount() {
        // Arrange
        int minExpectedBeans = 10; // Minimum expected beans in context
        
        // Act
        int actualBeanCount = applicationContext.getBeanDefinitionCount();
        
        // Assert
        assertTrue(actualBeanCount >= minExpectedBeans, 
                   "Application context should contain at least " + minExpectedBeans + " beans");
    }

    /**
     * Test Case 9: Verify Spring Boot application name
     * Normal Case: Application name should match configuration
     */
    @Test
    @DisplayName("Test application name configuration")
    public void testApplicationName() {
        // Act
        String applicationName = applicationContext.getEnvironment()
                                                   .getProperty("spring.application.name");
        
        // Assert
        assertNotNull(applicationName, "Application name should be configured");
        assertEquals("warehouse-employee-management", applicationName, 
                     "Application name should match expected value");
    }

    /**
     * Test Case 10: Verify application profiles
     * Normal Case: Test profile should be active
     */
    @Test
    @DisplayName("Test active profiles configuration")
    public void testActiveProfiles() {
        // Act
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        
        // Assert
        assertNotNull(activeProfiles, "Active profiles should not be null");
        assertTrue(activeProfiles.length > 0, "At least one profile should be active");
    }
}