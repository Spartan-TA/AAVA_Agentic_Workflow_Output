package SpringBootTestSuite;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ApplicationStartupTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Test
    void testApplicationContextLoads_Success() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void testActuatorHealthEndpoint_ReturnsUp() {
        assertThat(healthEndpoint.health().getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    void testBasePackagesExist() {
        assertThat(applicationContext.containsBeanDefinition("employeeService")).isTrue();
        assertThat(applicationContext.containsBeanDefinition("employeeRepository")).isTrue();
        assertThat(applicationContext.containsBeanDefinition("attendanceService")).isTrue();
        assertThat(applicationContext.containsBeanDefinition("shiftService")).isTrue();
        // Add more base packages as needed
    }
}
