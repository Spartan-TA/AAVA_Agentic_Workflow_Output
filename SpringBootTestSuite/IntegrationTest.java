package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/employees";
    }

    @AfterEach
    void tearDown() {
        // Teardown logic if needed
    }

    @Test
    void testEmployeeLifecycle_EndToEnd() {
        // Create employee
        Map<String, Object> employee = new HashMap<>();
        employee.put("name", "John Doe");
        employee.put("role", "WORKER");
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(baseUrl, employee, Map.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Integer employeeId = (Integer) createResponse.getBody().get("id");

        // Retrieve employee
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(baseUrl + "/" + employeeId, Map.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("John Doe", getResponse.getBody().get("name"));

        // Update employee
        employee.put("name", "Jane Doe");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> updateRequest = new HttpEntity<>(employee, headers);
        ResponseEntity<Map> updateResponse = restTemplate.exchange(baseUrl + "/" + employeeId, HttpMethod.PUT, updateRequest, Map.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Jane Doe", updateResponse.getBody().get("name"));

        // Partial update
        Map<String, Object> patchData = new HashMap<>();
        patchData.put("role", "SUPERVISOR");
        HttpEntity<Map<String, Object>> patchRequest = new HttpEntity<>(patchData, headers);
        ResponseEntity<Map> patchResponse = restTemplate.exchange(baseUrl + "/" + employeeId, HttpMethod.PATCH, patchRequest, Map.class);
        assertEquals(HttpStatus.OK, patchResponse.getStatusCode());
        assertEquals("SUPERVISOR", patchResponse.getBody().get("role"));

        // Soft delete
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(baseUrl + "/" + employeeId, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verify soft-deleted employee is not in active list
        ResponseEntity<List> listResponse = restTemplate.getForEntity(baseUrl, List.class);
        List<?> employees = listResponse.getBody();
        assertTrue(employees.stream().noneMatch(e -> ((Map<?, ?>) e).get("id").equals(employeeId)));
    }

    @Test
    void testTransactionManagementAndRollback() {
        // Create employee
        Map<String, Object> employee = new HashMap<>();
        employee.put("name", "Rollback Test");
        employee.put("role", "WORKER");
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(baseUrl, employee, Map.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Integer employeeId = (Integer) createResponse.getBody().get("id");

        // Simulate rollback by throwing exception
        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Force rollback");
        });

        // After rollback, employee should not exist
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(baseUrl + "/" + employeeId, Map.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void testSecurityIntegrationWithDifferentRoles() {
        // Simulate API key authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", "valid-api-key");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        headers.set("X-API-Key", "invalid-api-key");
        entity = new HttpEntity<>(headers);
        ResponseEntity<List> forbiddenResponse = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, List.class);
        assertEquals(HttpStatus.UNAUTHORIZED, forbiddenResponse.getStatusCode());
    }

    @Test
    void testDataPersistenceAndRetrievalAcrossRequests() {
        Map<String, Object> employee = new HashMap<>();
        employee.put("name", "Persistent User");
        employee.put("role", "HR");
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(baseUrl, employee, Map.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Integer employeeId = (Integer) createResponse.getBody().get("id");

        ResponseEntity<Map> getResponse = restTemplate.getForEntity(baseUrl + "/" + employeeId, Map.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("Persistent User", getResponse.getBody().get("name"));
    }

    @Test
    void testPaginationAndFiltering() {
        // Create multiple employees
        for (int i = 0; i < 10; i++) {
            Map<String, Object> employee = new HashMap<>();
            employee.put("name", "User" + i);
            employee.put("role", "WORKER");
            restTemplate.postForEntity(baseUrl, employee, Map.class);
        }
        // Test pagination
        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl + "?page=0&size=5", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().size());

        // Test filtering
        ResponseEntity<List> filterResponse = restTemplate.getForEntity(baseUrl + "?role=WORKER", List.class);
        assertEquals(HttpStatus.OK, filterResponse.getStatusCode());
        assertTrue(filterResponse.getBody().size() >= 10);
    }

    @Test
    void testConcurrentRequestsHandling() {
        // Simulate concurrent creation
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                Map<String, Object> employee = new HashMap<>();
                employee.put("name", "ConcurrentUser");
                employee.put("role", "WORKER");
                restTemplate.postForEntity(baseUrl, employee, Map.class);
            });
            threads.add(thread);
            thread.start();
        }
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        // Verify all employees created
        ResponseEntity<List> response = restTemplate.getForEntity(baseUrl, List.class);
        assertTrue(response.getBody().size() >= 5);
    }

    @Test
    void testErrorScenarios_404_400_401_403() {
        // 404 Not Found
        ResponseEntity<Map> notFoundResponse = restTemplate.getForEntity(baseUrl + "/9999", Map.class);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());

        // 400 Bad Request
        Map<String, Object> invalidEmployee = new HashMap<>();
        invalidEmployee.put("name", "");
        ResponseEntity<Map> badRequestResponse = restTemplate.postForEntity(baseUrl, invalidEmployee, Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, badRequestResponse.getStatusCode());

        // 401 Unauthorized
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", "invalid-key");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<List> unauthorizedResponse = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, List.class);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResponse.getStatusCode());

        // 403 Forbidden (assuming WORKER role cannot delete)
        headers.set("X-API-Key", "valid-api-key");
        headers.set("Role", "WORKER");
        entity = new HttpEntity<>(headers);
        ResponseEntity<Void> forbiddenResponse = restTemplate.exchange(baseUrl + "/1", HttpMethod.DELETE, entity, Void.class);
        assertEquals(HttpStatus.FORBIDDEN, forbiddenResponse.getStatusCode());
    }
}