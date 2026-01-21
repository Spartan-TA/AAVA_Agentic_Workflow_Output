package com.wms.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * JUnit tests for AttendanceRecordMapper covering DTO-entity conversions and edge cases.
 */
public class AttendanceRecordMapperTest {

    private AttendanceRecord validRecord;
    private AttendanceRecordDTO validDTO;

    @BeforeEach
    public void setUp() {
        validRecord = new AttendanceRecord();
        validRecord.setId(1L);
        validRecord.setEmployeeId(1L);
        validRecord.setClockIn(LocalDateTime.of(2023, 6, 1, 8, 0));
        validRecord.setClockOut(LocalDateTime.of(2023, 6, 1, 16, 0));
        validRecord.setStatus("CLOCKED_OUT");
        validRecord.setDeviceInfo("Terminal2");

        validDTO = new AttendanceRecordDTO();
        validDTO.setId(1L);
        validDTO.setEmployeeId(1L);
        validDTO.setClockIn(LocalDateTime.of(2023, 6, 1, 8, 0));
        validDTO.setClockOut(LocalDateTime.of(2023, 6, 1, 16, 0));
        validDTO.setStatus("CLOCKED_OUT");
        validDTO.setDeviceInfo("Terminal2");
    }

    @Test
    public void testToDTO_ValidEntity_ReturnsDTO() {
        AttendanceRecordDTO dto = AttendanceRecordMapper.toDTO(validRecord);
        assertNotNull(dto);
        assertEquals(validRecord.getId(), dto.getId());
        assertEquals(validRecord.getEmployeeId(), dto.getEmployeeId());
        assertEquals(validRecord.getClockIn(), dto.getClockIn());
        assertEquals(validRecord.getClockOut(), dto.getClockOut());
        assertEquals(validRecord.getStatus(), dto.getStatus());
        assertEquals(validRecord.getDeviceInfo(), dto.getDeviceInfo());
    }

    @Test
    public void testToDTO_NullEntity_ReturnsNull() {
        AttendanceRecordDTO dto = AttendanceRecordMapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    public void testToDTO_EntityWithNullFields_ReturnsDTOWithNulls() {
        AttendanceRecord record = new AttendanceRecord();
        AttendanceRecordDTO dto = AttendanceRecordMapper.toDTO(record);
        assertNotNull(dto);
        assertNull(dto.getEmployeeId());
        assertNull(dto.getClockIn());
        assertNull(dto.getClockOut());
        assertNull(dto.getStatus());
        assertNull(dto.getDeviceInfo());
    }

    @Test
    public void testToEntity_ValidDTO_ReturnsEntity() {
        AttendanceRecord record = AttendanceRecordMapper.toEntity(validDTO);
        assertNotNull(record);
        assertEquals(validDTO.getId(), record.getId());
        assertEquals(validDTO.getEmployeeId(), record.getEmployeeId());
        assertEquals(validDTO.getClockIn(), record.getClockIn());
        assertEquals(validDTO.getClockOut(), record.getClockOut());
        assertEquals(validDTO.getStatus(), record.getStatus());
        assertEquals(validDTO.getDeviceInfo(), record.getDeviceInfo());
    }

    @Test
    public void testToEntity_NullDTO_ReturnsNull() {
        AttendanceRecord record = AttendanceRecordMapper.toEntity(null);
        assertNull(record);
    }

    @Test
    public void testToEntity_DTOWithNullFields_ReturnsEntityWithNulls() {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        AttendanceRecord record = AttendanceRecordMapper.toEntity(dto);
        assertNotNull(record);
        assertNull(record.getEmployeeId());
        assertNull(record.getClockIn());
        assertNull(record.getClockOut());
        assertNull(record.getStatus());
        assertNull(record.getDeviceInfo());
    }

    @Test
    public void testToDTO_EntityWithEmptyStrings_ReturnsDTOWithEmptyStrings() {
        AttendanceRecord record = new AttendanceRecord();
        record.setStatus("");
        record.setDeviceInfo("");
        AttendanceRecordDTO dto = AttendanceRecordMapper.toDTO(record);
        assertNotNull(dto);
        assertEquals("", dto.getStatus());
        assertEquals("", dto.getDeviceInfo());
    }

    @Test
    public void testToEntity_DTOWithEmptyStrings_ReturnsEntityWithEmptyStrings() {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setStatus("");
        dto.setDeviceInfo("");
        AttendanceRecord record = AttendanceRecordMapper.toEntity(dto);
        assertNotNull(record);
        assertEquals("", record.getStatus());
        assertEquals("", record.getDeviceInfo());
    }

    @Test
    public void testToDTO_EntityWithAllFields_ReturnsDTOWithAllFields() {
        AttendanceRecordDTO dto = AttendanceRecordMapper.toDTO(validRecord);
        assertEquals(validRecord.getId(), dto.getId());
        assertEquals(validRecord.getEmployeeId(), dto.getEmployeeId());
        assertEquals(validRecord.getClockIn(), dto.getClockIn());
        assertEquals(validRecord.getClockOut(), dto.getClockOut());
        assertEquals(validRecord.getStatus(), dto.getStatus());
        assertEquals(validRecord.getDeviceInfo(), dto.getDeviceInfo());
    }

    @Test
    public void testToEntity_DTOWithAllFields_ReturnsEntityWithAllFields() {
        AttendanceRecord record = AttendanceRecordMapper.toEntity(validDTO);
        assertEquals(validDTO.getId(), record.getId());
        assertEquals(validDTO.getEmployeeId(), record.getEmployeeId());
        assertEquals(validDTO.getClockIn(), record.getClockIn());
        assertEquals(validDTO.getClockOut(), record.getClockOut());
        assertEquals(validDTO.getStatus(), record.getStatus());
        assertEquals(validDTO.getDeviceInfo(), record.getDeviceInfo());
    }
}
