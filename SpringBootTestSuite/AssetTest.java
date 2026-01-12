package com.warehouse.employee;

import com.warehouse.employee.model.Asset;
import com.warehouse.employee.model.Employee;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AssetTest {
    @Test
    void testConstructorAndGetters() {
        Employee emp = new Employee();
        LocalDateTime now = LocalDateTime.now();
        Asset asset = new Asset(1L, "TAG123", "Forklift", "DAMAGED", true, emp, now, now);
        assertEquals(1L, asset.getId());
        assertEquals("TAG123", asset.getAssetTag());
        assertEquals("Forklift", asset.getAssetType());
        assertEquals("DAMAGED", asset.getCondition());
        assertTrue(asset.getAssigned());
        assertEquals(emp, asset.getAssignedTo());
        assertEquals(now, asset.getCreatedAt());
        assertEquals(now, asset.getUpdatedAt());
    }

    @Test
    void testDefaultValues() {
        Asset asset = new Asset();
        assertNull(asset.getId());
        assertNull(asset.getAssetTag());
        assertNull(asset.getAssetType());
        assertEquals("GOOD", asset.getCondition());
        assertFalse(asset.getAssigned());
        assertNull(asset.getAssignedTo());
        assertNotNull(asset.getCreatedAt());
        assertNotNull(asset.getUpdatedAt());
    }

    @Test
    void testSetters() {
        Asset asset = new Asset();
        Employee emp = new Employee();
        LocalDateTime created = LocalDateTime.of(2024, 7, 1, 8, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 7, 2, 8, 0);
        asset.setId(2L);
        asset.setAssetTag("TAG456");
        asset.setAssetType("Pallet Jack");
        asset.setCondition("EXCELLENT");
        asset.setAssigned(true);
        asset.setAssignedTo(emp);
        asset.setCreatedAt(created);
        asset.setUpdatedAt(updated);
        assertEquals(2L, asset.getId());
        assertEquals("TAG456", asset.getAssetTag());
        assertEquals("Pallet Jack", asset.getAssetType());
        assertEquals("EXCELLENT", asset.getCondition());
        assertTrue(asset.getAssigned());
        assertEquals(emp, asset.getAssignedTo());
        assertEquals(created, asset.getCreatedAt());
        assertEquals(updated, asset.getUpdatedAt());
    }

    @Test
    void testOnUpdateUpdatesUpdatedAt() {
        Asset asset = new Asset();
        LocalDateTime before = asset.getUpdatedAt();
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        asset.onUpdate();
        LocalDateTime after = asset.getUpdatedAt();
        assertTrue(after.isAfter(before));
    }

    @Test
    void testEdgeCases() {
        Asset asset = new Asset();
        asset.setCondition("");
        asset.setAssetTag("");
        asset.setAssetType(null);
        asset.setAssigned(null);
        assertEquals("", asset.getCondition());
        assertEquals("", asset.getAssetTag());
        assertNull(asset.getAssetType());
        assertNull(asset.getAssigned());
    }
}
