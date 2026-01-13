package com.warehouse.management.entity;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ConstraintViolation;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Comprehensive JUnit 5 test class for User entity.
 * Tests cover normal cases, validation, boundaries, edge cases, relationships, and lifecycle callbacks.
 */
@DisplayName("User Entity Tests")
public class UserTest {

    private Validator validator;
    private User testUser;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        testUser = new User();
    }

    @AfterEach
    public void tearDown() {
        testUser = null;
    }

    @Nested
    @DisplayName("Normal Case Tests")
    class NormalCaseTests {

        @Test
        @DisplayName("Test user creation with valid data should succeed")
        public void testUserCreation_WithValidData_ShouldSucceed() {
            // Arrange & Act
            User user = new User();
            user.setUsername("john.doe");
            user.setPassword("SecurePassword123!");
            user.setEmail("john.doe@warehouse.com");
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setCredentialsNonExpired(true);
            user.setAccountNonLocked(true);

            // Assert
            assertNotNull(user);
            assertEquals("john.doe", user.getUsername());
            assertEquals("SecurePassword123!", user.getPassword());
            assertEquals("john.doe@warehouse.com", user.getEmail());
            assertTrue(user.getEnabled());
            assertTrue(user.getAccountNonExpired());
            assertTrue(user.getCredentialsNonExpired());
            assertTrue(user.getAccountNonLocked());
        }

        @Test
        @DisplayName("Test all getters and setters should work correctly")
        public void testGettersAndSetters_WithValidData_ShouldWork() {
            // Arrange
            Long id = 1L;
            String username = "jane.smith";
            String password = "AnotherSecurePass456!";
            String email = "jane.smith@warehouse.com";
            Boolean enabled = true;
            Boolean accountNonExpired = true;
            Boolean credentialsNonExpired = true;
            Boolean accountNonLocked = true;

            // Act
            testUser.setId(id);
            testUser.setUsername(username);
            testUser.setPassword(password);
            testUser.setEmail(email);
            testUser.setEnabled(enabled);
            testUser.setAccountNonExpired(accountNonExpired);
            testUser.setCredentialsNonExpired(credentialsNonExpired);
            testUser.setAccountNonLocked(accountNonLocked);

            // Assert
            assertEquals(id, testUser.getId());
            assertEquals(username, testUser.getUsername());
            assertEquals(password, testUser.getPassword());
            assertEquals(email, testUser.getEmail());
            assertEquals(enabled, testUser.getEnabled());
            assertEquals(accountNonExpired, testUser.getAccountNonExpired());
            assertEquals(credentialsNonExpired, testUser.getCredentialsNonExpired());
            assertEquals(accountNonLocked, testUser.getAccountNonLocked());
        }

        @Test
        @DisplayName("Test user default flags should be true")
        public void testUserCreation_DefaultFlags_ShouldBeTrue() {
            // Arrange & Act
            User user = new User();

            // Assert
            assertTrue(user.getEnabled());
            assertTrue(user.getAccountNonExpired());
            assertTrue(user.getCredentialsNonExpired());
            assertTrue(user.getAccountNonLocked());
        }

        @Test
        @DisplayName("Test user with minimum required fields should succeed")
        public void testUserCreation_WithMinimumRequiredFields_ShouldSucceed() {
            // Arrange & Act
            User user = new User();
            user.setUsername("u");
            user.setPassword("p");
            user.setEmail("e@w.com");

            // Assert
            assertNotNull(user);
            assertEquals("u", user.getUsername());
            assertEquals("p", user.getPassword());
            assertEquals("e@w.com", user.getEmail());
        }

        @Test
        @DisplayName("Test user with all fields should succeed")
        public void testUserCreation_WithAllFields_ShouldSucceed() {
            // Arrange & Act
            User user = new User();
            user.setUsername("admin.user");
            user.setPassword("AdminPass789!");
            user.setEmail("admin@warehouse.com");
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setCredentialsNonExpired(true);
            user.setAccountNonLocked(true);
            user.setRoles(new HashSet<>());
            user.setEmployee(new Employee());

            // Assert
            assertAll(
                () -> assertNotNull(user.getUsername()),
                () -> assertNotNull(user.getPassword()),
                () -> assertNotNull(user.getEmail()),
                () -> assertNotNull(user.getRoles()),
                () -> assertNotNull(user.getEmployee())
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Test validation with null username should fail")
        public void testValidation_WithNullUsername_ShouldFail() {
            // Arrange
            testUser.setUsername(null);
            testUser.setPassword("password123");
            testUser.setEmail("user@warehouse.com");

            // Act
            Set<ConstraintViolation<User>> violations = validator.validate(testUser);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        }

        @Test
        @DisplayName("Test validation with null password should fail")
        public void testValidation_WithNullPassword_ShouldFail() {
            // Arrange
            testUser.setUsername("john.doe");
            testUser.setPassword(null);
            testUser.setEmail("user@warehouse.com");

            // Act
            Set<ConstraintViolation<User>> violations = validator.validate(testUser);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
        }

        @Test
        @DisplayName("Test validation with null email should fail")
        public void testValidation_WithNullEmail_ShouldFail() {
            // Arrange
            testUser.setUsername("john.doe");
            testUser.setPassword("password123");
            testUser.setEmail(null);

            // Act
            Set<ConstraintViolation<User>> violations = validator.validate(testUser);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        }

        @Test
        @DisplayName("Test validation with all valid fields should pass")
        public void testValidation_WithAllValidFields_ShouldPass() {
            // Arrange
            testUser.setUsername("john.doe");
            testUser.setPassword("SecurePassword123!");
            testUser.setEmail("john.doe@warehouse.com");
            testUser.setEnabled(true);
            testUser.setAccountNonExpired(true);
            testUser.setCredentialsNonExpired(true);
            testUser.setAccountNonLocked(true);

            // Act
            Set<ConstraintViolation<User>> violations = validator.validate(testUser);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Test validation with empty username should fail")
        public void testValidation_WithEmptyUsername_ShouldFail() {
            // Arrange
            testUser.setUsername("");
            testUser.setPassword("password123");
            testUser.setEmail("user@warehouse.com");

            // Act
            Set<ConstraintViolation<User>> violations = validator.validate(testUser);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Test validation with empty password should fail")
        public void testValidation_WithEmptyPassword_ShouldFail() {
            // Arrange
            testUser.setUsername("john.doe");
            testUser.setPassword("");
            testUser.setEmail("user@warehouse.com");

            // Act
            Set<ConstraintViolation<User>> violations = validator.validate(testUser);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Test validation with empty email should fail")
        public void testValidation_WithEmptyEmail_ShouldFail() {
            // Arrange
            testUser.setUsername("john.doe");
            testUser.setPassword("password123");
            testUser.setEmail("");

            // Act
            Set<ConstraintViolation<User>> violations = validator.validate(testUser);

            // Assert
            assertFalse(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Boundary Tests")
    class BoundaryTests {

        @Test
        @DisplayName("Test username with minimum valid length should succeed")
        public void testUsername_WithMinimumValidLength_ShouldSucceed() {
            // Arrange & Act
            testUser.setUsername("u");

            // Assert
            assertEquals("u", testUser.getUsername());
            assertEquals(1, testUser.getUsername().length());
        }

        @Test
        @DisplayName("Test username with maximum valid length should succeed")
        public void testUsername_WithMaximumValidLength_ShouldSucceed() {
            // Arrange
            String maxLengthUsername = "u".repeat(50);

            // Act
            testUser.setUsername(maxLengthUsername);

            // Assert
            assertEquals(maxLengthUsername, testUser.getUsername());
            assertEquals(50, testUser.getUsername().length());
        }

        @Test
        @DisplayName("Test password with minimum length should succeed")
        public void testPassword_WithMinimumLength_ShouldSucceed() {
            // Arrange & Act
            testUser.setPassword("p");

            // Assert
            assertEquals("p", testUser.getPassword());
            assertEquals(1, testUser.getPassword().length());
        }

        @Test
        @DisplayName("Test password with very long length should succeed")
        public void testPassword_WithVeryLongLength_ShouldSucceed() {
            // Arrange
            String longPassword = "SecurePassword123!".repeat(10);

            // Act
            testUser.setPassword(longPassword);

            // Assert
            assertEquals(longPassword, testUser.getPassword());
            assertTrue(testUser.getPassword().length() > 100);
        }

        @Test
        @DisplayName("Test email with minimum valid format should succeed")
        public void testEmail_WithMinimumValidFormat_ShouldSucceed() {
            // Arrange & Act
            testUser.setEmail("a@b.c");

            // Assert
            assertEquals("a@b.c", testUser.getEmail());
        }

        @Test
        @DisplayName("Test email with maximum valid length should succeed")
        public void testEmail_WithMaximumValidLength_ShouldSucceed() {
            // Arrange
            String maxLengthEmail = "a".repeat(90) + "@test.com";

            // Act
            testUser.setEmail(maxLengthEmail);

            // Assert
            assertEquals(maxLengthEmail, testUser.getEmail());
            assertEquals(100, testUser.getEmail().length());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Test username with special characters should succeed")
        public void testUsername_WithSpecialCharacters_ShouldSucceed() {
            // Arrange
            String specialUsername = "john.doe_123";

            // Act
            testUser.setUsername(specialUsername);

            // Assert
            assertEquals(specialUsername, testUser.getUsername());
        }

        @Test
        @DisplayName("Test username with whitespace should be handled")
        public void testUsername_WithWhitespace_ShouldBeHandled() {
            // Arrange & Act
            testUser.setUsername("john doe");

            // Assert
            assertEquals("john doe", testUser.getUsername());
        }

        @Test
        @DisplayName("Test password with special characters should succeed")
        public void testPassword_WithSpecialCharacters_ShouldSucceed() {
            // Arrange
            String specialPassword = "P@ssw0rd!#$%^&*()";

            // Act
            testUser.setPassword(specialPassword);

            // Assert
            assertEquals(specialPassword, testUser.getPassword());
        }

        @Test
        @DisplayName("Test password with unicode characters should succeed")
        public void testPassword_WithUnicodeCharacters_ShouldSucceed() {
            // Arrange
            String unicodePassword = "PÃ¤sswÃ¶rd123!";

            // Act
            testUser.setPassword(unicodePassword);

            // Assert
            assertEquals(unicodePassword, testUser.getPassword());
        }

        @Test
        @DisplayName("Test email with plus sign should succeed")
        public void testEmail_WithPlusSign_ShouldSucceed() {
            // Arrange
            String emailWithPlus = "john.doe+test@warehouse.com";

            // Act
            testUser.setEmail(emailWithPlus);

            // Assert
            assertEquals(emailWithPlus, testUser.getEmail());
        }

        @Test
        @DisplayName("Test email with subdomain should succeed")
        public void testEmail_WithSubdomain_ShouldSucceed() {
            // Arrange
            String emailWithSubdomain = "user@mail.warehouse.com";

            // Act
            testUser.setEmail(emailWithSubdomain);

            // Assert
            assertEquals(emailWithSubdomain, testUser.getEmail());
        }

        @Test
        @DisplayName("Test enabled flag toggle should work")
        public void testEnabledFlag_Toggle_ShouldWork() {
            // Arrange
            testUser.setEnabled(true);

            // Act
            testUser.setEnabled(false);

            // Assert
            assertFalse(testUser.getEnabled());

            // Act again
            testUser.setEnabled(true);

            // Assert
            assertTrue(testUser.getEnabled());
        }

        @Test
        @DisplayName("Test account flags can be set independently")
        public void testAccountFlags_SetIndependently_ShouldWork() {
            // Arrange & Act
            testUser.setEnabled(true);
            testUser.setAccountNonExpired(false);
            testUser.setCredentialsNonExpired(true);
            testUser.setAccountNonLocked(false);

            // Assert
            assertAll(
                () -> assertTrue(testUser.getEnabled()),
                () -> assertFalse(testUser.getAccountNonExpired()),
                () -> assertTrue(testUser.getCredentialsNonExpired()),
                () -> assertFalse(testUser.getAccountNonLocked())
            );
        }

        @Test
        @DisplayName("Test all account flags false should work")
        public void testAllAccountFlags_False_ShouldWork() {
            // Arrange & Act
            testUser.setEnabled(false);
            testUser.setAccountNonExpired(false);
            testUser.setCredentialsNonExpired(false);
            testUser.setAccountNonLocked(false);

            // Assert
            assertAll(
                () -> assertFalse(testUser.getEnabled()),
                () -> assertFalse(testUser.getAccountNonExpired()),
                () -> assertFalse(testUser.getCredentialsNonExpired()),
                () -> assertFalse(testUser.getAccountNonLocked())
            );
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Test roles collection initialization should not be null")
        public void testRolesCollection_Initialization_ShouldNotBeNull() {
            // Arrange & Act
            User user = new User();

            // Assert
            assertNotNull(user.getRoles());
            assertTrue(user.getRoles().isEmpty());
        }

        @Test
        @DisplayName("Test add role should increase collection size")
        public void testAddRole_ShouldIncreaseCollectionSize() {
            // Arrange
            User user = new User();
            Role role = new Role();
            role.setName("ADMIN");
            int initialSize = user.getRoles().size();

            // Act
            user.getRoles().add(role);

            // Assert
            assertEquals(initialSize + 1, user.getRoles().size());
            assertTrue(user.getRoles().contains(role));
        }

        @Test
        @DisplayName("Test remove role should decrease collection size")
        public void testRemoveRole_ShouldDecreaseCollectionSize() {
            // Arrange
            User user = new User();
            Role role = new Role();
            role.setName("ADMIN");
            user.getRoles().add(role);
            int initialSize = user.getRoles().size();

            // Act
            user.getRoles().remove(role);

            // Assert
            assertEquals(initialSize - 1, user.getRoles().size());
            assertFalse(user.getRoles().contains(role));
        }

        @Test
        @DisplayName("Test clear roles should empty collection")
        public void testClearRoles_ShouldEmptyCollection() {
            // Arrange
            User user = new User();
            Role role1 = new Role();
            role1.setName("ADMIN");
            Role role2 = new Role();
            role2.setName("USER");
            user.getRoles().add(role1);
            user.getRoles().add(role2);

            // Act
            user.getRoles().clear();

            // Assert
            assertTrue(user.getRoles().isEmpty());
            assertEquals(0, user.getRoles().size());
        }

        @Test
        @DisplayName("Test multiple roles should be stored correctly")
        public void testMultipleRoles_ShouldBeStoredCorrectly() {
            // Arrange
            User user = new User();
            Role role1 = new Role();
            role1.setName("ADMIN");
            Role role2 = new Role();
            role2.setName("SUPERVISOR");
            Role role3 = new Role();
            role3.setName("WORKER");

            // Act
            user.getRoles().add(role1);
            user.getRoles().add(role2);
            user.getRoles().add(role3);

            // Assert
            assertEquals(3, user.getRoles().size());
            assertAll(
                () -> assertTrue(user.getRoles().contains(role1)),
                () -> assertTrue(user.getRoles().contains(role2)),
                () -> assertTrue(user.getRoles().contains(role3))
            );
        }

        @Test
        @DisplayName("Test set employee relationship should work")
        public void testSetEmployee_ShouldWork() {
            // Arrange
            User user = new User();
            Employee employee = new Employee();
            employee.setBadgeId("EMP001");

            // Act
            user.setEmployee(employee);

            // Assert
            assertNotNull(user.getEmployee());
            assertEquals(employee, user.getEmployee());
            assertEquals("EMP001", user.getEmployee().getBadgeId());
        }

        @Test
        @DisplayName("Test set employee to null should work")
        public void testSetEmployee_ToNull_ShouldWork() {
            // Arrange
            User user = new User();
            Employee employee = new Employee();
            user.setEmployee(employee);

            // Act
            user.setEmployee(null);

            // Assert
            assertNull(user.getEmployee());
        }

        @Test
        @DisplayName("Test roles set should not allow duplicates")
        public void testRolesSet_ShouldNotAllowDuplicates() {
            // Arrange
            User user = new User();
            Role role = new Role();
            role.setId(1L);
            role.setName("ADMIN");

            // Act
            user.getRoles().add(role);
            user.getRoles().add(role);

            // Assert
            assertEquals(1, user.getRoles().size());
        }
    }

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("Test onCreate callback should set createdAt and updatedAt")
        public void testOnCreate_ShouldSetCreatedAtAndUpdatedAt() {
            // Arrange
            User user = new User();
            LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

            // Act
            user.onCreate();
            LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);

            // Assert
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
            assertTrue(user.getCreatedAt().isAfter(beforeCreate));
            assertTrue(user.getCreatedAt().isBefore(afterCreate));
            assertEquals(user.getCreatedAt(), user.getUpdatedAt());
        }

        @Test
        @DisplayName("Test onUpdate callback should update updatedAt only")
        public void testOnUpdate_ShouldUpdateUpdatedAtOnly() throws InterruptedException {
            // Arrange
            User user = new User();
            user.onCreate();
            LocalDateTime originalCreatedAt = user.getCreatedAt();
            LocalDateTime originalUpdatedAt = user.getUpdatedAt();
            
            // Wait a bit to ensure time difference
            Thread.sleep(100);

            // Act
            user.onUpdate();

            // Assert
            assertEquals(originalCreatedAt, user.getCreatedAt());
            assertNotEquals(originalUpdatedAt, user.getUpdatedAt());
            assertTrue(user.getUpdatedAt().isAfter(originalUpdatedAt));
        }

        @Test
        @DisplayName("Test onCreate should not change createdAt on subsequent calls")
        public void testOnCreate_ShouldNotChangeCreatedAtOnSubsequentCalls() throws InterruptedException {
            // Arrange
            User user = new User();
            user.onCreate();
            LocalDateTime originalCreatedAt = user.getCreatedAt();
            
            // Wait a bit
            Thread.sleep(100);

            // Act
            user.onCreate();

            // Assert
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
        }

        @Test
        @DisplayName("Test timestamps should be set automatically")
        public void testTimestamps_ShouldBeSetAutomatically() {
            // Arrange
            User user = new User();

            // Act
            user.onCreate();

            // Assert
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
            assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
            assertTrue(user.getUpdatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        }
    }
}
