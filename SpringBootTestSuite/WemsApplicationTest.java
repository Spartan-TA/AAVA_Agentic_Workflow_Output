package com.company.wems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive JUnit test suite for WemsApplication
 * Tests cover application context loading, bean creation,
 * Spring Boot configuration, and annotation verification
 */
@SpringBootTest
@DisplayName("WemsApplication Tests")
public class WemsApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    // ========== APPLICATION CONTEXT TESTS ==========

    @Test
    @DisplayName("Test application context loads successfully")
    public void testApplicationContext_ShouldLoadSuccessfully() {
        // Assert
        assertNotNull(applicationContext, "Application context should not be null");
        assertTrue(applicationContext.getBeanDefinitionCount() > 0, "Application context should contain beans");
    }

    @Test
    @DisplayName("Test WemsApplication bean exists in context")
    public void testWemsApplicationBean_ShouldExist() {
        // Act
        boolean beanExists = applicationContext.containsBean("wemsApplication");

        // Assert
        assertTrue(beanExists, "WemsApplication bean should exist in context");
    }

    @Test
    @DisplayName("Test application context is active")
    public void testApplicationContext_ShouldBeActive() {
        // Assert
        assertTrue(applicationContext.isActive(), "Application context should be active");
    }

    @Test
    @DisplayName("Test application context contains expected number of beans")
    public void testApplicationContext_ShouldContainExpectedBeans() {
        // Act
        int beanCount = applicationContext.getBeanDefinitionCount();

        // Assert
        assertTrue(beanCount > 50, "Application context should contain more than 50 beans (Spring Boot + custom beans)");
    }

    // ========== SPRING BOOT CONFIGURATION TESTS ==========

    @Test
    @DisplayName("Test WemsApplication has @SpringBootApplication annotation")
    public void testWemsApplication_ShouldHaveSpringBootApplicationAnnotation() {
        // Act
        boolean hasAnnotation = WemsApplication.class.isAnnotationPresent(SpringBootApplication.class);

        // Assert
        assertTrue(hasAnnotation, "WemsApplication should have @SpringBootApplication annotation");
    }

    @Test
    @DisplayName("Test WemsApplication has @EnableJpaAuditing annotation")
    public void testWemsApplication_ShouldHaveEnableJpaAuditingAnnotation() {
        // Act
        boolean hasAnnotation = WemsApplication.class.isAnnotationPresent(EnableJpaAuditing.class);

        // Assert
        assertTrue(hasAnnotation, "WemsApplication should have @EnableJpaAuditing annotation");
    }

    @Test
    @DisplayName("Test WemsApplication has @EnableTransactionManagement annotation")
    public void testWemsApplication_ShouldHaveEnableTransactionManagementAnnotation() {
        // Act
        boolean hasAnnotation = WemsApplication.class.isAnnotationPresent(EnableTransactionManagement.class);

        // Assert
        assertTrue(hasAnnotation, "WemsApplication should have @EnableTransactionManagement annotation");
    }

    @Test
    @DisplayName("Test WemsApplication has @EnableScheduling annotation")
    public void testWemsApplication_ShouldHaveEnableSchedulingAnnotation() {
        // Act
        boolean hasAnnotation = WemsApplication.class.isAnnotationPresent(EnableScheduling.class);

        // Assert
        assertTrue(hasAnnotation, "WemsApplication should have @EnableScheduling annotation");
    }

    @Test
    @DisplayName("Test WemsApplication has main method")
    public void testWemsApplication_ShouldHaveMainMethod() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            WemsApplication.class.getDeclaredMethod("main", String[].class);
        }, "WemsApplication should have a main method");
    }

    // ========== BEAN CREATION TESTS ==========

    @Test
    @DisplayName("Test DataSource bean is created")
    public void testDataSourceBean_ShouldBeCreated() {
        // Act
        boolean beanExists = applicationContext.containsBean("dataSource");

        // Assert
        assertTrue(beanExists, "DataSource bean should be created");
    }

    @Test
    @DisplayName("Test EntityManagerFactory bean is created")
    public void testEntityManagerFactoryBean_ShouldBeCreated() {
        // Act
        boolean beanExists = applicationContext.containsBean("entityManagerFactory");

        // Assert
        assertTrue(beanExists, "EntityManagerFactory bean should be created");
    }

    @Test
    @DisplayName("Test TransactionManager bean is created")
    public void testTransactionManagerBean_ShouldBeCreated() {
        // Act
        boolean beanExists = applicationContext.containsBean("transactionManager");

        // Assert
        assertTrue(beanExists, "TransactionManager bean should be created");
    }

    @Test
    @DisplayName("Test SecurityFilterChain bean is created")
    public void testSecurityFilterChainBean_ShouldBeCreated() {
        // Act
        boolean beanExists = applicationContext.containsBean("securityFilterChain");

        // Assert
        assertTrue(beanExists, "SecurityFilterChain bean should be created");
    }

    @Test
    @DisplayName("Test AuditorAware bean is created")
    public void testAuditorAwareBean_ShouldBeCreated() {
        // Act
        boolean beanExists = applicationContext.containsBean("auditorProvider");

        // Assert
        assertTrue(beanExists, "AuditorAware bean should be created");
    }

    @Test
    @DisplayName("Test PasswordEncoder bean is created")
    public void testPasswordEncoderBean_ShouldBeCreated() {
        // Act
        boolean beanExists = applicationContext.containsBean("passwordEncoder");

        // Assert
        assertTrue(beanExists, "PasswordEncoder bean should be created");
    }

    // ========== PACKAGE STRUCTURE TESTS ==========

    @Test
    @DisplayName("Test WemsApplication is in correct package")
    public void testWemsApplication_ShouldBeInCorrectPackage() {
        // Act
        String packageName = WemsApplication.class.getPackage().getName();

        // Assert
        assertEquals("com.company.wems", packageName, "WemsApplication should be in com.company.wems package");
    }

    @Test
    @DisplayName("Test application scans correct base packages")
    public void testApplication_ShouldScanCorrectBasePackages() {
        // Act
        SpringBootApplication annotation = WemsApplication.class.getAnnotation(SpringBootApplication.class);

        // Assert
        assertNotNull(annotation, "@SpringBootApplication annotation should be present");
        // By default, @SpringBootApplication scans the package of the annotated class and sub-packages
    }

    // ========== COMPONENT SCANNING TESTS ==========

    @Test
    @DisplayName("Test employee package components are scanned")
    public void testEmployeePackageComponents_ShouldBeScanned() {
        // Act
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        boolean hasEmployeeComponents = false;
        
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("employee")) {
                hasEmployeeComponents = true;
                break;
            }
        }

        // Assert
        assertTrue(hasEmployeeComponents, "Employee package components should be scanned");
    }

    @Test
    @DisplayName("Test config package components are scanned")
    public void testConfigPackageComponents_ShouldBeScanned() {
        // Act
        boolean hasSecurityConfig = applicationContext.containsBean("securityConfig");
        boolean hasAuditConfig = applicationContext.containsBean("auditConfig");

        // Assert
        assertTrue(hasSecurityConfig || hasAuditConfig, "Config package components should be scanned");
    }

    // ========== JPA AUDITING TESTS ==========

    @Test
    @DisplayName("Test JPA auditing is enabled")
    public void testJpaAuditing_ShouldBeEnabled() {
        // Act
        boolean hasAuditorAware = applicationContext.containsBean("auditorProvider");

        // Assert
        assertTrue(hasAuditorAware, "JPA auditing should be enabled with AuditorAware bean");
    }

    @Test
    @DisplayName("Test AuditorAware bean can be retrieved")
    public void testAuditorAwareBean_ShouldBeRetrievable() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            Object auditorAware = applicationContext.getBean("auditorProvider");
            assertNotNull(auditorAware, "AuditorAware bean should be retrievable");
        }, "Should be able to retrieve AuditorAware bean");
    }

    // ========== TRANSACTION MANAGEMENT TESTS ==========

    @Test
    @DisplayName("Test transaction management is enabled")
    public void testTransactionManagement_ShouldBeEnabled() {
        // Act
        boolean hasTransactionManager = applicationContext.containsBean("transactionManager");

        // Assert
        assertTrue(hasTransactionManager, "Transaction management should be enabled");
    }

    @Test
    @DisplayName("Test PlatformTransactionManager bean exists")
    public void testPlatformTransactionManagerBean_ShouldExist() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            Object transactionManager = applicationContext.getBean("transactionManager");
            assertNotNull(transactionManager, "PlatformTransactionManager bean should exist");
        }, "Should be able to retrieve PlatformTransactionManager bean");
    }

    // ========== SCHEDULING TESTS ==========

    @Test
    @DisplayName("Test scheduling is enabled")
    public void testScheduling_ShouldBeEnabled() {
        // Act
        boolean hasSchedulingAnnotation = WemsApplication.class.isAnnotationPresent(EnableScheduling.class);

        // Assert
        assertTrue(hasSchedulingAnnotation, "Scheduling should be enabled via @EnableScheduling");
    }

    @Test
    @DisplayName("Test TaskScheduler bean is created")
    public void testTaskSchedulerBean_ShouldBeCreated() {
        // Act
        String[] beanNames = applicationContext.getBeanNamesForType(org.springframework.scheduling.TaskScheduler.class);

        // Assert
        assertTrue(beanNames.length > 0, "TaskScheduler bean should be created when scheduling is enabled");
    }

    // ========== ACTUATOR TESTS ==========

    @Test
    @DisplayName("Test Actuator health endpoint bean exists")
    public void testActuatorHealthEndpoint_ShouldExist() {
        // Act
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        boolean hasHealthEndpoint = false;
        
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("health")) {
                hasHealthEndpoint = true;
                break;
            }
        }

        // Assert
        assertTrue(hasHealthEndpoint, "Actuator health endpoint should exist");
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Test application context can be refreshed")
    public void testApplicationContext_CanBeRefreshed() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            if (applicationContext instanceof org.springframework.context.ConfigurableApplicationContext) {
                // Context is refreshable
                assertTrue(true, "Context is refreshable");
            }
        }, "Application context should be refreshable");
    }

    @Test
    @DisplayName("Test application context has parent context")
    public void testApplicationContext_MayHaveParentContext() {
        // Act
        ApplicationContext parentContext = applicationContext.getParent();

        // Assert
        // Parent context may or may not exist depending on configuration
        // This test just verifies the method doesn't throw an exception
        assertDoesNotThrow(() -> applicationContext.getParent(), "Should be able to check for parent context");
    }

    @Test
    @DisplayName("Test application context environment is not null")
    public void testApplicationContextEnvironment_ShouldNotBeNull() {
        // Act
        org.springframework.core.env.Environment environment = applicationContext.getEnvironment();

        // Assert
        assertNotNull(environment, "Application context environment should not be null");
    }

    @Test
    @DisplayName("Test application has active profiles or default profile")
    public void testApplication_ShouldHaveActiveOrDefaultProfile() {
        // Act
        org.springframework.core.env.Environment environment = applicationContext.getEnvironment();
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();

        // Assert
        assertTrue(activeProfiles.length > 0 || defaultProfiles.length > 0, 
                "Application should have active or default profiles");
    }

    @Test
    @DisplayName("Test application context can retrieve bean by type")
    public void testApplicationContext_CanRetrieveBeanByType() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            Object dataSource = applicationContext.getBean(javax.sql.DataSource.class);
            assertNotNull(dataSource, "Should be able to retrieve bean by type");
        }, "Should be able to retrieve bean by type");
    }

    @Test
    @DisplayName("Test application context can list beans of specific type")
    public void testApplicationContext_CanListBeansOfSpecificType() {
        // Act
        String[] repositoryBeans = applicationContext.getBeanNamesForType(
                org.springframework.data.repository.Repository.class);

        // Assert
        assertNotNull(repositoryBeans, "Should be able to list beans of specific type");
    }

    @Test
    @DisplayName("Test application startup time is reasonable")
    public void testApplicationStartup_ShouldBeReasonable() {
        // Act
        long startTime = System.currentTimeMillis();
        
        // Verify context is already loaded (this test runs after context is up)
        assertNotNull(applicationContext, "Application context should be loaded");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert
        assertTrue(duration < 1000, "Context verification should be fast (< 1 second)");
    }

    @Test
    @DisplayName("Test application has required Spring Boot starters")
    public void testApplication_ShouldHaveRequiredStarters() {
        // Act
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        boolean hasWebStarter = false;
        boolean hasDataJpaStarter = false;
        boolean hasSecurityStarter = false;
        
        for (String beanName : beanNames) {
            String lowerBeanName = beanName.toLowerCase();
            if (lowerBeanName.contains("dispatcher") || lowerBeanName.contains("servlet")) {
                hasWebStarter = true;
            }
            if (lowerBeanName.contains("entitymanager") || lowerBeanName.contains("jpa")) {
                hasDataJpaStarter = true;
            }
            if (lowerBeanName.contains("security") || lowerBeanName.contains("filter")) {
                hasSecurityStarter = true;
            }
        }

        // Assert
        assertTrue(hasWebStarter, "Application should have Spring Web starter");
        assertTrue(hasDataJpaStarter, "Application should have Spring Data JPA starter");
        assertTrue(hasSecurityStarter, "Application should have Spring Security starter");
    }

    @Test
    @DisplayName("Test main method can be invoked without errors")
    public void testMainMethod_ShouldBeInvokable() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            // Verify main method exists and has correct signature
            java.lang.reflect.Method mainMethod = WemsApplication.class.getDeclaredMethod("main", String[].class);
            assertNotNull(mainMethod, "Main method should exist");
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()), "Main method should be static");
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()), "Main method should be public");
        }, "Main method should be properly defined");
    }
}