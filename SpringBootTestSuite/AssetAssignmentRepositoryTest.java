package com.warehouse.employee;

import com.warehouse.employee.model.AssetAssignment;
import com.warehouse.employee.model.Asset;
import com.warehouse.employee.model.Employee;
import com.warehouse.employee.repository.AssetAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AssetAssignmentRepositoryTest {
    @Autowired
    private AssetAssignmentRepository assetAssignmentRepository;

    private Asset asset1, asset2;
    private Employee emp1, emp2;
    private AssetAssignment assign1, assign2;

    @BeforeEach
    void setUp() {
        asset1 = new Asset(); asset1.setId(1L); asset1.setAssetTag("TAG123");
        asset2 = new Asset(); asset2.setId(2L); asset2.setAssetTag("TAG456");
        emp1 = new Employee(); emp1.setId(1L); emp1.setFirstName("John");
        emp2 = new Employee(); emp2.setId(2L); emp2.setFirstName("Jane");
        assign1 = new AssetAssignment(null, asset1, emp1, LocalDateTime.of(2024,6,1,10,0), null, null);
        assign2 = new AssetAssignment(null, asset2, emp2, LocalDateTime.of(2024,6,2,11,0), LocalDateTime.of(2024,6,3,12,0), "DAMAGED");
        assetAssignmentRepository.save(assign1);
        assetAssignmentRepository.save(assign2);
    }

    @Test
    void testFindAll() {
        List<AssetAssignment> all = assetAssignmentRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testFindById() {
        AssetAssignment found = assetAssignmentRepository.findById(assign1.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("TAG123", found.getAsset().getAssetTag());
    }

    @Test
    void testDelete() {
        assetAssignmentRepository.delete(assign1);
        List<AssetAssignment> all = assetAssignmentRepository.findAll();
        assertEquals(1, all.size());
        assertFalse(all.stream().anyMatch(a -> a.getAsset().getAssetTag().equals("TAG123")));
    }

    @Test
    void testEdgeCases() {
        AssetAssignment edge = new AssetAssignment(null, asset1, emp1, null, null, "");
        assetAssignmentRepository.save(edge);
        List<AssetAssignment> all = assetAssignmentRepository.findAll();
        assertTrue(all.size() >= 2);
    }
}
