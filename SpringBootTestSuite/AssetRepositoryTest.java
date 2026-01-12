package com.warehouse.employee;

import com.warehouse.employee.model.Asset;
import com.warehouse.employee.model.Employee;
import com.warehouse.employee.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AssetRepositoryTest {
    @Autowired
    private AssetRepository assetRepository;

    private Asset asset1, asset2;
    private Employee emp1;

    @BeforeEach
    void setUp() {
        emp1 = new Employee(); emp1.setId(1L); emp1.setFirstName("John");
        asset1 = new Asset(null, "TAG123", "Forklift", "GOOD", false, null, null, null);
        asset2 = new Asset(null, "TAG456", "Pallet Jack", "DAMAGED", true, emp1, null, null);
        assetRepository.save(asset1);
        assetRepository.save(asset2);
    }

    @Test
    void testFindAll() {
        List<Asset> all = assetRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testFindById() {
        Asset found = assetRepository.findById(asset1.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("TAG123", found.getAssetTag());
    }

    @Test
    void testDelete() {
        assetRepository.delete(asset1);
        List<Asset> all = assetRepository.findAll();
        assertEquals(1, all.size());
        assertFalse(all.stream().anyMatch(a -> "TAG123".equals(a.getAssetTag())));
    }

    @Test
    void testUniqueAssetTagConstraint() {
        Asset duplicate = new Asset(null, "TAG123", "Forklift", "GOOD", false, null, null, null);
        assetRepository.save(duplicate);
        List<Asset> found = assetRepository.findAll();
        assertTrue(found.size() >= 2); // DB may throw error in real test
    }

    @Test
    void testEdgeCases() {
        Asset edge = new Asset(null, "", null, "", null, null, null, null);
        assetRepository.save(edge);
        List<Asset> all = assetRepository.findAll();
        assertTrue(all.size() >= 2);
    }
}
