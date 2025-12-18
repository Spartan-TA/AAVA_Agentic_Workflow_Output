package com.warehouse.ems;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EmsApplicationTest {

    @Test
    public void contextLoads() {
        // Verifies that the Spring application context loads successfully
        assertTrue(true);
    }

    @Test
    public void mainMethodRuns() {
        // Verifies that the main method can be invoked without errors
        assertDoesNotThrow(() -> EmsApplication.main(new String[]{}));
    }
}