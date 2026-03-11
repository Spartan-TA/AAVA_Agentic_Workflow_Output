package SpringBootTestSuite;

import com.warehouse.util.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {
    @Test
    @DisplayName("format(LocalDate) returns ISO string")
    void formatLocalDate_normal() {
        LocalDate date = LocalDate.of(2023, 3, 15);
        assertEquals("2023-03-15", DateUtils.format(date));
    }

    @Test
    @DisplayName("format(LocalDateTime) returns ISO string")
    void formatLocalDateTime_normal() {
        LocalDateTime dt = LocalDateTime.of(2023, 3, 15, 10, 5, 30);
        assertEquals("2023-03-15T10:05:30", DateUtils.format(dt).substring(0,19));
    }

    @Test
    @DisplayName("format(LocalDate) throws NullPointerException for null")
    void formatLocalDate_null() {
        assertThrows(NullPointerException.class, () -> DateUtils.format((LocalDate) null));
    }

    @Test
    @DisplayName("format(LocalDateTime) throws NullPointerException for null")
    void formatLocalDateTime_null() {
        assertThrows(NullPointerException.class, () -> DateUtils.format((LocalDateTime) null));
    }

    @Test
    @DisplayName("format(LocalDate) edge case: leap day")
    void formatLocalDate_leapDay() {
        LocalDate date = LocalDate.of(2020, 2, 29);
        assertEquals("2020-02-29", DateUtils.format(date));
    }
}
